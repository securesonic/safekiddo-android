package com.ardurasolutions.safekiddo.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Browser;

import com.hv.console.Console;

public class BrowserObserverService extends Service {
	
	private static String CHROME_BOOKMARKS_URI = "content://com.android.chrome.browser/bookmarks";
	//private static final Uri CONTENT_URI = Browser.BOOKMARKS_URI;
	//private static final Uri BOOKMARKS_CONTENT_URI_POST_11 = Uri.parse("content://com.android.browser/bookmarks");
	
	private ContentObserver mContentObserver;
	private final Handler mHandler = new Handler();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (Console.isEnabled())
			Console.logi("Browser monitor service starts...");
		
		Cursor cursor2 = getContentResolver().query(Browser.BOOKMARKS_URI, Browser.HISTORY_PROJECTION, "bookmark = 0", null, Browser.BookmarkColumns.DATE + " DESC");
		if (cursor2.moveToFirst()) {
			Console.logi("HISTORY: " + cursor2.getString(Browser.HISTORY_PROJECTION_URL_INDEX));
		}
		cursor2.close();
		
		mContentObserver = new ContentObserver(mHandler) {
			@Override
			public void onChange(boolean selfChange, Uri uri) {
				
				//Console.logw("ContentObserver uri: " + uri);
				
				String sortOrder = Browser.BookmarkColumns.DATE + " DESC";
				Cursor cursor = getContentResolver().query(Uri.parse(CHROME_BOOKMARKS_URI), Browser.HISTORY_PROJECTION, "bookmark = 0", null, sortOrder); //new String[] {"title", "url"}
				if (cursor.moveToFirst()) {
					Console.logi("BROWSER URL: " + cursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX));
					if (cursor.getString(Browser.HISTORY_PROJECTION_URL_INDEX).equals("http://m.onet.pl/")) {
						ActivityManager amen = (ActivityManager)getBaseContext().getSystemService(Activity.ACTIVITY_SERVICE);
						@SuppressWarnings("deprecation")
						ComponentName paramComponentName = amen.getRunningTasks(1).get(0).baseActivity;
						
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kenumir.pl/"));
						intent.addCategory("android.intent.category.BROWSABLE");
						intent.setComponent(paramComponentName);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("create_new_tab", false); // true or false // api 12+ -> Browser.EXTRA_CREATE_NEW_TAB
						
						//intent.putExtra("org.mozilla.firefox.application_id", paramComponentName.getPackageName());
						intent.putExtra(Browser.EXTRA_APPLICATION_ID, paramComponentName.getPackageName());
						startActivity(intent);
					}
				}
				cursor.close();
			}
		};
		
		//Bookmarks.CONTENT_URI;
		
		// Uri.parse("content://com.android.chrome.browser/bookmarks")
		getContentResolver().registerContentObserver(getBookmarksUri(), false, mContentObserver);
	}
	
	private Uri getBookmarksUri() {
		//04-24 09:18:02.992: D/safekiddo(1235): CI: ComponentInfo{com.android.browser/com.android.browser.BrowserActivity}

		
		//return Uri.parse("content://com.android.chrome.browser/bookmarks");
		//return Uri.parse("content://com.android.browser/history");
		// content://com.sec.android.app.sbrowser.browser/history <- if ((Build.MANUFACTURER.compareToIgnoreCase("samsung") == 0) && (Build.VERSION.SDK_INT >= 16))
		//return Browser.BOOKMARKS_URI;
		//return BOOKMARKS_CONTENT_URI_POST_11;
		return Uri.parse("content://com.sec.android.app.sbrowser.browser/history");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		if (mContentObserver != null)
			getContentResolver().unregisterContentObserver(mContentObserver);
		super.onDestroy();
	}

}
