package com.ardurasolutions.safekiddo.helpers;

import java.io.File;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.ardurasolutions.safekiddo.activities.DummyUserSettings;
import com.ardurasolutions.safekiddo.activities.FirstRunActivity;
import com.ardurasolutions.safekiddo.auth.SKAdmin;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.browser.BrowserMainActivity;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager;
import com.ardurasolutions.safekiddo.launcher.Desktop;
import com.ardurasolutions.safekiddo.receviers.BootRecevier;
import com.ardurasolutions.safekiddo.receviers.PackageInstall;
import com.ardurasolutions.safekiddo.services.GuardService;
import com.ardurasolutions.safekiddo.services.ProxyService;
import com.ardurasolutions.safekiddo.sql.BrowserLocalSQL;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class UserHelper {
	
	public static boolean isUserLogedIn(Context ctx) {
		Config prefs = Config.getInstance(ctx);
		
		// no saved user name
		String userLogin = prefs.load(Config.KeyNames.USER_LOGIN, "");
		if (userLogin.equals("")) return false;
		
		// user logout by server
		if (prefs.load(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, false)) {
			return false;
		}
		
		// if no device admin
		DevicePolicyManager mDPM = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(ctx, SKAdmin.class);
		if (!mDPM.isAdminActive(mDeviceAdmin))
			return false;
		
		// no current profile
		if (getCurrentChildProfile(ctx) == null) 
			return false;
		
		return true;
	}
	
	public static void saveUserName(Context ctx, String name) {
		Config.getInstance(ctx)
			.save(Config.KeyNames.USER_LOGIN, name);
	}
	
	public static String getUserName(Context ctx) {
		return 
			Config.getInstance(ctx)
				.load(Config.KeyNames.USER_LOGIN, (String) null);
	}
	
	public static void saveCurrentChildProfile(Context ctx, ChildElement child) {
		if (child != null) {
			Config.getInstance(ctx)
				.save(Config.KeyNames.USER_CHILD_UUID, child.getUuid())
				.save(Config.KeyNames.USER_CHILD_ID, child.getId().toString())
				.save(Config.KeyNames.USER_CHILD_NAME, child.getName());
		} else {
			Config.getInstance(ctx)
				.save(Config.KeyNames.USER_CHILD_UUID, (String) null)
				.save(Config.KeyNames.USER_CHILD_ID, (String) null)
				.save(Config.KeyNames.USER_CHILD_NAME, (String) null);
		}
		setBugSenseUserId(child);
	}
	
	public static void setBugSenseUserId(ChildElement child) {
		BugSenseHandler.setUserIdentifier(child != null ? child.getUuid() : "");
	}
	
	public static ChildElement getCurrentChildProfile(Context ctx) {
		ChildElement res = new ChildElement();
		Config c = Config.getInstance(ctx);
		res.setId(c.load(Config.KeyNames.USER_CHILD_ID, 0L));
		res.setUuid(c.load(Config.KeyNames.USER_CHILD_UUID, ""));
		res.setName(c.load(Config.KeyNames.USER_CHILD_NAME, (String) null));
		
		return res.getId() != 0L && res.getUuid() != null && res.getName() != null ? res : null;
	}
	
	public static void setUserBlockApps(Context ctx, boolean blockApps) {
		Config.getInstance(ctx)
			.save(Config.KeyNames.USER_BLOCK_APPS, blockApps);
	}
	
	public static interface OnBeforeKillApp {
		public void onBeforeKillApp();
	}
	
	public static void cleanupSafekiddo(final Context ctx, final OnBeforeKillApp bk) {
		// defaults
		try {
			ctx.getPackageManager().clearPackagePreferredActivities(ctx.getPackageName());
		} catch (SecurityException e) {
			if (Console.isEnabled())
				Console.loge("UserHelper :: cleanupSafekiddo", e);
		}
		
		// services
		CommonUtils.stopServiceSafe(ctx, new Intent(ctx, GuardService.class));
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				//ProxySystem.usetWiFiProxySafe(ctx, "127.0.0.1", Constants.LOCAL_PROXY_PORT);
				
				//ctx.stopService(new Intent(ctx, BrowserObserverService.class));
				CommonUtils.stopServiceSafe(ctx, new Intent(ctx, ProxyService.class));
			}
		}).start();
		
		// cache
		File c = WebFragmentsManager.getWebCacheDir(ctx.getCacheDir());
		File[] fl = c.listFiles();
		if (fl != null) {
			for(File f : fl) {
				f.delete();
			}
		}
		c.delete();
		
		// prefs
		Config.getInstance(ctx).clearAllData();
		
		// DB
		LocalSQL sql1 = LocalSQL.getInstance(ctx);
		//sql1.clearTablesData();
		sql1.clearDbFile();
		sql1.clearInstance();
		
		BrowserLocalSQL sql2 = BrowserLocalSQL.getInstance(ctx);
		//sql2.clearTablesData();
		sql2.clearDbFile();
		sql2.clearInstance();
		
		//ConfigSQL sql3 = ConfigSQL.getInstance(ctx);
		//sql3.clearTablesData();
		
		
		// DA
		DevicePolicyManager mDPM = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName mDeviceAdmin = new ComponentName(ctx, SKAdmin.class);
		mDPM.removeActiveAdmin(mDeviceAdmin);
		
		// launcher
		final PackageManager pm = ctx.getPackageManager();
		pm.setComponentEnabledSetting(
			new ComponentName(ctx.getPackageName(), Desktop.class.getName()), 
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
			PackageManager.DONT_KILL_APP
		);
		pm.setComponentEnabledSetting(
			new ComponentName(ctx.getPackageName(), DummyUserSettings.class.getName()), 
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
			PackageManager.DONT_KILL_APP
		);
		
		pm.setComponentEnabledSetting(
			new ComponentName(ctx.getPackageName(), FirstRunActivity.class.getName()), 
			PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
			PackageManager.DONT_KILL_APP
		);
		pm.setComponentEnabledSetting(
			new ComponentName(ctx.getPackageName(), PackageInstall.class.getName()), 
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
			PackageManager.DONT_KILL_APP
		);
		pm.setComponentEnabledSetting(
			new ComponentName(ctx.getPackageName(), BootRecevier.class.getName()), 
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
			PackageManager.DONT_KILL_APP
		);
		
		HeartBeatHelper.clearAlarm(ctx);
		
		// load def luncher
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(i);
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				pm.setComponentEnabledSetting(
					new ComponentName(ctx.getPackageName(), BrowserMainActivity.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					0 //PackageManager.DONT_KILL_APP
				);
			}
		};
		
		if (bk != null) {
			bk.onBeforeKillApp();
			new Handler().postDelayed(r, 1500);
		} else {
			r.run();
		}
	}

}
