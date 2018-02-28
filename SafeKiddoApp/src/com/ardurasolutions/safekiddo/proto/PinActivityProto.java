package com.ardurasolutions.safekiddo.proto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.proto.view.PinView;
import com.hv.console.Console;

/**
 * Not support for AccessType: ACC_ADMIN and ACC_UNINSTALL<br>
 * is no longer bindet with GuardService
 * @author Hivedi2
 *
 */
public abstract class PinActivityProto extends Activity {
	
	public static void showActivityProto(Context ctx, PinActivityConfig config, Class<?> cls) {
		Intent it = new Intent(ctx, cls);
		config.saveToIntent(it);
		ctx.startActivity(it);
	}
	
	protected PinActivityConfig mPinActivityConfig;
	protected PinView mPinView;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String userPIN = Config.getInstance(this).load(Config.KeyNames.USER_PIN, (String) null);
		if (userPIN == null) {
			if (Console.isEnabled())
				Console.loge("No user pin, finish() activity");
			finish();
			return;
		}
		
		mPinActivityConfig = PinActivityConfig.fromIntent(getIntent());
		
		if (Console.isEnabled())
			Console.logi("START PIN ACTIVITY: " + mPinActivityConfig);
		
		mPinView = new PinView(this);
		mPinView.setOnInputValidPin(new PinView.OnInputValidPin() {
			@Override
			public void onInputValidPin() {
				if (mPinActivityConfig.getAccessType() != null) {
					switch (mPinActivityConfig.getAccessType()) {
						default: 
							if (Console.isEnabled())
								Console.loge("NOT SUPPORT FOR AccessType=" + mPinActivityConfig.getAccessType());
						break;
						case ACC_FOR_RESULT: 
							setResult(Activity.RESULT_OK, new Intent());
						break;
						case ACC_START_ACTIVITY:
							try {
								if (mPinActivityConfig.getActivityClass() != null) {
									Intent it = new Intent(PinActivityProto.this, mPinActivityConfig.getActivityClass());
									startActivity(it);
								}
							} catch (Exception e) {
								if (Console.isEnabled())
									Console.loge("ACC_START_ACTIVITY", e);
							}
						break;
					}
				}
				
				finish();
			}
		});
		setContentView(mPinView);
	}

	
	@Override
	public void onBackPressed() {
		if (mPinActivityConfig.isFinishOnBack()) {
			super.onBackPressed();
		} else {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			i.addCategory(Intent.CATEGORY_DEFAULT);
			startActivity(i);
		}
	}

}
