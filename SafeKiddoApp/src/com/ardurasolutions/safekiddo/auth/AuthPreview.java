package com.ardurasolutions.safekiddo.auth;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.activities.DummyUserSettings;
import com.ardurasolutions.safekiddo.activities.FirstRunActivity;
import com.ardurasolutions.safekiddo.auth.FrameChildProfile.OnChildListRefresh;
import com.ardurasolutions.safekiddo.auth.FrameChildProfile.OnProfileSelect;
import com.ardurasolutions.safekiddo.auth.FrameDeviceAdmin.OnDeviceAdminInstall;
import com.ardurasolutions.safekiddo.auth.FrameFinalize.OnFinalizeSelect;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.browser.BrowserMainActivity;
import com.ardurasolutions.safekiddo.helpers.AppsHelper;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.GuardHelper;
import com.ardurasolutions.safekiddo.helpers.HeartBeatHelper;
import com.ardurasolutions.safekiddo.helpers.ProxySystem;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.launcher.Desktop;
import com.ardurasolutions.safekiddo.proto.view.CheckableLinearLayout;
import com.ardurasolutions.safekiddo.receviers.BootRecevier;
import com.ardurasolutions.safekiddo.receviers.PackageInstall;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.hv.console.Console;

public class AuthPreview extends FragmentActivity implements 
		//OnAuthSuccess, 
		OnProfileSelect/*, OnMoreSecureSelect*/, 
		OnFinalizeSelect, 
		OnDeviceAdminInstall,
		OnChildListRefresh {
	
	public static final String KEY_CHILDS = "childs";
	
	private CheckableLinearLayout tab1, tab2, tab3;//, tab4; //, tab5;
	//private int selectedChildProfile = -1;
	private boolean isMoreSecureChecked = true;
	private boolean useLauncherChecked = true;
	private ArrayList<ChildElement> childList = new ArrayList<ChildElement>();
	private ChildElement mChildElement;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		childList.clear();
		
		if (getIntent() != null && getIntent().hasExtra(KEY_CHILDS)) {
			ArrayList<String> childs = getIntent().getStringArrayListExtra(KEY_CHILDS);
			if (childs != null && childs.size() > 0) {
				for(String c : childs) {
					childList.add(ChildElement.fromJSON(c));
				}
			}
		}
		
		LocalSQL.getInstance(this); // INFO - init for get default apps to unlock
		
		loadContentFirst(new FrameChildProfile().setChilds(childList).setProfileId(mChildElement != null ? mChildElement.getId().intValue() : -1));
		
		tab1 = (CheckableLinearLayout) findViewById(R.id.tab1);
		tab2 = (CheckableLinearLayout) findViewById(R.id.tab2);
		tab3 = (CheckableLinearLayout) findViewById(R.id.tab3);
		//tab4 = (CheckableLinearLayout) findViewById(R.id.tab4);
		//tab5 = (CheckableLinearLayout) findViewById(R.id.tab5);
		
		tab1.setChecked(true);
		tab2.setChecked(false);
		tab3.setChecked(false);
		//tab4.setChecked(false);
		//tab5.setChecked(false);
	}
	
	public void loadContentFirst(Fragment frag) {
		loadContent(frag, false, true);
	}
	
	public void loadContent(Fragment frag, boolean isBackAnim) {
		loadContent(frag, isBackAnim, false);
	}
	
	public void loadContent(Fragment frag, boolean isBackAnim, boolean noInAnim) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(
			noInAnim ? 0 : (isBackAnim ? android.R.anim.slide_in_left : R.anim.slide_in_right), 
			isBackAnim ? android.R.anim.slide_out_right : R.anim.slide_out_left
		);
		ft.replace(R.id.content, frag);
		ft.commit();
	}
	
	// 1
//	@Override
//	public void onAuthSuccess(ArrayList<ChildElement> childs, String userName) {
//		tab1.setChecked(false);
//		tab2.setChecked(true);
//		tab3.setChecked(false);
//		tab4.setChecked(false);
//		//tab5.setChecked(false);
//		childList = childs;
//		UserHelper.saveUserName(this, userName);
//		loadContent(new FrameChildProfile().setChilds(childList).setProfileId(mChildElement != null ? mChildElement.getId().intValue() : -1), false);
//	}
	
	// 2
	@Override
	public void onProfileSelect(ChildElement profileId) {
		//selectedChildProfile = profileId.getId().intValue();
		mChildElement = profileId;
		tab1.setChecked(false);
		tab2.setChecked(true);
		tab3.setChecked(false);
		//tab4.setChecked(false);
		//tab5.setChecked(false);
		//loadContent(new FrameMoreSecure().setMoreSecure(isMoreSecureChecked).setUseLauncher(useLauncherChecked), false);
		loadContent(new FrameDeviceAdmin(), false);
		UserHelper.setUserBlockApps(this, isMoreSecureChecked);
	}
	
	/*
	// 3
	@Override
	public void onMoreSecureSelect(boolean isChecked, boolean useLauncher) {
		isMoreSecureChecked = isChecked;
		useLauncherChecked = useLauncher;
		tab1.setChecked(false);
		tab2.setChecked(false);
		tab3.setChecked(false);
		tab4.setChecked(true);
		tab5.setChecked(false);
		loadContent(new FrameDeviceAdmin(), false);
		UserHelper.setUserBlockApps(this, isMoreSecureChecked);
	}
	
	// 2 <- 3
	@Override
	public void onMoreSecureSelectBack(boolean isChecked, boolean useLauncher) {
		isMoreSecureChecked = isChecked;
		useLauncherChecked = useLauncher;
		tab1.setChecked(false);
		tab2.setChecked(true);
		tab3.setChecked(false);
		tab4.setChecked(false);
		tab5.setChecked(false);
		loadContent(new FrameChildProfile().setChilds(childList).setProfileId(selectedChildProfile), true);
	}
	*/
	
	// 4
	@Override
	public void onDeviceAdminInstall() {
		tab1.setChecked(false);
		tab2.setChecked(false);
		tab3.setChecked(true);
		//tab4.setChecked(true);
		//tab5.setChecked(true);
		loadContent(new FrameFinalize().setInstallLauncher(useLauncherChecked), false);
	}
	
	// 3 <- 4
	@Override
	public void onDeviceAdminInstallBack() {
		tab1.setChecked(true);
		tab2.setChecked(false);
		tab3.setChecked(false);
		//tab4.setChecked(false);
		loadContent(new FrameChildProfile().setChilds(childList).setProfileId(mChildElement != null ? mChildElement.getId().intValue() : -1), true);
		//tab5.setChecked(false);
		//loadContent(new FrameMoreSecure().setMoreSecure(isMoreSecureChecked).setUseLauncher(useLauncherChecked), true);
	}
	
	// 4 <- 5
	@Override
	public void onFinalizeSelectBack() {
		tab1.setChecked(false);
		tab2.setChecked(true);
		tab3.setChecked(false);
		//tab4.setChecked(false);
		//tab5.setChecked(false);
		loadContent(new FrameDeviceAdmin(), true);
	}

	// 5
	@Override
	public void onFinalizeSelect() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				final PackageManager pm = getPackageManager();
				
				LocalSQL.getInstance(AuthPreview.this).getTable(AllAppsTable.class).blockApps(AppsHelper.getInstalledBrowsers(AuthPreview.this));
				
				UserHelper.saveCurrentChildProfile(AuthPreview.this, mChildElement);
				GuardHelper.setGuardEnabled(AuthPreview.this, isMoreSecureChecked);
				
				GuardHelper.checkGuardService(AuthPreview.this);
				ProxySystem.setupWiFiProxy(AuthPreview.this);
				
				
				
				//CommonUtils.checkAndSetupProxyServer(this);
				//CommonUtils.checkWiFiProxyChecks(this, true);
				
				// enable user settings
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), DummyUserSettings.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				// enable browser
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), BrowserMainActivity.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				// disable login first run activity
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), FirstRunActivity.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				// enable package state change recevier
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), PackageInstall.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				// desktop
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), Desktop.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				// BootRecevier
				pm.setComponentEnabledSetting(
					new ComponentName(getPackageName(), BootRecevier.class.getName()), 
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 
					PackageManager.DONT_KILL_APP
				);
				
				//WallpaperHelper.setDefaultWallpaper(AuthPreview.this);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (useLauncherChecked) {
							boolean isMyDefLaucher = CommonUtils.isMyLauncherDefault(AuthPreview.this);
							if (isMyDefLaucher) {
								
								finish();
								
							} else {
								
								/*
								 * poprawka na błąd wystaępujący gdy aplikacja jest zainstalowana na 2 koncie na urządzeniu
								 */
								try {
									pm.clearPackagePreferredActivities(getPackageName());
								} catch (SecurityException e) {
									if (Console.isEnabled())
										Console.loge("AuthPreview :: onFinalizeSelect", e);
								}
								CommonUtils.startHomeChooser(getApplicationContext(), false);
								
								finish();
							}
						} else {
							
							finish();
							
						}
						
						HeartBeatHelper.initAlarm(AuthPreview.this);
					}
				});
			}
		}).start();
	}

	@Override
	public void onChildListRefresh(ArrayList<ChildElement> childList) {
		this.childList = childList;
	}

}
