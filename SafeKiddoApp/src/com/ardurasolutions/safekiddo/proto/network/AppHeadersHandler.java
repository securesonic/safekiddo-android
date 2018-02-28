package com.ardurasolutions.safekiddo.proto.network;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.hv.console.Console;

public class AppHeadersHandler implements HeadersHandler {
	
	private Context mContext;
	
	public AppHeadersHandler(Context ctx) {
		mContext = ctx;
	}

	@Override
	public HashMap<String, String> getInHeaders(ConnectionParams cp) {
		
		String appVersion = "";
		try {
			appVersion = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			if (Console.isEnabled())
				Console.loge("AppHeadersHandler :: getInHeaders[NameNotFoundException]", e);
		}
		
		// User-Agent: SafeKiddo Mobile (v0.9.60; Android 4.4.4; http://www.safekiddo.com)
		
		HashMap<String, String> res = new HashMap<String, String>();
		res.put(BasicRequest.HEADER_USER_AGENT,
			String.format(
				"SafeKiddo Mobile (v%s; Android %s; http://www.safekiddo.com)", 
				appVersion, android.os.Build.VERSION.RELEASE
			)
		);
		res.put(BasicRequest.HEADER_ACCEPT_ENCODING, BasicRequest.HEADER_VALUE_ACCEPT_ENCODING);
		return res;
	}

	@Override
	public void getOutHeaders(Map<String, String> h) {
		// NOOP
	}

}
