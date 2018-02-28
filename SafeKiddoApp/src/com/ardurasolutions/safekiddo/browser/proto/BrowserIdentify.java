package com.ardurasolutions.safekiddo.browser.proto;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.ardurasolutions.safekiddo.R;

public enum BrowserIdentify {
	
	IDENTIFY_ANDROID(1),
	IDENTIFY_DESKTOP(2);
	
	private int id = 1;
	BrowserIdentify(int idx) {
		this.id = idx;
	}
	
	public int getValue() {
		return id;
	}
	
	public static BrowserIdentify fromInt(int val) {
		BrowserIdentify res = BrowserIdentify.IDENTIFY_ANDROID;
		for(BrowserIdentify f : BrowserIdentify.values()) {
			if (f.getValue() == val) {
				res = f;
				break;
			}
		}
		return res;
	}
	
	/**
	 * cursor cols:<br>
	 * 0 - _id<br>
	 * 1 - name<br>
	 * @param ctx
	 * @return
	 */
	public static Cursor getCursorValues(Context ctx) {
		String[] names = ctx.getResources().getStringArray(R.array.label_browser_identyfication_array);
		
		MatrixCursor c = new MatrixCursor(new String[]{"_id", "Name"});
		for(BrowserIdentify f : BrowserIdentify.values()) {
			c.addRow(new String[]{Integer.toString(f.getValue()), names[f.getValue() - 1]});
			
		}
		return c;
	}

}
