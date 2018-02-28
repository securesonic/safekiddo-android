package com.ardurasolutions.safekiddo.browser.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.browser.interfaces.OnTakeScreenShoot;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class WebViewHv extends WebView {
	
	public static interface OnScrollChanged {
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
	
	public static enum WebViewState {
		NO_URL, LOADED, LOADING;
	}
	
	public static final String SAVE_RESTORE_STATE_KEY = "WEBVIEW_CHROMIUM_STATE";
	
	private OnScrollChanged mOnScrollChanged;
	private String oryginalUserAgent = "";
	private OnTakeScreenShoot mOnTakeScreenShoot;
	private BrowserIdentify mBrowserIdentify;
	private String forceLoadSSL = null;

	public WebViewHv(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public WebViewHv(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public WebViewHv(Context context) {
		super(context);
		init();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		try {
			// INFO http://stackoverflow.com/questions/15127762/webview-fails-to-render-until-touched-android-4-2-2
			// http://stackoverflow.com/questions/13500452/android-webview-renders-blank-white-view-doesnt-update-on-css-changes-or-html
			if (android.os.Build.VERSION.SDK_INT >= 16 && android.os.Build.VERSION.SDK_INT <= 17) {
				invalidate();
			}
			super.onDraw(canvas);
			return;
		} catch (Exception e) {
			BugSenseHandler.sendExceptionMessage("WebView", "onDraw", e);
			if (Console.isEnabled())
				Console.loge("WebView :: onDraw", e);
		}
	}

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@SuppressWarnings("deprecation")
	private void init() {
		
		// INFO odkomentowane wyłacza hardrawe acceleration co eliminuje błąd z "zamarzaniem" przeglądarki - ale spowalnia działanie
		//if (Build.VERSION.SDK_INT >= 19)
		//	setLayerType(View.LAYER_TYPE_NONE, null);
		
		// SETTINGS
		if (!isInEditMode()) {
			WebSettings settings = getSettings();
			
			oryginalUserAgent = settings.getUserAgentString();
			
			settings.setJavaScriptEnabled(true);
			settings.setBuiltInZoomControls(true);
			settings.setUserAgentString(getSettings().getUserAgentString() + Constants.getUserAgent(getContext()));
			
			if (Build.VERSION.SDK_INT >= 11) {
				settings.setDisplayZoomControls(false);
			}
			if (Build.VERSION.SDK_INT >= 16) {
				settings.setAllowFileAccessFromFileURLs(false);
				settings.setAllowUniversalAccessFromFileURLs(false);
			}
			if (Build.VERSION.SDK_INT >= 17) {
				settings.setMediaPlaybackRequiresUserGesture(true);
			}
			if (Build.VERSION.SDK_INT < 19) { // INFO deprecated in api 18
				settings.setPluginState(PluginState.ON);
			}
			
			settings.setLoadWithOverviewMode(true);
			settings.setUseWideViewPort(true);
			settings.setJavaScriptCanOpenWindowsAutomatically(true);
			
			// rest
			settings.setDomStorageEnabled(true);
			settings.setAppCachePath(getContext().getCacheDir().toString());
			settings.setAppCacheEnabled(true);
			settings.setCacheMode(WebSettings.LOAD_DEFAULT);
			settings.setGeolocationDatabasePath(getContext().getCacheDir().getAbsolutePath());
			settings.setAllowFileAccess(true);
			settings.setDatabaseEnabled(true);
			settings.setSupportZoom(true);
			settings.setBuiltInZoomControls(true);
			settings.setAllowContentAccess(true);
			settings.setDefaultTextEncodingName("utf-8");
		}
	}
	/*
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (getOnScrollChanged() != null)
			getOnScrollChanged().onScrollChanged(l, t, oldl, oldt);
	}
	*/
	public OnScrollChanged getOnScrollChanged() {
		return mOnScrollChanged;
	}

	public void setOnScrollChanged(OnScrollChanged mOnScrollChanged) {
		this.mOnScrollChanged = mOnScrollChanged;
	}
	
	public void takeScreenShoot() {
		try {
			if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
				Bitmap b = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(b);
				draw(canvas);
				Resources r = getResources();
				int offminus = r.getDimensionPixelOffset(R.dimen.tab_switcher_divider_size);// * r.getInteger(R.integer.tab_switcher_cols);
				int thumbWidth = (r.getDisplayMetrics().widthPixels / r.getInteger(R.integer.tab_switcher_cols)) - offminus;
				int thumbHeight = b.getHeight() / ( b.getWidth() / thumbWidth);
				if (thumbWidth > 0) {
					Bitmap b2 = Bitmap.createScaledBitmap(b, thumbWidth, thumbHeight, true);
					if (getOnTakeScreenShoot() != null) {
						getOnTakeScreenShoot().onTakeScreenShoot(Bitmap.createBitmap(b2, 0, 0, thumbWidth, r.getDimensionPixelSize(R.dimen.tab_switcher_thumb_height)));
					}
				}
			}
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("WebViewHv :: takeScreenShoot", e);
		}
	}
	
	private void callHiddenWebViewMethod(String name){
		try {
		    Method method = WebView.class.getMethod(name);
		    method.invoke(this);
		} catch (NoSuchMethodException e) {
		   // Log.error("No such method: " + name, e);
		} catch (IllegalAccessException e) {
		    //Log.error("Illegal Access: " + name, e);
		} catch (InvocationTargetException e) {
		    //Log.error("Invocation Target Exception: " + name, e);
		}
	}

	@SuppressLint("NewApi")
	public void onPauseCall() {
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			onPause();
		} else {
			callHiddenWebViewMethod("onPause");
		}
	}
	
	@SuppressLint("NewApi")
	public void onResumeCall() {
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			onResume();
		} else {
			callHiddenWebViewMethod("onResume");
		}
	}

	public OnTakeScreenShoot getOnTakeScreenShoot() {
		return mOnTakeScreenShoot;
	}

	public void setOnTakeScreenShoot(OnTakeScreenShoot mOnTakeScreenShoot) {
		this.mOnTakeScreenShoot = mOnTakeScreenShoot;
	}
	
	public void loadDataCompat(String data) {
		loadDataCompat(data, null);
	}
	
	public void loadDataCompat(String data, String compatUrl) {
		if (compatUrl == null) {
			if (Build.VERSION.SDK_INT > 10) {
				loadData(data, "text/html; charset=UTF-8", "UTF-8");
			} else {
				loadDataWithBaseURL("about:blank", data, "text/html", "utf-8", "about:blank");
			}
		} else {
			loadDataWithBaseURL(compatUrl, data, "text/html", "utf-8", compatUrl);
		}
	}
	
	public static void saveStateToFile(File f, Bundle webViewState) {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			byte[] state = webViewState.getByteArray(SAVE_RESTORE_STATE_KEY);
			if (state != null) {
				try {
					FileOutputStream fs = new FileOutputStream(f);
					fs.write(state);
					fs.flush();
					fs.close();
				} catch (FileNotFoundException e1) {
					if (Console.isEnabled())
						Console.loge("WebViewHv :: saveStateToFile", e1);
				} catch (IOException e2) {
					if (Console.isEnabled())
						Console.loge("WebViewHv :: saveStateToFile", e2);
				}
			}
		} else {
			// SAVE STATE: Bundle[{history=[[B@4205d4c0], scale=0.7346939, textwrapScale=2.0, privateBrowsingEnabled=false, index=0, overview=true}]
			try {
				FileOutputStream fs = new FileOutputStream(f);
				WebViewStateContener wvsc = new WebViewStateContener(webViewState);
				ObjectOutputStream out = new ObjectOutputStream(fs);
				out.writeObject(wvsc);
				out.close();
				fs.close();
			} catch (FileNotFoundException e) {
				if (Console.isEnabled())
					Console.loge("WebViewHv :: saveStateToFile", e);
			} catch (IOException e) {
				if (Console.isEnabled())
					Console.loge("WebViewHv :: saveStateToFile", e);
			}
		}
	}
	
	public static Bundle loadStateFromFile(File f) {
		Bundle res = null;
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			if (f.exists()) {
				byte[] state = null;
				try {
					state = new byte[(int) f.length()];
					FileInputStream fs = new FileInputStream(f);
					fs.read(state);
					fs.close();
				} catch (FileNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("loadFragment[FileNotFound]", e);
				} catch (IOException e) {
					if (Console.isEnabled())
						Console.loge("loadFragment[IO", e);
				}
				if (state != null) {
					res = new Bundle();
					res.putByteArray(SAVE_RESTORE_STATE_KEY, state);
				}
			}
		} else {
			if (f.exists()) {
				try {
					
					FileInputStream fs = new FileInputStream(f);
					ObjectInputStream in = new ObjectInputStream(fs);
					Object o = in.readObject();
					if (o instanceof WebViewStateContener) {
						WebViewStateContener dRes = (WebViewStateContener) o;
						res = dRes.toBundle();
					}
					in.close();
					fs.close();
					
				} catch (FileNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("WebViewHv :: loadStateFromFile[FileNotFound]", e);
				} catch (IOException e) {
					if (Console.isEnabled())
						Console.loge("WebViewHv :: loadStateFromFile[IO]", e);
				} catch (ClassNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("WebViewHv :: loadStateFromFile[ClassNotFound]", e);
				}
			}
		}
		return res;
	}
	
	public static boolean hasHistoryRestored(Bundle b) {
		return b != null && b.containsKey("history") && b.get("history") != null;
	}
	
	public static boolean isForceReloadAfterRestore(Bundle b) {
		return b != null && b.containsKey(WebSessionItem.KEY_FORDE_RELOAD) && b.getBoolean(WebSessionItem.KEY_FORDE_RELOAD);
	}
	
	public BrowserIdentify getBrowserIdentify() {
		return mBrowserIdentify;
	}

	public void setBrowserIdentify(BrowserIdentify mBrowserIdetify) {
		this.mBrowserIdentify = mBrowserIdetify;
		switch (this.mBrowserIdentify) {
			default:
			case IDENTIFY_ANDROID:
				getSettings().setUserAgentString(oryginalUserAgent + Constants.getUserAgent(getContext()));
			break;
			case IDENTIFY_DESKTOP:
				String defaultUa = new String(oryginalUserAgent);
				// mobile
				// Mozilla\/5.0 (Linux; Android 4.4.4; Nexus 7 Build\/KTU84P) AppleWebKit\/537.36 (KHTML, like Gecko) Version\/4.0 Chrome\/33.0.0.0 Safari\/537.36 SafeKiddo\/0.9.58
				// desktop
				// Mozilla\/5.0 (X11; Linux x86_64) AppleWebKit\/537.36 (KHTML, like Gecko) Chrome\/37.0.2062.117 Safari\/537.36
				
				final Pattern pattern = Pattern.compile("\\(((.+?))\\)");
				final Matcher matcher = pattern.matcher(defaultUa);
				if (matcher.find()) {
					String res = matcher.group();
					defaultUa = defaultUa.replace(res, "(X11; Linux x86_64)");
				}
				
				getSettings().setUserAgentString(defaultUa + Constants.getUserAgent(getContext()));
			break;
		}
	}

	public String getForceLoadSSL() {
		return forceLoadSSL;
	}

	public void setForceLoadSSL(String forceLoadSSL) {
		this.forceLoadSSL = forceLoadSSL;
	}
	
	public boolean isForceLoadSSL(String url) {
		return getForceLoadSSL() != null && getForceLoadSSL().equals(url);
	}

	private static class WebViewStateContener implements Serializable {
		
		private static final long serialVersionUID = -5696460595612428399L;
		
		private ArrayList<Byte[]> history;
		private float scale;
		private float textwrapScale;
		private boolean privateBrowsingEnabled = false;
		private int index = 0;
		private boolean overview = true;
		
		@SuppressWarnings("unchecked")
		public WebViewStateContener(Bundle b) {
			history = (ArrayList<Byte[]>) b.get("history");
			scale = b.getFloat("scale", 1f);
			textwrapScale = b.getFloat("textwrapScale", 2f);
			privateBrowsingEnabled = b.getBoolean("privateBrowsingEnabled", false);
			index = b.getInt("index", 0);
			overview = b.getBoolean("overview", true);
		}
		
		public Bundle toBundle() {
			Bundle res = new Bundle();
			if (history != null)
				res.putSerializable("history", history);
			res.putFloat("scale", scale);
			res.putFloat("textwrapScale", textwrapScale);
			res.putBoolean("privateBrowsingEnabled", privateBrowsingEnabled);
			res.putInt("index", index);
			res.putBoolean("overview", overview);
			return res;
		}
		
		@Override
		public String toString() {
			return 
				"scale=" + scale + 
				", textwrapScale=" + textwrapScale + 
				", privateBrowsingEnabled=" + privateBrowsingEnabled + 
				", index=" + index + 
				", overview=" + overview +
				", history=" + history;
		}
	}
}
