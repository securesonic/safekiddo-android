package com.ardurasolutions.safekiddo.helpers;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

import com.hv.console.Console;

public class Constants {
	
	/*
	 * DEV settings ----------------------
	 */
	/**
	 * Tryb w jakim działa aplikacja
	 */
	public static enum AppMode {
		/**
		 * serwery developerskie Hivedi
		 */
		MODE_ALPHA,
		/**
		 * serwery developerkie ogólne
		 */
		MODE_BETA,
		/**
		 * serwery produkcyjne
		 */
		MODE_PRODUCTION,
		/**
		 * sieć lokalna
		 */
		MODE_LOCAL;
	}
	public static final boolean DEV_DISABLE_PROXY_SET = false;
	/**
	 * APP_MODE = AppMode.MODE_BETA to servery są przełaczone na wersję "tst-"....
	 */
	public static final AppMode APP_MODE = AppMode.MODE_PRODUCTION;
	/*
	 * -----------------------------------
	 */
	
	/**
	 * log file stored on sdcard
	 */
	public static final String LOG_FILE_NAME = "safekiddo_log.txt";
	
	/**
	 * nam eo f session cookie
	 */
	public static final String SESSION_COOKIE_NAME = "safekiddo_mobile"; //"laravel_session";
	
	/**
	 * network conncetion time out
	 */
	//public static final int CONN_TIMEOUT = 20000;
	
	/**
	 * user agent for network requests
	 */
	public static String getUserAgent(Context ctx) {
		try {
			return " SafeKiddo/" + ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			if (Console.isEnabled())
				Console.loge("Constants::getUserAgent", e);
		}
		return " SafeKiddo ";
	}
	
	/**
	 * key for bug sense
	 */
	public static final String BUG_SENSE_KEY = "3ce2592a";
	
	/**
	 * Account type string.
	 */
	public static final String ACCOUNT_TYPE = "com.ardurasolutions.safekiddo.sync";
	
	/**
	 * Authtoken type string.
	 */
	public static final String AUTHTOKEN_TYPE = "com.ardurasolutions.safekiddo.sync";
	
	public static final String AUTH_KEY_USERNAME = "username";
	public static final String AUTH_KEY_PASSWORD = "password";
	
	private static final String BASE_URL = APP_MODE == AppMode.MODE_ALPHA ? 
			"https://dev-api.safekiddo.com/" : 
			(
				APP_MODE == AppMode.MODE_BETA ? "https://tst-api.safekiddo.com/" : 
				(
					APP_MODE == AppMode.MODE_LOCAL ? "http://192.168.1.6:8889/" : "http://api.safekiddo.com/"
				)
			);
	private static final String REGISTER_URL = APP_MODE == AppMode.MODE_ALPHA ? 
			"https://dev-my.safekiddo.com/register" : 
			(
				APP_MODE == AppMode.MODE_BETA ? "https://tst-my.safekiddo.com/register" : 
				(
					APP_MODE == AppMode.MODE_LOCAL ? "http://192.168.8.106:8888/register" : "https://my.safekiddo.com/register"
				)
			);
	private static final String BROWSER_HOMEPAGE_URL = BASE_URL + "api/v1/home_page";;
	
	public static final String CHILD_BLOCK_HEADER_UUID = "SK-UUID";
	public static final String CHILD_BLOCK_HEADER_CODE = "SK-CODE";
	public static final String CHILD_BLOCK_HEADER_URL = "SK-URL";
	public static final String CHILD_BLOCK_HEADER_CATEGORY_ID = "SK-CATEGORY-ID";
	public static final String CHILD_BLOCK_HEADER_CATEGORY_NAME = "SK-CATEGORY-NAME";
	
	private static final String BLOCK_URL = BASE_URL + "api/v1/block";
	private static final String CHILD_SET_URL = BASE_URL + "api/v1/child/set";
	private static final String AUTH_URL = BASE_URL + "api/v1/login";
	private static final String AUTH_FETCH_CHILDS_URL = BASE_URL + "api/v1/childs";
	private static final String HEART_BEAT_URL = BASE_URL + "api/v1/hb";
	private static final String LOGOUT_URL = BASE_URL + "api/v1/logout";
	
	public static String getBaseUrl() {
		return BASE_URL;
	}
	
	public static String getChildSetUrl() {
		return CHILD_SET_URL;
	}
	
	public static String getBlockUrl() {
		return BLOCK_URL;
	}
	
	public static String getAuthUrl() {
		return AUTH_URL;
	}
	
	public static String getAuthFetchChildsUrl() {
		return AUTH_FETCH_CHILDS_URL;
	}
	
	public static String getHeartBeatUrl() {
		return HEART_BEAT_URL;
	}
	
	public static String getLogoutUrl() {
		return LOGOUT_URL;
	}
	
	public static String getRegisterUrl() {
		return REGISTER_URL;
	}
	
	public static String getPanelUrl() {
		return REGISTER_URL.replace("/register", "");
	}
	
	public static String getBrowserHomepageUrl() {
		return BROWSER_HOMEPAGE_URL;
	}
	
	private static final String URL_PREFIX =
		APP_MODE == AppMode.MODE_ALPHA ? "dev-" : 
		(
				APP_MODE == AppMode.MODE_BETA ? "tst-" : 
				(
						APP_MODE == AppMode.MODE_LOCAL ? "dev-" : ""
				)
		);
	public static final String URL_TERMS_OF_SERVICE = "https://" + URL_PREFIX + "www.safekiddo.com/mobile/terms";
	public static final String URL_PRIVACY_POLICY   = "https://" + URL_PREFIX + "www.safekiddo.com/mobile/privacy";
	public static final String URL_COOKIES_POLICY   = "https://" + URL_PREFIX + "www.safekiddo.com/mobile/cookies";
	
	/**
	 * time for sync account (in minutes)
	 */
	public static final int ACCOUNT_SYNC_TIME = 15;
	
	/**
	 * port for local proxy
	 */
	public static final int LOCAL_PROXY_PORT = 8080;
	
	/**
	 * flaga Do Not Track
	 */
	public static final String SAFEKIDO_EXTRA_HEDER_DNT = "DNT";
	
	/**
	 * web service chcek url
	 */
	public static final String SAFEKIDO_WS_CHECK_URL = APP_MODE == AppMode.MODE_ALPHA ? 
			"http://dev-ws.safekiddo.com:8080/cc" : 
			(
				APP_MODE == AppMode.MODE_BETA ? "http://tst-ws.safekiddo.com:8080/cc" : 
				(
					APP_MODE == AppMode.MODE_LOCAL ? "http://dev-ws.safekiddo.com:8080/cc" : "http://ws.safekiddo.com:80/cc"
				)
			); 

	public static final String GCM_SENDER_ID = "649401718362";
	//public static final String GCM_SENDER_SAVE_URL = "http://192.168.3.155/gcm/save.php";
	
	public static int HEART_BEAT_INTERVAL = 1000 * 60 * 5;
	
	/*
	 * BROADCAST's
	 */
	/**
	 * broadcast approved apps change
	 */
	public static final String BRODCAST_BLOCKED_APPS = "com.ardurasolutions.safekiddo.blocked_apps";
	/**
	 * broadcast safekiddo unistall action
	 */
	public static final String BRODCAST_SAFEKIDDO_REMOVE = "com.ardurasolutions.safekiddo.safekiddo_remove";
	/**
	 * fired when current profile was changed
	 */
	public static final String BRODCAST_CHILD_PROFILE_CHANGED = "com.ardurasolutions.safekiddo.child_profile_changed";
	/**
	 * fires when session expires
	 */
	public static final String BRODCAST_SESSION_EXPIRES = "com.ardurasolutions.safekiddo.session_expires";
	/**
	 * fires when local proxy port changed
	 */
	public static final String BRODCAST_LOCAL_PROXY_PORT = "com.ardurasolutions.safekiddo.local_proxy_port";
}
