package com.ardurasolutions.safekiddo.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.ardurasolutions.safekiddo.sql.tables.DesktopConfigTable;

public class LocalSQL extends BasicLocalSQL {
	
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "sync";
	private static LocalSQL sInstance = null;
	
	public static synchronized LocalSQL getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new LocalSQL(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public synchronized void clearInstance() {
		sInstance = null;
	}
	
	@Override
	public String getDbFileName() {
		return DB_NAME;
	}

	@Override
	public int getDbVersion() {
		return DB_VERSION;
	}
	
	public LocalSQL(Context context) {
		super(context);
		
		this
			.registerTable(new DesktopConfigTable(this))
			.registerTable(new AllAppsTable(this))
			.initTables();

	}
	
	@Override
	public void afterFirstInit(SQLiteDatabase db) {
		getTable(AllAppsTable.class).refreshList(false, db);
		//BlockedAppsTable.initDefaultValues(db, getContext());
	}

}
