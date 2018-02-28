package com.ardurasolutions.safekiddo.helpers;

import java.util.Locale;

import android.content.Context;

import com.ardurasolutions.safekiddo.sql.ConfigSQL;
import com.ardurasolutions.safekiddo.sql.tables.AppConfigTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AppConfig;
import com.hv.console.Console;

public class Config {
	
	public static enum KeyNames {
		/**
		 * identyfikator sesji (cookie)<br>
		 * String
		 */
		SESSION_ID,
		/**
		 * czy user został wylogowany od strony servera
		 * boolean
		 */
		IS_USER_LOGOUT_BY_SERVER,
		/**
		 * first run of desktop
		 * boolean
		 */
		DESKTOP_FIRST_RUN,
		/**
		 * local proxy port number
		 */
		LOCAL_PROXY_PORT,
		/**
		 * unique device id from server - after login<br>
		 * String
		 */
		DEVICE_UUID,
		/**
		 * timestamp ostatniego uruchomienia HeartBeat
		 */
		HB_LAST_RUN_TIME,
		
		/*
		 * GCM
		 */
		/**
		 * saved GCM reg ID
		 * String
		 */
		GCM_REG_ID,
		/**
		 * app version with registered GCM id
		 * Integer
		 */
		GCM_REG_APP_VER,
		/**
		 * boolean - jeżeli reg id zostało poprawnie zapisane po stronie servera = 0/1
		 */
		GCM_REG_ID_SENDET_TO_SERVER,
		
		/*
		 * USER PREFS
		 */
		/**
		 * login usera
		 * String
		 */
		USER_LOGIN,
		/**
		 * block apps with overlays
		 * boolean
		 */
		USER_BLOCK_APPS,
		/**
		 * DO NOT TRACK option
		 * boolean
		 */
		USER_DNT,
		/**
		 * ustawienie geolokalizacji w przeglądrce
		 * integer - default 1
		 */
		USER_GEOLOCATION,
		/**
		 * identyfikacja przeglądarki: mobile/desktop/inne?
		 */
		USER_BROWSER_IDENTYFICATION,
		/**
		 * wartość do pola nagłówka If-None-Match<br>
		 * type: String
		 */
		USER_BROWSER_HOMEPAGE_MODIFIED,
		
		/*
		 * USER CHILD
		 */
		/**
		 * id dziecka
		 * Long
		 */
		USER_CHILD_ID,
		/**
		 * naz wayświetlana w profilu (nazwa z panelu WS)
		 * String
		 */
		USER_CHILD_NAME,
		/**
		 * Unique ID, as String
		 * String
		 */
		USER_CHILD_UUID,
		/**
		 * user ping - init from WS
		 * String
		 */
		USER_PIN,
		
		/*
		 * DEV
		 */
		/**
		 * server address
		 * String
		 */
		DEV_SERVER,
		/**
		 * debugowanie webview (about:inspect)
		 * Boolean
		 */
		DEV_BROWSER_CONTENT_DEBUG;
		
		public String toString() {
			String res = super.toString();
			if (res.length() > 4 && res.substring(0, 5).equals("USER_")) {
				res = "prefs_key_" + res.substring(5).toLowerCase(Locale.getDefault());
			}
			return res;
		}
		
		public static KeyNames valueOfString(String k) {
			if (k != null && k.length() > 9 && k.substring(0, 10).equals("prefs_key_"))
				return KeyNames.valueOf("USER_" + k.substring(10).toUpperCase(Locale.getDefault()));
			else
				return KeyNames.valueOf(k);
		}
	}

	private static Config sInstance;
	
	public static Config getInstance(Context ctx) {
		if (sInstance == null)
			sInstance = new Config(ctx);
		return sInstance;
	}
	
	private ConfigSQL mConfigSQL;
	private Context mContext;
	
	public Config(Context ctx) {
		mContext = ctx;
		mConfigSQL = ConfigSQL.getInstance(ctx);
	}
	
	public Config save(KeyNames key, String value) {
		mConfigSQL
			.getTable(AppConfigTable.class)
			.save(key.toString(), value);
		if (Console.isEnabled() && key == KeyNames.SESSION_ID)
			Console.logi("SESSION SAVE: " + value);
		return this;
	}
	
	public Config save(KeyNames key, Long value) {
		mConfigSQL
			.getTable(AppConfigTable.class)
			.save(key.toString(), value.toString());
		return this;
	}
	
	public Config save(KeyNames key, Integer value) {
		mConfigSQL
			.getTable(AppConfigTable.class)
			.save(key.toString(), value.toString());
		return this;
	}
	
	public Config save(KeyNames key, Boolean value) {
		mConfigSQL
			.getTable(AppConfigTable.class)
			.save(key.toString(), value ? "1" : "0");
		return this;
	}
	
	public Config save(KeyNames key, Float value) {
		mConfigSQL
			.getTable(AppConfigTable.class)
			.save(key.toString(), value.toString());
		return this;
	}
	
	public String load(KeyNames key, String defaultValue) {
		AppConfig mAppConfig = mConfigSQL
			.getTable(AppConfigTable.class)
			.load(key.toString());
		
		if (mAppConfig == null)
			return defaultValue;
		else {
			if (Console.isEnabled() && key == KeyNames.SESSION_ID)
				Console.logi("SESSION LOAD: " + mAppConfig.app_config_value);
			return mAppConfig.app_config_value;
		}
	}
	
	public Long load(KeyNames key, Long defaultValue) {
		String val = load(key, (String) null);
		if (val == null) {
			return defaultValue;
		} else {
			try {
				return Long.valueOf(val);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}
	
	public Integer load(KeyNames key, Integer defaultValue) {
		String val = load(key, (String) null);
		if (val == null) {
			return defaultValue;
		} else {
			try {
				return Integer.valueOf(val);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}
	
	public Boolean load(KeyNames key, Boolean defaultValue) {
		String val = load(key, (String) null);
		if (val == null) {
			return defaultValue;
		} else {
			try {
				return Integer.valueOf(val) != 0 ? true : false;
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}
	
	public Float load(KeyNames key, Float defaultValue) {
		String val = load(key, (String) null);
		if (val == null) {
			return defaultValue;
		} else {
			try {
				return Float.valueOf(val);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
	}
	
	/**
	 * load value, if not exists return NULL
	 * @param key
	 * @return
	 */
	public String load(KeyNames key) {
		return load(key, (String) null);
	}
	
	public void clearAllData() {
		ConfigSQL mConfigSQL = ConfigSQL.getInstance(mContext);
		mConfigSQL.clearDbFile();
		mConfigSQL.clearInstance();
	}

}
