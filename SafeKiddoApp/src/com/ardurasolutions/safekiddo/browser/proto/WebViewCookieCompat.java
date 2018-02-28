package com.ardurasolutions.safekiddo.browser.proto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

/**
 * W api 21+ CookieSyncManager jest wyłączony i nalezy inaczej obsługiwać 
 * cookie (ich synchronizację)<br>
 * Dla starszych systemów wszystko pozostaje jak było
 * @author Hivedi
 */
@SuppressWarnings("deprecation")
public class WebViewCookieCompat {
	
	private static WebViewCookieCompat sInstance = null;
	
	public static synchronized WebViewCookieCompat getInstance() {
		if (sInstance == null) {
			sInstance = new WebViewCookieCompat();
		}
		return sInstance;
	}
	
	public WebViewCookieCompat() {
		
	}
	
	public void createInstance(Context webViewCtx) {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			// no longer create instance
		} else {
			CookieSyncManager.createInstance(webViewCtx);
		}
	}
	
	@SuppressLint("NewApi")
	public WebViewCookieCompat sync() {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			CookieManager.getInstance().flush();
		} else {
			CookieSyncManager.getInstance().sync();
		}
		return this;
	}
	
	public WebViewCookieCompat setCookie(String url, String cookieString) {
		CookieManager.getInstance().setCookie(url, cookieString);
		return this;
	}
	
	public String getCookie(String url) {
		return CookieManager.getInstance().getCookie(url);
	}
	
	@SuppressLint("NewApi")
	public WebViewCookieCompat removeAllCookie() {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean> () {
				@Override
				public void onReceiveValue(Boolean value) {
					
				}
			});
		} else {
			CookieManager.getInstance().removeAllCookie();
		}
		return this;
	}

}
