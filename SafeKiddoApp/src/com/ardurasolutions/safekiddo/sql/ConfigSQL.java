package com.ardurasolutions.safekiddo.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AppConfigTable;

public class ConfigSQL extends BasicLocalSQL {
	
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "config";
	private static ConfigSQL sInstance = null;
	
	public static synchronized ConfigSQL getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ConfigSQL(context.getApplicationContext());
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
	
	public ConfigSQL(Context context) {
		super(context);
		
		this
			.registerTable(new AppConfigTable(this))
			.initTables();

	}
	
	@Override
	public void afterFirstInit(SQLiteDatabase db) {
	}

}
