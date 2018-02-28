package com.ardurasolutions.safekiddo.sql.tables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.ardurasolutions.safekiddo.launcher.views.AppIcon;
import com.ardurasolutions.safekiddo.launcher.views.DesktopView;
import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.proto.TableProto;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.DesktopConfig;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public class DesktopConfigTable extends TableProto {
	
	public DesktopConfigTable(BasicLocalSQL lSql) {
		super(lSql, DesktopConfig.class);
	}
	
	public Long saveIcon(DesktopView dv, AppIcon ai) {
		return saveIcon(dv, ai, getTransactionConn());
	}
	
	public Long saveIcon(DesktopView dv, AppIcon ai, SQLiteDatabase conn) {
		ContentValues cv = new ContentValues();
		cv.put(DesktopConfig.FIELD_DESKTOP_CONFIG_PKG, ai.getAppInfo().getComponentName().getPackageName());
		cv.put(DesktopConfig.FIELD_DESKTOP_CONFIG_CLASS, ai.getAppInfo().getComponentName().getClassName());
		cv.put(DesktopConfig.FIELD_DESKTOP_CONFIG_TOP, ai.getTopMargin());
		cv.put(DesktopConfig.FIELD_DESKTOP_CONFIG_LEFT, ai.getLeftMargin());
		cv.put(DesktopConfig.FIELD_DESKTOP_CONFIG_TAG, dv.getTag().toString());
		
		if (ai.getDbId() != null) {
			this.update(cv, "_id=?", new String[]{ai.getDbId().toString()}, conn);
			return ai.getDbId();
		} else {
			long idx = this.insert(cv, false, conn);
			if (idx > 0) {
				ai.setDbId(idx);
				return idx;
			} else
				return null;
		}
	}
	
	public void saveIcons(DesktopView dv, ArrayList<AppIcon> aiList) {
		if (dv == null) return;
		if (dv.getTag() == null) return;
		
		getTransactionConn().beginTransaction();
		try {
			for(AppIcon ai : aiList) {
				saveIcon(dv, ai, getTransactionConn());
			}
			getTransactionConn().setTransactionSuccessful();
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("DesktopConfigTable::saveIcons", e);
		} finally {
			getTransactionConn().endTransaction();
		}
	}
	
	public void deleteIcons(HashMap<DesktopView, ArrayList<View>> config) {
		getTransactionConn().beginTransaction();
		try {
			Iterator<DesktopView> rm = config.keySet().iterator();
			while(rm.hasNext()) {
				DesktopView desk = rm.next();
				for(View v : config.get(desk)) {
					if (v instanceof AppIcon) {
						if (((AppIcon) v).getDbId() != null) {
							getTransactionConn().delete(getTableName(), "_id=?", new String[]{((AppIcon) v).getDbId().toString()});
							//this.delete(((AppIcon) v).getDbId(), getTransactionConn());
						}
					}
				}
			}
			getTransactionConn().setTransactionSuccessful();
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("DesktopConfigTable::deleteIcons", e);
		} finally {
			getTransactionConn().endTransaction();
		}
	}
	
	public void deleteIcon(AppIcon ai) {
		if (ai.getDbId() != null)
			getTransactionConn().delete(getTableName(), "_id=?", new String[]{ai.getDbId().toString()});
	}
	
	public ArrayList<DesktopConfig> getItems(String tag) {
		ArrayList<DesktopConfig> res = new ArrayList<DesktopConfig>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(DesktopConfig.FIELD_DESKTOP_CONFIG_TAG + "=?")
			.addParam(tag);
	
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			do {
				
				res.add(DBUtils.currToObj(c, DesktopConfig.class));
				
			} while (c.moveToNext());
		}
		c.close();
		
		return res;
	}
	
	public HashMap<String, ArrayList<DesktopConfig>> getAllItems() {
		HashMap<String, ArrayList<DesktopConfig>> res = new HashMap<String, ArrayList<DesktopConfig>>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(DesktopConfig.FIELD_DESKTOP_CONFIG_TAG);
		
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			do {
				DesktopConfig dc = DBUtils.currToObj(c, DesktopConfig.class);
				
				ArrayList<DesktopConfig> list = res.get(dc.desktop_config_tag);
				if (list == null) {
					list = new ArrayList<DesktopConfig>();
					res.put(dc.desktop_config_tag, list);
				}
				
				list.add(dc);
				
			} while (c.moveToNext());
		}
		c.close();
		
		return res;
	}
	
	public boolean hasAnyConfig() {
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.select("IFNULL(COUNT(*), 0) as res");
		boolean res = false;
		
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			res = c.getInt(0) > 0;
		}
		c.close();
		
		return res;
	}
	
	public void deleteFully() {
		this.getTransactionConn().delete(getTableName(), null, null);
	}

	@Override
	public String getFriendlyName() {
		return "";
	}

}
