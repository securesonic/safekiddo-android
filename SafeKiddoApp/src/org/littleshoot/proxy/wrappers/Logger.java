package org.littleshoot.proxy.wrappers;

import android.util.Log;

import com.ardurasolutions.safekiddo.BuildConfig;

public class Logger {
	
	public static boolean isEnabled() {
		return false;
	}
	
	private static String TAG = "logger";
	
	public Logger(String tagName) {
		TAG = tagName;
	}

	public void error(String s, Throwable t){
		if (BuildConfig.DEBUG)
			Log.e(TAG, s);
	}
	
	public void warn(String s, Throwable t){
		if (BuildConfig.DEBUG)
			Log.w(TAG, s);
	}
	
	public void warn(String s){
		if (BuildConfig.DEBUG)
			Log.w(TAG, s);
	}
	
	public void info(String s, Throwable t){
		if (BuildConfig.DEBUG)
			Log.i(TAG, s);
	}
	
	public void info(String s){
		if (BuildConfig.DEBUG)
			Log.i(TAG, s);
	}
	
}
