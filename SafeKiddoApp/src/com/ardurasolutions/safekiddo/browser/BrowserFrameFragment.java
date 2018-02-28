package com.ardurasolutions.safekiddo.browser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.validator.routines.UrlValidator;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ardurasolutions.safekiddo.BuildConfig;
import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.proto.BasicUserOperation.OnError;
import com.ardurasolutions.safekiddo.auth.proto.LockScreenOperation;
import com.ardurasolutions.safekiddo.auth.proto.LockScreenOperation.OnLockScreenResult;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations;
import com.ardurasolutions.safekiddo.browser.interfaces.OnAllMenuItemsGenerated;
import com.ardurasolutions.safekiddo.browser.interfaces.OnLogoutUser;
import com.ardurasolutions.safekiddo.browser.interfaces.OnProxyService;
import com.ardurasolutions.safekiddo.browser.interfaces.OnProxyService.CheckCallback;
import com.ardurasolutions.safekiddo.browser.interfaces.OnRequestUserAgnetChange;
import com.ardurasolutions.safekiddo.browser.interfaces.OnTabAction;
import com.ardurasolutions.safekiddo.browser.interfaces.OnTakeScreenShoot;
import com.ardurasolutions.safekiddo.browser.interfaces.ThreadCallback;
import com.ardurasolutions.safekiddo.browser.proto.BrowserIdentify;
import com.ardurasolutions.safekiddo.browser.proto.BrowserMenuItem;
import com.ardurasolutions.safekiddo.browser.proto.HistoryListAdapter;
import com.ardurasolutions.safekiddo.browser.proto.IntentUtils;
import com.ardurasolutions.safekiddo.browser.proto.SafeSearchHelper;
import com.ardurasolutions.safekiddo.browser.proto.UrlObserver;
import com.ardurasolutions.safekiddo.browser.proto.UrlObserver.OnUrlChange;
import com.ardurasolutions.safekiddo.browser.proto.WebChromeClientHv;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.WebFragmentItem;
import com.ardurasolutions.safekiddo.browser.proto.WebInterfaceForceSSLError;
import com.ardurasolutions.safekiddo.browser.proto.WebInterfaceForceSSLError.OnForceLoadClick;
import com.ardurasolutions.safekiddo.browser.proto.WebSessionItem;
import com.ardurasolutions.safekiddo.browser.proto.WebViewCookieCompat;
import com.ardurasolutions.safekiddo.browser.proto.WebViewHv;
import com.ardurasolutions.safekiddo.browser.proto.WebViewHv.WebViewState;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Config.KeyNames;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.ProxySystem;
import com.ardurasolutions.safekiddo.helpers.WSHelper;
import com.ardurasolutions.safekiddo.helpers.WSHelper.AsyncCheckUrl;
import com.ardurasolutions.safekiddo.helpers.WSHelper.CheckRedirectUrlResult;
import com.ardurasolutions.safekiddo.helpers.WSHelper.CheckResult;
import com.ardurasolutions.safekiddo.helpers.WSHelper.ErrorCode;
import com.ardurasolutions.safekiddo.helpers.WSHelper.UserActionType;
import com.ardurasolutions.safekiddo.proto.FragmentProto;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.ardurasolutions.safekiddo.proto.network.RequestError;
import com.ardurasolutions.safekiddo.proto.view.AutoCompleteTextViewHv;
import com.ardurasolutions.safekiddo.proto.view.CheckedTextViewHv;
import com.ardurasolutions.safekiddo.sql.BrowserLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.BrowserHistoryTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserFavs;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserHistory;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;
import com.hv.styleddialogs.TextDialog;

public class BrowserFrameFragment extends FragmentProto {
	
	private static final int REQUEST_FOR_FAVS = 121;
	private static final int REQUEST_FOR_SETTINGS = 122;
	
	private static final String CMD_HISTORY = "//history";
	private static final String PAGE_BLANK = "about:blank";
	private static final String PAGE_DATA = "data:text/html";
	
	//private static final int BM_ITEM_BACK = 0;
	//private static final int BM_ITEM_NEXT = 1;
	private static final int BM_ITEM_NEW_TAB = 0;
	private static final int BM_ITEM_SHARE = 1;
	private static final int BM_ITEM_ADD_FAV = 2;
	private static final int BM_ITEM_FAVS = 3;
	private static final int BM_ITEM_HISTORY = 4;
	private static final int BM_ITEM_SETTINGS = 5;
	
	private static final String URL_YOUTUBE = ".youtube.com";
	
	private AutoCompleteTextViewHv urlText;
	private WebViewHv webView, webViewContent;
	private SimpleCursorAdapter suggestAdapter;
	private BrowserHistoryTable mBrowserHistoryTable;
	private PopupWindow menu;
	private ListView popupList;
	private ArrayList<BrowserMenuItem> items;
	private ArrayAdapter<BrowserMenuItem> la;
	private OnAllMenuItemsGenerated mOnAllMenuItemsGenerated;
	private String lastUrlLoad = "";
	private Config prefs;
	private View webViewLoadOverlay;
	
	private WebViewState mWebViewState = WebViewState.NO_URL;
	private View masterView, toolbarProgressView;
	
	private StickyListHeadersListView historyList;
	private HistoryListAdapter LA;
	private View historyListBox;
	
	private View loadProgress, titleIcon, actionBack;
	private ImageView actionStopRefresh;
	private TextView titleText;
	private View webViewOverlay;
	private RelativeLayout mainView;
	private FrameLayout fullscreenCustomContent;
	private WebChromeClientHv mWebChromeClientHv;
	private String sslErrorPage = null;
	private WebViewClient mWebViewClient;
	private Bundle restoredState = null;
	
	private String urlToLoadAfterInit;
	private String lastBlockUrl = "", lastTryLoadUrl = "", lastCheckUrl = "";
	
	private OnProxyService mOnProxyService;
	private OnLogoutUser mOnLogoutUser;
	private UrlObserver mUrlObserver;
	private WebInterfaceForceSSLError mWebInterfaceForceSSLError;
	
	private String urlToWebViewContent = "";
	private boolean skipUrlChangeOnPageFinish = false;
	private boolean clearHistoryOnFinish = false;
	
	private BroadcastReceiver mLocalProxyPortRecevier;
	private ArrayList<Runnable> execAfterAttach = new ArrayList<Runnable>();
	
	private WebFragmentItem mWebFragmentItem;
	private OnTabAction mOnTabAction;
	private OnRequestUserAgnetChange mOnRequestUserAgnetChange;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		masterView = inflater.inflate(R.layout.fragment_browser_frame, container, false);
		
		webView = (WebViewHv) masterView.findViewById(R.id.webView);
		webViewContent = (WebViewHv) masterView.findViewById(R.id.webViewContent);
		historyListBox = masterView.findViewById(R.id.historyListBox);
		urlText = (AutoCompleteTextViewHv) masterView.findViewById(R.id.urlText);
		historyList = (StickyListHeadersListView) masterView.findViewById(R.id.historyList);
		loadProgress = masterView.findViewById(R.id.loadProgress);
		titleIcon = masterView.findViewById(R.id.titleIcon);
		titleText = (TextView) masterView.findViewById(R.id.titleText);
		actionBack = masterView.findViewById(R.id.actionBack);
		actionStopRefresh = (ImageView) masterView.findViewById(R.id.actionStopRefresh);
		webViewOverlay = masterView.findViewById(R.id.webViewOverlay);
		//actionMenuDivider = masterView.findViewById(R.id.actionMenuDivider);
		mainView = (RelativeLayout) masterView.findViewById(R.id.mainView);
		fullscreenCustomContent = (FrameLayout) masterView.findViewById(R.id.fullscreenCustomContent);
		webViewLoadOverlay = masterView.findViewById(R.id.webViewLoadOverlay);
		toolbarProgressView = masterView.findViewById(R.id.toolbarProgressView);
		
		actionBack.setVisibility(View.GONE) ;//CommonUtils.isTablet(getActivity()) ? View.VISIBLE : View.GONE);
		masterView.findViewById(R.id.titleBar).setVisibility(CommonUtils.isTablet(getActivity()) ? View.VISIBLE : View.GONE);
		
		webViewOverlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getWebViewUrl() != null && !getWebViewUrl().startsWith(PAGE_DATA) && !isContentWebViewVisible())
					setUrlAdressBar(getWebViewUrl());
				webView.requestFocus();
				CommonUtils.hideKeyboard(getActivity(), urlText);
				onKeyboardShowHideEvent(false);
			}
		});
		
		actionStopRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (historyListBox.getVisibility() == View.VISIBLE) {
					LA.changeCursor(mBrowserHistoryTable.getItems());
				} else {
					handleRefresh();
				}
			}
		});
		
		actionBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleBackAction();
			}
		});
		masterView.findViewById(R.id.buttonCLearHistory).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TextDialog d = new TextDialog();
				d.setTitle(getResources().getString(R.string.dialog_clear_history_title));
				d.setText(getResources().getString(R.string.dialog_clear_history_msg));
				d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						lastTryLoadUrl = "";
						clearHistoryOnFinish = true;
						cancelAll();
						webView.stopLoading();
						webView.clearHistory();
						loadHomePage();
						
						mBrowserHistoryTable.deleteAll();
						LA.changeCursor(mBrowserHistoryTable.getItems());
						
						hideContentWebView();
						webViewLoadOverlay.setVisibility(View.GONE);
					}
				});
				d.setNegativeButton(R.string.label_cancel, null);
				d.show(getFragmentManager(), "d0");
			}
		});
		
		LA = new HistoryListAdapter(inflater.getContext());
		
		historyList.setAdapter(LA);
		historyList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor c = LA.getCursor();
				if (c != null) {
					c.moveToPosition(position);
					BrowserHistory bh = DBUtils.currToObj(c, BrowserHistory.class);
					
					historyListBox.setVisibility(View.GONE);
					CommonUtils.hideKeyboard(getActivity(), urlText);
					cancelAll();
					webView.stopLoading();
					setUrlAdressBar(bh.browser_history_url);
					loadUrlWebViewWithCheck(bh.browser_history_url, true);
				}
			}
		});
		
		final View actionMenu = masterView.findViewById(R.id.bottomToolbarMore);
		
		masterView.findViewById(R.id.bottomToolbarBack).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleBackAction();
			}
		});
		masterView.findViewById(R.id.bottomToolbarNext).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleForwardAction();
			}
		});
		masterView.findViewById(R.id.bottomToolbarFavsAdd).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!PAGE_BLANK.equals(getWebViewUrl())) {
					BrowserFavsFormDialog d = new BrowserFavsFormDialog()
						.setFavLabel(webView.getTitle())
						.setFavUrl(getWebViewUrl());
					d.show(getFragmentManager(), "favs");
				}
			}
		});
		masterView.findViewById(R.id.bottomToolbarWindows).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnTabAction != null)
					mOnTabAction.onShowTabList();
			}
		});

		View popupView = inflater.inflate(R.layout.popup_browser_menu, (ViewGroup) masterView, false);
		popupList = (ListView) popupView.findViewById(R.id.popupList);
		
		items = new ArrayList<BrowserMenuItem>();
		
		items.add(new BrowserMenuItem(getResources().getString(R.string.label_open_page), R.drawable.ic_browser_window_plus_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnTabAction != null)
					mOnTabAction.onNewTabClick();
			}
		}, BM_ITEM_NEW_TAB));

		items.add(new BrowserMenuItem(getResources().getString(R.string.label_share), R.drawable.ic_action_share_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = getWebViewUrl();
				String title = webView.getTitle();
				if (title == null)
					title = url.replace("http://", "").replace("https://", "");
				
				if (url != null && url.trim().length() > 0 && !PAGE_BLANK.equals(url) && !url.startsWith(PAGE_DATA)) {
					Intent sendIntent = new Intent();
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_TEXT, url + "\n\n" + getResources().getString(R.string.label_share_extra));
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
					sendIntent.setType("text/plain");
					startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_share)));
				}
			}
		}, BM_ITEM_SHARE));

		items.add(new BrowserMenuItem(getResources().getString(R.string.label_add_to_favs), R.drawable.ic_action_star_add_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!PAGE_BLANK.equals(getWebViewUrl())) {
					BrowserFavsFormDialog d = new BrowserFavsFormDialog()
						.setFavLabel(webView.getTitle())
						.setFavUrl(getWebViewUrl());
					d.show(getFragmentManager(), "favs");
				}
			}
		}, BM_ITEM_ADD_FAV));
		items.add(new BrowserMenuItem(getResources().getString(R.string.label_favs), R.drawable.ic_action_star_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), BrowserFavsActivity.class), REQUEST_FOR_FAVS);
			}
		}, BM_ITEM_FAVS));
		items.add(new BrowserMenuItem(getResources().getString(R.string.label_history), R.drawable.ic_action_history_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				lastTryLoadUrl = urlText.getText().toString();
				loadUrlWebView(CMD_HISTORY);
			}
		}, BM_ITEM_HISTORY));
		
		items.add(new BrowserMenuItem(getResources().getString(R.string.label_settings_browser), R.drawable.ic_action_settings_full, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getActivity(), BrowserSettings.class), REQUEST_FOR_SETTINGS);
			}
		}, BM_ITEM_SETTINGS));
		
		la = new ArrayAdapter<BrowserMenuItem>(inflater.getContext(), R.layout.item_broswer_menu, items) {
			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				final CheckedTextViewHv v = (CheckedTextViewHv) super.getView(position, convertView, parent);
				final BrowserMenuItem item = items.get(position);
				v.setText(item.getLabel());
				
				v.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), 0, 0, 0);
				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View vx) {
						if (item.getOnClick() != null)
							item.getOnClick().onClick(v);
						menu.dismiss();
					}
				});
				v.setTag(Integer.toString(position));
				item.setView(v);
				if (mOnAllMenuItemsGenerated != null && position == items.size()-1) {
					mOnAllMenuItemsGenerated.onAllItemsGenerated();
				}
				return v;
			}
			
		};
		popupList.setAdapter(la);
		mOnAllMenuItemsGenerated = new OnAllMenuItemsGenerated() {
			@Override
			public void onAllItemsGenerated() {
				//setmenuItemState(BM_ITEM_BACK, webView.canGoBack());
				//setmenuItemState(BM_ITEM_NEXT, webView.canGoForward());
				setmenuItemState(BM_ITEM_ADD_FAV, getWebViewUrl() != null || (getWebViewUrl() != null && getWebViewUrl().trim().length() > 0));
				updateShareMenuItem();
			}
		};
		
		menu = new PopupWindow(popupView, getResources().getDimensionPixelSize(R.dimen.menu_width), LayoutParams.WRAP_CONTENT);
		menu.setTouchable(true);
		menu.setOutsideTouchable(true);
		menu.getContentView().setFocusableInTouchMode(true);
		menu.getContentView().setOnKeyListener(new View.OnKeyListener() {        
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
					menu.dismiss();
					return true;
				}
				return false;
			}
		});
		/*menu.setBackgroundDrawable(hasHardwaremenu ? 
				getResources().getDrawable(android.support.v7.appcompat.R.drawable.abc_menu_hardkey_panel_holo_light) :
				getResources().getDrawable(android.support.v7.appcompat.R.drawable.abc_menu_dropdown_panel_holo_light));*/
		//menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.abc_menu_dropdown_panel_holo_light));
		menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.abc_popup_background_mtrl_mult));

		actionMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateShareMenuItem();
				if (menu.isShowing()) {
					menu.dismiss();
				} else {
					menu.showAsDropDown(v, 0, -1);
					menu.setFocusable(true);
					menu.update();
				}
			}
		});
		
		return masterView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (Activity.RESULT_OK == resultCode) {
			
			switch(requestCode) {
				case REQUEST_FOR_FAVS:
					BrowserFavs bf = BrowserFavs.readFromIntent(data, BrowserFavs.class);
					if (bf.browser_favs_url != null && bf.browser_favs_url.length() > 0) {
						loadUrlWebViewWithCheck(bf.browser_favs_url, true);
					}
				break;
				case REQUEST_FOR_SETTINGS:
					if (data.getBooleanExtra(BrowserSettings.KEY_WEBVIEW_CACHE_CLEAR, false)) {
						webView.clearCache(true);
					}
					if (data.getBooleanExtra(BrowserSettings.KEY_WEBVIEW_HISTORY_CLEAR, false)) {
						webView.clearHistory();
						mBrowserHistoryTable.clearData();
						loadHomePage();
						setUrlAdressBar("");
					}
					if (data.hasExtra(BrowserSettings.KEY_WEBVIEW_USER_AGNET_CHANGE) && mOnRequestUserAgnetChange != null) {
						BrowserIdentify bi = BrowserIdentify.fromInt(data.getIntExtra(BrowserSettings.KEY_WEBVIEW_USER_AGNET_CHANGE, BrowserIdentify.IDENTIFY_ANDROID.getValue()));
						mOnRequestUserAgnetChange.onRequestUserAgnetChange(bi);
					}
				break;
			}
		}
	}
	
	private void updateShareMenuItem() {
		String url = getWebViewUrl();
		setmenuItemState(BM_ITEM_SHARE, url != null && url.trim().length() > 0 && !PAGE_BLANK.equals(url) && !url.startsWith(PAGE_DATA));
	}
	
	private void showProgress(boolean inProgress) {
		loadProgress.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
		titleIcon.setVisibility(!inProgress ? View.VISIBLE : View.INVISIBLE);
		actionStopRefresh.setImageResource(inProgress ? R.drawable.ic_action_x_light : R.drawable.ic_action_refresh_light);
		if (getActivity() != null && !CommonUtils.isTablet(getActivity()))
			toolbarProgressView.setVisibility(inProgress ? View.VISIBLE : View.GONE);
	}
	
	private void setmenuItemState(int itemId, boolean isEnabled) {
		View v = popupList.findViewWithTag(Integer.toString(itemId));
		if (v != null) {
			v.setEnabled(isEnabled);
		}
	}
	
	public void showMenu() {
		updateShareMenuItem();
		if (menu.isShowing()) {
			menu.dismiss();
		} else {
			//menu.showAtLocation(masterView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			menu.showAsDropDown(masterView.findViewById(R.id.bottomToolbarMore), 0, -1);
			menu.setFocusable(true);
			menu.update();
		}
	}
	
	public Bundle getWebViewState() {
		if (webView != null) {
			Bundle res = new Bundle();
			webView.saveState(res);
			if (lastCheckUrl.length() > 0)
				res.putBoolean(WebSessionItem.KEY_FORDE_RELOAD, true);
			return res;
		} else
			return null;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		prefs = Config.getInstance(activity);
		BrowserLocalSQL sql = BrowserLocalSQL.getInstance(activity);
		mBrowserHistoryTable = sql.getTable(BrowserHistoryTable.class);
		
		try {
			mOnProxyService = (OnProxyService) activity;
		} catch (ClassCastException e) {}
		
		try {
			mOnLogoutUser = (OnLogoutUser) activity;
		} catch (ClassCastException e) {}
		
		try {
			mOnTabAction = (OnTabAction) activity;
		} catch (ClassCastException e) {}
		
		try {
			mOnRequestUserAgnetChange = (OnRequestUserAgnetChange) activity;
		} catch (ClassCastException e) {}
		
		/*
		 * sprawdzenie czy jest coś do wykonania po tym jak fragment jest już prawidłowo dodany do widoku activity
		 */
		if (execAfterAttach.size() > 0) {
			final Handler h = new Handler();
			for(Runnable r : execAfterAttach) {
				h.post(r);
			}
			execAfterAttach.clear();
		}
	}
	
	@SuppressLint({ "NewApi", "SetJavaScriptEnabled" })
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mLocalProxyPortRecevier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled()) 
					Console.logi("PROXY port changed... setup webview");
				CommonUtils.setupBrowserProxy(webView);
			}
		};
		
		CommonUtils.setupBrowserProxy(webView);
		//CommonUtils.checkAndSetupProxyServerAsync(getActivity());
		
		suggestAdapter = new SimpleCursorAdapter(getActivity(), R.layout.item_browser_suggest, null, new String[]{}, new int[]{}, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
			@Override
			public void bindView(View view, Context ctx, Cursor cursor) {
				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				BrowserHistory mBrowserHistory = DBUtils.currToObj(cursor, BrowserHistory.class);
				text1.setText(mBrowserHistory.browser_history_label);
				text2.setText(mBrowserHistory.browser_history_url);
				text2.setMaxLines(2);
				text2.setEllipsize(TextUtils.TruncateAt.END);
			}
		};
		suggestAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor c) {
				BrowserHistory bs = DBUtils.currToObj(c, BrowserHistory.class);
				return bs.browser_history_url;
			}
		});
		suggestAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence f) {
				return mBrowserHistoryTable.getSuggest(f != null ? f.toString() : "");
			}
		});
		
		urlText.setRightIcon(R.drawable.ic_action_remove_small);
		urlText.setOnRightIconClick(new AutoCompleteTextViewHv.OnRightIconClick() {
			@Override
			public void onIconClick() {
				setUrlAdressBar("");
				urlText.requestFocus();
			}
		});
		urlText.setOnKeyboardEvent(new AutoCompleteTextViewHv.OnKeyboardEvent() {
			@Override
			public void onKeyboardEvent(boolean isShowing, boolean fromPreIme) {
				onKeyboardShowHideEvent(isShowing);
			}
		});
		urlText.setOnKeyboardBack(new AutoCompleteTextViewHv.OnKeyboardBack() {
			@Override
			public void onKeyboardBack() {
				webView.requestFocus();
			}
		});
		urlText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_DONE) {
					historyListBox.setVisibility(View.GONE);
					mWebViewState = WebViewState.NO_URL;
					urlText.setText(urlText.getText().toString().trim());
					//urlText.copyTextToDisplayText();
					
					handleRefresh();
				}
				return false;
			}
		});
		urlText.setAdapter(suggestAdapter);
		urlText.setThreshold(2);
		urlText.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				cancelAll();
				webView.stopLoading();
				
				loadUrlWebViewWithCheck(urlText.getText().toString(), true);
				webView.requestFocus();
				CommonUtils.hideKeyboard(getActivity(), urlText);
			}
		});
		urlText.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		urlText.setDropDownHeight(ViewGroup.LayoutParams.MATCH_PARENT);
		urlText.setDropDownAnchor(R.id.toolbar);
		urlText.setDropDownBackgroundResource(R.drawable.white_bg);
		urlText.setDropDownVerticalOffset((int) (1f * getResources().getDisplayMetrics().density));
		urlText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
//				if (!hasFocus) {
//					if (urlText.getDisplayText().length() > 0)
//						urlText.setText(urlText.getDisplayText());
//				}
				/*
				String url = getWebViewUrl();
				if (url != null && url.length() > 0) {
					if (lastTryLoadUrl.length() > 0)
						setUrlAdressBar(lastTryLoadUrl);
					else
						setUrlAdressBar(url);
				} else {
					if (lastBlockUrl.length() > 0) {
						setUrlAdressBar(lastBlockUrl);
					} else if (lastTryLoadUrl.length() > 0) {
						setUrlAdressBar(lastTryLoadUrl);
					} else if (lastCheckUrl.length() > 0) {
						setUrlAdressBar(lastCheckUrl);
					}
				}
				*/
			}
		});
		
		mWebViewClient = new WebViewClient() {
			
			@Override
			public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
				if (Console.isEnabled())
					Console.loge("SSL ERROR: " + error);
				
				urlText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_lock4), null, urlText.getCompoundDrawables()[2], null);
				
				if (webView.isForceLoadSSL(parseUrl(error.getUrl()))) {
					handler.proceed();
				} else {
					handler.cancel();
				
					if (error.getUrl().equals(view.getUrl())) {
						// TODO error.getUrl() - od api 14
						sslErrorPage = parseUrl(view.getUrl()); // TODO dokończyć
						hideOverlayAfterLoad = false;
						showContentWebViewAssets("html/ssl_error.html");
					}
				}
			}
			
			@Override
			public void doUpdateVisitedHistory(WebView view, final String url2, boolean isReload) {
				final String trueUrl = Build.VERSION.SDK_INT < 19 ? view.getUrl() : url2;
				mUrlObserver.updateUrl(parseUrl(trueUrl), "doUpdateVisitedHistory"); //webView.getUrl()
				if (mWebFragmentItem != null)
					mWebFragmentItem.setUrl(trueUrl);
			}
			
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, final String url) {
				if (url.startsWith("about:")) {
					return super.shouldOverrideUrlLoading(view, url);
				}
				if (url.contains("mailto:")) {
					MailTo mailTo = MailTo.parse(url);
					Intent i = CommonUtils.emailIntent(getActivity(), mailTo.getTo(), mailTo.getSubject(), mailTo.getBody(), mailTo.getCc());
					getActivity().startActivity(i);
					view.reload();
					return true;
				} else if (url.startsWith("intent://")) {
					Intent intent = null;
					try {
						intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
					} catch (URISyntaxException ex) {
						return false;
					}
					if (intent != null) {
						try {
							getActivity().startActivity(intent);
						} catch (ActivityNotFoundException e) {
							BugSenseHandler.sendExceptionMessage("WEBVIEW", "shouldOverrideUrlLoading", e);
							if (Console.isEnabled())
								Console.loge("WEBVIEW::shouldOverrideUrlLoading", e);
						}
						return true;
					}
				}
				
				boolean hasOtherApp = new IntentUtils(getActivity()).startActivityForUrl(webView, url);
				if (!hasOtherApp) {
					loadUrlWebViewWithCheck(parseUrl(url), false);
				}
				return true;
			}
			
			@Override
			public void onReceivedHttpAuthRequest(WebView view, final android.webkit.HttpAuthHandler handler, String host, String realm) {
				BrowserHttpAuthDialog dialog = new BrowserHttpAuthDialog();
				dialog.setOnAuthEvent(new BrowserHttpAuthDialog.OnAuthEvent() {
					@Override
					public void onAuthProcess(String login, String pass) {
						handler.proceed(login, pass);
					}
					@Override
					public void onAuthCancel() {
						handler.cancel();
					}
				});
				dialog.show(getFragmentManager(), "auth");
			}
			
			@Override
			public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
				
			}
			
			@Override
			public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
				resend.sendToTarget();
			}
			@Override
			public void onPageStarted(WebView view, final String url, Bitmap favicon) {
				// url został już sprawdzony więc można go wyzerować
				lastCheckUrl = "";
				
				if (mWebFragmentItem != null)
					mWebFragmentItem.setUrl(url);
				
				mUrlObserver.updateUrl(parseUrl(url), "onPageStarted");
				mWebViewState = WebViewState.LOADING;
				urlText.dismissDropDown();
				
				// pomijamy pokazywanie loadera przy ładowaniu strony startowej
				if (!url.equals(PAGE_BLANK)) {
					showProgress(true);
					actionStopRefresh.setImageResource(R.drawable.ic_action_x_light);
				}
				
				int drawable = url.startsWith("https://") ? (webView.isForceLoadSSL(parseUrl(url)) ? R.drawable.ic_lock4 : R.drawable.ic_world_url_2a) : R.drawable.ic_world_url_2a;
				urlText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawable), null, urlText.getCompoundDrawables()[2], null);
			}
			
			@Override
			public void onPageFinished(final WebView view, final String url) {
				
				if (mWebFragmentItem != null)
					mWebFragmentItem.setUrl(url);
				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (url.equals(PAGE_BLANK)) {
							if (mWebFragmentItem != null) {
								mWebFragmentItem.setTitle(null); //getResources().getString(R.string.label_empty_page));
								mWebFragmentItem.setThumb(null);
							}
						} else {
							webView.takeScreenShoot();
							webView.postDelayed(new Runnable() {
								@Override
								public void run() {
									// może byc tak że ktoś zamknie okno przeglądarki i webview będzie nie ustawione (null)
									if (webView != null)
										webView.takeScreenShoot();
								}
							}, 1000);
						}
					}
				});
				
				
				if (!skipUrlChangeOnPageFinish)
					mUrlObserver.updateUrl(getWebViewUrl(), "onPageFinished");
				
				skipUrlChangeOnPageFinish = false;
				
				//setmenuItemState(BM_ITEM_BACK, webView.canGoBack());
				//setmenuItemState(BM_ITEM_NEXT, webView.canGoForward());
				
				// INFO na potrzeby starszego androida, dopiero po zainicjowaniu pustej strony "wskakuje" proxy 
				if (url.toLowerCase(Locale.getDefault()).equals(PAGE_BLANK)) {
					CommonUtils.setupBrowserProxy(webView);
					if (clearHistoryOnFinish) {
						clearHistoryOnFinish = false;
						webView.clearHistory();
					}
				} else {
					urlText.dismissDropDown();
				}
				
				showProgress(false);
				actionStopRefresh.setImageResource(R.drawable.ic_action_refresh_light);
				mWebViewState = WebViewState.LOADED;
				setPageTitle(view.getTitle());
				
				if (hideOverlayAfterLoad && !parseUrl(url).equals(lastBlockUrl) && !parseUrl(url).equals(sslErrorPage)) {
					hideOverlayAfterLoad = false;
					hideAllContentOvelays(true);
				}
				
				if (sslErrorPage != null) {
					sslErrorPage = null;
					urlText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_lock4), null, urlText.getCompoundDrawables()[2], null);
				} else {
					int drawable = url.startsWith("https://") ? (webView.isForceLoadSSL(parseUrl(url)) ? R.drawable.ic_lock4 : R.drawable.ic_lock3) : R.drawable.ic_world_url_2a;
					urlText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawable), null, urlText.getCompoundDrawables()[2], null);
				}
				
				//webView.setForceLoadSSL(null);
				
				if (url.equals(lastUrlLoad)) {
					return;
				}
				
				lastUrlLoad = url;
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				if (Console.isEnabled())
					Console.loge("errorCode=" + errorCode + ", description=" + description + ", failingUrl=" + failingUrl);
				
				if (failingUrl.equals(view.getUrl()))
					showContentWebViewAssetsCustom(getResources().getString(R.string.label_browser_error_load_page, errorCode));
			}
		};
		
		webView.setWebViewClient(mWebViewClient);
		webView.setOnTakeScreenShoot(new OnTakeScreenShoot() {
			@Override
			public void onTakeScreenShoot(Bitmap bmp) {
				if (mWebFragmentItem != null)
					mWebFragmentItem.setThumb(bmp);
			}
		});
		webView.setBrowserIdentify(BrowserIdentify.fromInt(prefs.load(KeyNames.USER_BROWSER_IDENTYFICATION, BrowserIdentify.IDENTIFY_ANDROID.getValue())));
		
		mWebChromeClientHv = new WebChromeClientHv(mainView, fullscreenCustomContent) {
			@Override 
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (mWebFragmentItem != null)
					mWebFragmentItem.setTitle(title);
				String url = parseUrl(view.getUrl());
				if (!url.equals(lastBlockUrl))
					setPageTitle(title);
				if (url != null && !url.equals(PAGE_BLANK))
					mBrowserHistoryTable.addEntry(url, title, true);
			}
			@Override 
			public Context getContext() {
				return getActivity();
			}
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				/*if (newProgress > 90) {
					if (hideOverlayAfterLoad && !parseUrl(view.getUrl()).equals(lastBlockUrl) && !parseUrl(view.getUrl()).equals(sslErrorPage)) {
						hideOverlayAfterLoad = false;
						hideAllContentOvelays(true);
					}
				}*/
			}
		};
		webView.setWebChromeClient(mWebChromeClientHv);
		registerForContextMenu(webView);
		registerForContextMenu(historyList);
		
		webViewContent.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished (WebView view, String url) {
				if (mWebViewState != WebViewState.LOADING)
					showProgress(false);
				
				if (mWebFragmentItem != null) {
					String parsedUrl = parseUrl(urlText.getText().toString());
					if (!CommonUtils.isOnline(getActivity())) {
						
						if (parsedUrl.length() > 0) {
							mWebFragmentItem.setTitle(parsedUrl);
							mWebFragmentItem.setThumb(null);
							mWebFragmentItem.setUrl(parsedUrl);
						} else {
							mWebFragmentItem.setTitle(getResources().getString(R.string.label_empty_page));
							mWebFragmentItem.setThumb(null);
							mWebFragmentItem.setUrl(PAGE_BLANK);
						}
						
					} else {
					
						if (!(url.equals("data:text/html; charset=UTF-8,") || url.equals("data:text/html;%20charset=UTF-8,")) && mWebFragmentItem != null && (lastBlockUrl != null && lastBlockUrl.length() > 0)) { // <- blank page
							mWebFragmentItem.setTitle(getResources().getString(R.string.label_page_blocked) + " - " + com.ardurasolutions.safekiddo.helpers.TextUtils.getUrlDomain(urlText.getText().toString()));
							mWebFragmentItem.setThumb(BitmapFactory.decodeResource(getResources(), R.drawable.ghost));
							mWebFragmentItem.setUrl(parsedUrl);
						}
					
					}
				}
				
				// TODO - czy zapisywać cookie otrzymane od serwera?
				//android.webkit.CookieManager.getInstance().setCookie(
				//		Constants.getChildBlockUrl(getActivity()), 
				//		Constants.SESSION_COOKIE_NAME + "=" + Config.getInstance(getActivity()).load(Config.KeyNames.SESSION_ID)
				//	);
				//Console.logd("COOK: " + android.webkit.CookieManager.getInstance().getCookie(Constants.getChildBlockUrl(getActivity())));
			}
		});
		
		WebSettings webSettings = webViewContent.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebInterfaceForceSSLError = new WebInterfaceForceSSLError(webView);
		mWebInterfaceForceSSLError.setOnForceLoadClick(new OnForceLoadClick() {
			@Override
			public void onForceLoadClick() {
				hideContentWebView();
			}
		});
		webViewContent.addJavascriptInterface(mWebInterfaceForceSSLError, "SafeKiddo");
		
		if(BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			if (Config.getInstance(getActivity()).load(Config.KeyNames.DEV_BROWSER_CONTENT_DEBUG, false)) {
				WebView.setWebContentsDebuggingEnabled(true);
			}
		}
		
		if (mUrlObserver == null) {
			mUrlObserver = new UrlObserver(getWebViewUrl(), new OnUrlChange() {
				@Override
				public void onUrlChanged(final String url, String debugParam) {
					if (Console.isEnabled())
						Console.logd("URL CHANGE[" + debugParam + "]: " + (url != null ? (url.startsWith(PAGE_DATA) ? "PAGE DATA" : url) : "NULL"));
					
					if (url == null) return;
					
					if (mWebFragmentItem != null)
						mWebFragmentItem.setUrl(url);
					
					setupYoutubeCookie(url);
					
					//setmenuItemState(BM_ITEM_BACK, webView.canGoBack());
					//setmenuItemState(BM_ITEM_NEXT, webView.canGoForward());
					
					if (!url.startsWith(PAGE_DATA)) {
						if (!url.equals(PAGE_BLANK)) {
							setUrlAdressBar(url);
						} else {
							if (!CommonUtils.isOnline(getActivity()))
								setUrlAdressBar(url);
						}
					} else {
						return;
					}
					
					SafeSearchHelper.SafeSearchCheckResult mSafeSearchCheckResult = SafeSearchHelper.checkSafeSearchIsOk(url);
					if (mSafeSearchCheckResult.isChanaged()) {
						webView.stopLoading();
						webView.loadUrl(mSafeSearchCheckResult.getNewUrl());
						return;
					}
					
					boolean goToCheck = !Constants.getBlockUrl().equals(url) && !url.toLowerCase(Locale.getDefault()).equals(PAGE_BLANK) && !url.startsWith(PAGE_DATA);
					
					if (goToCheck) {
						mBrowserHistoryTable.addEntry(url, null, false);
						loadBlockedPage(url);
					}
					
				}
			});
		}
		
		
		if (getUrlToLoadAfterInit() != null && !getUrlToLoadAfterInit().equals(PAGE_BLANK)) {
			setUrlAdressBar(parseUrl(getUrlToLoadAfterInit()));
			setPageTitle(parseUrl(getUrlToLoadAfterInit()));
			showProgress(true);
		}
		if (getWebFragmentItem() != null) {
			getWebFragmentItem().setUrl(getUrlToLoadAfterInit());
		}
		
		//CookieSyncManager.createInstance(webView.getContext());
		WebViewCookieCompat.getInstance().createInstance(webView.getContext());
		setupYoutubeCookie(URL_YOUTUBE);
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		if (v == webView) {
			WebView.HitTestResult hitTestResult = webView.getHitTestResult();
			
			if (Console.isEnabled())
				Console.logd("HIT TEST TYPE: " + hitTestResult.getType() + ", EXTRA: " + hitTestResult.getExtra());
			
			final String extra = hitTestResult.getExtra();
			switch(hitTestResult.getType()) {
				case WebView.HitTestResult.SRC_ANCHOR_TYPE:
					menu.add(R.string.label_open_in_new_tab).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							if (mOnTabAction != null)
								mOnTabAction.onNewTabUrlClick(extra);
							return false;
						}
					});
					if (android.os.Build.VERSION.SDK_INT >= 11) {
						menu.add(R.string.label_copy_link_address).setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
								clipboard.setPrimaryClip(ClipData.newPlainText("SafeKiddo", extra));
								return false;
							}
						});
					} else {
						// TODO
					}
				break;
				case WebView.HitTestResult.IMAGE_TYPE:
					menu.add(R.string.label_save_image).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							DownloadManager dm = (DownloadManager) getActivity().getSystemService(Activity.DOWNLOAD_SERVICE);
							DownloadManager.Request request = new DownloadManager.Request(Uri.parse(extra));
							if (android.os.Build.VERSION.SDK_INT >= 11) {
								request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							}
							dm.enqueue(request);
							return false;
						}
					});
					menu.add(R.string.label_open_in_new_tab).setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							if (mOnTabAction != null)
								mOnTabAction.onNewTabUrlClick(extra);
							return false;
						}
					});
					if (android.os.Build.VERSION.SDK_INT >= 11) {
						menu.add(R.string.label_copy_image_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
							@Override
							public boolean onMenuItemClick(MenuItem item) {
								ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Activity.CLIPBOARD_SERVICE);
								clipboard.setPrimaryClip(ClipData.newPlainText("SafeKiddo", extra));
								return false;
							}
						});
					} else {
						// TODO
					}
				break;
				case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				break;
			}
		} else {
			
			final BrowserHistory bh = mBrowserHistoryTable.getRowById(((AdapterView.AdapterContextMenuInfo)menuInfo).id, BrowserHistory.class);
			
			menu.add(R.string.label_open_in_new_tab).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (mOnTabAction != null)
						mOnTabAction.onNewTabUrlClick(bh.browser_history_url);
					return false;
				}
			});
			menu.add(R.string.label_details).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					BrowserHistoryItemDetails mBrowserHistoryItemDetails = new BrowserHistoryItemDetails();
					mBrowserHistoryItemDetails.setBrowserHistory(bh);
					mBrowserHistoryItemDetails.setTitle(getResources().getString(R.string.label_details));
					mBrowserHistoryItemDetails.setPositiveButton(R.string.label_ok);
					mBrowserHistoryItemDetails.show(getFragmentManager(), "hdetails");
					return false;
				}
			});
			menu.add(R.string.label_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					TextDialog tx = new TextDialog();
					tx.setTitle(getResources().getString(R.string.label_delete));
					tx.setText(getResources().getString(R.string.label_confirm_delete_history));
					tx.setNegativeButton(R.string.label_cancel);
					tx.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mBrowserHistoryTable.deleteEntry(bh._id);
							LA.changeCursor(mBrowserHistoryTable.getItems());
							dialog.dismiss();
						}
					});
					tx.show(getFragmentManager(), "hdel");
					return false;
				}
			});
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	private void setupYoutubeCookie(String url) {
		if (url.contains(URL_YOUTUBE)) {
			String cookieString = WebViewCookieCompat.getInstance().getCookie(url);
			//String cookieString = android.webkit.CookieManager.getInstance().getCookie(url);
			if (cookieString != null) {
				String[] cookies = cookieString.split(";");
				String outCookie = "";
				for(String c : cookies) {
					String cc = c.trim();
					if (cc.startsWith("PREF")) {
						if (cc.contains("f2=")) {
							if (!cc.contains("f2=8000000")) {
								cc += "&f2=8000000";
							} else {
								String[] ccc = cc.split("&");
								for(String cx : ccc) {
									String[] nvpar = cx.split("=");
									if (nvpar[0].equals("f2")) {
										cx = "f2=8000000";
									}
								}
								cc = com.ardurasolutions.safekiddo.helpers.TextUtils.join(ccc, "&");
							}
						} else {
							cc += "&f2=8000000";
						}
						c = cc;
					}
					c = cc;
					outCookie += c + "; ";
				}
				
				outCookie = outCookie.substring(0, outCookie.length() - 2);
				WebViewCookieCompat.getInstance()
					.setCookie(URL_YOUTUBE, outCookie)
					.sync();
				//android.webkit.CookieManager.getInstance().setCookie(URL_YOUTUBE, outCookie);
				//android.webkit.CookieSyncManager.getInstance().sync();
			} else {
				//android.webkit.CookieManager.getInstance().setCookie(URL_YOUTUBE, "PREF=f2=8000000");
				WebViewCookieCompat.getInstance()
					.setCookie(URL_YOUTUBE, "PREF=f2=8000000")
					.sync();
			}
		}
	}
	
	/**
	 * ustawia adres w pasku adresu, sprawdza czy strona jest pusta (blank) i ustawia co trzeba
	 * @param url
	 */
	private void setUrlAdressBar(String url) {
		
		if (historyListBox.getVisibility() == View.VISIBLE) {
			urlText.setText(CMD_HISTORY);
			return;
		}
		
		if (url == null || (url != null && url.trim().length() == 0)) {
			urlText.setText("");
			return;
		}
		if (PAGE_BLANK.equals(url.toLowerCase(Locale.getDefault()))) {
			urlText.setText("");
		} else {
			if (!url.startsWith(PAGE_DATA))
				urlText.setText(WSHelper.decodeUrl(url));
		}
	}
	
	/**
	 * ustawia tekst okna tytułowego
	 * @param title
	 */
	private void setPageTitle(String title) {
		if (title != null) {
			if (PAGE_BLANK.equals(title.toLowerCase(Locale.getDefault()))) {
				titleText.setText(R.string.browser_app_name);
			} else
				titleText.setText(title);
		}
	}
	
	/**
	 * ustawia tekst okna tytułowego
	 * @param title
	 */
	private void setPageTitle(int titleres) {
		if (isAdded()) {
			if (PAGE_BLANK.equals(getResources().getString(titleres))) {
				titleText.setText(R.string.browser_app_name);
			} else
				titleText.setText(titleres);
		} else {
			
		}
	}
	
	private Thread mCancelThread = null;
	public boolean cancelLoadBlockedPage() {
		if (mCancelThread != null && mCancelThread.isAlive()) {
			mCancelThread.interrupt();
			mCancelThread = null;
			return true;
		} else {
			mCancelThread = null;
			return false;
		}
	}
	
	private void loadBlockedPage(final String url) {
		loadBlockedPage(url, null);
	}
	
	/**
	 * load with UserActionType.LOG_REQUEST as default
	 * @param url
	 * @param tc
	 */
	private void loadBlockedPage(final String url, final ThreadCallback tc) {
		loadBlockedPage(url, tc, UserActionType.LOG_REQUEST);
	}
	
	private void loadBlockedPage(final String url, final ThreadCallback tc, final UserActionType uat) {
		
		// no internet connection
		if (!CommonUtils.isOnline(getActivity())) {
			if (url != null && url.equals(PAGE_BLANK)) {
				//loadHomePage();
				//hideContentWebView();
				setUrlAdressBar("");
			}
			showContentWebViewAssets("html/no_internet.html");
			return;
		}
		
		
		showProgress(true);
		
		if (Build.VERSION.SDK_INT > 10) {
			webViewContent.loadData("", "text/html; charset=UTF-8", "UTF-8");
		} else {
			webViewContent.loadDataWithBaseURL(url, "", "text/html", "utf-8", url);
		}
		
		//Console.logw("loadBlockedPage = " + url);
		final String ua = webView.getSettings().getUserAgentString();
		final android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
		
		mCancelThread = new Thread() {
			@Override
			public void run() {
				CheckRedirectUrlResult check = WSHelper.checkIsRedirecUrl(url, ua, cm);
				
				if (check.hasError() || check.getNewUrl() == null) {
					return;
				}
				
				final String newUrl = check.getNewUrl();
				final CheckResult mCheckResult = WSHelper.checkUrl(newUrl, uat, getActivity());
				
				if (Thread.currentThread().isInterrupted()) {
					return;
				}
				
				if (!mCheckResult.isSuccess()) {
					lastBlockUrl = parseUrl(newUrl);
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							webView.stopLoading();
						}
					});
					
					if (Thread.currentThread().isInterrupted()) {
						return;
					}
					
					UserOperations uo = new UserOperations();
					uo.addOperation(
						new LockScreenOperation(getActivity())
							.setUrl(newUrl)
							.setErrorCode(mCheckResult.getErrorCode())
							.setOnLockScreenResult(new OnLockScreenResult() {
								@Override
								public void onLockScreenResultHtml(final String html) {
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											String sesCookieValue = Config.getInstance(getActivity()).load(Config.KeyNames.SESSION_ID);
											WebViewCookieCompat.getInstance().setCookie(
												Constants.getBlockUrl(), 
												Constants.SESSION_COOKIE_NAME + "=" + sesCookieValue
											).sync();
											//android.webkit.CookieManager.getInstance().setCookie(
											//	Constants.getBlockUrl(), 
											//	Constants.SESSION_COOKIE_NAME + "=" + sesCookieValue
											//);
											//CookieSyncManager.getInstance().sync();
											//WebViewCookieCompat.getInstance();
											showContentWebView(html, newUrl);
										}
									});
								}
								@Override
								public void onLockScreenResultLogout() {
									if (Console.isEnabled())
										Console.logw("Logout");
									getActivity().runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (mOnLogoutUser != null)
												mOnLogoutUser.onLogoutUser();
										}
									});
									
								}
							})
							.setOnError(new OnError() {
								@Override
								public void onError(int errorCode, Object extraData) {
									if (Console.isEnabled())
										Console.logw("error=" + errorCode);
									showContentWebViewAssetsCustom(getResources().getString(R.string.label_browser_error_load_page, errorCode));
								}
							})
					);
					uo.execute();
					
				} else {
					//hideAllContentOvelays(true);
					if (tc != null)
						tc.onThreadCallback(newUrl);
				}
				
			}
		};
		mCancelThread.start();
	}
	
	private boolean isHideAnimationPlay = false;
	private void hideAllContentOvelays(boolean withFadeAnimation) {
		if (withFadeAnimation) {
			final View animView;
			
			if (webViewContent.getVisibility() == View.VISIBLE) {
				animView = webViewContent;
				webViewLoadOverlay.setVisibility(View.GONE);
			} else if (webViewLoadOverlay.getVisibility() == View.VISIBLE) {
				animView = webViewLoadOverlay;
			} else {
				animView = null;
			}
			
			if (animView != null && !isHideAnimationPlay) {
				isHideAnimationPlay = true;
				Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_layer);
				a.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						animView.setVisibility(View.GONE);
						animView.clearAnimation();
						isHideAnimationPlay = false;
					}
					@Override public void onAnimationRepeat(Animation animation) { }
				});
				animView.startAnimation(a);
			}
		} else {
			hideContentWebView();
			webViewLoadOverlay.setVisibility(View.GONE);
		}
	}
	
	/* /////////////////////////////
	 * obsługa WebView Content
	 */
	public void hideContentWebView() {
		showContentWebView("", "");
		webViewContent.setVisibility(View.GONE);
	}
	
	public void showContentWebViewAssetsCustom(String customText) {
		String body = "";
		try {
			body = CommonUtils.streamToString(getResources().getAssets().open("html/custom_error.html"), 1024 * 8);
			if (customText != null && customText.trim().length() > 0) {
				body = body.replace("{{ERROR}}", customText);
			}
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("showContentWebViewAssets[IO]: html/custom_error.html", e);
		}
		showContentWebView(body, null);
	}
	
	public void showContentWebViewAssets(String assetsFileName) {
		String body = "";
		try {
			body = CommonUtils.streamToString(getResources().getAssets().open(assetsFileName), 1024 * 8);
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("showContentWebViewAssets[IO]: " + assetsFileName, e);
		}
		showContentWebView(body, null);
	}
	
	public void showContentWebView(String c, String forUrl) {
		showProgress(true);
		webViewContent.setVisibility(View.VISIBLE);
		//webViewLoadOverlay.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT > 10) {
			webViewContent.loadData(c, "text/html; charset=UTF-8", "UTF-8");
		} else {
			webViewContent.loadDataWithBaseURL(forUrl, c, "text/html", "utf-8", forUrl);
		}
	}
	
	public boolean isContentWebViewVisible() {
		return webViewContent.getVisibility() == View.VISIBLE;
	}
	///////////////////////////
	
	private String parseUrl(String url) {
		return com.ardurasolutions.safekiddo.helpers.TextUtils.parseToNiceUrl(url);
	}
	
	private String getWebViewUrl() {
		return webView != null ? parseUrl(webView.getUrl()) : null;
	}
	
	public void loadUrlWebView(String u, boolean forceUpdateUrl) {
		loadUrlWebView(u, null, forceUpdateUrl);
	}
	
	public void loadUrlWebView(String u) {
		loadUrlWebView(u, null, false);
	}
	
	private AsyncCheckUrl loadUrlCheck = null;
	private boolean loadUrlCheckCancelled = false;
	
	public boolean cancelLoadUrlWithCheck() {
		boolean res = false;
		if (loadUrlCheck != null) {
			loadUrlCheck.cancelCheck();
			loadUrlCheck = null;
			res = true;
		}
		loadUrlCheckCancelled = true;
		return res;
	}
	
	/**
	 * sparwdz czy dany url jest prawidłowy<br>
	 * jeżlei jest nieprawidłowy to wyszukuje w google<br>
	 * dekoduje i enkodue url dla "ładnego" pokazania adresu
	 * @param u
	 * @return
	 */
	private String checkUrlValid(String u) {
		if (u == null) return null;
		if (u.trim().length() == 0) return u;
		String url = u.trim();
		
		if (url.equals(PAGE_BLANK)) {
			// blank page
		} else {
		
			if (!url.startsWith("http")) url = "http://" + url;
			
			// google safe=on
			if (url.startsWith("http://google.") || 
				url.startsWith("http://www.google.") ||
				url.startsWith("https://www.google.") || 
				url.startsWith("https://google.")) 
			{
				if (!url.contains("safe=on") && !url.contains("safe=active")) {
					if (url.contains("#")) {
						url += "&safe=active";
					} else {
						if (url.contains("?"))
							url += "&safe=on";
						else
							url += "?&safe=on";
					}
				}
			}
			
			url = WSHelper.encodeUrl(WSHelper.decodeUrl(url));
			
			UrlValidator defaultValidator = UrlValidator.getInstance();
			if (!defaultValidator.isValid(url)) {
				if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file://")) {
					url = url.substring(url.indexOf("://") + 3);
				}
				url = "https://www.google.pl/#q=" + url + "&safe=active";
			}
		}
		return url;
	}
	
	private void checkProxyService() {
		if (mOnProxyService != null) {
			if (!mOnProxyService.isProxyServiceConnectd()) {
				mOnProxyService.startProxyService();
			} else {
				mOnProxyService.isProxyServiceWorking(new CheckCallback() {
					@Override
					public void onCheckCallback(boolean result) {
						if (!result)
							mOnProxyService.startProxyService();
					}
				});
			}
		}
	}
	
	public void loadUrlWebViewWithCheck(final String urlToCheck, final boolean forceUpdateUrl) {
		
		checkProxyService();
		
		final String urlInit = parseUrl(urlToCheck);
		
		if (urlInit.startsWith("//") || urlInit.startsWith(PAGE_DATA) || urlInit.equals(PAGE_BLANK)) {
			loadUrlWebView(urlInit, forceUpdateUrl);
			lastBlockUrl = "";
			return;
		}
		
		if (urlInit.startsWith("file://")) {
			webView.loadUrl(urlInit);
			return;
		}
		
		// no internet connection
		if (!CommonUtils.isOnline(getActivity())) {
			showContentWebViewAssets("html/no_internet.html");
			return;
		}
		
		final String url = checkUrlValid(urlInit);
		
		showProgress(true);
		loadUrlCheckCancelled = false;
		
		int drawable = url.startsWith("https://") ? (webView.isForceLoadSSL(parseUrl(url)) ? R.drawable.ic_lock4 : R.drawable.ic_world_url_2a) : R.drawable.ic_world_url_2a;
		urlText.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawable), null, urlText.getCompoundDrawables()[2], null);
		
		loadUrlCheck = new AsyncCheckUrl(url, UserActionType.NO_LOG_REQUEST, getActivity())
			.setUserAgent(webView.getSettings().getUserAgentString())
			.setCookieManager(android.webkit.CookieManager.getInstance())
			.setOnStatusChanged(new AsyncCheckUrl.OnStatusChanged() {
				@Override
				public void onResult(ErrorCode result, final String newUrl) {
					loadUrlCheck = null;
					if (loadUrlCheckCancelled) return;
					
					if (result.isSuccess()) {
						lastBlockUrl = "";
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadUrlWebView(newUrl, forceUpdateUrl);
							}
						});
					} else {
						lastBlockUrl = url;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								webView.stopLoading();
								setPageTitle(R.string.label_page_blocked);
								if (forceUpdateUrl)
									mUrlObserver.updateUrlForce(newUrl, "loadUrlWebViewWithCheck");
								else
									mUrlObserver.updateUrl(newUrl, "loadUrlWebViewWithCheck");
							}
						});
					}
				}
				@Override
				public void onError(ErrorCode result) {
					loadUrlCheck = null;
					lastBlockUrl = "";
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showProgress(false);
							showContentWebViewAssets("html/error_load_block_content.html");
							setPageTitle(R.string.label_browser_error);
						}
					});
				}
				@Override
				public void onCancel() {
					loadUrlCheck = null;
					lastBlockUrl = "";
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showProgress(false);
						}
					});
				}
				@Override
				public void onUrlRedirected(final String newUrl) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setUrlAdressBar(parseUrl(newUrl));
						}
					});
				}
				@Override
				public void onCheckError(final RequestError error) {
					loadUrlCheck = null;
					lastBlockUrl = "";
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							lastCheckUrl = parseUrl(url);
							handleCheckError(lastCheckUrl, error);
						}
					});
				}
			})
			.start();
	}
	
	private void handleCheckError(final String url, final RequestError error) {
		
		showProgress(false);
		
		switch(error) {
			default:
			case UNKNOWN:
			case IO:
			case INTERRUPTED:
				showContentWebViewAssetsCustom(getResources().getString(R.string.label_browser_error_load_page, error.getValue()));
			break;
			case UNKNOWN_HOST:
				showContentWebViewAssetsCustom(getResources().getString(R.string.label_browser_not_found));
			break;
			case SSL:
				if (webView.isForceLoadSSL(url)) {
					mWebViewState = WebViewState.NO_URL;
					webView.loadUrl(url);
					//handleRefresh();
				} else {
					mWebInterfaceForceSSLError.setActivity(getActivity());
					mWebInterfaceForceSSLError.setUrl(url);
					// TODO add SSL error handle
					String body = "";
					try {
						body = CommonUtils.streamToString(getResources().getAssets().open("html/ssl_error_custom.html"), 1024 * 8);
					} catch (IOException e) {
						if (Console.isEnabled())
							Console.loge("showContentWebViewAssets[IO]: html/ssl_error_custom.html", e);
					}
					webViewContent.setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT > 10) {
						webViewContent.loadData(body, "text/html; charset=UTF-8", "UTF-8");
					} else {
						webViewContent.loadDataWithBaseURL(url, body, "text/html", "utf-8", url);
					}
				}
			break;
		}
		setPageTitle(R.string.label_browser_error);
		setUrlAdressBar(url);
	}
	
	public void loadUrlWebView(String u, HashMap<String, String> extraHeaders, boolean forceUpdateUrl) {
		
		checkProxyService();
		
		onKeyboardShowHideEvent(false);
		if (u.equals(CMD_HISTORY)) {
			historyListBox.setVisibility(View.VISIBLE);
			LA.changeCursor(mBrowserHistoryTable.getItems());
			actionStopRefresh.setImageResource(R.drawable.ic_action_refresh_light);
			setUrlAdressBar(CMD_HISTORY);
			return;
		}
		
		if (u != null && u.startsWith("//")) {
			// ignone load
			return;
		}
		
		if (u.equals(PAGE_BLANK)) {
			loadHomePage();
			return;
		}
		
		// no internet connection
		if (getActivity() != null && !CommonUtils.isOnline(getActivity())) {
			showContentWebViewAssets("html/no_internet.html");
			return;
		}
		
		Map<String, String> additionalHttpHeaders = new HashMap<String, String>();
		
		if (extraHeaders != null)
			additionalHttpHeaders.putAll(extraHeaders);
		
		if (prefs.load(Config.KeyNames.USER_DNT, false))
			additionalHttpHeaders.put(Constants.SAFEKIDO_EXTRA_HEDER_DNT, "1");
		
		hideContentWebView();
		//historyListBox.setVisibility(View.GONE);
		//LA.changeCursor(null);
		
		if (u.startsWith("file://")) {
			webView.loadUrl(u);
			return;
		}
		
		String url = checkUrlValid(u);
		
		setUrlAdressBar(parseUrl(url));
		setupYoutubeCookie(url);
		
		webView.loadUrl(url, additionalHttpHeaders);
		if (forceUpdateUrl)
			mUrlObserver.updateUrlForce(url, "loadUrlWebView");
		else
			mUrlObserver.updateUrl(url, "loadUrlWebView");
	}
	
	public void onKeyboardShowHideEvent(boolean isShowing) {
		webViewOverlay.setVisibility(isShowing ? View.VISIBLE : View.GONE);
		if (!isShowing && urlText.getText().toString().equals("")) {
			if (getWebViewUrl() != null && !getWebViewUrl().startsWith(PAGE_DATA))
				setUrlAdressBar(getWebViewUrl());
		}
	}
	
	@TargetApi(10)
	public boolean isInFullscreen() {
		return mWebChromeClientHv.isInFullscreenMode();
	}
	
	@TargetApi(10)
	public void closeFullscreenMode() {
		mWebChromeClientHv.onHideCustomView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		webView.resumeTimers();
		webView.onResumeCall();
		
		if (mLocalProxyPortRecevier != null) {
			getActivity().registerReceiver(mLocalProxyPortRecevier, new IntentFilter(Constants.BRODCAST_LOCAL_PROXY_PORT));
		}
		
		ProxySystem.setupWiFiProxy(getActivity());
		
		/*if (getUrlToLoadAfterInit() != null) {
			
			mOnProxyService.isProxyServiceWorking(new CheckCallback() {
				@Override
				public void onCheckCallback(boolean result) {
					if (Console.isEnabled())
						Console.logi("SERVER STARTS FRAGMENT (resume) result=" + result);
					if (result) {
						setPageTitle(getUrlToLoadAfterInit());
						webView.loadUrl(getUrlToLoadAfterInit());
						setUrlToLoadAfterInit(null);
					}
				}
			});
			
			//loadUrlWebView(getUrlToLoadAfterInit());
			//setUrlToLoadAfterInit(null);
		}*/
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		cancelAll();
		webView.clearCache(false);
		webView.clearHistory();
		webView.removeAllViews();
		webView.setWebChromeClient(null);
		webView.setWebViewClient(null);
		webView.destroy();
		webView = null;
		mainView.removeAllViews();
	}
	
	@Override
	public void onPause() {
		Activity a = getActivity();
		if (a != null) {
			if (mLocalProxyPortRecevier != null) {
				a.unregisterReceiver(mLocalProxyPortRecevier);
			}
		}
		super.onPause();
		webView.pauseTimers();
		webView.onPauseCall();
	}
	
	public boolean cancelAll() {
		// cancel load home page always
		if (loadHomePageThread != null)
			loadHomePageThread.interrupt();
		
		boolean res1 = false;
		if (mAsyncWSChecker != null) {
			mAsyncWSChecker.cancelCheck();
			mAsyncWSChecker = null;
			res1 = true;
		}
		isUrlCheckngInProgress = false;
		
		boolean res2 = cancelLoadUrlWithCheck();
		boolean res3 = cancelLoadBlockedPage();
		return res1 || res2 || res3;
	}
	
	public void handleRefresh() {
		if (cancelAll()) {
			showProgress(false);
			return;
		}
		
		switch(mWebViewState) {
			default:
			case NO_URL:
				skipUrlChangeOnPageFinish = true;
				
				webView.stopLoading();
				if (urlText.getText().toString().trim().length() == 0) {
					loadHomePage();
				} else {
					loadUrlWebViewWithCheck(urlText.getText().toString(), true);
				}
				
				CommonUtils.hideKeyboard(getActivity(), urlText);
				
				if (mWebFragmentItem != null)
					mWebFragmentItem.setUrl(urlText.getText().toString());
				
				webView.requestFocus();
			break;
			case LOADING:
				webView.stopLoading();
				actionStopRefresh.setImageResource(R.drawable.ic_action_refresh_light);
				webView.requestFocus();
				CommonUtils.hideKeyboard(getActivity(), urlText);
				hideContentWebView();
				if (mWebFragmentItem != null)
					mWebFragmentItem.setUrl(getWebViewUrl());
			break;
			case LOADED:
				if (isContentWebViewVisible()) {
					
					loadUrlWebViewWithCheck(urlText.getText().toString(), true);
					
				} else {
					
					if (getWebViewUrl().startsWith(PAGE_DATA)) {
						
						if (urlText.getText().toString().trim().length() == 0) {
							loadHomePage();
						} else {
							loadUrlWebViewWithCheck(urlText.getText().toString(), true);
							actionStopRefresh.setImageResource(R.drawable.ic_action_x_light);
						}
						if (mWebFragmentItem != null)
							mWebFragmentItem.setUrl(urlText.getText().toString());
						
					} else {
						
						if (getWebViewUrl() == null || (getWebViewUrl() != null && getWebViewUrl().equals(PAGE_BLANK))) {
							loadHomePage();
						} else {
							webView.reload();
							actionStopRefresh.setImageResource(R.drawable.ic_action_x_light);
						}
						mUrlObserver.updateUrlForce(getWebViewUrl(), "refresh button");
						if (mWebFragmentItem != null)
							mWebFragmentItem.setUrl(getWebViewUrl());
						
					}
					
					webView.requestFocus();
					CommonUtils.hideKeyboard(getActivity(), urlText);
					
				}
			break;
		}
	}
	
	//private boolean isUrlChecking = false;
	private boolean hideOverlayAfterLoad = false;
	
	private boolean handleForwardAction() {
		
		if (historyListBox.getVisibility() == View.VISIBLE) {
			historyListBox.setVisibility(View.GONE);
			if (!getWebViewUrl().startsWith(PAGE_DATA))
				setUrlAdressBar(getWebViewUrl());
			
			mUrlObserver.updateUrl(getWebViewUrl(), "handleBackAction[no back]");
			return true;
		}
		
		if (cancelLoadBlockedPage()) {
			//Console.logw("cancelLoadBlockedPage");
			showProgress(false);
			return true;
		}
		
		//Console.logw("isUrlCheckngInProgress=" + isUrlCheckngInProgress);
		
		if (isUrlCheckngInProgress) {
			if (mAsyncWSChecker != null) {
				mAsyncWSChecker.cancelCheck();
				mAsyncWSChecker = null;
			}
			isUrlCheckngInProgress = false;
			
			return true;
		}
		
		
		if (webView.canGoForward()){
			
			// no internet connection
			if (!CommonUtils.isOnline(getActivity())) {
				showContentWebViewAssets("html/no_internet.html");
				webView.goForward();
				return true;
			}
			
			WebBackForwardList bh = webView.copyBackForwardList();
			final String nextUrl = parseUrl(bh.getItemAtIndex(bh.getCurrentIndex() + 1).getUrl());
			
			// ładowanie strony domowej
			if (nextUrl != null && nextUrl.equals(PAGE_BLANK)) {
				//isUrlCheckngInProgress = false;
				webView.goBack();
				setUrlAdressBar("");
				loadHomePage();
				return true;
			}
			
			showProgress(true);
			isUrlCheckngInProgress = true;
			
			mAsyncWSChecker = new AsyncCheckUrl(nextUrl, UserActionType.NO_LOG_REQUEST, getActivity())
				.setUserAgent(webView.getSettings().getUserAgentString())
				.setCookieManager(android.webkit.CookieManager.getInstance())
				.setOnStatusChanged(new AsyncCheckUrl.OnStatusChanged() {
					@Override
					public void onResult(ErrorCode result, final String newUrl) {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						
						if (result.isSuccess()) {
							lastBlockUrl = "";
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (isContentWebViewVisible()) {
										hideOverlayAfterLoad = true;
										webViewLoadOverlay.setVisibility(View.VISIBLE);
										webViewContent.setVisibility(View.GONE);
									}
									webView.goForward();
								}
							});
						} else {
							lastBlockUrl = nextUrl;
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// załadować strone (goBack)
									webView.goForward();
									// załadować ekran blokady
									webViewContent.setVisibility(View.VISIBLE);
									loadBlockedPage(nextUrl, new ThreadCallback() {
										@Override
										public void onThreadCallback(Object... params) {
											// INFO pusty parametrz oznacza błąd - prawidłowy ma adres url
	//										if (!params[0].equals("")) {
	//											getActivity().runOnUiThread(new Runnable() {
	//												@Override
	//												public void run() {
	//													webView.goForward();
	//												}
	//											});
	//										}
											isUrlCheckngInProgress = false;
										}
									}, UserActionType.NO_LOG_REQUEST);
								}
							});
						}
					}
					@Override
					public void onError(ErrorCode result) {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
								showContentWebViewAssets("html/error_load_block_content.html");
								setPageTitle("Błąd");
							}
						});
					}
					@Override
					public void onCancel() {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
							}
						});
					}
					@Override
					public void onUrlRedirected(final String newUrl) {
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setUrlAdressBar(parseUrl(newUrl));
							}
						});
					}
					@Override
					public void onCheckError(final RequestError error) {
						loadUrlCheck = null;
						lastBlockUrl = "";
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								lastCheckUrl = parseUrl(nextUrl);
								handleCheckError(lastCheckUrl, error);
							}
						});
					}
				})
				.start();
			
			return true;
		}
		return false;
	}
	
	private boolean isUrlCheckngInProgress = false;
	private AsyncCheckUrl mAsyncWSChecker = null;
	
	private boolean handleBackAction() {
		if (historyListBox.getVisibility() == View.VISIBLE) {
			historyListBox.setVisibility(View.GONE);
			if (!getWebViewUrl().startsWith(PAGE_DATA)) {
				setUrlAdressBar(getWebViewUrl());
			}
			mUrlObserver.updateUrl(getWebViewUrl(), "handleBackAction[no back]");
			return true;
		}
		
		if (cancelLoadBlockedPage()) {
			showProgress(false);
			return true;
		}
		
		if (isUrlCheckngInProgress) {
			if (mAsyncWSChecker != null) {
				mAsyncWSChecker.cancelCheck();
				mAsyncWSChecker = null;
			}
			isUrlCheckngInProgress = false;
			return true;
		}
		
		
		if (webView.canGoBack()){
			
			// no internet connection
			if (!CommonUtils.isOnline(getActivity())) {
				showContentWebViewAssets("html/no_internet.html");
				webView.goBack();
				return true;
			}
			
			final String backUrl;
			final String backTitle;
			if (isContentWebViewVisible()) {
				
				if (lastBlockUrl != null && getWebViewUrl().equals(lastBlockUrl)) {
					WebBackForwardList bh = webView.copyBackForwardList();
					backUrl = parseUrl(bh.getItemAtIndex(bh.getCurrentIndex() - 1).getUrl());
					backTitle = null;
				} else {
					backUrl = getWebViewUrl();
					backTitle = webView.getTitle();
				}
				
			} else {
				WebBackForwardList bh = webView.copyBackForwardList();
				backUrl = parseUrl(bh.getItemAtIndex(bh.getCurrentIndex() - 1).getUrl());
				backTitle = null;
			}
			
			// ładowanie strony domowej
			if (backUrl != null && backUrl.equals(PAGE_BLANK)) {
				//isUrlCheckngInProgress = false;
				webView.goBack();
				setUrlAdressBar("");
				loadHomePage();
				return true;
			}
			
			showProgress(true);
			isUrlCheckngInProgress = true;
			
			mAsyncWSChecker = new AsyncCheckUrl(backUrl, UserActionType.NO_LOG_REQUEST, getActivity())
				.setUserAgent(webView.getSettings().getUserAgentString())
				.setCookieManager(android.webkit.CookieManager.getInstance())
				.setOnStatusChanged(new AsyncCheckUrl.OnStatusChanged() {
					@Override
					public void onResult(ErrorCode result, final String newUrl) {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						
						if (result.isSuccess()) {
							lastBlockUrl = "";
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (isContentWebViewVisible()) {
										if (backTitle != null)
											setPageTitle(backTitle);
										
										if (!getWebViewUrl().equals(backUrl)) {
											hideOverlayAfterLoad = true;
											webView.goBack();
										} else {
											mUrlObserver.updateUrl(parseUrl(backUrl), "BACK");
											hideAllContentOvelays(true);
											showProgress(false);
										}
									} else {
										webView.goBack();
									}
								}
							});
						} else {
							lastBlockUrl = backUrl;
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// załadować strone (goBack)
									webView.goBack();
									// załadować ekran blokady
									webViewContent.setVisibility(View.VISIBLE);
									loadBlockedPage(backUrl, new ThreadCallback() {
										@Override
										public void onThreadCallback(final Object... params) {
											// INFO pusty parametrz oznacza błąd - prawidłowy ma adres url
											/*if (!params[0].equals("")) {
												final String thisUrl = parseUrl(params[0].toString());
												getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														if (isContentWebViewVisible()) {
															mUrlObserver.updateUrl(thisUrl, "BACK blocked");
															if (backTitle != null)
																setPageTitle(backTitle);
															if (!getWebViewUrl().equals(backUrl)) {
																webView.goBack();
															}
														} else {
															webView.goBack();
														}
													}
												});
											}
											isUrlCheckngInProgress = false;*/
										}
									}, UserActionType.NO_LOG_REQUEST);
								}
							});
							
						}
					}
					@Override
					public void onError(ErrorCode result) {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
								showContentWebViewAssets("html/error_load_block_content.html");
								setPageTitle(R.string.label_browser_error);
							}
						});
					}
					@Override
					public void onCancel() {
						mAsyncWSChecker = null;
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
							}
						});
					}
					@Override
					public void onUrlRedirected(final String newUrl) {
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setUrlAdressBar(parseUrl(newUrl));
							}
						});
					}
					@Override
					public void onCheckError(final RequestError error) {
						loadUrlCheck = null;
						lastBlockUrl = "";
						isUrlCheckngInProgress = false;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								lastCheckUrl = parseUrl(backUrl);
								handleCheckError(lastCheckUrl, error);
							}
						});
					}
				})
				.start();
			
			return true;
		} else {
			// nie ma już nic wcześniej to ustawiamy strone na pustą (nie zostanie zapisana w sesji)
			if (getWebFragmentItem() != null) {
				getWebFragmentItem().setUrl(PAGE_BLANK);
				getWebFragmentItem().setThumb(null);
			}
		}
		
		return false;
	}
	
	public boolean onBackButtonPressed() {
		if (menu.isShowing()) {
			menu.dismiss();
			return true;
		}
		if (historyListBox.getVisibility() == View.VISIBLE) {
			historyListBox.setVisibility(View.GONE);
			if (lastTryLoadUrl.length() > 0) {
				setUrlAdressBar(lastTryLoadUrl);
				lastTryLoadUrl = "";
			} else {
				String urlChanged = "";
				if (!getWebViewUrl().startsWith(PAGE_DATA)) {
					setUrlAdressBar(getWebViewUrl());
					urlChanged = getWebViewUrl();
				} else {
					if (urlToWebViewContent.length() > 0) {
						setUrlAdressBar(parseUrl(urlToWebViewContent));
						urlChanged = parseUrl(urlToWebViewContent);
					}
				}
				mUrlObserver.updateUrl(urlChanged, "onBackButtonPressed");
			}
			return true;
		}
		
		if (urlText.isFocused()) {
			if (!getWebViewUrl().startsWith(PAGE_DATA))
				setUrlAdressBar(getWebViewUrl());
			mUrlObserver.updateUrl(getWebViewUrl(), "onBackButtonPressed");
			return true;
		}
		return handleBackAction();
	}

	public String getUrlToLoadAfterInit() {
		return urlToLoadAfterInit;
	}

	public BrowserFrameFragment setUrlToLoadAfterInit(String urlToLoadAfterInit) {
		this.urlToLoadAfterInit = urlToLoadAfterInit;
		return this;
	}
	
	public WebFragmentItem getWebFragmentItem() {
		return mWebFragmentItem;
	}
	
	public BrowserFrameFragment setWebFragmentItem(WebFragmentItem i) {
		mWebFragmentItem = i;
		return this;
	}
	
	/*
	 * ładowanie strony głównej (domowej) to nic innego jak wczytanie z sieci treści i zapisanie go w cache
	 * za pomoca ETag-u sprawdzane jest po stronie servera czy konieczne jest odesłanie nowej treści czy content-length=0
	 */
	private Thread loadHomePageThread = null;
	public void loadHomePage() {
		if (loadHomePageThread != null) return;
		setPageTitle(R.string.app_name);
		
		final File homePageCache = new File(getActivity().getCacheDir(), "homepage.html");
		
		// no internet connection
		if (!CommonUtils.isOnline(getActivity())) {
			hideAllContentOvelays(true);
			String content = null;
			try {
				FileInputStream fis = new FileInputStream(homePageCache);
				content = CommonUtils.streamToString(fis, 1024);
				fis.close();
			} catch (FileNotFoundException e) {
				content = null;
			} catch (IOException e) {
				content = null;
			}
			
			webView.loadDataCompat(content, PAGE_BLANK);
			return;
		}
		
		loadHomePageThread = new Thread() {
			@Override
			public void run() {
				try {
					String content = null;
					Config config = Config.getInstance(getActivity());
					String IfNoneMatch = config.load(Config.KeyNames.USER_BROWSER_HOMEPAGE_MODIFIED, "");
					
					BasicRequest br = new BasicRequest(Constants.getBrowserHomepageUrl());
					br.setHeadersHandler(new AppHeadersHandler(getActivity()))
						.setSessionHandler(new AppSessionHandler(getActivity()));
					
					if (IfNoneMatch.length() > 0)
						br.getConnectionParams().addInHeader(BasicRequest.HEADER_IF_NONE_MATCH, IfNoneMatch);
					
					br.executeSafe();
					
					if (!br.hasError() && br.getResponse() != null) {
						
						final String ETag = br.getResponse().header(BasicRequest.HEADER_ETAG);
						
						if (Console.isEnabled())
							Console.logi("Response code: " + br.getResponse().code() + ", ETag: " + ETag);
						
						/*
						 * jeżeli dostaniemy kod 304 Not Modified
						 * to mamy pewność że server wie że treść jest ta sama
						 * i mże pominąc dalsze czytanie z sieci tylko załadować z cache
						 */
						if (br.getResponse().code() == 304) {
							br.getResponse().body().close();
							// load from cache (cache file in cache dir)
							if (homePageCache.exists() && homePageCache.length() > 0L) {
								try {
									FileInputStream fis = new FileInputStream(homePageCache);
									content = CommonUtils.streamToString(fis, 1024);
									fis.close();
								} catch (FileNotFoundException e) {
									content = null;
								} catch (IOException e) {
									content = null;
								}
							} else {
								content = null;
							}
							
						} else {
							
							// zapisujemy plik w cache na przyszłość
							InputStream is = br.getStream();
							if (is != null) {
								content = CommonUtils.streamToString(is, 1024);
								try {
									FileOutputStream fos = new FileOutputStream(homePageCache);
									fos.write(content.getBytes());
									fos.flush();
									fos.close();
									
									// ETag jest zapisywany dopiero po prawidłowym zapisie do pliku
									// inaczej mogło by się rozjechać coś w przypadku błędu zapisu
									if (ETag != null && ETag.trim().length() > 0)
										config.save(Config.KeyNames.USER_BROWSER_HOMEPAGE_MODIFIED, ETag);
									
								} catch (FileNotFoundException e) {
								} catch (IOException e) {
								}
							} else {
								content = null;
							}
						
						}
					}
					
					final String contentFinal = content;
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							hideAllContentOvelays(true);
							if (contentFinal == null) {
								webView.loadUrl(PAGE_BLANK);
							} else {
								webView.loadDataCompat(contentFinal, PAGE_BLANK);
							}
						}
					});
				} catch (Exception e) {
					// jeżeli cokolwiek zawiśnie będzie można odświezyć 
				}
				loadHomePageThread = null;
			}
		};
		loadHomePageThread.start();
	}
	
	public void onServerStarts() {
		if (getUrlToLoadAfterInit() != null) {
			
			if (Console.isEnabled())
				Console.logi("LOAD URL AFTER PROXY START " + getUrlToLoadAfterInit());
			
			final String urlToLoad = parseUrl(getUrlToLoadAfterInit().toString());
			
			Runnable codeExec = new Runnable() {
				@Override
				public void run() {
					if (urlToLoad.equals(PAGE_BLANK)) {
						
						// ładowanie strony startowej
						setUrlToLoadAfterInit(null);
						loadHomePage();
						
					} else {
					
						boolean hasHistoryRestored = false;
						Bundle restoreBundle = getRestoredState();
						if (restoreBundle != null && webView != null) {
							hasHistoryRestored = WebViewHv.hasHistoryRestored(restoreBundle);
							webView.restoreState(restoreBundle);
						} else {
							hasHistoryRestored = false;
						}
						
						// w silniku chrome nie jest przywracany content strony
						// trzeba samemu wczytać URL
						// ale tylko wtedy kiedy historia została przywrócona (było coś w historii wcześniej zapisanej)
						// jak nie było nic to konieczne jest wczytanie adresu URL (może byc tak że nic do cache nie zostanie zapisane)
						if (!hasHistoryRestored || android.os.Build.VERSION.SDK_INT >= 19 || (getWebFragmentItem() != null && getWebFragmentItem().isForceReloadAfterRestore())) {
							loadUrlWebViewWithCheck(urlToLoad, true);
						}
						
						setPageTitle(urlToLoad);
						setUrlAdressBar(urlToLoad);
						setUrlToLoadAfterInit(null);
						
					}
				}
			};
			
			/*
			 * sprawdzanie czy fragment jest już dodany do widoku activity
			 * jeżeli nie to koniczen będzie wywołanie kodu po tym jak zostanie dodany
			 */
			if (isAdded()) {
				codeExec.run();
			} else {
				setUrlToLoadAfterInit(null);
				execAfterAttach.add(codeExec);
			}
			
		}
	}

	public Bundle getRestoredState() {
		return restoredState;
	}

	public BrowserFrameFragment setRestoredState(Bundle restoredState) {
		this.restoredState = restoredState;
		return this;
	}
	
	public void changeBrowserIdetify(BrowserIdentify bi) {
		if (webView != null)
			webView.setBrowserIdentify(bi);
	}

}
