package com.ardurasolutions.safekiddo.sql.tables;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.TextUtils;
import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.proto.TableProto;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AllApps;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public class AllAppsTable extends TableProto {

	public AllAppsTable(BasicLocalSQL lSql) {
		super(lSql, AllApps.class);
	}
	
	public ArrayList<String> getBlockedAppsPackages() {
		ArrayList<String> res = new ArrayList<String>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(AllApps.FIELD_SYNC_IS_DELETED + "=0")
			.where(AllApps.FIELD_ALL_APPS_IS_BLOCKED + "=1")
			.orderBy(AllApps.FIELD_ALL_APPS_LABEL);
		
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		try {
			if (c.moveToFirst()) {
				do {
					res.add(DBUtils.currToObj(c, AllApps.class).all_apps_package);
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("AllAppsTable::getBlockedAppsPackages", e);
		} finally {
			c.close();
		}
		
		return res;
	}
	
	public ArrayList<AllApps> getBlockedApps() {
		ArrayList<AllApps> res = new ArrayList<AllApps>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(AllApps.FIELD_SYNC_IS_DELETED + "=0")
			.where(AllApps.FIELD_ALL_APPS_IS_BLOCKED + "=1")
			.orderBy(AllApps.FIELD_ALL_APPS_LABEL);
		
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		try {
			if (c.moveToFirst()) {
				do {
					res.add(DBUtils.currToObj(c, AllApps.class));
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("AllAppsTable::getBlockedApps", e);
		} finally {
			c.close();
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param onlyNonBlocked - may by null then gets all
	 * @return
	 */
	public ArrayList<AllApps> getAll(Boolean onlyNonBlocked, SQLiteDatabase conn) {
		ArrayList<AllApps> res = new ArrayList<AllApps>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(AllApps.FIELD_SYNC_IS_DELETED + "=0")
			.orderBy(AllApps.FIELD_ALL_APPS_LABEL);
		
		if (onlyNonBlocked != null)
			qb.where(AllApps.FIELD_ALL_APPS_IS_BLOCKED + "=?").addParam(onlyNonBlocked ? 0 : 1);
		
		Cursor c = conn.rawQuery(qb.getSelect(), qb.getParams());
		try {
			if (c.moveToFirst()) {
				do {
					res.add(DBUtils.currToObj(c, AllApps.class));
				} while (c.moveToNext());
			}
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("AllAppsTable::getAll", e);
		} finally {
			c.close();
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param onlyNonBlocked - may by null then gets all
	 * @return
	 */
	public ArrayList<AllApps> getAllWithIcons(Boolean onlyNonBlocked, boolean bigIcons) {
		ArrayList<AllApps> res = getAll(onlyNonBlocked, this.db);
		
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
		final PackageManager pm = getContext().getPackageManager();
		final List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);
		final int iconSize = bigIcons ? getContext().getResources().getDimensionPixelSize(R.dimen.all_apps_icon_size) : getContext().getResources().getDimensionPixelSize(R.dimen.all_apps_list_icon_size);
		
		for(ResolveInfo iApp : installedApps) {
			AllApps tmp = new AllApps(iApp.activityInfo.packageName, iApp.activityInfo.name);
			
			if (res.contains(tmp)) {
				AllApps app = res.get(res.indexOf(tmp));
				Drawable icon = iApp.activityInfo.loadIcon(pm);
				if (icon != null) {
					Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
					if (bitmap.getWidth() != iconSize && bitmap.getHeight()!= iconSize ){
						bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
						icon = new BitmapDrawable(getContext().getResources(), bitmap);
					}
					app.all_apps_icon = icon;
					//app.setShadow(CommonUtils.makeBitmapShadow(bitmap));
				}
			}
		}
		
		return res;
	}
	
	/**
	 * fields: all_apps_package, all_apps_class, all_apps_is_blocked, all_apps_label
	 * @param app
	 * @param dbConn
	 */
	public Long addEntry(AllApps app, SQLiteDatabase dbConn) {
		ContentValues cv = new ContentValues();
		cv.put(AllApps.FIELD_ALL_APPS_PACKAGE, app.all_apps_package);
		cv.put(AllApps.FIELD_ALL_APPS_CLASS, app.all_apps_class);
		cv.put(AllApps.FIELD_ALL_APPS_IS_BLOCKED, app.all_apps_is_blocked);
		cv.put(AllApps.FIELD_ALL_APPS_LABEL, app.all_apps_label);
		//insert(cv, dbConn);
		return dbConn.insert(getTableName(), null, cv);
	}
	
	public void updateEntry(AllApps app, SQLiteDatabase dbConn) {
		ContentValues cv = new ContentValues();
		cv.put(AllApps.FIELD_ALL_APPS_PACKAGE, app.all_apps_package);
		cv.put(AllApps.FIELD_ALL_APPS_CLASS, app.all_apps_class);
		cv.put(AllApps.FIELD_ALL_APPS_IS_BLOCKED, app.all_apps_is_blocked);
		cv.put(AllApps.FIELD_ALL_APPS_LABEL, app.all_apps_label);
		dbConn.update(getTableName(), cv, "_id=?", new String[]{app._id.toString()});
		//update(cv, "_id=?", new String[]{app._id.toString()}, dbConn);
	}
	
	public void updateBlockeFlagOnAll(boolean isBlocked) {
		// TODO tutej trzeba zamienić na funkcję update jak będzie miało być synchro - to jest na szybko (nie zmiania pól sync_is...)
		ContentValues cv = new ContentValues();
		cv.put(AllApps.FIELD_ALL_APPS_IS_BLOCKED, isBlocked ? 1 : 0);
		this.getTransactionConn().update(
			getTableName(), 
			cv, 
			AllApps.FIELD_SYNC_IS_DELETED + "=? AND " + AllApps.FIELD_ALL_APPS_PACKAGE + "<>?", 
			new String[]{"0", getContext().getPackageName()}
		);
	}
	
	public void updateEntryBlockedFlag(Long _id, boolean isBlocked) {
		ContentValues cv = new ContentValues();
		cv.put(AllApps.FIELD_ALL_APPS_IS_BLOCKED, isBlocked ? 1 : 0);
		update(cv, "_id=?", new String[]{_id.toString()}, getTransactionConn());
	}
	
	public void deleteByPkg(String pkg, SQLiteDatabase dbConn) {
		delete(
			AllApps.FIELD_ALL_APPS_PACKAGE + "=? AND " + AllApps.FIELD_SYNC_IS_DELETED + "=0", 
			new String[]{pkg}, 
			dbConn
		);
	}
	
	public void refreshList() {
		refreshList(false);
	}
	
	public void refreshList(boolean forceUpdate) {
		refreshList(forceUpdate, getTransactionConn());
	}
	
	public boolean entryExists(Long _id, SQLiteDatabase conn) {
		boolean res = false;
		Cursor c = conn.rawQuery("SELECT IFNULL(COUNT(*), 0) FROM " + getTableName() + " WHERE _id=?", new String[]{_id.toString()});
		if (c.moveToFirst()) {
			res = c.getInt(0) > 0;
		}
		c.close();
		return res;
	}
	
	/**
	 * sprawdza i porównuje listę zainstalowanych i to co jest w bazie<br>
	 * dodaje usuwa co trzeba
	 */
	public void refreshList(boolean forceUpdate, SQLiteDatabase conn) {
		final SQLiteDatabase dbConn = conn == null ? getTransactionConn() : conn;
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
		final PackageManager pm = getContext().getPackageManager();
		final List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);
		
		dbConn.beginTransaction();
		
		final ArrayList<AllApps> all = getAll(null, conn);
		final boolean isFirstRun = all.size() == 0;
		
		try {
			
			if (isFirstRun) {
				for(ResolveInfo iApp : installedApps) {
					AllApps app = new AllApps(iApp.activityInfo.packageName, iApp.activityInfo.name);
					app.all_apps_label = iApp.loadLabel(pm).toString();
					app.all_apps_is_blocked = 0;
					addEntry(app, dbConn);
				}
			} else {
				ArrayList<String> listOfIds = new ArrayList<String>();
				for(ResolveInfo iApp : installedApps) {
					AllApps app = new AllApps(iApp.activityInfo.packageName, iApp.activityInfo.name);
					app.all_apps_label = iApp.loadLabel(pm).toString();
					
					if (all.contains(app)) {
						AllApps fromAll = all.get(all.indexOf(app));
						app._id = fromAll._id;
						app.all_apps_is_blocked = fromAll.all_apps_is_blocked;
						updateEntry(app, dbConn);
						listOfIds.add(app._id.toString());
					} else {
						// insert - domyślnie blokujemy nowe aplikacje chyba że coś jest w packageName safekiddo
						app.all_apps_is_blocked = app.all_apps_package.equals(getContext().getPackageName()) ? 0 : 1;
						Long newId = addEntry(app, dbConn);
						listOfIds.add(newId.toString());
					}
					
					/*if (isFirstRun) {
						app.all_apps_is_blocked = 0;
						addEntry(app, dbConn);
					} else {
						if (all.contains(app)) {
							if (forceUpdate) {
								AllApps fromAll = all.get(all.indexOf(app));
								if (entryExists(fromAll._id, dbConn)) {
									app._id = fromAll._id;
									updateEntry(app, dbConn);
								} else
									addEntry(app, dbConn);
							}
						} else {
							// insert - domyślnie blokujemy nowe aplikacje chyba że coś jest w packageName safekiddo
							app.all_apps_is_blocked = app.all_apps_package.equals(getContext().getPackageName()) ? 0 : 1;
							addEntry(app, dbConn);
						}
					}*/
				}
				
				if (listOfIds.size() > 0)
					dbConn.delete(getTableName(), "_id NOT IN (" + TextUtils.join(listOfIds, ",") + ")", null);
			}
			dbConn.setTransactionSuccessful();
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("AllAppsTable::refreshList", e);
		} finally {
			dbConn.endTransaction();
		}
	}
	
	public void blockApp(ComponentName cn, SQLiteDatabase conn) {
		ContentValues cv = new ContentValues();
		cv.put(AllApps.FIELD_ALL_APPS_IS_BLOCKED, 1);
		update(
			cv, 
			//AllApps.FIELD_ALL_APPS_PACKAGE + "=? AND " + AllApps.FIELD_ALL_APPS_CLASS + "=?", 
			AllApps.FIELD_ALL_APPS_PACKAGE + "=?",
			//new String[]{cn.getPackageName(), cn.getClassName()}, 
			new String[]{cn.getPackageName()}, 
			conn
		);
	}
	
	public void blockApps(List<ComponentName> apps) {
		if (apps != null && apps.size() > 0) {
			SQLiteDatabase conn = getTransactionConn();
			conn.beginTransaction();
			try {
				for(ComponentName cn : apps) {
					blockApp(cn, conn);
				}
				conn.setTransactionSuccessful();
			} finally {
				conn.endTransaction();
			}
		}
	}
	
	@Override
	public String getFriendlyName() {
		return "";
	}

}
