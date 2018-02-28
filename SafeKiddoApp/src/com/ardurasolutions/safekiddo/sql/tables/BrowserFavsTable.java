package com.ardurasolutions.safekiddo.sql.tables;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.proto.TableProto;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserFavs;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public class BrowserFavsTable extends TableProto {
	
	public class Tree {
		private ArrayList<Tree> children = new ArrayList<Tree>();
		private Tree parent = null;
		private BrowserFavs data;
		
		public Tree(BrowserFavs data, Tree parent) {
			this.setData(data);
			this.setParent(parent);
			getData().browser_favs_lvl = parent == null ? 0 : parent.getData().browser_favs_lvl + 1;
		}

		public BrowserFavs getData() {
			return data;
		}

		public Tree setData(BrowserFavs data) {
			this.data = data;
			return this;
		}

		public Tree getParent() {
			return parent;
		}

		public Tree setParent(Tree parent) {
			this.parent = parent;
			return this;
		}

		public ArrayList<Tree> getChildren() {
			return children;
		}

		public Tree setChildren(ArrayList<Tree> children) {
			this.children = children;
			return this;
		}
		
		@Override
		public String toString() {
			String res = "{" + data.toString() + " : ";
			if (getChildren().size() > 0) {
				res += "[";
				for(int i=0; i<getChildren().size(); i++)
					res += getChildren().get(i) + ", ";
				res = res.substring(0, res.length()-2);
				res += "]";
			} else {
				res += "none";
			}
			res += "}";
			return res;
		}
		
		public void assignChilds(final LinkedHashMap<Long, BrowserFavs> allItems) {
			for(Long id : allItems.keySet()) {
				BrowserFavs cc = allItems.get(id);
				if (cc.browser_favs_parent.equals(data.browser_favs_id)) {
					Tree newTree = new Tree(cc, this);
					getChildren().add(newTree);
					newTree.assignChilds(allItems);
				}
			}
		}
		
		public void assignList(ArrayList<BrowserFavs> toList) {
			if (getChildren().size() > 0) {
				for(Tree c : getChildren()) {
					toList.add(c.getData());
					c.assignList(toList);
				}
			}
		}
	}
	
	public ArrayList<BrowserFavs> getFoldersTree() {
		final LinkedHashMap<Long, BrowserFavs> all = new LinkedHashMap<Long, BrowserFavs>();
		final ArrayList<BrowserFavs> roots = new ArrayList<BrowserFavs>();
		final ArrayList<Tree> rootTree = new ArrayList<Tree>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(BrowserFavs.FIELD_BROWSER_FAVS_PARENT + " ASC")
			.orderBy(BrowserFavs.FIELD_BROWSER_FAVS_POS + " ASC")
			.where(BrowserFavs.FIELD_BROWSER_FAVS_TYPE + "=?").addParam(BrowserFavs.FavsType.FOLDER.getValue());

		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			do {
				BrowserFavs item = DBUtils.currToObj(c, BrowserFavs.class);
				all.put(item.browser_favs_id, item);
				if (item.browser_favs_parent == 0) {
					roots.add(item);
					rootTree.add(new Tree(item, null));
				}
			} while (c.moveToNext());
		}
		c.close();
		
		for(Tree item : rootTree) {
			item.assignChilds(all);
		}
		
		ArrayList<BrowserFavs> result = new ArrayList<BrowserFavs>();
		
		BrowserFavs fakeRoot = new BrowserFavs();
		fakeRoot.browser_favs_id = 0L;
		fakeRoot.browser_favs_label = getContext().getResources().getString(R.string.label_root_directory);
		fakeRoot.browser_favs_parent = 0L;
		fakeRoot.browser_favs_pos = -1L;
		fakeRoot.browser_favs_type = BrowserFavs.FavsType.FOLDER.getValue();
		result.add(fakeRoot);
		
		for(Tree item : rootTree) {
			result.add(item.getData());
			item.assignList(result);
		}
		
		return result;
	}
	
	private void deleteInTransaction(SQLiteDatabase conn, BrowserFavs folder) {
		conn.delete(getTableName(), 
			BrowserFavs.FIELD_BROWSER_FAVS_PARENT + "=? AND " + BrowserFavs.FIELD_BROWSER_FAVS_TYPE + "=?", 
			new String[]{folder.browser_favs_id.toString(), Integer.toString(BrowserFavs.FavsType.FAV.getValue())}
		);
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(BrowserFavs.FIELD_BROWSER_FAVS_PARENT + "=?").addParam(folder.browser_favs_id);
		
		Cursor c = conn.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			do {
				BrowserFavs item = DBUtils.currToObj(c, BrowserFavs.class);
				if (item.browser_favs_type == BrowserFavs.FavsType.FAV.getValue()) {
					// nop
					if (Console.isEnabled())
						Console.loge("deleteInTransaction - fav item is bookmark: " + item);
				} else {
					deleteInTransaction(conn, item);
					if (Console.isEnabled())
						Console.logi("DELETE FAV FOLDER: " + item);
					conn.delete(getTableName(), 
						BrowserFavs.FIELD_ID + "=?", 
						new String[]{item._id.toString()}
					);
				}
			} while (c.moveToNext());
		}
		c.close();
		
		conn.delete(getTableName(), 
			BrowserFavs.FIELD_ID + "=?", 
			new String[]{folder._id.toString()}
		);
	}
	
	public void deleteWithSubchilds(BrowserFavs deletedItem) {
		if (deletedItem.browser_favs_type == BrowserFavs.FavsType.FOLDER.getValue()) {
			this.getTransactionConn().beginTransaction();
			try {
				deleteInTransaction(this.getTransactionConn(), deletedItem);
				this.getTransactionConn().setTransactionSuccessful();
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage("BrowserFavsTable", "deleteWithSubchilds", e);
			} finally {
				this.getTransactionConn().endTransaction();
			}
		} else {
			this.db.delete(getTableName(), "_id=?", new String[]{deletedItem._id.toString()});
		}
	}

	public BrowserFavsTable(BasicLocalSQL lSql) {
		super(lSql, BrowserFavs.class);
	}
	
	/**
	 * 
	 * @param type - null for all
	 * @return
	 */
	public Cursor getItems(BrowserFavs.FavsType type, BrowserFavs parent) {
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(BrowserFavs.FIELD_BROWSER_FAVS_TYPE + " DESC")
			.orderBy(BrowserFavs.FIELD_BROWSER_FAVS_PARENT + " ASC")
			.orderBy(BrowserFavs.FIELD_BROWSER_FAVS_POS + " ASC");
		
		if (type != null)
			qb.where(BrowserFavs.FIELD_BROWSER_FAVS_TYPE + "=?").addParam(type.getValue());
		
		if (parent != null)
			qb.where(BrowserFavs.FIELD_BROWSER_FAVS_PARENT + "=?").addParam(parent.browser_favs_id);
		
		return this.db.rawQuery(qb.getSelect(), qb.getParams());
	}
	
	public ArrayList<BrowserFavs> getItemsArray(BrowserFavs.FavsType type, BrowserFavs parent) {
		ArrayList<BrowserFavs> res = new ArrayList<BrowserFavs>();
		
		Cursor c = getItems(type, parent);
		if (c.moveToFirst()) {
			do {
				res.add(DBUtils.currToObj(c, BrowserFavs.class));
			} while (c.moveToNext());
		}
		c.close();
		
		return res;
	}
	
	public Long addEntryFav(BrowserFavs parent, String label, String url) {
		return addEntry(parent, label, url, BrowserFavs.FavsType.FAV);
	}
	
	public Long addEntryFolder(BrowserFavs parent, String label) {
		return addEntry(parent, label, null, BrowserFavs.FavsType.FOLDER);
	}
	
	public void updateLabel(BrowserFavs entry) {
		ContentValues cv = new ContentValues();
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_LABEL, entry.browser_favs_label);
		update(cv, "_id=?", new String[]{entry._id.toString()}, getTransactionConn());
	}
	
	/**
	 * update fields: label, url, parent
	 * @param entry
	 */
	public void updateLabelUrlParent(BrowserFavs entry) {
		ContentValues cv = new ContentValues();
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_LABEL, entry.browser_favs_label);
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_URL, entry.browser_favs_url);
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_PARENT, entry.browser_favs_parent);
		update(cv, "_id=?", new String[]{entry._id.toString()}, getTransactionConn());
	}
	
	public Long addEntry(BrowserFavs parent, String label, String url, BrowserFavs.FavsType type) {
		ContentValues cv = new ContentValues();
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_LABEL, label);
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_TYPE, type.getValue());
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_PARENT, parent != null ? parent.browser_favs_id : 0L);
		
		switch(type) {
			default:
			case FAV:
				cv.put(BrowserFavs.FIELD_BROWSER_FAVS_URL, url);
			break;
			case FOLDER:
				
			break;
		}
		
		int pos = 0;
		/*
		$this->etu->db->select('(IFNULL(max(menu_items_pos), -1) + 1) as new_pos', FALSE);
		$this->etu->db->where('menu_items_parent_id', $data['menu_items_parent_id']);
		$pos = $this->etu->db->get('menu_items')->row()->new_pos;
		$data['menu_items_pos'] = $pos;
		 */
		//if (parent != null) {
			QueryBuilder qb = new QueryBuilder()
				.from(getTableName())
				.select("(IFNULL(max(" + BrowserFavs.FIELD_BROWSER_FAVS_POS + "), -1) + 1) as new_pos")
				.where(BrowserFavs.FIELD_BROWSER_FAVS_PARENT + "=?").addParam(parent != null ? parent.browser_favs_id : 0L);
			Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
			if (c.moveToFirst()) {
				pos = c.getInt(0);
			}
			c.close();
		//}
		
		cv.put(BrowserFavs.FIELD_BROWSER_FAVS_POS, pos);
		
		return insert(cv, getTransactionConn());
	}

	@Override
	public String getFriendlyName() {
		return "";
	}

}
