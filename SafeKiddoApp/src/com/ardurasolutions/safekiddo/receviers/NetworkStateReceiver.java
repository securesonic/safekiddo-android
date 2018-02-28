package com.ardurasolutions.safekiddo.receviers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ardurasolutions.safekiddo.helpers.ProxySystem;
import com.ardurasolutions.safekiddo.helpers.UserHelper;

public class NetworkStateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent.getExtras() != null) {
			if (UserHelper.isUserLogedIn(context)) {
				ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = cm.getActiveNetworkInfo();
				
				if (ni != null) {
					
					if (ni.getState().equals(NetworkInfo.State.DISCONNECTED) || ni.getState().equals(NetworkInfo.State.SUSPENDED)) {
						
					} else if (ni.getState().equals(NetworkInfo.State.CONNECTED)){
						
						ProxySystem.setupWiFiProxy(context);
						
					}
				}
			}
		}
	}

}
