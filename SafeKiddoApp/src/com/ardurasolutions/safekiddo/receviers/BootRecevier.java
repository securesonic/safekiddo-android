package com.ardurasolutions.safekiddo.receviers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ardurasolutions.safekiddo.helpers.HeartBeatHelper;
import com.ardurasolutions.safekiddo.helpers.UserHelper;

public class BootRecevier extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (UserHelper.isUserLogedIn(context)) {
			//CommonUtils.checkWiFiProxyChecks(context, true);
			//CommonUtils.checkAndSetupProxyServer(context);
			HeartBeatHelper.initAlarm(context);
		}
	}

}
