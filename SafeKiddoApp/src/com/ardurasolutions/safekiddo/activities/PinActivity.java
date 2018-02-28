package com.ardurasolutions.safekiddo.activities;

import android.content.Context;

import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.proto.PinActivityProto;


public class PinActivity extends PinActivityProto {
	
	public static void showActivity(Context ctx, PinActivityConfig config) {
		PinActivityProto.showActivityProto(ctx, config, PinActivity.class);
	}

}
