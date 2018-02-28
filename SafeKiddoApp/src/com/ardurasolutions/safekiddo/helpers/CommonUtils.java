package com.ardurasolutions.safekiddo.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;

import com.ardurasolutions.safekiddo.R;
import com.bugsense.trace.BugSenseHandler;


public class CommonUtils {
	
	/**
	 * try get unique device id
	 * @param ctx
	 * @return
	 */
	/*public static String getDeviceId(Context ctx) {
		String id = getIMEI(ctx);
		if (id != null && id.trim().length() > 0) {
			return id;
		} else {
			
			WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE); 
			id = mWifiManager.getConnectionInfo().getMacAddress();
			if (id != null && id.trim().length() > 0) {
				return id;
			} else {
				id = android.os.Build.SERIAL;
				if (id != null && id.trim().length() > 0) {
					return id;
				} else {
				
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
					id = mBluetoothAdapter.getAddress();
					if (id != null && id.trim().length() > 0) {
						return id;
					} else {
						id = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
						return id;
					}
				
				}
			}
		}
	}*/
	
	/**
	 * device frendly name [MANUFACTURER] [MODEL]
	 * @return
	 */
	/*public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return model;
		} else {
			return manufacturer.toUpperCase(Locale.getDefault()) + " " + model;
		}
	}*/
	
	public static Intent emailIntent(Context context, String address, String subject, String body, String cc) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_CC, cc);
		intent.setType("message/rfc822");
		return intent;
	}
	
	public static void stopServiceSafe(Context ctx, Intent it) {
		try {
			ctx.stopService(it);
		} catch (SecurityException e) {}
	}

	public static String getIMEI(Context ctx) {
		TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static boolean isWiFiEnabled(Context ctx) {
		return ((WifiManager)ctx.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
	}

	public static boolean isWiFi(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = null;
		if (cm != null)
			networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return networkInfo == null ? false : networkInfo.isConnected();
	}
	
	/**
	 * 
	 * @param ctx if null return false
	 * @return
	 */
	public static boolean isOnline(Context ctx) {
		if (ctx == null) return false;
		ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null) {
			return false;
		} else {
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
	}
	
	public static boolean isTablet(Context ctx) {
		return ctx.getResources().getBoolean(R.bool.is_tablet);
	}
	
	@SuppressLint("NewApi")
	public static boolean hasHardwaremenuButton(Context ctx) {
		boolean res = true;
		
		if (Build.VERSION.SDK_INT >= 14) {
			res = ViewConfiguration.get(ctx).hasPermanentMenuKey();
		}
		
		return res;
	}
	
	//private static boolean isProxyServiceWorking(Context ctx) {
	//	return isServiceRunning(ctx, ProxyService.class.getName());
	//}
	
	public static boolean isServiceRunning(Context ctx, String serviceClassName){
		final ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if proxy is working<br>
	 * if not, run its
	 */
//	public static void checkAndSetupProxyServer(Context ctx) {
//		if (!isProxyServiceWorking(ctx)) {
//			ctx.startService(new Intent(ctx, ProxyService.class));
//		}
//	}
	
	/**
	 * same as checkAndSetupProxy but asynchronous
	 * @param ctx
	 */
//	public static void checkAndSetupProxyServerAsync(final Context ctx) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				checkAndSetupProxyServer(ctx);
//			}
//		}).start();
//	}
	
	public static void setupBrowserProxy(WebView ctx) {
		int port = com.ardurasolutions.safekiddo.helpers.Config
				.getInstance(ctx.getContext())
				.load(com.ardurasolutions.safekiddo.helpers.Config.KeyNames.LOCAL_PROXY_PORT, Constants.LOCAL_PROXY_PORT);
		ProxyWebView.setProxy(ctx, "127.0.0.1", port);
	}
	
	public static final int PROXY_CHECK_ALARM_REQUEST = 1211;

	/*public static void checkWiFiProxyChecks(Context ctx, boolean forceSetUp) {
		if (Constants.DEV_DISABLE_PROXY_SET) return;
		
		Long lastCheck = SharedPrefs.getInstance(ctx).loadLong(SharedPrefs.KeyNames.LAST_PROXY_CHECK, 0L);
		boolean setAlarmAndSetup = false;
		
		if (lastCheck == 0L) {
			ProxySystem.setupWiFiProxy(ctx);
			setAlarmAndSetup = true;
		} else {
			if (System.currentTimeMillis() > lastCheck + Constants.PROXY_SETINGS_CHECK_PERIOD + (5 * 1000)) {
				setAlarmAndSetup = true;
				if (Console.isEnabled())
					Console.logi("Proxy check is late, setup and set alarm");
			}
		}
		
		if (setAlarmAndSetup || forceSetUp) {
			ProxySystem.setupWiFiProxy(ctx);
			((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME_WAKEUP, 
				SystemClock.elapsedRealtime() + Constants.PROXY_SETINGS_CHECK_PERIOD, 
				Constants.PROXY_SETINGS_CHECK_PERIOD, 
				PendingIntent.getBroadcast(ctx, PROXY_CHECK_ALARM_REQUEST, new Intent(ctx, CheckProxyRecevier.class), PendingIntent.FLAG_UPDATE_CURRENT)
			);
		}
	}*/
	
	public static String streamToString(final InputStream is, final int bufferSize) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try {
			final Reader in = new InputStreamReader(is, "UTF-8");
			try {
				for (;;) {
					int rsz = in.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			} finally {
				in.close();
			}
		} 
		catch (UnsupportedEncodingException ex) { }
		catch (IOException ex) { }
		return out.toString();
	}
	
	public static Bitmap makeBitmapShadow(Bitmap src) {  
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap transBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(transBitmap);
		canvas.drawARGB(0, 0, 0, 0);
		
		ColorFilter filter = new PorterDuffColorFilter(0x33000000, android.graphics.PorterDuff.Mode.SRC_IN);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColorFilter(filter);
		
		canvas.drawBitmap(src, 0, 0, paint);
		
		return transBitmap;
	}
	
	/**
	 * sync provider:<br>
	 * - DataContentProvider.AUTHORITY<br>
	 * - FilesContentProvider.AUTHORITY<br>
	 * @param authority
	 */
	public static void runSync(String authority) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		//bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(null, authority, bundle);
	}
	
	/**
	 * recursive delete dir
	 * @param dir
	 */
	public static void wipeDirectory(File dir) {
		if (dir.isDirectory())
			for (File child : dir.listFiles())
				wipeDirectory(child);
		dir.delete();
	}
	
	/**
	 * keyboard
	 */
	
	public static void hideKeyboard(Context ctx, EditText txt) {
		txt.clearFocus();
		InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE); 
		inputManager.hideSoftInputFromWindow(txt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
	}
	
	public static void showKeyboard(Context ctx, View view) {
		if (view.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
		}
	}
	
	/**
	 * check is run app i background
	 */
	/*@Deprecated
	public static boolean isRunIBackground(Application a) {
		List<RunningTaskInfo> localList = ((ActivityManager)a.getSystemService(Activity.ACTIVITY_SERVICE)).getRunningTasks(1);
		if ((localList != null) && (localList.size() != 0))
			return !((ActivityManager.RunningTaskInfo)localList.get(0)).topActivity.getPackageName().contains(a.getPackageName());
		return true;
	}*/
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static boolean isScreenOn(Context ctx) {
		PowerManager pm =(PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= 20)
			return pm.isInteractive();
		else
			return pm.isScreenOn();
	}
	
	/*
	public static void closeAudioPlayback(Context ctx) {
		((AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(
			new OnAudioFocusChangeListener() {@Override public void onAudioFocusChange(int focusChange) {}}, 
			AudioManager.STREAM_MUSIC, 
			AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
		);
	}
	*/
	
	/**
	 * sprawdzanie czy safekiddo jest domyślnym launcherem
	 * @param ctx
	 * @return
	 */
	public static boolean isMyLauncherDefault(Context ctx) {
		final Intent intent = new Intent(Intent.ACTION_MAIN); 
		intent.addCategory(Intent.CATEGORY_HOME); 
		final ResolveInfo res = ctx.getPackageManager().resolveActivity(intent, 0); 
		if (res.activityInfo == null) {
			// should not happen. A home is always installed, isn't it?
			return false;
		} else {
			return res.activityInfo.packageName.equals(ctx.getPackageName());
		}
	}
	
	public static void startHomeChooser(Context ctx, boolean forceShow) {
		
		if (forceShow) {
			try {
				Intent selector = new Intent(Intent.ACTION_MAIN);
				selector.addCategory(Intent.CATEGORY_HOME);
				selector.addCategory(Intent.CATEGORY_DEFAULT);
				selector.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
				ctx.startActivity(selector);
			} catch (Exception e1) {
				try {
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.addCategory(Intent.CATEGORY_HOME);
					i.addCategory(Intent.CATEGORY_DEFAULT);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					ctx.startActivity(i);
				} catch (Exception e2) {
					BugSenseHandler.sendExceptionMessage("launcher", "launcher_select_dialog", e2);
				}
			}
		} else {
			try {
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.addCategory(Intent.CATEGORY_HOME);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				ctx.startActivity(i);
			} catch (Exception e2) {
				BugSenseHandler.sendExceptionMessage("launcher", "launcher_select_dialog", e2);
			}
		}
	}
	
	public static interface ProxyCheckCallback {
		public void onProxyCheckFinish(boolean result);
	}
	
	public static void checkLocalProxy(final Context ctx, final ProxyCheckCallback cb) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final int localPort = com.ardurasolutions.safekiddo.helpers.Config.getInstance(ctx).load(com.ardurasolutions.safekiddo.helpers.Config.KeyNames.LOCAL_PROXY_PORT, Constants.LOCAL_PROXY_PORT);
				boolean cbRuns = false;
				
				try {
					Socket s = new Socket("127.0.0.1", localPort);
					PrintWriter pw = new PrintWriter(s.getOutputStream());
					pw.println("GET http://127.0.0.1/test HTTP/1.1");
					pw.println("Host: 127.0.0.1");
					pw.println("");
					pw.flush();
					BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
					String t;
					
					while((t = br.readLine()) != null) {
						if (t.equals("SAFEKIDDO PROXY OK")) {
							if (cb != null) {
								cb.onProxyCheckFinish(true);
								cbRuns = true;
							}
						}
					}
					br.close();
					s.close();
				} catch (UnknownHostException e) {
				} catch (IOException e) {
				}
				
				if (cb != null && !cbRuns) {
					cb.onProxyCheckFinish(false);
				}
			}
		}).start();
	}
	
}
