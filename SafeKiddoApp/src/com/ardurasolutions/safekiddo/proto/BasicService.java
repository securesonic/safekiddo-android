package com.ardurasolutions.safekiddo.proto;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

public abstract class BasicService extends Service {
	
	/**
	 * używane do wybudzania usługi w systemahc KitKat
	 * @return
	 */
	public abstract int getServiceUniqueId();
	
	@SuppressLint("NewApi")
	@Override
	public void onTaskRemoved(Intent rootIntent){
		// INFO fix for kitkat killing service
		if (Build.VERSION.SDK_INT >= 19) {
			Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
			restartServiceIntent.setPackage(getPackageName());
			
			PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), getServiceUniqueId(), restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
			AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);
			
			super.onTaskRemoved(rootIntent);
		} else {
			super.onTaskRemoved(rootIntent);
		}
	 }

}
