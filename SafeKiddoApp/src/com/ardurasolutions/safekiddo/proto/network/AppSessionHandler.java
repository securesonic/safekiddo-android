package com.ardurasolutions.safekiddo.proto.network;

import android.content.Context;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;

/**
 * 
 * In app session save and read handler
 * @author Hivedi
 *
 */
public class AppSessionHandler implements SessionHandler {
	
	private Context mContext;
	
	public AppSessionHandler(Context ctx) {
		mContext = ctx;
	}

	@Override
	public String getSessionCookieName() {
		return Constants.SESSION_COOKIE_NAME;
	}

	@Override
	public String getSessionCookieValue() {
		return 
			Config
				.getInstance(mContext)
				.load(Config.KeyNames.SESSION_ID, (String) null);
	}

	@Override
	public void onSaveSession(String value) {
		Config
			.getInstance(mContext)
			.save(Config.KeyNames.SESSION_ID, value);
	}

}
