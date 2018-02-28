package com.ardurasolutions.safekiddo.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.ardurasolutions.safekiddo.receviers.HeartBeatBroadcastReceiver;
import com.hv.console.Console;

public class HeartBeatHelper {
	
	public static final int DEFAULT_START_DELAY = 60 * 1000;
	
	public static void initAlarm(Context ctx) {
		initAlarm(ctx, false);
	}
	
	public static void initAlarm(Context ctx, boolean runNow) {
		if (Console.isEnabled())
			Console.logi("HB INIT: " + DateTime.now() + " + 60sec");
		
		AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pi = PendingIntent.getBroadcast(ctx, 1, new Intent(ctx, HeartBeatBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
		mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (runNow ? 1 : DEFAULT_START_DELAY), Constants.HEART_BEAT_INTERVAL, pi);
	}
	
	public static void clearAlarm(Context ctx) {
		AlarmManager mgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		mgr.cancel(PendingIntent.getBroadcast(ctx, 1, new Intent(ctx, HeartBeatBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));
		
		if (Console.isEnabled())
			Console.logi("HB CLEAR: " + DateTime.now());
	}
	
	public static void saveLastHBTime(Context ctx) {
		Config
			.getInstance(ctx)
			.save(Config.KeyNames.HB_LAST_RUN_TIME, System.currentTimeMillis());
	}
	
	public static void checkHB(Context ctx) {
		Long lastTime = Config
				.getInstance(ctx)
				.load(Config.KeyNames.HB_LAST_RUN_TIME, 0L);
		
		if (lastTime == 0 || (System.currentTimeMillis() - lastTime > Constants.HEART_BEAT_INTERVAL + (30 * 1000))) {
			if (Console.isEnabled())
				Console.logi("HB IS LATE, init...");
			initAlarm(ctx);
		}
	}
	
	public static void runNow(Context ctx) {
		initAlarm(ctx, true);
	}

}
