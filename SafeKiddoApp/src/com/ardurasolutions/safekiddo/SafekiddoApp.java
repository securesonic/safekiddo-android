package com.ardurasolutions.safekiddo;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.dev.DevActivity;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.DateTime;
import com.ardurasolutions.safekiddo.helpers.GuardHelper;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.proto.ApplicationProto;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class SafekiddoApp extends ApplicationProto {
	
	private static SafekiddoApp sSafekiddoApp;
	
	public static SafekiddoApp get() {
		return sSafekiddoApp;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sSafekiddoApp = this;
		// INFO disable netty.io logs
		Logger.getLogger("io.netty").setLevel(Level.OFF);
		
		// DB Init
		LocalSQL.getInstance(this);
		
		Console.setEnabled(BuildConfig.DEBUG);
		if (BuildConfig.DEBUG) {
			Console.setTag("safekiddo");
			Console.addLogWriter(new com.hv.console.LogWriterLogCat());
			Console.addLogWriter(new com.hv.console.LogWriterFile(new File(Environment.getExternalStorageDirectory(), Constants.LOG_FILE_NAME)));
			Console.logi("APP INIT: " + DateTime.now());
			
			// INFO enable dev 
			PackageManager pm = getPackageManager();
			ComponentName cnDev = new ComponentName(getPackageName(), DevActivity.class.getName());
			if (pm.getComponentEnabledSetting(cnDev) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
				pm.setComponentEnabledSetting(cnDev, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			}
		}
		
		BugSenseHandler.initAndStartSession(this, Constants.BUG_SENSE_KEY);
		ChildElement cn = UserHelper.getCurrentChildProfile(this);
		if (cn != null)
			UserHelper.setBugSenseUserId(cn);
		
		// TODO : init alarms, services etc...
		if (UserHelper.isUserLogedIn(SafekiddoApp.this)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					
					GuardHelper.checkGuardService(SafekiddoApp.this);
					
				}
			}).start();
		}
		
		//GCMHelper.initGCM(this);
	}

}
