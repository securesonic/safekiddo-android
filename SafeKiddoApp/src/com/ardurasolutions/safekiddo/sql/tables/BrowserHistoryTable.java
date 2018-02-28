package com.ardurasolutions.safekiddo.sql.tables;

import java.util.Calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.sql.proto.BasicLocalSQL;
import com.ardurasolutions.safekiddo.sql.proto.TableProto;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserHistory;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.hv.console.Console;
import com.hv.querybuilder.QueryBuilder;

public class BrowserHistoryTable extends TableProto {

	public BrowserHistoryTable(BasicLocalSQL lSql) {
		super(lSql, BrowserHistory.class);
	}
	
	public Cursor getSuggest(String filter) {
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(BrowserHistory.FIELD_BROWSER_HISTORY_DATE + " DESC")
			.like(BrowserHistory.FIELD_BROWSER_HISTORY_LABEL + " LIKE ? OR " + BrowserHistory.FIELD_BROWSER_HISTORY_URL + " LIKE ?")
			.addParam("%" + filter + "%")
			.addParam("%" + filter + "%")
			.groupBy(BrowserHistory.FIELD_BROWSER_HISTORY_URL);
		
		return this.db.rawQuery(qb.getSelect(), qb.getParams());
	}
	
	public Cursor getItems() {
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(BrowserHistory.FIELD_BROWSER_HISTORY_DATE + " DESC");
		return this.db.rawQuery(qb.getSelect(), qb.getParams());
	}
	
	public BrowserHistory getLatsItem() {
		BrowserHistory res = null;
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.orderBy(BrowserHistory.FIELD_BROWSER_HISTORY_DATE + " DESC")
			.limit(1);
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			res = DBUtils.currToObj(c, BrowserHistory.class);
		}
		c.close();
		return res;
	}
	
	public BrowserHistory getUrlFromToday(String url) {
		BrowserHistory res = null;
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 1);
		cal.set(Calendar.SECOND, 1);
		
		QueryBuilder qb = new QueryBuilder()
			.from(getTableName())
			.where(BrowserHistory.FIELD_BROWSER_HISTORY_URL + "=?").addParam(url)
			.where(BrowserHistory.FIELD_BROWSER_HISTORY_DATE, ">", Long.toString(cal.getTimeInMillis()))
			.limit(1);
		Cursor c = this.db.rawQuery(qb.getSelect(), qb.getParams());
		if (c.moveToFirst()) {
			res = DBUtils.currToObj(c, BrowserHistory.class);
		}
		c.close();
		return res;
	}
	
	/**
	 * title is laways update when inserts new item
	 * @param url
	 * @param label
	 * @param updateTitle
	 */
	public void addEntry(String url, String label, boolean updateTitle) {
		if (url == null) {
			return;
		}
		if (Constants.getBlockUrl().equals(url)) {
			// INFO nie zapisuj do historii urla z blokadą
			return;
		}
		if (url.startsWith("data:text/html")) {
			// INFO nie zapisuje stron ładowanych za pomocą loadData bez atrybutu <title>
			return;
		}
		if (url.startsWith("file://")) {
			// INFO nie zapisuje ładowania zawartości plików
			return;
		}
		
		//if (Console.isEnabled())
		//	Console.logd("HISTORY: " + url + ", updateTitle=" + updateTitle + ", title=" + label);
		
		try {
			String newLabel = label == null || (label != null && label.trim().length() == 0) ? url.replace("http://", "").replace("https://", "") : label;
			
			BrowserHistory last = getLatsItem();
			if (last != null && last.browser_history_url.equals(url)) {
				ContentValues cv = new ContentValues();
				cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_DATE, System.currentTimeMillis());
				if (updateTitle)
					cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_LABEL, newLabel);
				this.update(cv, BrowserHistory.FIELD_ID + "=?", new String[]{last._id.toString()}, this.getTransactionConn());
			} else {
				BrowserHistory isTodayUrl = getUrlFromToday(url);
				if (isTodayUrl != null) {
					ContentValues cv = new ContentValues();
					cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_DATE, System.currentTimeMillis());
					if (updateTitle)
						cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_LABEL, newLabel);
					this.update(cv, BrowserHistory.FIELD_ID + "=?", new String[]{isTodayUrl._id.toString()}, this.getTransactionConn());
				} else {
					ContentValues cv = new ContentValues();
					cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_LABEL, newLabel);
					cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_URL, url);
					cv.put(BrowserHistory.FIELD_BROWSER_HISTORY_DATE, System.currentTimeMillis());
					this.insert(cv, this.getTransactionConn());
				}
			}
			
		} catch (SQLException e) {
			if (Console.isEnabled())
				Console.loge("BrowserHistoryTable::addEntry[addEntry]", e);
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("BrowserHistoryTable::addEntry[addEntry]", e);
		}
	}
	
	public void deleteEntry(Long _id) {
		this.db.delete(getTableName(), "_id=?", new String[]{_id.toString()});
	}
	
	public void deleteAll() {
		this.db.delete(getTableName(), "_id>?", new String[]{"0"});
	}

	@Override
	public String getFriendlyName() {
		return "";
	}

}
