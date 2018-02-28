package com.ardurasolutions.safekiddo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.hv.console.Console;

public class WebActivity extends ActionBarActivity {
	
	private static final String KEY_URL = "url";
	private static final String KEY_TITLE = "title";
	
	public static void showActivity(Activity ctx, String url, String title) {
		Intent it = new Intent(ctx, WebActivity.class);
		it.putExtra(KEY_URL, url);
		it.putExtra(KEY_TITLE, title);
		ctx.startActivityForResult(it, 2);
	}
	
	private WebView webView;
	private String urlToLoad = "about:blank";
	private String title = "Info";
	private ProgressBar pb;

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		
		
		if (getIntent() != null && getIntent().hasExtra(KEY_URL))
			urlToLoad = getIntent().getStringExtra(KEY_URL);
		
		if (getIntent() != null && getIntent().hasExtra(KEY_TITLE))
			title = getIntent().getStringExtra(KEY_TITLE);
		
		setContentView(R.layout.activity_web);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    
		ActionBar ab = getSupportActionBar();
		ab.setTitle(title);
		//ab.setLogo(R.drawable.ic_action_settings);
		//ab.setIcon(R.drawable.ic_action_settings);
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		
		Console.logw("H: " + ab.getHeight());
		
		int padd = (int) (getResources().getDisplayMetrics().density * 10f);
		pb = new ProgressBar(this);
		//pb.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
		pb.setPadding(0, 0, padd, 0);
		
		pb.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
		toolbar.addView(pb);
		
		//ab.setCustomView(pb);
		ab.setDisplayShowCustomEnabled(true);
		
		//Toolbar.LayoutParams.
		//toolbar.addView(child, params)
		
		webView = (WebView) findViewById(R.id.webView);
		
		// SETTINGS
		WebSettings settings = webView.getSettings();
		
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setUserAgentString(webView.getSettings().getUserAgentString() + Constants.getUserAgent(this) + " SafeKiddo/WebActivity");
		
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
		settings.setAppCachePath(getCacheDir().toString());
		settings.setAppCacheEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setGeolocationDatabasePath(getCacheDir().getAbsolutePath());
		settings.setAllowFileAccess(true);
		settings.setDatabaseEnabled(true);
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setAllowContentAccess(true);
		settings.setDefaultTextEncodingName("utf-8");
		
		webView.setWebChromeClient(new WebChromeClient() {});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				//setProgressBarIndeterminateVisibility(Boolean.TRUE); 
				pb.setVisibility(View.VISIBLE);
			}
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				//setProgressBarIndeterminateVisibility(Boolean.FALSE); 
				pb.setVisibility(View.GONE);
			}
		});
		
		webView.loadUrl(urlToLoad);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				setResult(Activity.RESULT_OK);
				finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		webView.clearCache(false);
		webView.clearHistory();
		webView.removeAllViews();
		webView.setWebChromeClient(null);
		webView.setWebViewClient(null);
		webView.destroy();
		webView = null;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	
	@Override
	public void onBackPressed() {
		if (webView != null && webView.canGoBack()) {
			webView.goBack();
		} else {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

}
