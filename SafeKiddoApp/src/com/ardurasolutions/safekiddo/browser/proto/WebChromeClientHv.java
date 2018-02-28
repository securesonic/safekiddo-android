package com.ardurasolutions.safekiddo.browser.proto;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.hv.console.Console;
import com.hv.styleddialogs.TextDialog;

public class WebChromeClientHv extends WebChromeClient {
	
	private FrameLayout.LayoutParams LayoutParameters = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
	private View mCustomView;
	private CustomViewCallback mCustomViewCallback;
	private RelativeLayout mainView;
	private FrameLayout fullscreenCustomContent;
	private boolean isInFullscreenMode = false;
	//private Context mContext;
	
	public WebChromeClientHv(RelativeLayout mainView, FrameLayout fl) {
		this.mainView = mainView;
		this.fullscreenCustomContent = fl;
	}
	
	public boolean isCustomViewVisible() {
		return mCustomView != null;
	}
	
	@Override public void onProgressChanged(WebView view, int newProgress) { }
	@Override public void onReceivedTitle(WebView view, String title) { }
	
	@Override
	public boolean onConsoleMessage(ConsoleMessage cm) {
		if (Console.isEnabled())
			Console.logi("CONSOLE[" + cm.messageLevel() + "]: " + cm.message());
		return false;
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		if (mCustomView != null) {
			callback.onCustomViewHidden();
			return;
		}
		setInFullscreenMode(true);
		mainView.setVisibility(View.GONE);
		fullscreenCustomContent.setBackgroundResource(android.R.color.black);
		view.setLayoutParams(LayoutParameters);
		fullscreenCustomContent.addView(view);
		mCustomView = view;
		mCustomViewCallback = callback;
		fullscreenCustomContent.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onHideCustomView () {
		if (mCustomView == null) {
			return;
		} else {
			setInFullscreenMode(false);
			mCustomView.setVisibility(View.GONE);
			fullscreenCustomContent.removeView(mCustomView);
			mCustomView = null;
			fullscreenCustomContent.setVisibility(View.GONE);
			//mCustomViewCallback.onCustomViewHidden();
			
			if (android.os.Build.VERSION.SDK_INT < 19) {
				try {
					mCustomViewCallback.onCustomViewHidden();
				} catch (Throwable ignored) {

				}
			}
			
			mainView.setVisibility(View.VISIBLE);
		}
	}
	
	public Context getContext() {
		return null;
	}
	
	@Override
	public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
		if (getContext() == null) {
			if (Console.isEnabled())
				Console.loge("WebChromeClientHv::onGeolocationPermissionsShowPrompt = NO CONTEXT");
			return;
		}
		
		final Config prefs = Config.getInstance(getContext());
		
		switch(prefs.load(Config.KeyNames.USER_GEOLOCATION, 1)) {
			default:
			case 1:
				TextDialog d = new TextDialog();
				d.setTitle(getContext().getResources().getString(R.string.label_browser_geolocation_dialog_title));
				d.setText(getContext().getResources().getString(R.string.label_browser_geolocation_dialog_msg));
				d.setPositiveButton(R.string.label_browser_geolocation_grant, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						callback.invoke(origin, true, false);
						prefs.save(Config.KeyNames.USER_GEOLOCATION, 2);
					}
				});
				d.setNegativeButton(R.string.label_browser_geolocation_revoke, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						callback.invoke(origin, false, false);
						prefs.save(Config.KeyNames.USER_GEOLOCATION, 3);
					}
				});
				d.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "d0");
			break;
			case 2:
				callback.invoke(origin, true, false);
			break;
			case 3:
				callback.invoke(origin, false, false);
			break;
		}
	}

	public boolean isInFullscreenMode() {
		return isInFullscreenMode;
	}

	public void setInFullscreenMode(boolean isInFullscreenMode) {
		this.isInFullscreenMode = isInFullscreenMode;
	}

}
