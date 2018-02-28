package com.ardurasolutions.safekiddo.browser;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.browser.interfaces.OnLogoutUser;
import com.ardurasolutions.safekiddo.browser.interfaces.OnProxyServerStarts;
import com.ardurasolutions.safekiddo.browser.interfaces.OnProxyService;
import com.ardurasolutions.safekiddo.browser.interfaces.OnRequestUserAgnetChange;
import com.ardurasolutions.safekiddo.browser.interfaces.OnTabAction;
import com.ardurasolutions.safekiddo.browser.proto.BrowserIdentify;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.OnFragmentDestroy;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.OnFragmentUpdate;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.WebFragmentItem;
import com.ardurasolutions.safekiddo.browser.proto.WebTabsAdapter;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.proto.LocalServiceBinder;
import com.ardurasolutions.safekiddo.services.ProxyService;
import com.bugsense.trace.BugSenseHandler;

public class BrowserMainActivity extends FragmentActivity implements OnProxyService, OnLogoutUser, OnTabAction, OnRequestUserAgnetChange {
	
	public static final String KEY_LOAD_URL = "load_url";
	
	public static interface OnCloseAnimationEnd {
		public void onCloseAnimationEnd();
	}
	
	private boolean onServerStartsCalled = false;
	boolean mBound = false;
	private LocalServiceBinder<ProxyService> binder;
	private ServiceConnection mConnection = new ServiceConnection() {
		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			try {
				binder = (LocalServiceBinder<ProxyService>) service;
				mBound = true;
				
				if (binder != null && binder.getService() != null) {
					
					if (binder.getService().isRuned()) {
						onServerStartsCalled = true;
						onServerStarts();
					} else {
						binder.getService().setOnProxyServerStarts(new OnProxyServerStarts() {
							@Override
							public void onProxyServerStarts() {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										onServerStartsCalled = true;
										onServerStarts();
									}
								});
							}
						});
					}
				}
			} catch (ClassCastException e) {
				mBound = false;
				BugSenseHandler.sendExceptionMessage("BROWSER", "bind_service_onServiceConnected", e);
			}
			
			if (mBound && !onServerStartsCalled) {
				isProxyServiceWorking(new CheckCallback() {
					@Override
					public void onCheckCallback(boolean result) {
						if (result) {
							onServerStarts();
						}
					}
				});
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	private TextView overlayTitle, overlayDesc;
	private Button overlayButton;
	private View overlayAll;
	
	private WebFragmentsManager mWebFragmentsManager;
	private GridView tabSwitchList;
	private WebTabsAdapter LA;
	private View tabSwitch;
	private boolean isTabAnimInProgress = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startProxyService();
		setContentView(R.layout.activity_browser_main);
		
		overlayTitle = (TextView) findViewById(R.id.overlayTitle);
		overlayDesc = (TextView) findViewById(R.id.overlayDesc);
		overlayButton = (Button) findViewById(R.id.overlayButton);
		overlayAll = findViewById(R.id.overlayAll);
		tabSwitchList = (GridView) findViewById(R.id.tabSwitchList);
		tabSwitch = findViewById(R.id.tabSwitch);
		
		findViewById(R.id.tabSwitchNewTab).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeTabList(new OnCloseAnimationEnd() {
					@Override
					public void onCloseAnimationEnd() {
						onNewTabClick();
					}
				});
			}
		});
		
		mWebFragmentsManager = new WebFragmentsManager(getCacheDir())
			.setFragmentFrameId(R.id.webFrame)
			.setFragmentManager(getSupportFragmentManager())
			.setOnFragmentDestroy(new OnFragmentDestroy() {
				@Override
				public void onFragmentDestroy(WebFragmentItem item) {
					mWebFragmentsManager.unLoadFragment(item);
				}
			})
			.setOnFragmentUpdate(new OnFragmentUpdate() {
				@Override
				public void onFragmentUpdate(WebFragmentItem item) {
					updatelist();
				}
				@Override
				public void onShowFragment(BrowserFrameFragment wfi) {
					isProxyServiceWorking(new CheckCallback() {
						@Override
						public void onCheckCallback(boolean result) {
							if (result)
								onServerStarts();
						}
					});
				}
				@Override
				public void onHideFragment(BrowserFrameFragment wfi) {
				}
			});
		
		LA = new WebTabsAdapter(this, tabSwitchList) {
			@Override
			public ArrayList<WebFragmentItem> getAllItems() {
				return mWebFragmentsManager.getOrderedList();
			}
			@Override
			public OnClickListener onDeleteClick(final WebFragmentItem item) {
				return new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mWebFragmentsManager.closeFragment(item);
						if (mWebFragmentsManager.getItemsCount() == 0) {
							openTab("about:blank");
						}
						updatelist();
					}
				};
			}
		};
		tabSwitchList.setAdapter(LA);
		tabSwitchList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				WebFragmentItem i = mWebFragmentsManager.getOrderedList().get(position);
				mWebFragmentsManager.swithToFragment(i);
				
				updatelist();
				closeTabList(new OnCloseAnimationEnd() {
					@Override
					public void onCloseAnimationEnd() {
						//
					}
				});
			}
		});

		String urlToOpen = "about:blank";
		
		if (getIntent() != null) {
			String url = getIntent().getDataString();
			if (url != null && url.startsWith("http")) {
				urlToOpen = url;
			}
		}
		
		if (savedInstanceState == null) {
			if (mWebFragmentsManager.restoreSession()) {
				if (!urlToOpen.equals("about:blank"))
					openTab(urlToOpen);
			} else {
				openTab(urlToOpen);
			}
			
			isProxyServiceWorking(new CheckCallback() {
				@Override
				public void onCheckCallback(boolean result) {
					if (result)
						onServerStarts();
				}
			});
			
		} else {
			openTab(urlToOpen);
		}
		
		
	}
	
	private void openTab(String url) {
		mWebFragmentsManager.registerNewFragment(new BrowserFrameFragment().setUrlToLoadAfterInit(url));
		updatelist();
	}
	
	private void updatelist() {
		LA.notifyDataSetChanged();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {
			Uri url = intent.getData();
			
			if (url != null && url.toString().startsWith("http")) {
				Bundle bundle = intent.getExtras();
				if (bundle != null && bundle.get(getPackageName() + ".Origin") != null && bundle.get(getPackageName() + ".Origin") == Integer.valueOf(1)) {
					final BrowserFrameFragment f = mWebFragmentsManager.getTopItem() != null ? mWebFragmentsManager.getTopItem().getWebFragment() : null;
					if (f != null) {
						f.loadUrlWebViewWithCheck(url.toString(), true);
					} else {
						openTab(url.toString());
						closeTabListIntermediate();
					}
				} else {
					openTab(url.toString());
					closeTabListIntermediate();
				}
			}
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event ) {
		final BrowserFrameFragment f = mWebFragmentsManager.getTopItem() != null ? mWebFragmentsManager.getTopItem().getWebFragment() : null;
		switch(keyCode) {
			default: return super.onKeyUp(keyCode, event);
			case KeyEvent.KEYCODE_BACK:
				if (isTabAnimInProgress) {
					return true;
				}
				if (tabSwitch.getVisibility() == View.VISIBLE) {
					closeTabList(new OnCloseAnimationEnd() {
						@Override
						public void onCloseAnimationEnd() {
							if (f == null) {
								openTab("about:blank");
							}
						}
					});
					return true;
				}
				if (f != null && android.os.Build.VERSION.SDK_INT <= 16) {
					if (f.isInFullscreen()) {
						f.closeFullscreenMode();
						return true;
					}
				}
				if (f != null && !f.onBackButtonPressed()) {
					return super.onKeyUp(keyCode, event);
				} else
					return true;
			case KeyEvent.KEYCODE_MENU:
				f.showMenu();
				return super.onKeyUp(keyCode, event);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!UserHelper.isUserLogedIn(this)) {
			onLogoutUser();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, ProxyService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		mWebFragmentsManager.saveSession();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean isProxyServiceConnectd() {
		return mBound;
	}

	@Override
	public ProxyService getProxyService() {
		if (isProxyServiceConnectd())
			return binder.getService();
		else
			return null;
	}

	@Override
	public void startProxyService() {
		startService(new Intent(this, ProxyService.class));
	}

	@Override
	public void isProxyServiceWorking(final CheckCallback cb) {
		final boolean isConnected = isProxyServiceConnectd();
		if (isConnected) {
			if (binder.getService().isRuned()) {
				if (cb != null)
					cb.onCheckCallback(true);
			} else {
				startProxyService();
			}
		} else {
			if (cb != null)
				cb.onCheckCallback(false);
		}
	}

	@Override
	public void onLogoutUser() {
		sendBroadcast(new Intent().setAction(Constants.BRODCAST_SESSION_EXPIRES));
	}
	
	public void showOverlay(String title, String msg, String btnCaption, View.OnClickListener onClick) {
		overlayTitle.setText(title);
		overlayDesc.setText(msg);
		overlayButton.setText(btnCaption);
		overlayButton.setOnClickListener(onClick);
		overlayAll.setVisibility(View.VISIBLE);
	}

	@Override
	public void onServerStarts() {
		//new Handler().postDelayed(new Runnable() {
			//@Override
			//public void run() {
				//Console.logw("ON PROXY STARTS");
				//BrowserFrameFragment bf =  mWebFragmentsManager.getTopItem() != null ? mWebFragmentsManager.getTopItem().getWebFragment() : null;
				for(WebFragmentItem wf : mWebFragmentsManager.getOrderedList()) {
					BrowserFrameFragment bf = wf.isFragmentExists() ? wf.getWebFragment() : null;
					if (bf != null) {
						((BrowserFrameFragment) bf).onServerStarts();
					}
				}
			//}
		//}, 1500);
	}
	
	@Override
	public void onRequestUserAgnetChange(BrowserIdentify bi) {
		for(WebFragmentItem wf : mWebFragmentsManager.getOrderedList()) {
			BrowserFrameFragment bf = wf.isFragmentExists() ? wf.getWebFragment() : null;
			if (bf != null) {
				((BrowserFrameFragment) bf).changeBrowserIdetify(bi);
			}
		}
	}

	@Override
	public void onNewTabClick() {
		openTab("about:blank");
	}
	
	/**
	 * close without animation
	 */
	private void closeTabListIntermediate() {
		tabSwitch.setVisibility(View.GONE);
	}
	
	private void closeTabList(final OnCloseAnimationEnd cb) {
		Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
		a.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {
				isTabAnimInProgress = true;
			}
			@Override public void onAnimationEnd(Animation animation) {
				isTabAnimInProgress = false;
				tabSwitch.setVisibility(View.GONE);
				if (cb != null)
					cb.onCloseAnimationEnd();
			}
			@Override public void onAnimationRepeat(Animation animation) { }
		});
		tabSwitch.startAnimation(a);
	}

	@Override
	public void onShowTabList() {
		updatelist();
		tabSwitch.setVisibility(View.VISIBLE);
		Animation a = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
		a.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) {
				isTabAnimInProgress = true;
			}
			@Override public void onAnimationEnd(Animation animation) {
				isTabAnimInProgress = false;
			}
			@Override public void onAnimationRepeat(Animation animation) { }
		});
		tabSwitch.startAnimation(a);
	}

	@Override
	public void onNewTabUrlClick(String url) {
		openTab(url);
	}

}
