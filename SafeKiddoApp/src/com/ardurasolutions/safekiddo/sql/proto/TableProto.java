package com.ardurasolutions.safekiddo.sql.proto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.ardurasolutions.safekiddo.helpers.DateTime;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public abstract class TableProto {
	
	public static final String FIELD_LOCAL_ID = "local_id";
	
	protected Class<?> skeletonClass = TableSkeleton.class;
	public SQLiteDatabase db;
	protected BasicLocalSQL sql;
	
	public abstract String getFriendlyName();
	
	private void constructor(BasicLocalSQL lSql, Class<?> tableClass) {
		this.sql = lSql;
		this.skeletonClass = tableClass;
		this.db = this.sql.getConnection();
	}

	public SQLiteDatabase getNonTransactionConn() {
		return this.db;
	}

	public SQLiteDatabase getTransactionConn() {
		return this.sql.getSyncConnection();
	}

	public TableProto(BasicLocalSQL lSql, Class<?> tableClass) {
		//super(lSql.getContext(), LocalSQL.DB_NAME, null, LocalSQL.DB_VERSION);
		constructor(lSql, tableClass);
	}
	
	public void initTableStruct(SQLiteDatabase dbx) {
		if (Console.isEnabled())
			Console.logd("TABLE INIT: " + getTableName());
		dbx.execSQL(TableSkeleton.getCreateString(getSkeletonClass()));
	}
	
	public Context getContext() {
		return sql.getContext();
	}

	public Class<?> getSkeletonClass() {
		return skeletonClass;
	}

	public TableProto setSkeletonClass(Class<?> skeletonClass) {
		this.skeletonClass = skeletonClass;
		return this;
	}
	
	protected String getTableName(Class<?> c) {
		return TableSkeleton.getTableName(c);
	}
	
	public String getTableName() {
		return getTableName(getSkeletonClass());
	}
	
	public boolean isSyncable() {
		TableAnnotation ta = getSkeletonClass().getAnnotation(TableAnnotation.class);
		return ta == null ? true : ta.syncable();
	}
	
	public String getSyncKey() {
		return "sync_" + getTableName().toLowerCase(Locale.getDefault());
	}
	
	public String getPrimaryKeyName() {
		return TableSkeleton.getPrimaryFieldName(getSkeletonClass());
	}
	
	public void proccessGeneratedValues(LinkedHashMap<String, String> rowData) {
		// default is nothing here
	}
	
	public void dropTable() {
		dropTable(this.db);
	}
	
	public void dropTable(SQLiteDatabase dbx) {
		dbx.execSQL("DROP TABLE IF EXISTS " + getTableName());
	}
	
	public boolean isTableExists() {
		boolean res = false;
		Cursor c = this.db.rawQuery("SELECT name FROM sqlite_master WHERE type=? AND name=?", new String[]{"table", getTableName()});
		if (c.moveToFirst() && c.getCount() == 1) 
			res = true;
		c.close();
		return res;
	}
	
	public void clearData() {
		clearData(this.db);
	}
	
	public void clearData(SQLiteDatabase dbx) {
		dbx.execSQL("DELETE FROM " + getTableName());
	}
	
	/**
	 * return false if sync must be stoped
	 * @param db
	 * @param row
	 * @return
	 */
	public boolean afterRowProcess(SQLiteDatabase db, LinkedHashMap<String, String> row) {
		return true;
	}
	
	public void afterSyncUpdate(SQLiteDatabase db) {}
	
	/*
	 * statment preparations
	 */
	
	public SQLiteStatement prepareSyncDeleteStatement(SQLiteDatabase db){
		
		return db.compileStatement("DELETE FROM " + getTableName() + " WHERE " + getPrimaryKeyName() + "=? AND " + TableSkeleton.FIELD_SYNC_IS_REJECTED + "=0 ");
	}
	
	public SQLiteStatement prepareSyncInsertStatement(String[] f, SQLiteDatabase db){
		/*
		 * składanie zapytania INSERT
		 * sprawdzane jest czy pola jakie przyszły są zgodne z tym co
		 * jest zdefiniowane w strukturze tabeli, pomijany jest _id
		 */
		QueryBuilder qb = new QueryBuilder().from(getTableName());
		ArrayList<String> tableFields = TableSkeleton.getFieldsArray(getSkeletonClass());
		
		for(String s : f) {
			if (s.equals("_id")) continue;
			if (!tableFields.contains(s)) continue;
			qb.into(s).values("?");
		}
		
		// na potrzeby generowania danych (pola z "_ss")
		for(String s : TableSkeleton.getFieldsGeneratedArray(getSkeletonClass()))
			qb.into(s).values("?");
		
		qb.into(TableSkeleton.FIELD_SYNC_IS_CHANGED).into(TableSkeleton.FIELD_SYNC_IS_DELETED)
			.values("0").values("0");
		
		return db.compileStatement(qb.getInsert());
	}
	
	public SQLiteStatement prepareSyncUpdateStatement(String[] f, SQLiteDatabase db){
		/*
		 * składanie zapytania UPDATE
		 * w pętli pomijamy local_id oraz klucz głowny
		 * doklejany jest po pętli w warunku WHERE
		 */
		
		QueryBuilder qb = new QueryBuilder().from(getTableName());
		ArrayList<String> tableFields = TableSkeleton.getFieldsArray(getSkeletonClass());
		
		for(String s : f) {
			if (s.equals("_id") || s.equals(getPrimaryKeyName())) continue;
			if (!tableFields.contains(s)) continue;
			qb.updateSet(s, "?");
		}
		
		// na potrzeby generowania danych (pola z "_ss")
		for(String s : TableSkeleton.getFieldsGeneratedArray(getSkeletonClass()))
			qb.updateSet(s, "?");
		
		qb.updateSet(TableSkeleton.FIELD_SYNC_IS_CHANGED, "0").where(getPrimaryKeyName() + "=? AND " + TableSkeleton.FIELD_SYNC_IS_REJECTED + "=0");
		
		return db.compileStatement(qb.getUpdate());
	}
	
	public SQLiteStatement prepareSyncUpdateLocalStatement(String[] f, SQLiteDatabase db){
		/*
		 * składanie zapytania UPDATE
		 * które podmieni wartosć z klucza głownego
		 * tym co jest w _id, pomijane sa nieistniejące pola
		 */
		
		QueryBuilder qb = new QueryBuilder().from(getTableName());
		ArrayList<String> tableFields = TableSkeleton.getFieldsArray(getSkeletonClass());
		
		for(String s : f) {
			if (s.equals("_id")) continue;
			if (!tableFields.contains(s)) continue;
			qb.updateSet(s, "?");
		}
		
		// na potrzeby generowania danych (pola z "_ss")
		for(String s : TableSkeleton.getFieldsGeneratedArray(getSkeletonClass()))
			qb.updateSet(s, "?");
		
		qb.updateSet(TableSkeleton.FIELD_SYNC_IS_CHANGED, "0").where(getPrimaryKeyName() + "=? AND " + TableSkeleton.FIELD_SYNC_IS_REJECTED + "=0");
		return db.compileStatement(qb.getUpdate());
	}
	
	// ----------------------------------------------------
	
	public static String getCreateString(Class<?> c) {
		StringBuilder res = new StringBuilder();
		
		res.append("CREATE TABLE IF NOT EXISTS " + TableSkeleton.getTableName(c) + "(\n_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL \n");
		for(Field f : c.getFields()) {
			if (f.getName().equals("_id")) continue;
			
			// INFO : pominięcie virtualnych pól oraz satycznych oznaczajacych nazwy kolumn
			FieldAnnotation fa = f.getAnnotation(FieldAnnotation.class);
			if (fa != null && fa.virtualField()) continue;
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
			
			res.append(", " + f.getName() + " ");
			if (f.getType().equals(String.class)) {
				res.append("TEXT");
			} else if (f.getType().equals(Integer.class) || f.getType().equals(Long.class)){
				res.append("INTEGER");
			} else if (f.getType().equals(int.class)){
				res.append("INT");
			} else if (f.getType().equals(Double.class)){
				res.append("DOUBLE");
			} else if (f.getType().equals(byte[].class)){
				res.append("BLOB");
			} else {
				res.append("TEXT");
			}
			
			if (fa != null) {
				res.append(" " + fa.extra());
			}
			res.append(" \n");
		}
		res.append(")");
		
		return res.toString();
	}
	
	public Cursor selectAll(String extra, String[] params) {
		return this.db.rawQuery("SELECT * FROM " + getTableName() + " " + extra, params);
	}
	
	public Cursor selectAll() {
		return this.db.rawQuery("SELECT * FROM " + getTableName(), null);
	}
	
	public <T> ArrayList<T> selectAllArray(Class<T> cx) {
		ArrayList<T> res = new ArrayList<T>();
		Cursor c = this.db.rawQuery("SELECT * FROM " + getTableName() + " WHERE " + TableSkeleton.FIELD_SYNC_IS_DELETED + " = 0", null);
		if (c.moveToFirst()) {
			do {
				res.add(DBUtils.currToObj(c, cx));
			} while (c.moveToNext());
		}
		c.close();
		return res;
	}
	
	public <T> T getRowById(Long _id, Class<T> cc) {
		return selectRow("_id=?", new String[]{_id.toString()}, cc, this.db);
	}
	
	public <T> T getRow(Long primaryKeyId, Class<T> cc) {
		return selectRow(getPrimaryKeyName() + "=?", new String[]{primaryKeyId.toString()}, cc, this.db);
	}
	
	public <T> T selectRow(String where, String[] args,Class<T> cc) {
		return selectRow(where, args, cc, this.db);
	}
	
	public <T> T selectRow(String where, String[] args,Class<T> cc, SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT * FROM " + getTableName() + " " + (!where.equals("") ? "WHERE " + where : "") + " LIMIT 1", args);
		T res = null;
		if (c.moveToFirst()) {
			res = DBUtils.currToObj(c, cc);
		}
		c.close();
		return res;
	}
	
	/**
	 * Delete by "_id" field
	 * @param _id
	 * @return
	 */
	public int delete(Long _id, SQLiteDatabase dbConn) {
		return delete("_id=?", new String[]{Long.toString(_id)}, dbConn);
	}
	
	public int delete(String where, String[] args, SQLiteDatabase dbConn) {
		ContentValues cv = new ContentValues();
		cv.put(TableSkeleton.FIELD_SYNC_IS_DELETED, 1);
		//cv.put(TableSkeleton.FIELD_SYNC_IS_REJECTED, LocalSQL.isSyncInProgress(getSyncGroup()) ? 1 : 0);
		cv.put(TableSkeleton.FIELD_DEVICE_TIMESTAMP, DateTime.nowLong());
		return dbConn.update(getTableName(), cv, where, args);
	}
	
	/**
	 * add system fields:<ul>
	 * <li>sync_isChanged = 2 (only local, not synced)</li>
	 * <li>sync_isDeleted = 0 (not deleted)</li>
	 * <li>server_timestamp = 0 (not known for now)</li>
	 * <li>generated_id = 0 - wywalone pole</li>
	 * </ul>
	 * @param cv
	 * @return
	 */
	public long insert(ContentValues cv, SQLiteDatabase dbConn) {
		return insert(cv, false, dbConn);	
	}
	public long insert(ContentValues cv, boolean reaturnGeneratedId, SQLiteDatabase dbConn) {
		return insert(cv, reaturnGeneratedId, dbConn, getPrimaryKeyName(), getTableName());
	}
	public static long insert(ContentValues cv, boolean reaturnGeneratedId, SQLiteDatabase dbConn, String primaryKey, String tableName) {
		Long generatedID = generateId();
		if (!cv.containsKey(TableSkeleton.FIELD_SYNC_IS_CHANGED))
			cv.put(TableSkeleton.FIELD_SYNC_IS_CHANGED, 2);
		if (!cv.containsKey(TableSkeleton.FIELD_SYNC_IS_DELETED))
			cv.put(TableSkeleton.FIELD_SYNC_IS_DELETED, 0);
		cv.put(TableSkeleton.FIELD_SERVER_TIMESTAMP, 0);
		cv.put(TableSkeleton.FIELD_DEVICE_TIMESTAMP, DateTime.nowLong());
		cv.put(primaryKey, generatedID);
		if (reaturnGeneratedId) {
			dbConn.insert(tableName, null, cv);
			return generatedID;
		} else
			return dbConn.insert(tableName, null, cv);
	}
	
	/**
	 * Update a single row<br />
	 * Before update checking item state (sync_isChanged value)<br />
	 * 0 -> 1<br />
	 * 1 -> 1<br />
	 * 2 -> 2<br />
	 * TableSkeleton.FIELD_SYNC_IS_REJECTED = isSyncInProgress ? 1 : 0
	 * @param cv
	 * @param where
	 * @param args
	 * @return number of rows affected 
	 */
	public long update(ContentValues cv, String where, String[] args, SQLiteDatabase dbConn) {
		
		// INFO : sprawdzenie jaka flagę ustawić w sync_isChanged
		//if (!LocalSQL.isSyncInProgress(getSyncGroup())) {
			try {
				QueryBuilder q = new QueryBuilder()
					.from(getTableName())
					.select(TableSkeleton.FIELD_SYNC_IS_CHANGED)
					.where(where)
					.addParams(args);
				
				Cursor c = dbConn.rawQuery(q.getSelect(), q.getParams());//getNonTransactionConn().rawQuery(q.getSelect(), q.getParams());
				if (c.moveToFirst()) {
					switch (c.getInt(0)) {
						case 0: cv.put(TableSkeleton.FIELD_SYNC_IS_CHANGED, 1); break;
						case 1: break;
						case 2: break;
					}
				}
				c.close();
			} catch (SQLException e) {
				BugSenseHandler.sendExceptionMessage("TableProto", "update", e);
			}
		//}
		
		//cv.put(TableSkeleton.FIELD_SYNC_IS_REJECTED, LocalSQL.isSyncInProgress(getSyncGroup()) ? 1 : 0);
		cv.put(TableSkeleton.FIELD_DEVICE_TIMESTAMP, DateTime.nowLong());
		
		return dbConn.update(getTableName(), cv, where, args);
	}

	/**
	 * Generate a unique (for device) number
	 * @return
	 */
	private static Long lastGeneratedId = 0L;
	public static Long generateId() {
		Long genId = Calendar.getInstance().getTimeInMillis();
		if (lastGeneratedId == genId) {
			genId++;
		} 
		lastGeneratedId = genId;
		return genId;
	}
	
	/**
	 * return number of records in table
	 * @return
	 */
	public int getCount() {
		return getCount("", null);
	}
	
	/**
	 * return records in table whith WHERE params
	 * @param where
	 * @param params
	 * @return
	 */
	public int getCount(String where, String[] params) {
		int res = 0;
		
		QueryBuilder qb = new QueryBuilder()
			.select("IFNULL(COUNT(*), 0)")
			.from(getTableName())
			.where(where)
			.addParams(params);
		
		Cursor c = this.db.rawQuery(qb.getSelect(), null);
		if (c.moveToFirst()) {
			res = c.getInt(0);
		}
		c.close();
		return res;
	}

	public MaxRow getMaxRow() {
		MaxRow res = null;
		
		// INFO : dodno sortowanie po kluczu głownym (max id klucza głownego)
		Cursor c = this.db.rawQuery(
			"SELECT " + TableSkeleton.FIELD_SERVER_TIMESTAMP + "," + getPrimaryKeyName() + " FROM " + getTableName() + 
			" WHERE " + TableSkeleton.FIELD_SYNC_IS_CHANGED + " < 2 ORDER BY " + TableSkeleton.FIELD_SERVER_TIMESTAMP + " DESC, " + getPrimaryKeyName() + 
			" DESC LIMIT 1", null
		);
		if (c.moveToFirst()) {
			res = new MaxRow();
			res.setTimestamp(c.getLong(0));
			res.setId(c.getLong(1));
		}
		c.close();
		
		return res;
	}
	
	/**
	 * get all [primary key], sync_isDeleted values from table
	 * @param db
	 * @return
	 */
	@SuppressLint("UseSparseArrays")
	public HashMap<Long, Integer> getAllPrimaryData(SQLiteDatabase db){
		HashMap<Long, Integer> res = new HashMap<Long, Integer>();
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.select(getPrimaryKeyName() + "," + TableSkeleton.FIELD_SYNC_IS_DELETED);
		
		Cursor c = db.rawQuery(qb.getSelect(), null);
		if (c.moveToFirst()) {
			do {
				res.put(c.getLong(0), c.getInt(1));
			} while(c.moveToNext());
		}
		c.close();
		
		return res;
	}

}
