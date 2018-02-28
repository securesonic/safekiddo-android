package com.ardurasolutions.safekiddo.helpers;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import com.hv.console.Console;

public class AppsHelper {
	
	/**
	 * check is ComponentName is system resolver activity (default app chooser)<br>
	 * if is system resolver return null otherwise return <i>cn</i>
	 * @param cn
	 * @return
	 */
	public static ComponentName isSystemResolver(ComponentName cn) {
		// ComponentInfo{android/com.android.internal.app.ResolverActivity}
		return cn.getPackageName() != null && cn.getPackageName().equals("android") ? null : cn;
	}
	
	public static ComponentName getDefaultLauncherApp(Context ctx) {
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo defaultLauncher = ctx.getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
		if (defaultLauncher != null)
			return new ComponentName(defaultLauncher.activityInfo.applicationInfo.packageName, defaultLauncher.activityInfo.name);
		else
			return null;
	}
	
	public static ComponentName getDefaultGallery(Context ctx) {
		Intent it = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(it, 0);
		if (list.size() > 0) {
			ResolveInfo mInfo = list.get(0);
			return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
		} else
			return null;
	}
	
	public static ComponentName getDefaultCameraApp(Context ctx) {
		Intent it = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		ResolveInfo mInfo = ctx.getPackageManager().resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY);
		if (mInfo != null) {
			return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
		} else
			return null;
	}
	
	public static ResolveInfo getDefaultCameraAppResolve(Context ctx) {
		Intent it = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		ResolveInfo mInfo = ctx.getPackageManager().resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY);
		if (mInfo != null && !mInfo.activityInfo.packageName.equals("android")) {
			return mInfo;
		} else
			return null;
	}
	
	/**
	 * pobiera domyślną aplikację do smsów, jeżeli nie ma wybranej domyślnej to pobiera wszystkie aplikacji do SMSów z systemu
	 * i wybiera pierwszą z listy
	 * @param ctx
	 * @return
	 */
	public static ComponentName getDefaultSmsApp(Context ctx) {
		Intent it = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("sms:"));
		ResolveInfo mInfo = ctx.getPackageManager().resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY);
		if (mInfo != null) {
			ComponentName res = isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
			if (res == null) {
				List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(it, 0);
				mInfo = list.size() > 0 ? list.get(0) : null;
				if (mInfo != null)
					res = isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
			}
			return res;
		} else
			return null;
	}
	
	public static ComponentName getDefaultDialApp(Context ctx) {
		Intent it = new Intent(Intent.ACTION_VIEW, CallLog.Calls.CONTENT_URI);
		ResolveInfo mInfo = ctx.getPackageManager().resolveActivity(it, 0);// PackageManager.MATCH_DEFAULT_ONLY);
		if (mInfo == null) {
			List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(it, 0);
			mInfo = list.size() > 0 ? list.get(0) : null;
			if (mInfo != null)
				return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
			return null;
		} else {
			return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
		}
//		Intent it = new Intent(Intent.ACTION_CALL);
//		it.setData(Uri.parse("tel:000000"));
//		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(it, 0);
//		if (list.size() > 0) {
//			ResolveInfo mInfo = list.size() > 0 ? list.get(0) : null;
//			if (mInfo != null)
//				return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
//			else
//				return null;
//		} else {
//			return null;
//		}
	}
	
	// 05-05 13:18:03.493: D/safekiddo(17974): ComponentInfo{com.android.contacts/com.android.contacts.activities.PeopleActivity}

	public static ComponentName getDefaultContactsApp(Context ctx) {
		Intent it = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
		ResolveInfo mInfo = ctx.getPackageManager().resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY);// PackageManager.MATCH_DEFAULT_ONLY);
		if (mInfo == null) {
			List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(it, 0);
			mInfo = list.size() > 0 ? list.get(0) : null;
			if (mInfo != null)
				return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
			return null;
		} else {
			return isSystemResolver(new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name));
		}
	}
	
	private static ComponentName tryGetDefaultMailApp(Context ctx) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("text/html");
		List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(emailIntent, 0);
		if (list.size() > 0) {
			return new ComponentName(list.get(0).activityInfo.packageName, list.get(0).activityInfo.name);
		} else {
			return null;
		}
	}
	
	public static ComponentName getMailApp(Context ctx) {
		final PackageManager pm = ctx.getPackageManager();
		try {
			Intent it = pm.getLaunchIntentForPackage("com.google.android.gm");
			
			if (it != null) {
				ResolveInfo mInfo = pm.resolveActivity(it, 0);
				if (mInfo != null) {
					return new  ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name);
				} else
					return tryGetDefaultMailApp(ctx);
			} else {
				it = pm.getLaunchIntentForPackage("com.google.android.email");
				if (it != null) {
					ResolveInfo mInfo = pm.resolveActivity(it, 0);
					if (mInfo != null) {
						return new ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name);
					} else
						return tryGetDefaultMailApp(ctx);
				}
			}
			
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("AppsHelper::getMailApp", e);
		}
		return null;
	}
	
	public static ComponentName getClockApp(Context ctx) {
		final PackageManager pm = ctx.getPackageManager();
		try {
			Intent it = pm.getLaunchIntentForPackage("com.google.android.deskclock");
			if (it != null) {
				ResolveInfo mInfo = pm.resolveActivity(it, 0);
				if (mInfo != null) {
					return new  ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name);
				} else
					return null;
			}
		} catch (Exception e) { }
		return null;
	}
	
	public static ComponentName getCalcApp(Context ctx) {
		final PackageManager pm = ctx.getPackageManager();
		try {
			Intent it = pm.getLaunchIntentForPackage("com.android.calculator2");
			if (it != null) {
				ResolveInfo mInfo = pm.resolveActivity(it, 0);
				if (mInfo != null) {
					return new  ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name);
				} else
					return null;
			} else {
				it = pm.getLaunchIntentForPackage("com.android.calculator");
				if (it != null) {
					ResolveInfo mInfo = pm.resolveActivity(it, 0);
					if (mInfo != null) {
						return new  ComponentName(mInfo.activityInfo.applicationInfo.packageName, mInfo.activityInfo.name);
					} else
						return null;
				}
			}
		} catch (Exception e) { }
		return null;
	}
	
	public static List<ComponentName> getInstalledBrowsers(Context ctx) {
		List<ComponentName> res = new ArrayList<ComponentName>();
		
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hivedi.com/"));
		PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> handlers = pm.queryIntentActivities(i, PackageManager.GET_RESOLVED_FILTER);
		String selfPackage = ctx.getPackageName();
		
		for(ResolveInfo ri : handlers) {
			//if (Console.isEnabled())
			//	Console.log("BROWSER: " + ri.activityInfo.packageName + "/" + ri.activityInfo.name + " - " + pm.getApplicationLabel(ri.activityInfo.applicationInfo));
			if (!ri.activityInfo.packageName.equals(selfPackage)) {
				res.add(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
			}
		}
		
		
		return res;
	}
	
	public static List<String> getInstalledBrowsersPackages(Context ctx) {
		List<String> res = new ArrayList<String>();
		List<ComponentName> b = getInstalledBrowsers(ctx);
		for(ComponentName cn : b) {
			res.add(cn.getPackageName());
		}
		return res;
	}

}
