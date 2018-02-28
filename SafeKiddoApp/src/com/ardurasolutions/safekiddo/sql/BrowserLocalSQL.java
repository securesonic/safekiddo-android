package com.ardurasolutions.safekiddo.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.BrowserFavsTable;
import com.ardurasolutions.safekiddo.sql.tables.BrowserHistoryTable;

public class BrowserLocalSQL extends BasicLocalSQL {
	
	public static final int DB_VERSION = 1;
	public static final String DB_NAME = "browser";
	private static BrowserLocalSQL sInstance = null;
	
	public static synchronized BrowserLocalSQL getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new BrowserLocalSQL(context.getApplicationContext());
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
	
	public BrowserLocalSQL(Context context) {
		super(context);
		
		this
			.registerTable(new BrowserHistoryTable(this))
			.registerTable(new BrowserFavsTable(this))
			.initTables();
	}
	
	@Override
	public void afterFirstInit(SQLiteDatabase db) { }

}
