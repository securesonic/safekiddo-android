package com.ardurasolutions.safekiddo.browser;

import java.io.File;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.activities.UserSettings;
import com.ardurasolutions.safekiddo.activities.WebActivity;
import com.ardurasolutions.safekiddo.browser.proto.BrowserIdentify;
import com.ardurasolutions.safekiddo.browser.proto.WebViewCookieCompat;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.BrowserHistoryTable;
import com.hv.console.Console;
import com.hv.styleddialogs.SingleChoiceDialog;
import com.hv.styleddialogs.TextDialog;
import com.hv.styleddialogs.proto.OnDialogCallback;

public class BrowserSettings extends ActionBarActivity implements OnDialogCallback {
	
	public static final String KEY_WEBVIEW_CACHE_CLEAR = "wb_cache_clear";
	public static final String KEY_WEBVIEW_HISTORY_CLEAR = "wb_history_clear";
	public static final String KEY_WEBVIEW_USER_AGNET_CHANGE = "wb_user_agent_change";
	
	private final int DIALOG_GEO = 1;
	private final int DIALOG_UA = 2;
	private final int DIALOG_DNT = 3;
	
	private Config prefs;
	private boolean webViewCacheCleared = false;
	private boolean webViewHistoryCleared = false;
	private BrowserIdentify mBrowserIdentifyChanged = null;
	private TextView geoValue, uaValue, dntValue;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser_settings);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
		
		prefs = Config.getInstance(this);
		
		geoValue = (TextView) findViewById(R.id.geoValue);
		uaValue = (TextView) findViewById(R.id.uaValue);
		dntValue = (TextView) findViewById(R.id.dntValue);
		
		dntValue.setText(prefs.load(Config.KeyNames.USER_DNT, false) ? R.string.label_enabled : R.string.label_disabled);
		
		String geoValueString = getResources().getString(R.string.label_browser_geolocation_always_ask);
		switch(prefs.load(Config.KeyNames.USER_GEOLOCATION, 1)) {
			default:
			case 1: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_ask); break;
			case 2: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_grant); break;
			case 3: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_revoke); break;
		}
		geoValue.setText(geoValueString);
		
		String uaValueString = getResources().getString(R.string.label_browser_identyfication_android);
		BrowserIdentify mBrowserIdentify = BrowserIdentify.fromInt(prefs.load(Config.KeyNames.USER_BROWSER_IDENTYFICATION, BrowserIdentify.IDENTIFY_ANDROID.getValue()));
		switch(mBrowserIdentify) {
			default:
			case IDENTIFY_ANDROID:
				uaValueString = getResources().getString(R.string.label_browser_identyfication_android);
			break;
			case IDENTIFY_DESKTOP:
				uaValueString = getResources().getString(R.string.label_browser_identyfication_desktop);
			break;
		}
		uaValue.setText(uaValueString);
	}
	
	public void handleClearCookies(View v) {
		TextDialog d = new TextDialog();
		d.setTitle(getResources().getString(R.string.dialog_del_cookies_title));
		d.setText(getResources().getString(R.string.dialog_del_cookies_msg));
		d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new Thread(new Runnable() {
					@Override
					public void run() {
						//CookieManager.getInstance().removeAllCookie();
						WebViewCookieCompat.getInstance().removeAllCookie();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toaster.showMsg(BrowserSettings.this, R.string.toast_cookies_cleared);
							}
						});
					}
				}).start();
			}
		});
		d.setNegativeButton(R.string.label_cancel, null);
		d.show(getSupportFragmentManager(), "d0");
	}
	
	public void handleClearHistory(View v) {
		TextDialog d = new TextDialog();
		d.setTitle(getResources().getString(R.string.dialog_del_history_title));
		d.setText(getResources().getString(R.string.dialog_del_history_msg));
		d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				new Thread(new Runnable() {
					@Override
					public void run() {
						webViewHistoryCleared = true;
						LocalSQL.getInstance(BrowserSettings.this)
							.getTable(BrowserHistoryTable.class)
							.deleteAll();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toaster.showMsg(BrowserSettings.this, R.string.toast_history_cleared);
							}
						});
					}
				}).start();
			}
		});
		d.setNegativeButton(R.string.label_cancel, null);
		d.show(getSupportFragmentManager(), "d1");
	}
	
	public void handleClearCache(View v) {
		TextDialog d = new TextDialog();
		d.setTitle(getResources().getString(R.string.dialog_clear_cache_title));
		d.setText(getResources().getString(R.string.dialog_clear_cache_msg));
		d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				webViewCacheCleared = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							// keep cache folder exists, delete subfolders
							File dir = new File(getCacheDir().getParent(), "/app_webview");
							if (!dir.exists())
								dir = getCacheDir();
							if (dir != null && dir.isDirectory()) {
								String[] children = dir.list();
								for(int i=0; i<children.length; i++) {
									deleteDir(new File(dir, children[i]));
								}
							}
						} catch (Exception e) {}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toaster.showMsg(BrowserSettings.this, R.string.toast_cache_cleared);
							}
						});
					}
					
				}).start();
			}
		});
		d.setNegativeButton(R.string.label_cancel, null);
		d.show(getSupportFragmentManager(), "d1x");
	}
	
	public void handleGeo(View v) {
		int geo = prefs.load(Config.KeyNames.USER_GEOLOCATION, 1);
		
		MatrixCursor c = new MatrixCursor(new String[]{"_id", "Name"});
		c.addRow(new String[]{"1", getResources().getString(R.string.label_browser_geolocation_always_ask)});
		c.addRow(new String[]{"2", getResources().getString(R.string.label_browser_geolocation_always_grant)});
		c.addRow(new String[]{"3", getResources().getString(R.string.label_browser_geolocation_always_revoke)});
		
		SingleChoiceDialog d3 = new SingleChoiceDialog();
		d3.setDialogId(DIALOG_GEO);
		d3.setItemConfig(R.layout.item_dialog_single_choice, 0);
		d3.setTitle(getResources().getString(R.string.label_browser_geolocation));
		d3.setSingleChoiceItems(this, c, geo - 1, "Name", null);
		d3.setPositiveButton(R.string.label_ok);
		d3.show(getSupportFragmentManager(), "d3");
	}
	
	public void handleTerms(View v) {
		WebActivity.showActivity(this, Constants.URL_TERMS_OF_SERVICE, getResources().getString(R.string.label_terms_of_service));
	}
	
	public void handlePrivacy(View v) {
		WebActivity.showActivity(this, Constants.URL_PRIVACY_POLICY, getResources().getString(R.string.label_privacy_policy));
	}
	
	public void handleDnt(View v) {
		MatrixCursor c = new MatrixCursor(new String[]{"_id", "Name"});
		c.addRow(new String[]{"1", getResources().getString(R.string.label_enabled)});
		c.addRow(new String[]{"2", getResources().getString(R.string.label_disabled)});
		
		SingleChoiceDialog d3 = new SingleChoiceDialog();
		d3.setItemConfig(R.layout.item_dialog_single_choice, 0);
		d3.setTitle(getResources().getString(R.string.label_dnt));
		d3.setSingleChoiceItems(this, c, prefs.load(Config.KeyNames.USER_DNT, false) ? 0 : 1, "Name", null);
		d3.setPositiveButton(R.string.label_ok);
		d3.show(getSupportFragmentManager(), "d5");
	}
	
	public void handleUA(View v) {
		BrowserIdentify mBrowserIdentify = BrowserIdentify.fromInt(prefs.load(Config.KeyNames.USER_BROWSER_IDENTYFICATION, BrowserIdentify.IDENTIFY_ANDROID.getValue()));
		SingleChoiceDialog d3 = new SingleChoiceDialog();
		d3.setDialogId(DIALOG_UA);
		d3.setItemConfig(R.layout.item_dialog_single_choice, 0);
		d3.setTitle(getResources().getString(R.string.label_browser_identyfication));
		d3.setSingleChoiceItems(this, BrowserIdentify.getCursorValues(this), mBrowserIdentify.getValue() - 1, "Name", null);
		d3.setPositiveButton(R.string.label_ok);
		d3.show(getSupportFragmentManager(), "d4");
	}
	
	public void handleParentMode(View v) {
		BrowserPinActivity.showActivity(this, new PinActivityConfig(UserSettings.class).setFinishOnBack(true));
	}
	
	private boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					if (Console.isEnabled())
						Console.loge("del error: " + dir.getAbsolutePath() + "/" + children[i]);
					return false;
				}
				if (Console.isEnabled())
					Console.logi("del : " + dir.getAbsolutePath() + "/" + children[i]);
			}
		}
		return dir.delete();
	}
	
	@Override
	public void onBackPressed() {
		Intent it = new Intent();
		it.putExtra(KEY_WEBVIEW_CACHE_CLEAR, webViewCacheCleared);
		if (webViewHistoryCleared)
			it.putExtra(KEY_WEBVIEW_HISTORY_CLEAR, webViewHistoryCleared);
		if (mBrowserIdentifyChanged != null)
			it.putExtra(KEY_WEBVIEW_USER_AGNET_CHANGE, mBrowserIdentifyChanged.getValue());
		setResult(Activity.RESULT_OK, it);
		super.onBackPressed();
	}


	@Override
	public void onPositive(DialogInterface dialog, int which, int dialogId) {
		
		dialog.dismiss();
		
		switch(dialogId) {
			case DIALOG_GEO:
				int selectedPosition = which;
				prefs.save(Config.KeyNames.USER_GEOLOCATION, selectedPosition + 1);
				String geoValueString = getResources().getString(R.string.label_browser_geolocation_always_ask);
				switch(selectedPosition + 1) {
					default:
					case 1: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_ask); break;
					case 2: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_grant); break;
					case 3: geoValueString = getResources().getString(R.string.label_browser_geolocation_always_revoke); break;
				}
				geoValue.setText(geoValueString);
			break;
			case DIALOG_UA:
				Cursor c = BrowserIdentify.getCursorValues(BrowserSettings.this);
				c.moveToPosition(which);
				prefs.save(Config.KeyNames.USER_BROWSER_IDENTYFICATION, c.getInt(0));
				uaValue.setText(c.getString(1));
				mBrowserIdentifyChanged = BrowserIdentify.fromInt(c.getInt(0));
			break;
			case DIALOG_DNT:
				prefs.save(Config.KeyNames.USER_DNT, which == 0);
				dntValue.setText(which == 0 ? R.string.label_enabled : R.string.label_disabled);
			break;
		} 
		
	}
	@Override
	public void onNegative(DialogInterface dialog, int which, int dialogId) { 
		dialog.dismiss();
	}
	@Override
	public void onMiddle(DialogInterface dialog, int which, int dialogId) { 
		dialog.dismiss();
	}

}
