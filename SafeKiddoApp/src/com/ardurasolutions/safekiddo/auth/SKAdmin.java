package com.ardurasolutions.safekiddo.auth;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.ardurasolutions.safekiddo.R;

public class SKAdmin extends DeviceAdminReceiver {
	
	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
	}
	
	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
	}
	
	 @Override
	public CharSequence onDisableRequested(Context arg0, Intent arg1) {
		return arg0.getResources().getString(R.string.label_da_disabling);
	}
	
}
