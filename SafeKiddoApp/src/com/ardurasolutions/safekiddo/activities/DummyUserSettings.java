package com.ardurasolutions.safekiddo.activities;

import android.app.Activity;
import android.os.Bundle;

import com.ardurasolutions.safekiddo.proto.PinActivityConfig;

public class DummyUserSettings extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PinActivity.showActivity(this, new PinActivityConfig(UserSettings.class));
		//PinActivity.showActivityRes(this, UserSettings.class);
	}

}
