package com.ardurasolutions.safekiddo.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ardurasolutions.safekiddo.BuildConfig;
import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.activities.PinActivity;
import com.ardurasolutions.safekiddo.activities.UserSettings;
import com.ardurasolutions.safekiddo.browser.BrowserMainActivity;
import com.ardurasolutions.safekiddo.dev.DevActivity;
import com.ardurasolutions.safekiddo.extra.SystemBarTintManager;
import com.ardurasolutions.safekiddo.extra.SystemBarTintManager.SystemBarConfig;
import com.ardurasolutions.safekiddo.helpers.AppsHelper;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.HeartBeatHelper;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.launcher.proto.AppInfo;
import com.ardurasolutions.safekiddo.launcher.proto.DesktopHelper;
import com.ardurasolutions.safekiddo.launcher.proto.DesktopsAdapter;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEdgeEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEvent;
import com.ardurasolutions.safekiddo.launcher.views.AllAppsGrid;
import com.ardurasolutions.safekiddo.launcher.views.AllAppsGrid.AllAppsAdapter;
import com.ardurasolutions.safekiddo.launcher.views.AllAppsGrid.OnFullyInited;
import com.ardurasolutions.safekiddo.launcher.views.AllAppsGrid.OnInitState;
import com.ardurasolutions.safekiddo.launcher.views.AppIcon;
import com.ardurasolutions.safekiddo.launcher.views.DesktopPager;
import com.ardurasolutions.safekiddo.launcher.views.DesktopPagerIndicator;
import com.ardurasolutions.safekiddo.launcher.views.DesktopView;
import com.ardurasolutions.safekiddo.proto.LocalServiceBinder;
import com.ardurasolutions.safekiddo.proto.PinActivityConfig;
import com.ardurasolutions.safekiddo.proto.PopupWindowMenu;
import com.ardurasolutions.safekiddo.proto.PopupWindowMenu.PopupWindowMenuItem;
import com.ardurasolutions.safekiddo.services.GuardService;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.ardurasolutions.safekiddo.sql.tables.DesktopConfigTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AllApps;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class Desktop extends Activity {
	
	private RelativeLayout allAppsBox;
	private View desktopBox, allAppsBoxOverlay;
	private AllAppsGrid grid;
	private DesktopView desktopFastBarLeft, desktopFastBarRight;
	private View demoveApp;
	private DesktopPager pager;
	private DesktopsAdapter DA;
	private OnDragEdgeEvent mOnDragEdgeEvent;
	private OnDragEvent mOnDragEvent;
	private DesktopPagerIndicator pagerIndicator;
	private BroadcastReceiver mLogoutEvent, mApprovedAppsUpdate, mPackageChange;
	private EditText searchEdit;
	private TextView textTitle;
	private View btnSearch, btnMoreActions;
	private PopupWindowMenu menu;
	private boolean firstRun = false, firstRunDeskCenter = false, firstRunOthers = false;
	private Config prefs;
	private DesktopConfigTable mDesktopConfigTable;
	private boolean mainAnimationInProgress = false;
	
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
				BugSenseHandler.sendExceptionMessage("DESKTOP", "bind_service_onServiceConnected", e);
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	private boolean isBadOrientation = false;
	private View rootView;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
			if (rootView != null)
				rootView.setVisibility(View.INVISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			if (Console.isEnabled())
				Console.logw("SET TO PORTRAIT");
		} else {
			if (rootView != null)
				rootView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*
		 * z jakiegoś powodu system uruchamia activity z nieprawidłowa orientacją 
		 * pomimo ustawionej na sztywno w AndroidManifest.xml
		 */
		WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display mDisplay = mWindowManager.getDefaultDisplay();
	    if (mDisplay.getRotation() != 0) {
	    	if (Console.isEnabled())
	    		Console.loge("BAD ORIENTATION: " + mDisplay.getRotation());
	    	isBadOrientation = true;
	    	return;
	    }
		
		setContentView(R.layout.activity_desktop);
		
		rootView = findViewById(R.id.rootView);
		
		mDesktopConfigTable = LocalSQL.getInstance(this).getTable(DesktopConfigTable.class);
		prefs = Config.getInstance(this);
		firstRun = prefs.load(Config.KeyNames.DESKTOP_FIRST_RUN, true);
		firstRunDeskCenter = firstRunOthers = firstRun;
		if (firstRun) {
			prefs.save(Config.KeyNames.DESKTOP_FIRST_RUN, false);
		}
		
		if (Console.isEnabled())
			Toaster.showMsg(this, "ON CREATE");
		
		mLogoutEvent = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logi("Desktop : recevie logout action - finish activity");
				Desktop.this.finish();
				if (mLogoutEvent != null) {
					unregisterReceiver(mLogoutEvent);
					mLogoutEvent = null;
				}
			}
		};
		
		mPackageChange = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logd("Notify apps install/update/remove");
				grid.notifyInstalledAppChange();
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addDataScheme("package");
		registerReceiver(mPackageChange, intentFilter);
		
		mApprovedAppsUpdate = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logi("Desktop : Recevie Approved Apps Update");
				grid.notifyInstalledAppChange();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						final HashMap<DesktopView, ArrayList<View>> viewsToRemove = new HashMap<DesktopView, ArrayList<View>>();
						final ArrayList<AllApps> blocked = LocalSQL.getInstance(Desktop.this).getTable(AllAppsTable.class).getBlockedApps();
						final ArrayList<DesktopView> allDesktopView = new ArrayList<DesktopView>();
						
						for(int i=0; i<pager.getChildCount(); i++) {
							allDesktopView.add((DesktopView) pager.getChildAt(i));
						}
						allDesktopView.add(desktopFastBarLeft);
						allDesktopView.add(desktopFastBarRight);
						
						for(DesktopView d : allDesktopView) {
							ArrayList<AppIcon> da = d.getIcons();
							ArrayList<View> toRemove = new ArrayList<View>();
							for(AppIcon ai : da) {
								if (blocked.contains(new AllApps(ai.getAppInfo().getComponentName()))) {
									toRemove.add(ai);
								}
							}
							if (toRemove.size() > 0) {
								viewsToRemove.put(d, toRemove);
							}
						}
						
						mDesktopConfigTable.deleteIcons(viewsToRemove);
						
						if (viewsToRemove.size() > 0) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Iterator<DesktopView> rm = viewsToRemove.keySet().iterator();
									while(rm.hasNext()) {
										DesktopView desk = rm.next();
										for(View v : viewsToRemove.get(desk)) {
											desk.removeView(v);
										}
									}
								}
							});
						}
					}
				}).start();
			}
		};
		
		registerReceiver(mApprovedAppsUpdate, new IntentFilter(Constants.BRODCAST_BLOCKED_APPS));
		
		allAppsBox = (RelativeLayout) findViewById(R.id.allAppsBox);
		desktopBox = findViewById(R.id.desktopBox);
		allAppsBoxOverlay = findViewById(R.id.allAppsBoxOverlay);
		grid = (AllAppsGrid) findViewById(R.id.grid);
		desktopFastBarLeft = (DesktopView) findViewById(R.id.desktopFastBarLeft);
		desktopFastBarRight = (DesktopView) findViewById(R.id.desktopFastBarRight);
		pager = (DesktopPager) findViewById(R.id.pager);
		pagerIndicator = (DesktopPagerIndicator) findViewById(R.id.pagerIndicator);
		demoveApp = findViewById(R.id.demoveApp);
		btnSearch = findViewById(R.id.btnSearch);
		btnMoreActions = findViewById(R.id.btnMoreActions);
		searchEdit = (EditText) findViewById(R.id.searchEdit);
		textTitle = (TextView) findViewById(R.id.textTitle);
		
		desktopFastBarLeft.setDeleteView(demoveApp);
		desktopFastBarRight.setDeleteView(demoveApp);
		
		desktopFastBarLeft.setShowOnlyIcon(true);
		desktopFastBarLeft.setSingleLine(true);
		desktopFastBarLeft.setIconSize(getResources().getDimensionPixelSize(R.dimen.desktop_fast_bar_item_size));
		
		desktopFastBarRight.setShowOnlyIcon(true);
		desktopFastBarRight.setSingleLine(true);
		desktopFastBarRight.setIconSize(getResources().getDimensionPixelSize(R.dimen.desktop_fast_bar_item_size));
		
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				textTitle.setVisibility(View.GONE);
				btnSearch.setVisibility(View.GONE);
				searchEdit.setVisibility(View.VISIBLE);
				searchEdit.requestFocus();
				CommonUtils.showKeyboard(Desktop.this, searchEdit);
			}
		});
		
		searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					
					return true;
				}
				return false;
			}
		});
		searchEdit.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			@Override
			public void afterTextChanged(Editable s) {
				if (searchEdit.getText().toString().length() > 1)
					grid.setFilter(searchEdit.getText().toString());
				else
					grid.setFilter(null);
			}
		});
		
		final WallpaperManager wpm = WallpaperManager.getInstance(this);
		
		DA = new DesktopsAdapter(this);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					reassignDesktopsTargets();
				}
			}
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				wpm.setWallpaperOffsets(pager.getWindowToken(), Math.max(0.f, Math.min((positionOffset+position)/(3 - 1), 1.f)), 0);
			}
			@Override
			public void onPageSelected(int arg0) {
				pagerIndicator.setSelectedPoint(pager.getCurrentItem());
				reassignDesktopsTargets();
			}
		});
		pager.setOffscreenPageLimit(3);
		
		mOnDragEdgeEvent = new OnDragEdgeEvent() {
			@Override
			public void onDragRightEdge(AppIcon icon) {
				if (pager.getCurrentItem() < DA.getCount()) {
					pager.setCurrentItem(pager.getCurrentItem() + 1, true);
					reassignDesktopsTargets();
					icon.clearDropTargets();
					for(int i=0; i<pager.getChildCount(); i++)
						icon.addDropTarget(pager.getChildAt(i));
					icon.addDropTarget(desktopFastBarLeft);
					icon.addDropTarget(desktopFastBarRight);
				}
			}
			@Override
			public void onDragLeftEdge(AppIcon icon) {
				if (pager.getCurrentItem() > 0) {
					pager.setCurrentItem(pager.getCurrentItem() - 1, true);
					reassignDesktopsTargets();
					icon.clearDropTargets();
					for(int i=0; i<pager.getChildCount(); i++)
						icon.addDropTarget(pager.getChildAt(i));
					icon.addDropTarget(desktopFastBarLeft);
					icon.addDropTarget(desktopFastBarRight);
				}
			}
		};
		
		mOnDragEvent = new OnDragEvent(){
			@Override
			public void onStartDrag(AppIcon appIcon, DesktopView dv) {
				pager.deactivate();
				reassignDesktopsTargets();
			}
			@Override
			public void onStopDrag(AppIcon appIcon, DesktopView dv) {
				pager.activate();
			}
		};
		
		desktopFastBarLeft.addOtherDropTarget(desktopFastBarRight);
		desktopFastBarLeft.setOnDragEdgeEvent(mOnDragEdgeEvent);
		
		desktopFastBarRight.addOtherDropTarget(desktopFastBarLeft);
		desktopFastBarRight.setOnDragEdgeEvent(mOnDragEdgeEvent);
		
		findViewById(R.id.allAppsButton2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mainAnimationInProgress) {
					mainAnimationInProgress = true;
					desktopBox.startAnimation(AnimationUtils.loadAnimation(Desktop.this, R.anim.desktop_out));
					allAppsBox.setVisibility(View.VISIBLE);
					Animation a = AnimationUtils.loadAnimation(Desktop.this, R.anim.all_apps_in);
					a.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation) {
							mainAnimationInProgress = true;
						}
						@Override
						public void onAnimationEnd(Animation animation) {
							mainAnimationInProgress = false;
						}
						@Override
						public void onAnimationRepeat(Animation animation) { }
					});
					
					allAppsBox.startAnimation(a);
				}
			}
		});
		
		grid.setOnDragEvent(new OnDragEvent() {
			@Override
			public void onStartDrag(AppIcon appIcon, DesktopView dv) {
				appIcon.clearDropTargets();
				appIcon.addDropTarget(desktopFastBarLeft);
				appIcon.addDropTarget(desktopFastBarRight);
				for(int i=0; i<pager.getChildCount(); i++) {
					appIcon.addDropTarget(pager.getChildAt(i));
				}
				onBackPressed();
			}
			@Override
			public void onStopDrag(AppIcon appIcon, DesktopView dv) { }
		});
		grid.setOnDragEdgeEvent(mOnDragEdgeEvent);
		grid.addDropTarget(desktopFastBarLeft);
		grid.addDropTarget(desktopFastBarRight);
		grid.setOnInitState(new OnInitState() {
			@Override
			public void onInitStart() {
				allAppsBoxOverlay.setVisibility(View.VISIBLE);
			}
			@Override
			public void onInitEnd() {
				allAppsBoxOverlay.setVisibility(View.GONE);
			}
		});
		grid.setOnFullyInited(new OnFullyInited() {
			@Override
			public void onFullyInited(final AllAppsAdapter la) {
				
				DA.setOnPageRemoveCreate(new DesktopsAdapter.OnPageRemoveCreate() {
					@Override
					public void onPageRemove(DesktopView desktop) {
						desktopFastBarLeft.removeOtherDropTarget(desktop);
						desktopFastBarRight.removeOtherDropTarget(desktop);
						grid.removeDropTarget(desktop);
					}
					@Override
					public void onPageCreate(final DesktopView desktop) {
						desktop.setOnDragEvent(mOnDragEvent);
						desktop.setOnDragEdgeEvent(mOnDragEdgeEvent);
						desktop.setDeleteView(demoveApp);
						desktop.addOtherDropTarget(desktopFastBarLeft);
						desktop.addOtherDropTarget(desktopFastBarRight);
						desktop.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								final View popupView = LayoutInflater.from(Desktop.this).inflate(R.layout.popup_browser_menu, (ViewGroup) desktop, false);
								//final int padd = (int) (getResources().getDisplayMetrics().density * 20f);
								final PopupWindow popup = new PopupWindow(popupView, getResources().getDimensionPixelSize(R.dimen.menu_width), LayoutParams.WRAP_CONTENT);
								
								ListView popupList = (ListView) popupView.findViewById(R.id.popupList);
								popupList.setAdapter(new ArrayAdapter<String>(Desktop.this, R.layout.simple_list_item_1, new String[]{getResources().getString(R.string.label_change_wallpaper)})); /*{
									@Override
									public View getView(int position, View convertView, ViewGroup parent) {
										convertView = super.getView(position, convertView, parent);
										((TextView) convertView).setTextColor(0xFF000000);
										((TextView) convertView).setPadding(padd, padd, padd, padd);
										return convertView;
									}
								});*/
								popupList.setSelector(R.drawable.browser_menu_item_bg);
								popupList.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
										switch(position) {
											default:
											case 0: 
												startActivity(new Intent(Desktop.this, WallpaperChooser.class));
												popup.dismiss();
											break;
										}
									}
									
								});
								
								popup.setTouchable(true);
								popup.setOutsideTouchable(true);
								popup.getContentView().setFocusableInTouchMode(true);
								popup.getContentView().setOnKeyListener(new View.OnKeyListener() {        
									@Override
									public boolean onKey(View v, int keyCode, KeyEvent event) {
										if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
											popup.dismiss();
											return true;
										}
										return false;
									}
								});
								popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_full_holo_light));
								popup.showAtLocation(v, Gravity.CENTER, 0, 0);
								popup.setFocusable(true);
								popup.update();
								return false;
							}
							
						});
						
						desktopFastBarLeft.addOtherDropTarget(desktop);
						desktopFastBarRight.addOtherDropTarget(desktop);
						grid.addDropTarget(desktop);
						
						if (firstRunDeskCenter && desktop.getTag().equals("page_1")) {
							firstRunDeskCenter = false;
							desktop.post(new Runnable() {
								@Override
								public void run() {
									ComponentName cn = new ComponentName(getPackageName(), BrowserMainActivity.class.getName());
									desktop.addIconAndSave(la.getItem(cn), 0, 0, true);
									
//									cn = new ComponentName(getPackageName(), DummyUserSettings.class.getName());
//									desktop.addIconAndSave(la.getItem(cn), 0, 1, true);
									
									if (BuildConfig.DEBUG) {
										cn = new ComponentName(getPackageName(), DevActivity.class.getName());
										desktop.addIconAndSave(la.getItem(cn), 0, 1, true);
									}
								}
							});
						} else {
							DesktopHelper.loadConfig(desktop, grid.getAllAppsAdapter());
						}
						
					}
				});
				
				pager.setAdapter(DA);
				pager.post(new Runnable() {
					@Override
					public void run() {
						pagerIndicator.setPoints(DA.getCount());
						pagerIndicator.setSelectedPoint(pager.getCurrentItem());
						reassignDesktopsTargets();
					}
				});
				
				goToDefaultScrren(false);
				
				if (firstRunOthers) {
					firstRunOthers = false;
					pager.post(new Runnable() {
						@Override
						public void run() {
							loadInitialIcons(la);
						}
					});
				} else {
					DesktopHelper.loadConfig(desktopFastBarLeft, la);
					DesktopHelper.loadConfig(desktopFastBarRight, la);
				}
			}
		});
		
		menu = new PopupWindowMenu(this, getResources().getDimensionPixelSize(R.dimen.menu_apps_width));
		menu.setBackgroundDrawable(getResources().getDrawable(R.drawable.abc_menu_dropdown_panel_holo_dark));
		menu.addItem(new PopupWindowMenuItem(getResources().getString(R.string.label_parent_mode))
			.setOnClick(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					PinActivity.showActivity(Desktop.this, new PinActivityConfig(UserSettings.class).setFinishOnBack(true));
				}
			})
			.setIcon(getResources().getDrawable(R.drawable.parent_mode))
		);
		menu.addItem(new PopupWindowMenuItem(getResources().getString(R.string.label_change_wallpaper))
			.setOnClick(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(Desktop.this, WallpaperChooser.class));
				}
			})
			.setIcon(getResources().getDrawable(R.drawable.wallpaper))
		);
		if (BuildConfig.DEBUG) {
			menu.addItem(new PopupWindowMenuItem("DEV")
				.setOnClick(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(Desktop.this, DevActivity.class));
					}
				})
				.setIcon(getResources().getDrawable(R.drawable.ic_action_settings_lignt))
			);
		}
		
		btnMoreActions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (menu.isShowing()) {
					menu.dismiss();
				} else {
					menu.showAsDropDown(v, 0, -10);
					menu.setFocusable(true);
					menu.update();
				}
			}
		});
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			SystemBarConfig config = tintManager.getConfig();
			
			if (config.hasNavigtionBar()) {
				if (android.content.res.Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation && !config.isNavigationAtBottom()) {
					RelativeLayout.LayoutParams la = new RelativeLayout.LayoutParams(config.getNavigationBarHeight(), RelativeLayout.LayoutParams.MATCH_PARENT);
					la.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
					findViewById(R.id.gridBottomBar).setLayoutParams(la);
					
					RelativeLayout.LayoutParams la2 = (android.widget.RelativeLayout.LayoutParams) grid.getLayoutParams();
					la2.addRule(RelativeLayout.ABOVE, 0);
					la2.addRule(RelativeLayout.LEFT_OF, R.id.gridBottomBar);
					
				} else {
					RelativeLayout.LayoutParams la = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, config.getNavigationBarHeight());
					la.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
					findViewById(R.id.gridBottomBar).setLayoutParams(la);
					
					RelativeLayout.LayoutParams la2 = (android.widget.RelativeLayout.LayoutParams) grid.getLayoutParams();
					la2.addRule(RelativeLayout.ABOVE, R.id.gridBottomBar);
				}
				
			}
			findViewById(R.id.allAppsToolbar).setPadding(0, config.getStatusBarHeight(), 0, 0);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, GuardService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void reassignDesktopsTargets() {
		ArrayList<DesktopView> desktops = new ArrayList<DesktopView>();
		for(int i=0; i<pager.getChildCount(); i++) {
			DesktopView d = (DesktopView) pager.getChildAt(i);
			desktops.add(d);
		}
		for(DesktopView dd : desktops) {
			for(DesktopView v : desktops) {
				dd.clearOtherTargets();
				dd.addOtherDropTarget(v);
			}
			dd.addOtherDropTarget(desktopFastBarLeft);
			dd.addOtherDropTarget(desktopFastBarRight);
		}
	}
	
	private void loadInitialIcons(final AllAppsAdapter la) {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
			
			ComponentName contacts = AppsHelper.getDefaultContactsApp(Desktop.this);
			if (contacts != null) {
				ArrayList<AppInfo> contactsAll = la.getItems(contacts);
				if (contactsAll.size() > 0) {
					contacts = contactsAll.get(0).getComponentName();
					desktopFastBarLeft.addIconAndSave(contactsAll.get(0), 0, 0, true);
				}
			}
			
			ComponentName camera = AppsHelper.getDefaultCameraApp(Desktop.this);
			if (camera != null) {
				ArrayList<AppInfo> cameraAll = la.getItems(camera);
				if (cameraAll.size() > 0) {
					desktopFastBarLeft.addIconAndSave(cameraAll.get(0), 0, 1, true);
				}
			}
			
			ComponentName gallery = AppsHelper.getDefaultGallery(Desktop.this);
			if (gallery != null) {
				ArrayList<AppInfo> galleryAll = la.getItems(gallery);
				if (galleryAll.size() > 0) {
					desktopFastBarLeft.addIconAndSave(galleryAll.get(0), 0, 2, true);
				}
			}
			
			ComponentName mail = AppsHelper.getMailApp(Desktop.this);
			if (mail != null) {
				ArrayList<AppInfo> mailAll = la.getItems(mail);
				if (mailAll.size() > 0) {
					desktopFastBarRight.addIconAndSave(mailAll.get(0), 0, 0, true);
				}
			}
			
		} else {
			
			ComponentName phone = AppsHelper.getDefaultDialApp(Desktop.this);
			if (phone != null) {
				ArrayList<AppInfo> phoneAll = la.getItems(phone);
				if (phoneAll.size() > 0) {
					phone = phoneAll.get(0).getComponentName();
					desktopFastBarLeft.addIconAndSave(phoneAll.get(0), 0, 0, true);
				}
			}
			
			ComponentName sms = AppsHelper.getDefaultSmsApp(Desktop.this);
			if (sms != null) {
				ArrayList<AppInfo> smsAll = la.getItems(sms);
				if (smsAll.size() > 0) {
					desktopFastBarLeft.addIconAndSave(smsAll.get(0), 0, 1, true);
				}
			}
			
			ComponentName contacts = AppsHelper.getDefaultContactsApp(Desktop.this);
			if (contacts != null) {
				ArrayList<AppInfo> contactsAll = la.getItems(contacts);
				if (contactsAll.size() > 0) {
					contacts = contactsAll.get(0).getComponentName();
					if (!phone.equals(contacts)) {
						desktopFastBarRight.addIconAndSave(contactsAll.get(0), 0, 0, true);
					}
				}
			}
			
			ComponentName camera = AppsHelper.getDefaultCameraApp(Desktop.this);
			if (camera != null) {
				ArrayList<AppInfo> cameraAll = la.getItems(camera);
				if (cameraAll.size() > 0) {
					desktopFastBarRight.addIconAndSave(cameraAll.get(0), 0, 1, true);
				}
			}
		}
		
		sendBroadcast(new Intent().setAction(Constants.BRODCAST_BLOCKED_APPS));
	}
	
	public void goToDefaultScrren(boolean withAnimation) {
		pager.setCurrentItem(1, withAnimation);
	}
	
	private void hideAllApps(boolean withAnimation) {
		if (withAnimation) {
			if (!mainAnimationInProgress) {
				mainAnimationInProgress = true;
				Animation allAppsOut = AnimationUtils.loadAnimation(Desktop.this, R.anim.all_apps_out);
				allAppsOut.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationRepeat(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						allAppsBox.setVisibility(View.GONE);
						mainAnimationInProgress = false;
					}
				});
				allAppsBox.startAnimation(allAppsOut);
				desktopBox.startAnimation(AnimationUtils.loadAnimation(Desktop.this, R.anim.desktop_in));
			}
		} else {
			allAppsBox.setVisibility(View.GONE);
			desktopBox.clearAnimation();
			desktopBox.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onStop() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onStop();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isBadOrientation) return;
		
		if (menu.isShowing()) {
			menu.dismiss();
		}
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			getWindow().closeAllPanels();
			final boolean alreadyOnHome = ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			
			if (!alreadyOnHome) {
				hideAllApps(false);
			} else {
				if (allAppsBox.getVisibility() == View.VISIBLE)
					hideAllApps(true);
				goToDefaultScrren(true);
			}
			
			final View v = getWindow().peekDecorView();
			if (v != null && v.getWindowToken() != null) {
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mLogoutEvent != null) {
			registerReceiver(mLogoutEvent, new IntentFilter(Constants.BRODCAST_SAFEKIDDO_REMOVE));
		}
		if (mBound) {
			if (!binder.getService().isServiceWorking()) {
				startService(new Intent(this, GuardService.class));
			}
		}
		UserSettings us = UserSettings.get();
		if (us != null) {
			us.finish();
		}
		
		HeartBeatHelper.checkHB(this);
	}
	
	@Override
	protected void onDestroy() {
		if (mLogoutEvent != null) {
			unregisterReceiver(mLogoutEvent);
			mLogoutEvent = null;
		}
		if (mApprovedAppsUpdate != null) {
			unregisterReceiver(mApprovedAppsUpdate);
			mApprovedAppsUpdate = null;
		}
		if (mPackageChange != null) {
			unregisterReceiver(mPackageChange);
			mPackageChange = null;
		}
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (menu != null && menu.isShowing()) {
			menu.dismiss();
		}
		if (allAppsBox != null && allAppsBox.getVisibility() == View.VISIBLE) {
			if ((searchEdit != null && searchEdit.getVisibility() == View.VISIBLE) && (grid != null && !grid.isDragging())) {
				CommonUtils.hideKeyboard(this, searchEdit);
				textTitle.setVisibility(View.VISIBLE);
				btnSearch.setVisibility(View.VISIBLE);
				searchEdit.setVisibility(View.GONE);
				searchEdit.setText("");
				grid.setFilter(null);
			} else {
				hideAllApps(true);
			}
		} else {
			// super.onBackPressed();
		}
	}

}
