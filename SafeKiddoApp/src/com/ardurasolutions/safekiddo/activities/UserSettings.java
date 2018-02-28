package com.ardurasolutions.safekiddo.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.helpers.UserHelper.OnBeforeKillApp;
import com.ardurasolutions.safekiddo.proto.DialogProgress;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.hv.console.Console;
import com.hv.styleddialogs.TextDialog;
import com.hv.styleddialogs.proto.BasicDialog.OnDialogReady;

public class UserSettings extends ActionBarActivity {
	
	private TextView childAccountName;
	private CheckBox useLauncher;
	private boolean finishOnPause = true;
	private static UserSettings sUserSettings;
	private Thread loagoutThread = null;
	
	public static UserSettings get() {
		return sUserSettings;
	}
	
	public void selfFinish() {
		Console.logw("self finish");
		sUserSettings = null;
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//overridePendingTransition(0, 0);
		
		if (Config.getInstance(this).load(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, false)) {
			startActivity(new Intent(this, ParentAction.class));
			finish();
			return;
		}
		sUserSettings = this;
		setContentView(R.layout.activity_user_settings);
		
		childAccountName = (TextView) findViewById(R.id.childAccountName);
		useLauncher = (CheckBox) findViewById(R.id.useLauncher);
		
		useLauncher.setChecked(CommonUtils.isMyLauncherDefault(this));
		useLauncher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				/*final PackageManager pm = getPackageManager();
				final ComponentName cn1 = new ComponentName(getPackageName(), Constants.LAUNCHER_ALIAS_1);
				final ComponentName cn2 = new ComponentName(getPackageName(), Constants.LAUNCHER_ALIAS_2);
				
				if (isChecked) {
					boolean cn1enabled = pm.getComponentEnabledSetting(cn1) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
					boolean cn2enabled = pm.getComponentEnabledSetting(cn2) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
					
					if (!cn1enabled && !cn2enabled) {
						pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
						pm.setComponentEnabledSetting(cn2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					} else if (!cn1enabled && cn2enabled) {
						pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						pm.setComponentEnabledSetting(cn2, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					} else if (cn1enabled && !cn2enabled) {
						pm.setComponentEnabledSetting(cn2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					} else {
						pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						pm.setComponentEnabledSetting(cn2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					}
				} else {
					pm.setComponentEnabledSetting(cn1, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					pm.setComponentEnabledSetting(cn2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				
				CommonUtils.startHomeChooser(UserSettings.this, true);*/
			}
			
		});
		
		findViewById(R.id.selectChildAccount).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CommonUtils.isOnline(UserSettings.this)) {
					startActivityForResult(new Intent(UserSettings.this, UserSettingsChildProfile.class), 1);
					finishOnPause = false;
				} else {
					Toaster.showMsg(UserSettings.this, R.string.toast_child_profile_not_online);
				}
			}
		});
		
		findViewById(R.id.itemLogout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextDialog d = new TextDialog();
				d.setTitle(getResources().getString(R.string.dialog_logout_title));
				d.setText(getResources().getString(R.string.dialog_logout_msg));
				d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
						final DialogProgress dp = new DialogProgress();
						dp.setOnDialogReady(new OnDialogReady() {
							@Override
							public void onDialogReady(Dialog d) {
								d.setCancelable(true);
								d.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										if (!loagoutThread.isInterrupted()) {
											loagoutThread.interrupt();
											dialog.dismiss();
											
											UserHelper.cleanupSafekiddo(UserSettings.this, new OnBeforeKillApp() {
												@Override
												public void onBeforeKillApp() {
													finish();
												}
											});
										}
									}
								});
							}
						});
						dp.show(getSupportFragmentManager(), "progress");
						
						loagoutThread = new Thread() {
							@Override
							public void run() {
								//Network.post(Constants.getLogoutUrl(), new ArrayList<NameValuePair>(), UserSettings.this, true, true, null);
								BasicRequest br = new BasicRequest(Constants.getLogoutUrl());
								br.setSessionHandler(new AppSessionHandler(UserSettings.this));
								br.getConnectionParams().usePOST();
								br.executeSafe();
								
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										UserHelper.cleanupSafekiddo(UserSettings.this, new OnBeforeKillApp() {
											@Override
											public void onBeforeKillApp() {
												finish();
											}
										});
									}
								});
								
								dp.dismiss();
								
							}
						};
						loagoutThread.start();
					}
				});
				d.setNegativeButton(R.string.label_cancel, null);
				d.show(getSupportFragmentManager(), "d0");
			}
		});
		
		findViewById(R.id.appsMang).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(UserSettings.this, UserSettingsAppsManage.class), 1);
				finishOnPause = false;
			}
		});
		
		findViewById(R.id.itemTerms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.showActivity(UserSettings.this, Constants.URL_TERMS_OF_SERVICE, getResources().getString(R.string.label_terms_of_service));
				finishOnPause = false;
			}
		});
		
		findViewById(R.id.itemPolicy).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.showActivity(UserSettings.this, Constants.URL_PRIVACY_POLICY, getResources().getString(R.string.label_privacy_policy));
				finishOnPause = false;
			}
		});
		
		findViewById(R.id.itemPolicyCookies).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.showActivity(UserSettings.this, Constants.URL_COOKIES_POLICY, getResources().getString(R.string.label_cookies_policy));
				finishOnPause = false;
			}
		});
		
		findViewById(R.id.itemLicenses).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebActivity.showActivity(UserSettings.this, "file:///android_asset/html/licenses.html", getResources().getString(R.string.label_licenses));
				finishOnPause = false;
			}
		});
		
		String versionName = "1.0";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) { }
		
		((TextView) findViewById(R.id.itemVersion)).setText(getResources().getString(R.string.label_version, versionName));
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg1 != Activity.RESULT_OK && arg0 == 1) {
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (finishOnPause)
			finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		finishOnPause = true;
		ChildElement child = UserHelper.getCurrentChildProfile(this);
		if (child != null)
			childAccountName.setText(child.getName());
		else
			childAccountName.setText(R.string.label_nothing_selected);
	}

}
