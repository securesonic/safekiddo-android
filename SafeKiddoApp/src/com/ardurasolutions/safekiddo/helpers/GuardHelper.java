package com.ardurasolutions.safekiddo.helpers;

import android.content.Context;
import android.content.Intent;

import com.ardurasolutions.safekiddo.services.GuardService;

public class GuardHelper {
	
	public static void checkGuardService(Context ctx) {
		if (Config.getInstance(ctx).load(Config.KeyNames.USER_BLOCK_APPS, false)) {
			if (!isGuardServiceWorking(ctx)) {
				ctx.startService(new Intent(ctx, GuardService.class));
			}
		} else {
			CommonUtils.stopServiceSafe(ctx, new Intent(ctx, GuardService.class));
		}
	}
	
	private static boolean isGuardServiceWorking(Context ctx) {
		return CommonUtils.isServiceRunning(ctx, GuardService.class.getName());
	}
	
	public static void setGuardEnabled(Context ctx, boolean isEnabled) {
		Config
			.getInstance(ctx)
			.save(Config.KeyNames.USER_BLOCK_APPS, isEnabled);
	}
	
}
