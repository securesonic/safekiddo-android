package com.ardurasolutions.safekiddo.browser;

import android.content.Context;

import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.proto.PinActivityProto;


public class BrowserPinActivity extends PinActivityProto {
	
	public static void showActivity(Context ctx, PinActivityConfig config) {
		PinActivityProto.showActivityProto(ctx, config, BrowserPinActivity.class);
	}
	
}
