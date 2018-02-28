package com.ardurasolutions.safekiddo.sql.proto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ardurasolutions.safekiddo.sql.BrowserLocalSQL;
import com.ardurasolutions.safekiddo.sql.ConfigSQL;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.hv.console.Console;


public class DBConnection extends SQLiteOpenHelper {
	
	public SQLiteDatabase db;
	public boolean dbCreated = false;
	private String dbFileName;
	//private Context mContext;
	
	
	public DBConnection(Context context, String dbName, int version) {
		super(context, dbName, null, version);
		this.dbFileName = dbName;
		//this.mContext = context;
		this.db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) { 
		dbCreated = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (Console.isEnabled())
			Console.logd("onUpgrade DB: old=" + oldVersion + ", new=" + newVersion + ", file=" + dbFileName);
		
		if (dbFileName.equals(LocalSQL.DB_NAME)) {
			if (newVersion > oldVersion) {
				switch(newVersion) {
					case 2:
						//db.execSQL("DROP TABLE IF EXISTS approved_apps");// + ApprovedApps.getTableName(ApprovedApps.class));
						//db.execSQL(TableSkeleton.getCreateString(BlockedApps.class));
						//BlockedAppsTable.initDefaultValues(db, mContext);
					break;
				}
			}
		} else if (dbFileName.equals(BrowserLocalSQL.DB_NAME)) {
			if (newVersion > oldVersion) {
				
			}
		} else if (dbFileName.equals(ConfigSQL.DB_NAME)) {
			if (newVersion > oldVersion) {
				
			}
		}
	}
	
	public boolean isDbCreated() {
		return this.dbCreated;
	}

}
