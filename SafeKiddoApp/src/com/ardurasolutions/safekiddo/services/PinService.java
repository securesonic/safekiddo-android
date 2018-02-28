package com.ardurasolutions.safekiddo.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.proto.LocalServiceBinder;
import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.proto.view.PinView;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class PinService extends Service {
	
	/*
	public static void showActivityType(Context ctx, AccessType acc) {
		Intent it = new Intent(ctx, PinService.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.putExtra(PinActivityConfig.KEY_ACCESS_TYPE, acc.toString());
		ctx.startService(it);
	}
	
	public static void showActivityPkg(Context ctx, String pkg) {
		Intent it = new Intent(ctx, PinService.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.putExtra(PinActivityConfig.KEY_PKG_NAME, pkg);
		ctx.startService(it);
	}
	
	*/
	public static void showActivity(Context ctx, PinActivityConfig config) {
		Intent it = new Intent(ctx, PinService.class);
		config.saveToIntent(it);
		ctx.startService(it);
	}
	
	private final IBinder mBinder = new LocalServiceBinder<PinService>(this);
	private View layout = null;
	private WindowManager windowManager;
	private PinActivityConfig mPinActivityConfig;
	boolean mBound = false;
	private LocalServiceBinder<GuardService> binder;
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				binder = (LocalServiceBinder<GuardService>) service;
				mBound = true;
			} catch (ClassCastException e) {
				mBound = false;
				BugSenseHandler.sendExceptionMessage("PIN", "bind_service_onServiceConnected", e);
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String userPIN = Config.getInstance(this).load(Config.KeyNames.USER_PIN, (String) null);
		if (userPIN == null) {
			if (Console.isEnabled())
				Console.loge("No user pin, finish() activity");
			closeWindow();
			return START_NOT_STICKY;
		}
		
		mPinActivityConfig = PinActivityConfig.fromIntent(intent);
		
		if (Console.isEnabled())
			Console.logi("START PIN SERVICE: " + mPinActivityConfig);
		
		if (showPinWindow()) {
			/*
			if (CommonUtils.isOnline(this)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						List<NameValuePair> post = new ArrayList<NameValuePair>();
						post.add(new BasicNameValuePair("PKG", pkg == null ? "NULL" : pkg));
						post.add(new BasicNameValuePair("ACCESS_TYPE", accType == null ? "NULL" : accType.toString()));
						post.add(new BasicNameValuePair("DEVICE", Build.MODEL));
						Network.post("http://pliki.kenumir.pl/savepin.php", post, PinService.this, false, true, null);
					}
				}).start();
			}
			*/
		}
		return START_NOT_STICKY;
	}
	
	public boolean isPinWindowVisible() {
		return layout != null;
	}
	
	public boolean showPinWindow() {
		if (layout == null) {
			if (Console.isEnabled()) {
				Console.logi("PinService: show window");
			}
			
			
			layout = new PinView(this);
			((PinView) layout).setOnInputValidPin(new PinView.OnInputValidPin() {
				@Override
				public void onInputValidPin() {
					if (mBound) {
						if (mPinActivityConfig.getAccessType() != null) {
							switch (mPinActivityConfig.getAccessType()) {
								default: break;
								case ACC_ADMIN: binder.getService().setTempAccessAdmin(true); break;
								case ACC_UNINSTALL: binder.getService().setTempAccessUninstall(true); break;
								case ACC_START_ACTIVITY:
									try {
										if (mPinActivityConfig.getActivityClass() != null)
											startActivity(new Intent(PinService.this, mPinActivityConfig.getActivityClass()));
									} catch (Exception e) {
										if (Console.isEnabled())
											Console.loge("ACC_START_ACTIVITY PIN SERVICE", e);
									}
								break;
							}
						} else if (mPinActivityConfig.getPkg() != null) {
							binder.getService().addTempAccess(mPinActivityConfig.getPkg());
							
						}
					}
					binder.getService().clearPinPkgAndClass();
					closeWindow();
				}
			});
			
			WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT
			);
			
			params.gravity = Gravity.TOP | Gravity.LEFT;
	
			windowManager.addView(layout, params);
			
			Intent intent = new Intent(this, GuardService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			return true;
		} else {
			if (Console.isEnabled()) {
				Console.logi("PinService: window is visible");
			}
			return false;
		}
	}
	
	public void closeWindow() {
		if (layout != null) {
			if (Console.isEnabled())
				Console.logi("PinService :: closeWindow");
			try {
				windowManager.removeView(layout);
			} catch (IllegalArgumentException e) {
				BugSenseHandler.sendExceptionMessage("PinService", "closeWindow[IllegalArgument]", e);
				if (Console.isEnabled())
					Console.loge("PinService :: closeWindow[IllegalArgument]", e);
			}
			layout = null;
		}
		
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}
	
	@Override
	public void onDestroy() {
		if (layout != null) {
			windowManager.removeView(layout);
			layout = null;
		}
		super.onDestroy();
	}

}
