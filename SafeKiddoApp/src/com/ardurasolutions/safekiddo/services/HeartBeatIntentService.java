package com.ardurasolutions.safekiddo.services;

import android.app.IntentService;
import android.content.Intent;

import com.ardurasolutions.safekiddo.auth.proto.BasicUserOperation.OnError;
import com.ardurasolutions.safekiddo.auth.proto.HearBeatOperation;
import com.ardurasolutions.safekiddo.auth.proto.HearBeatOperation.OnHeartBeatResult;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.ardurasolutions.safekiddo.receviers.HeartBeatBroadcastReceiver;
import com.hv.console.Console;

public class HeartBeatIntentService extends IntentService {

	public HeartBeatIntentService() {
		super("HeartBeatIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		UserOperations uo = new UserOperations();
		uo.addOperation(
			new HearBeatOperation(this)
				.setOnHeartBeatResult(new OnHeartBeatResult() {
					@Override
					public void onHeartBeatSuccess() {
						if (Console.isEnabled())
							Console.logd("HB SUCCESS");
					}
					@Override
					public void onHeartBeatUninstall() {
						if (Console.isEnabled())
							Console.logd("HB UNINSTALL");
						
						BasicRequest br = new BasicRequest(Constants.getLogoutUrl());
						br.getConnectionParams().usePOST();
						br.setSessionHandler(new AppSessionHandler(HeartBeatIntentService.this));
						br.executeSafe();
						
						UserHelper.cleanupSafekiddo(HeartBeatIntentService.this, null);
						sendBroadcast(new Intent().setAction(Constants.BRODCAST_SAFEKIDDO_REMOVE));
					}
				})
				.setOnError(new OnError() {
					@Override
					public void onError(int errorCode, Object extraData) {
						if (Console.isEnabled())
							Console.loge("HB ERROR: " + errorCode);
					}
				})
		);
		uo.executeSync();
		
		HeartBeatBroadcastReceiver.completeWakefulIntent(intent);
	}
	
}
