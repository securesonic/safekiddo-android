package com.ardurasolutions.safekiddo.helpers;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
	
	public static void showMsg(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
	}
	
	public static void showMsg(Context context, String resId) {
		Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
	}
	
	public static void showMsgShort(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
	
	public static void showMsgShort(Context context, String resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

}
