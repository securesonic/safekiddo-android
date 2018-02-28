package com.ardurasolutions.safekiddo.sql.proto;

import java.io.File;
import java.util.LinkedHashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public abstract class BasicLocalSQL {
	
	private Context mContext;
	private LinkedHashMap<Class<?>, TableProto> tables = new LinkedHashMap<Class<?>, TableProto>();

	private DBConnection conn, syncConn;
	
	public synchronized BasicLocalSQL clearConnections() {
		if (conn != null && conn.db.isOpen())
			conn.db.close();
		if (syncConn != null && syncConn.db.isOpen())
			syncConn.db.close();
		return this;
	}
	
	/**
	 * remove db file<br>
	 * before delete file call <i>clearConnections</i>
	 */
	public synchronized BasicLocalSQL clearDbFile() {
		clearConnections();
		File dbFile = getContext().getDatabasePath(getDbFileName());
		if (dbFile != null && dbFile.exists()) {
			dbFile.delete();
		}
		return this;
	}
	
	public BasicLocalSQL(Context context) {
		mContext = context;
		
		this.conn = new DBConnection(mContext, getDbFileName(), getDbVersion());
		this.syncConn = new DBConnection(mContext, getDbFileName(), getDbVersion());
	}
	
	public abstract String getDbFileName();
	public abstract int getDbVersion();
	public abstract void afterFirstInit(SQLiteDatabase db);
	
	public BasicLocalSQL initTables() {
		if (this.conn.isDbCreated()) {
			forceCreateDb();
			afterFirstInit(this.conn.db);
		}
		return this;
	}
	
	public BasicLocalSQL registerTable(TableProto tab) {
		tables.put(tab.getSkeletonClass(), tab);
		return this;
	}
	
	public void clearTablesData() {
		this.syncConn.db.beginTransaction();
		try {
			for(TableProto t : getTables().values()) {
				t.delete("_id > 0", null, this.syncConn.db);
			}
			this.syncConn.db.setTransactionSuccessful();
		} catch (Exception e) {
			BugSenseHandler.sendExceptionMessage("BasicLocalSQL", "clearTablesData", e);
			if (Console.isEnabled())
				Console.loge("BasicLocalSQL :: clearTablesData", e);
		} finally {
			this.syncConn.db.endTransaction();
		}
	}
	
	/**
	 * init all table structures<br>not <b>files</b> table
	 */
	public void forceCreateDb() {
		if (tables.size() > 0) {
			try {
				getConnection().beginTransaction();
				try {
					for(Class<?> tabSkelClass : tables.keySet()) {
						TableProto tp = tables.get(tabSkelClass);
						tp.initTableStruct(getConnection());
					}
					getConnection().setTransactionSuccessful();
				} finally {
					getConnection().endTransaction();
				}
			} catch (Exception e) {
				BugSenseHandler.sendExceptionMessage("BasicLocalSQL", "DBStructCreation", e);
				if (Console.isEnabled())
					Console.loge("LocalSQL::DBStructCreation", e);
			}
		}
	}
	
	public SQLiteDatabase getConnection() {
		return this.conn.db;
	}
	
	public SQLiteDatabase getSyncConnection() {
		return this.syncConn.db;
	}
	

	@SuppressWarnings("unchecked")
	public <T> T getTable(Class<?> skel, Class<T> tabCLass) {
		if (tables.containsKey(skel)) {
			return (T)tables.get(skel);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getTable(Class<T> tabCLass) {
		for(Class<?> c : tables.keySet()) {
			if (tables.get(c).getClass().equals(tabCLass)) {
				return (T)tables.get(c);
			}
		}
		return null;
	}
	
	public TableProto getTableProto(Class<?> tab) {
		if (tables.containsKey(tab)) {
			return tables.get(tab);
		} else {
			return null;
		}
	}
	
	public TableProto getTableByName(String tabName) {
		TableProto res = null;
		for(TableProto tab : this.tables.values()) {
			if (tab.getTableName().equals(tabName)) {
				return tab;
			}
		}
		return res;
	}
	
	public LinkedHashMap<Class<?>, TableProto> getTables() {
		return tables;
	}
	
	public Context getContext() {
		return mContext;
	}

}
