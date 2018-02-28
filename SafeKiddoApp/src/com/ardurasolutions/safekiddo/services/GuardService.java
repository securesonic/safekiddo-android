package com.ardurasolutions.safekiddo.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.proto.LocalServiceBinder;
import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.proto.PinActivityConfig.AccessType;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class GuardService extends Service {
	
	//private static GuardService sGuardService;
	
	private final IBinder mBinder = new LocalServiceBinder<GuardService>(this);

	private ActivityManager am;
	private BroadcastReceiver screenRecevier;
	private boolean breakLoop = false;
	private ArrayList<BasicNameValuePair> blockedApps;
	private String tempAppAcccess = "";
	private boolean tempAccessUninstall = false;
	private boolean tempAccessAdmin = false;
	//private boolean tempAccessSettings = false;
	//private ApprovedAppsTable mApprovedAppsTable;
	private AllAppsTable mAllAppsTable;
	//private ArrayList<String> approvedPackeges;
	private ArrayList<String> blockedAppList;
	private boolean isSettingsBlocked = false;
	private BroadcastReceiver mApprovedAppsChanged;
	private Boolean isWorking = Boolean.FALSE;
	
	private static final String PKG_UNINSTALL = "com.android.packageinstaller";
	private static final String PKG_UNINSTALL_CLASS = "com.android.packageinstaller.UninstallerActivity";
	//private static final String PKG_UNINSTALL_PROCESS_CLASS = "com.android.packageinstaller.UninstallerActivity";
	
	private static final String PKG_SETTINGS = "com.android.settings";
	//private static final String PKG_SETTINGS_CLASS = "com.android.settings.Settings";
	//private static final String PKG_SETTINGS_SUBSETTINGS_CLASS = "com.android.settings.SubSettings";
	private static final String PKG_SETTINGS_ADMIN_CLASS = "com.android.settings.DeviceAdminAdd";
	private static final String PKG_TASK_LIST = "com.android.systemui";
	private static final String PKG_TASK_LIST_CLASS = "com.android.systemui.recent.RecentsActivity";
	// ComponentInfo{com.android.packageinstaller/com.android.packageinstaller.UninstallAppProgress}
	// ComponentInfo{com.android.systemui/com.android.systemui.recent.RecentsActivity}

	
	boolean mBound = false;
	private LocalServiceBinder<PinService> binder;
	private ServiceConnection mConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				binder = (LocalServiceBinder<PinService>) service;
				mBound = true;
			} catch (ClassCastException e) {
				mBound = false;
				BugSenseHandler.sendExceptionMessage("GUARD", "bind_service_onServiceConnected", e);
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		//sGuardService = this;
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE); 
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		
		screenRecevier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					breakLoop = true;
				} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					breakLoop = false;
					startService(new Intent(GuardService.this, GuardService.class));
				}
			}
		};
		
		registerReceiver(screenRecevier, filter);
		
		
		mAllAppsTable = LocalSQL.getInstance(this).getTable(AllAppsTable.class);
		blockedAppList = mAllAppsTable.getBlockedAppsPackages();
		isSettingsBlocked = blockedAppList.contains(PKG_SETTINGS);
		
		blockedApps = new ArrayList<BasicNameValuePair>();
		blockedApps.add(new BasicNameValuePair(PKG_SETTINGS, PKG_SETTINGS_ADMIN_CLASS));
		blockedApps.add(new BasicNameValuePair(PKG_UNINSTALL, PKG_UNINSTALL_CLASS));
		
		mApprovedAppsChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logi("Get refresh blocked apps");
				blockedAppList.clear();
				blockedAppList.addAll(mAllAppsTable.getBlockedAppsPackages());
				isSettingsBlocked = blockedAppList.contains(PKG_SETTINGS);
			}
		};
		registerReceiver(mApprovedAppsChanged, new IntentFilter(Constants.BRODCAST_BLOCKED_APPS));
		
		bindService(new Intent(this, PinService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private String pinPkg = "", pinClass= ""; 
	
	// TODO wyłaczyć dla api 21+
	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Console.isEnabled())
			Console.logi("Start GUARD service...");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				isWorking = Boolean.TRUE;
				/*
				 * od api 21+ nie pobierze się już listy tasków
				 */
				List<RunningTaskInfo> taskInfo = am.getRunningTasks(1);
				ComponentName prevTask = taskInfo != null && taskInfo.size() > 0 ? taskInfo.get(0).topActivity : null;
				while(true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) { }
					
					if (breakLoop) {
						break;
					}
					
					taskInfo = am.getRunningTasks(1);
					if (taskInfo == null) continue;
					if (taskInfo.size() == 0) continue;
					ComponentName componentInfo = taskInfo.get(0).topActivity;
					if (componentInfo == null) continue;
					
					// Console.log("TOP TASK: " + componentInfo);
					
					final String currPkg = componentInfo.getPackageName();
					final String currCls = componentInfo.getClassName();
					
					if (!componentInfo.equals(prevTask)) {
						if (Console.isEnabled())
							Console.logd("TASK CHANGED: " + componentInfo);
						if (binder != null && binder.getService() != null && binder.getService().isPinWindowVisible())
							binder.getService().closeWindow();
					}
					
					prevTask = componentInfo;
					
					// if top is task list continue - not reset nothing
					if (currPkg.equals(PKG_TASK_LIST) && currCls.equals(PKG_TASK_LIST_CLASS)) {
						continue;
					}
					
					if (currPkg.equals(getPackageName())) {
						tempAccessUninstall = false;
						tempAccessAdmin = false;
						tempAppAcccess = "";
						if (mBound && binder.getService().isPinWindowVisible())
							binder.getService().closeWindow();
						continue;
					}
					
					if (!isSettingsBlocked) {
						if (tempAccessAdmin && (currPkg.equals(PKG_SETTINGS) && currCls.equals(PKG_SETTINGS_ADMIN_CLASS))) {
							continue;
						} else {
							tempAccessAdmin = false;
						}
						
						if (tempAccessUninstall && (currPkg.equals(PKG_UNINSTALL))) {// && (currCls.equals(PKG_UNINSTALL_CLASS) || currCls.equals(PKG_UNINSTALL_PROCESS_CLASS)))) {
							continue;
						} else {
							tempAccessUninstall = false;
						}
						
						if (tempAppAcccess.equals(currPkg)) {
							continue;
						} else {
							tempAppAcccess = "";
						}
					} else {
						if (tempAppAcccess.equals(currPkg)) {
							continue;
						} else {
							if (PKG_SETTINGS.equals(tempAppAcccess)) {
								
								if ( ((currPkg.equals(PKG_SETTINGS) && currCls.equals(PKG_SETTINGS_ADMIN_CLASS)) || currPkg.equals(PKG_UNINSTALL))) {
									// nop
								} else
									tempAppAcccess = "";
								
							} else
								tempAppAcccess = "";
						}
					}
					
					if (currPkg.equals(pinPkg) && currCls.equals(pinClass) && (mBound && binder.getService().isPinWindowVisible())) {
						continue;
					}
					
					if (!isSettingsBlocked && blockedApps.contains(new BasicNameValuePair(currPkg, currCls))) {
						AccessType acc = null;
						
						if (currPkg.equals(PKG_SETTINGS) && currCls.equals(PKG_SETTINGS_ADMIN_CLASS))
							acc = AccessType.ACC_ADMIN;
						else if (currPkg.equals(PKG_UNINSTALL) && currCls.equals(PKG_UNINSTALL_CLASS))
							acc = AccessType.ACC_UNINSTALL;
						
						if (acc != null) {
							pinPkg = currPkg.toString();
							pinClass = currCls.toString();
							PinService.showActivity(GuardService.this, new PinActivityConfig(acc));
							
						}
					} else {
						
						if (blockedAppList.contains(currPkg)) {
							pinPkg = currPkg.toString();
							pinClass = currCls.toString();
							PinService.showActivity(GuardService.this, new PinActivityConfig(currPkg));
						}
						
					}
				}
				
				
				isWorking = Boolean.FALSE;
			}
		}).start();
		
		return START_STICKY;
	}
	
	public void clearPinPkgAndClass() {
		pinPkg = "";
		pinClass = "";
	}
	
	public Boolean isServiceWorking() {
		return isWorking;
	}
	
	@Override
	public void onDestroy() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		CommonUtils.stopServiceSafe(this, new Intent(this, PinService.class));
		
		isWorking = Boolean.FALSE;
		breakLoop = true;
		if (screenRecevier != null)
			unregisterReceiver(screenRecevier);
		if (mApprovedAppsChanged != null)
			unregisterReceiver(mApprovedAppsChanged);
		
		super.onDestroy();
	}
	
	public void setTempAccessUninstall(boolean acc) {
		tempAccessUninstall = acc;
		if (Console.isEnabled())
			Console.logi("set grant access to UNISTALL!");
	}
	
	public void setTempAccessAdmin(boolean acc) {
		tempAccessAdmin = acc;
		if (Console.isEnabled())
			Console.logi("set grant access to ADMIN ");
	}
	
	//public void setTempAccessSettings(boolean acc) {
	//	tempAccessSettings = acc;
	//	if (Console.isEnabled())
	//		Console.logi("set grant access to system settings ");
	//}
	
	public void addTempAccess(String pkg) {
		tempAppAcccess = pkg;
		if (Console.isEnabled())
			Console.logi("set grant access !!!!!! " + pkg);
	}

}
