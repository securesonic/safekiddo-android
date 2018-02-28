package com.ardurasolutions.safekiddo.browser.proto;

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.hv.console.Console;

public class WebInterfaceForceSSLError {
	
	public static interface OnForceLoadClick {
		public void onForceLoadClick();
	}
	
	private WebViewHv web;
	private Activity activity;
	private String url;
	private OnForceLoadClick mOnForceLoadClick;
	
	public WebInterfaceForceSSLError(WebViewHv wv) {
		web = wv;
	}
	
	public WebInterfaceForceSSLError setActivity(Activity a) {
		activity = a;
		return this;
	}
	
	public WebInterfaceForceSSLError setUrl(String u) {
		url = u;
		return this;
	}
	
	@JavascriptInterface
	public void forceLoad(){
		Console.logd("FORCE LOAD SSL: " + url);
		if (activity != null && url != null) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					web.setForceLoadSSL(url);
					web.loadUrl(url);
					if (mOnForceLoadClick != null)
						mOnForceLoadClick.onForceLoadClick();
				}
			});
		}
	}

	public OnForceLoadClick getOnForceLoadClick() {
		return mOnForceLoadClick;
	}

	public void setOnForceLoadClick(OnForceLoadClick mOnForceLoadClick) {
		this.mOnForceLoadClick = mOnForceLoadClick;
	}

}
