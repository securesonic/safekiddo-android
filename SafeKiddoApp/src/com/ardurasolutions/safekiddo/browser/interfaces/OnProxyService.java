package com.ardurasolutions.safekiddo.browser.interfaces;

import com.ardurasolutions.safekiddo.services.ProxyService;

public interface OnProxyService {
	
	public static interface CheckCallback {
		public void onCheckCallback(boolean result);
	}
	
	public boolean isProxyServiceConnectd();
	public void isProxyServiceWorking(CheckCallback cb);
	public ProxyService getProxyService();
	public void startProxyService();
	public void onServerStarts();
}
