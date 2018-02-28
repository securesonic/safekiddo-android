package com.ardurasolutions.safekiddo.proto;

import android.content.Intent;
import android.os.Bundle;

import com.hv.console.Console;

public class PinActivityConfig {
	
	public static enum AccessType {
		ACC_UNINSTALL,
		ACC_ADMIN,
		/**
		 * startActivityForResult
		 */
		ACC_FOR_RESULT,
		/**
		 * runs activity from SK app
		 */
		ACC_START_ACTIVITY;
	}
	
	public static final String KEY_PKG_NAME = " pkg_name";
	public static final String KEY_ACCESS_TYPE = "access_type";
	public static final String KEY_ACTIVITY_NAME = "activity_name";
	public static final String KEY_FINISH_ON_BACK = "finish_on_back";
	
	private String pkg = null;
	private AccessType mAccessType = null;
	private Class<?> activityClass = null;
	private boolean finishOnBack = false;
	
	public PinActivityConfig(){}
	public PinActivityConfig(Class<?> className) {
		setActivityClass(className);
	}
	public PinActivityConfig(AccessType acc) {
		setAccessType(acc);
	}
	/**
	 * constructor with package name
	 * @param p - package name
	 */
	public PinActivityConfig(String p) {
		setPkg(p);
	}
	
	public String getPkg() {
		return pkg;
	}
	public PinActivityConfig setPkg(String pkg) {
		this.pkg = pkg;
		return this;
	}
	public AccessType getAccessType() {
		return mAccessType;
	}
	public PinActivityConfig setAccessType(AccessType mAccessType) {
		this.mAccessType = mAccessType;
		return this;
	}
	public boolean isFinishOnBack() {
		return finishOnBack;
	}
	public PinActivityConfig setFinishOnBack(boolean finishOnBack) {
		this.finishOnBack = finishOnBack;
		return this;
	}
	public Class<?> getActivityClass() {
		return activityClass;
	}
	public PinActivityConfig setActivityClass(Class<?> activityClass) {
		this.activityClass = activityClass;
		return this;
	}
	
	public void saveToIntent(Intent it) {
		if (it != null) {
			
			if (getAccessType() != null)
				it.putExtra(KEY_ACCESS_TYPE, getAccessType().toString());
			
			if (getPkg() != null) 
				it.putExtra(KEY_PKG_NAME, getPkg());
			
			if (getActivityClass() != null) {
				it.putExtra(KEY_ACTIVITY_NAME, getActivityClass().getName());
				it.putExtra(KEY_ACCESS_TYPE, AccessType.ACC_START_ACTIVITY.toString());
			}
			
			if (isFinishOnBack())
				it.putExtra(KEY_FINISH_ON_BACK, true);
		}
	}
	
	public void saveToBundle(Bundle it) {
		if (it != null) {
			
			if (getAccessType() != null)
				it.putString(KEY_ACCESS_TYPE, getAccessType().toString());
			
			if (getPkg() != null) 
				it.putString(KEY_PKG_NAME, getPkg());
			
			if (getActivityClass() != null) {
				it.putString(KEY_ACTIVITY_NAME, getActivityClass().getName());
				it.putString(KEY_ACCESS_TYPE, AccessType.ACC_START_ACTIVITY.toString());
			}
			
			if (isFinishOnBack())
				it.putBoolean(KEY_FINISH_ON_BACK, true);
		}
	}
	
	@Override
	public String toString() {
		return "{pkg=" + pkg + ", mAccessType=" + mAccessType + ", activityClass=" + activityClass + ", finishOnBack=" + finishOnBack + "}";
	}
	
	public static PinActivityConfig fromIntent(Intent it) {
		PinActivityConfig res = new PinActivityConfig();
		
		if (it != null) {
			res.setPkg(it.getStringExtra(KEY_PKG_NAME));
			
			String accName = it.getStringExtra(KEY_ACCESS_TYPE);
			if (accName != null) {
				try {
					res.setAccessType(AccessType.valueOf(accName));
				} catch (Exception e) {}
			}
			
			String activityClassName = it.getStringExtra(KEY_ACTIVITY_NAME);
			if (activityClassName != null) {
				try {
					res.setActivityClass(Class.forName(activityClassName));
				} catch (ClassNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("PinActivityConfig :: fromIntent", e);
				}
			}
			
			res.setFinishOnBack(it.getBooleanExtra(KEY_FINISH_ON_BACK, false));
		}
		
		return res;
	}
	
	public static PinActivityConfig fromBundle(Bundle it) {
		PinActivityConfig res = new PinActivityConfig();
		
		if (it != null) {
			res.setPkg(it.getString(KEY_PKG_NAME));
			
			String accName = it.getString(KEY_ACCESS_TYPE);
			if (accName != null) {
				try {
					res.setAccessType(AccessType.valueOf(accName));
				} catch (Exception e) {}
			}
			
			String activityClassName = it.getString(KEY_ACTIVITY_NAME);
			if (activityClassName != null) {
				try {
					res.setActivityClass(Class.forName(activityClassName));
				} catch (ClassNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("PinActivityConfig :: fromIntent", e);
				}
			}
			
			res.setFinishOnBack(it.getBoolean(KEY_FINISH_ON_BACK, false));
		}
		
		return res;
	}
	

}
