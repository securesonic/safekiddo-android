package com.ardurasolutions.safekiddo.sql.tables;

import android.content.ContentValues;
import android.database.Cursor;

import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.proto.TableProto;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AppConfig;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public class AppConfigTable extends TableProto {
	
	
	public AppConfigTable(BasicLocalSQL lSql) {
		super(lSql, AppConfig.class);
	}
	
	public void save(String key, String value) {
		ContentValues cv = new ContentValues();
		cv.put(AppConfig.FIELD_APP_CONFIG_KEY, key);
		if (value == null)
			cv.putNull(AppConfig.FIELD_APP_CONFIG_VALUE);
		else
			cv.put(AppConfig.FIELD_APP_CONFIG_VALUE, value);
		AppConfig mAppConfig = load(key);
		if (mAppConfig == null) {
			this.insert(cv, getTransactionConn());
		} else {
			this.update(cv, "_id=?", new String[]{mAppConfig._id.toString()}, getTransactionConn());
		}
	}
	
	public AppConfig load(String key) {
		AppConfig res = null;
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(AppConfig.FIELD_APP_CONFIG_KEY + "=?").addParam(key)
			.limit(1);
		
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		try {
			if (c.moveToFirst()) {
				res = DBUtils.currToObj(c, AppConfig.class);
			}
		} catch (Exception e) {
			BugSenseHandler.sendException(e);
			if (Console.isEnabled())
				Console.loge("AppConfigTable::load", e);
		} finally {
			c.close();
		}
		
		return res;
	}

	@Override
	public String getFriendlyName() {
		return "";
	}

}
