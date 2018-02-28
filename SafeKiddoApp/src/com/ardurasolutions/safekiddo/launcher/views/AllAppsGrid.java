package com.ardurasolutions.safekiddo.launcher.views;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.launcher.proto.AppInfo;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEdgeEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDrawIcon;
import com.ardurasolutions.safekiddo.launcher.proto.OnDropIcon;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AllApps;

public class AllAppsGrid extends GridView implements OnDragEdgeEvent {
	
	public static interface OnFullyInited {
		public void onFullyInited(AllAppsAdapter la);
	}
	
	public static interface OnInitState {
		public void onInitStart();
		public void onInitEnd();
	}
	
	private final Comparator<AppInfo> ALPHA_COMPARATOR = new Comparator<AppInfo>() {
		private final Collator sCollator = Collator.getInstance();
		@Override
		public int compare(AppInfo object1, AppInfo object2) {
			return sCollator.compare(object1.getLabel(), object2.getLabel());
		}
	};
	
	private int savedDownX = -1, savedDownY = -1;
	private boolean isDragging = false;
	private Bitmap b;
	private DragView mDragView;
	private AppInfo drgItem;
	private AllAppsAdapter LA;
	private ArrayList<View> dropTargets = new ArrayList<View>();
	private View currentDropTarget = null;
	private OnFullyInited mOnFullyInited;
	private OnDragEvent mOnDragEvent;
	private OnDragEdgeEvent mOnDragEdgeEvent;
	private AppIcon dragAppIcon;
	private OnInitState mOnInitState;

	public AllAppsGrid(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public AllAppsGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AllAppsGrid(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		initAdapter(true);
		setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (startDragging((AppIcon) view)) {
					if (savedDownX != -1 && savedDownY != -1) {
						createDragObject(savedDownX - getLeft(), savedDownY - getTop());
						savedDownX = -1;
						savedDownY = -1;
					}
					return true;
				} else
					return false;
			}
		});
		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (view instanceof AppIcon) {
					((AppIcon) view).onClickAction();
				}
			}
		});
	}
	
	public AllAppsAdapter getAllAppsAdapter() {
		return LA;
	}
	
	private boolean isOnRightBorder = false, onRightEventFired = false;
	private long isOnRightBorderTime = 0l;
	private boolean isOnLeftBorder = false, onLeftEventFired = false;
	private long isOnLeftBorderTime = 0l;
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			default: break;
			case MotionEvent.ACTION_DOWN:
				savedDownX = (int) event.getRawX();
				savedDownY = (int) event.getRawY();
			break;
			
			case MotionEvent.ACTION_MOVE:
				if (isDragging) {
					if (mDragView == null)
						createDragObject((int) event.getRawX(), (int) event.getRawY());
					mDragView.move((int) event.getRawX(), (int) event.getRawY());
					
					if (getOnDragEdgeEvent() != null) {
						if (event.getRawX() >= getResources().getDisplayMetrics().widthPixels - (getMeasuredWidth() / AppIcon.DRAG_BORDER_EDGE_DIVIDER)) {
							if (!isOnRightBorder)
								isOnRightBorderTime = System.currentTimeMillis();
							isOnRightBorder = true;
						} else {
							isOnRightBorder = false;
							isOnRightBorderTime = 0L;
							onRightEventFired = false;
						}
						
						if (isOnRightBorder && (System.currentTimeMillis() - isOnRightBorderTime) > AppIcon.DRAG_BORDER_TIME) {
							if (!onRightEventFired) {
								getOnDragEdgeEvent().onDragRightEdge(dragAppIcon);
								onRightEventFired = true;
							}
						}
						
						if (event.getRawX() <= (getMeasuredWidth() / 4)) {
							if (!isOnLeftBorder)
								isOnLeftBorderTime = System.currentTimeMillis();
							isOnLeftBorder = true;
						} else {
							isOnLeftBorder = false;
							isOnLeftBorderTime = 0L;
							onLeftEventFired = false;
						}
						
						if (isOnLeftBorder && (System.currentTimeMillis() - isOnLeftBorderTime) > AppIcon.DRAG_BORDER_TIME) {
							if (!onLeftEventFired) {
								getOnDragEdgeEvent().onDragLeftEdge(dragAppIcon);
								onLeftEventFired = true;
							}
						}
					}
					
					if (dropTargets.size() > 0) {
						View prevCurrentDropTarget = currentDropTarget;
						for(View target : dropTargets) {
							if (isPointInsideView(event.getRawX(), event.getRawY(), target)) {
								currentDropTarget = target;
								int location[] = new int[2];
								target.getLocationOnScreen(location);
								OnDrawIcon odi = null;
								try {
									odi = (OnDrawIcon) target;
								} catch (ClassCastException e) {}
								
								if (odi != null) {
									odi.onDrawIcon(drgItem.getShadow(), (int) event.getRawX() - location[0], (int) event.getRawY() - location[1]);
								}
								break;
							}
						}
						
						if (prevCurrentDropTarget != null && currentDropTarget != null && !prevCurrentDropTarget.equals(currentDropTarget)) {
							OnDrawIcon odi = null;
							try {
								odi = (OnDrawIcon) prevCurrentDropTarget;
							} catch (ClassCastException e) {}
							
							if (odi != null) {
								odi.onClearicon();
							}
						}
					}

				} else 
					break;
				return true;
			case MotionEvent.ACTION_UP:
				View targetViewUp = null;
				if (isDragging && dropTargets.size() > 0) {
					for(View target : dropTargets) {
						if (isPointInsideView(event.getRawX(), event.getRawY(), target)) {
							targetViewUp = target;
							int location[] = new int[2];
							target.getLocationOnScreen(location);
							OnDropIcon odi = null;
							try {
								odi = (OnDropIcon) target;
							} catch (ClassCastException e) {}
							
							if (odi != null) {
								odi.onDropIcon(drgItem, (int) event.getRawX() - location[0], (int) event.getRawY() - location[1], null);
							}
							break;
						}
					}
					currentDropTarget = null;
				}
				stopDragging(targetViewUp);
			break;
			
			case MotionEvent.ACTION_CANCEL:
				View targetViewCancel = null;
				if (isDragging && dropTargets.size() > 0) {
					for(View target : dropTargets) {
						if (isPointInsideView(event.getRawX(), event.getRawY(), target)) {
							targetViewCancel = target;
							break;
						}
					}
					currentDropTarget = null;
				}
				stopDragging(targetViewCancel);
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private boolean startDragging(AppIcon ai) {
		if (isDragging) return false;
		isDragging = true;
		dragAppIcon = ai;
		if (getOnDragEvent() != null)
			getOnDragEvent().onStartDrag(ai, null);
		return true;
	}
	
	private void stopDragging(View target) {
		isDragging = false;
		
		if (mDragView != null) {
			mDragView.remove();
			mDragView = null;
		}
		drgItem = null;
		
		if (target != null) {
			OnDrawIcon odi = null;
			try {
				odi = (OnDrawIcon) target;
			} catch (ClassCastException e) {}
			
			if (odi != null) {
				odi.onClearicon();
			}
//			if (target instanceof DesktopView) {
//				LocalSQL.getInstance(getContext()).getTable(DesktopConfigTable.class).saveIcon((DesktopView) target, dragAppIcon);
//				if (Console.isEnabled())
//					Console.logd("SAVE ICON [" + target.getTag() + "]: " + dragAppIcon.getAppInfo());
//			}
		}
		
		
		
		if (getOnDragEvent() != null)
			getOnDragEvent().onStopDrag(dragAppIcon, (DesktopView) target);
		
		dragAppIcon = null;
	}
	
	private void createDragObject(int x, int y) {
		int itemPos = pointToPosition(x, y);
		if (itemPos == ListView.INVALID_POSITION) return;
		View dragView = findViewByPosition(itemPos);
		if (dragView == null) return;
		
		drgItem = LA.getItem(itemPos);
		
		b = snapshotBitmap(dragView);
		
		IBinder mWindowToken = dragView.getWindowToken();
		
		mDragView = new DragView(getContext(), b);
		mDragView.show(mWindowToken, x, y);
	}
	
	private Bitmap snapshotBitmap(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}
	
	public boolean isPointInsideView(float x, float y, View view){
		int location[] = new int[2];
		view.getLocationOnScreen(location);
		int viewX = location[0];
		int viewY = location[1];
		
		if(( x > viewX && x < (viewX + view.getWidth())) && ( y > viewY && y < (viewY + view.getHeight()))){
			return true;
		} else {
			return false;
		}
	}
	
	private View findViewByPosition(int position) {
		int firstPosition = this.getFirstVisiblePosition();
		int lastPosition = this.getLastVisiblePosition();

		if (position < firstPosition || position > lastPosition) {
			return null;
		}

		View v = this.getChildAt(position - firstPosition);

		return v;
	}
	
	public void notifyInstalledAppChange() {
		initAdapter(false);
	}
	
	private void initAdapter(final boolean fireOnFullyInited) {
		if (getOnInitState() != null)
			getOnInitState().onInitStart();
		new Thread(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {
				AllAppsTable mAllAppsTable = LocalSQL.getInstance(getContext()).getTable(AllAppsTable.class);
				
				final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
				final PackageManager pm = getContext().getPackageManager();
				final List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);
				final ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
				final ArrayList<AppInfo> fullAllApps = new ArrayList<AppInfo>();
				final int iconSize = getResources().getDimensionPixelSize(R.dimen.all_apps_icon_size);
				final ArrayList<AllApps> blockedApps = mAllAppsTable.getBlockedApps();
				final int mIconDpi = Build.VERSION.SDK_INT >= 11 ? ((ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconDensity() : 0;
				
				for(ResolveInfo iApp : installedApps) {
					AppInfo app = new AppInfo(new ComponentName(iApp.activityInfo.applicationInfo.packageName, iApp.activityInfo.name));
					app.setLabel(iApp.loadLabel(pm).toString());
					
					Drawable icon = null;
					if (Build.VERSION.SDK_INT >= 11) {
						Resources resources;
						try {
							resources = pm.getResourcesForApplication(iApp.activityInfo.applicationInfo);
						} catch (NameNotFoundException e) {
							resources = null;
						}
						
						if (resources != null) {
							int iconId = iApp.activityInfo.getIconResource();
							if (iconId != 0) {
								try {
									if (android.os.Build.VERSION.SDK_INT >= 15)
										icon = resources.getDrawableForDensity(iconId, mIconDpi);
									else
										icon = resources.getDrawable(iconId);
								} catch (Resources.NotFoundException e) {
									icon = null;
								}
							}
						}
						
						if (icon == null) {
							try {
								if (android.os.Build.VERSION.SDK_INT >= 15)
									icon = Resources.getSystem().getDrawableForDensity(android.R.mipmap.sym_def_app_icon, mIconDpi);
								else
									icon = Resources.getSystem().getDrawable(android.R.mipmap.sym_def_app_icon);
							} catch (Resources.NotFoundException e) {
								icon = null;
							}
						}
					}
					
					if (icon == null)
						icon = iApp.activityInfo.loadIcon(pm);
					
					if (icon != null) {
						Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
						if (bitmap.getWidth() != iconSize && bitmap.getHeight()!= iconSize ){
							bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
							icon = new BitmapDrawable(getResources(), bitmap);
						}
						app.setIcon(icon);
						app.setShadow(CommonUtils.makeBitmapShadow(bitmap));
					}
					
					fullAllApps.add(app);
					
					AllApps tmp = new AllApps();
					tmp.all_apps_package = iApp.activityInfo.applicationInfo.packageName;
					tmp.all_apps_class = iApp.activityInfo.name;
					if (blockedApps.contains(tmp)) continue;
					
					apps.add(app);
				}
				Collections.sort(apps, ALPHA_COMPARATOR);
				
				LA = new AllAppsAdapter(getContext(), apps, AllAppsGrid.this, fullAllApps);
				
				((Activity)getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setAdapter(LA);
						if (fireOnFullyInited && getOnFullyInited() != null) {
							getOnFullyInited().onFullyInited(LA);
						}
						if (getOnInitState() != null)
							getOnInitState().onInitEnd();
					}
				});
			}
		}).start();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final int colCount = w / getResources().getDimensionPixelSize(R.dimen.all_apps_item_size);
		post(new Runnable() {
			@Override
			public void run() {
				setNumColumns(colCount);
			}
		});
	}

	public AllAppsGrid addDropTarget(View v) {
		dropTargets.add(v);
		return this;
	}
	
	public AllAppsGrid removeDropTarget(View v) {
		dropTargets.remove(v);
		return this;
	}
	

	public OnFullyInited getOnFullyInited() {
		return mOnFullyInited;
	}

	public void setOnFullyInited(OnFullyInited mOnFullyInited) {
		this.mOnFullyInited = mOnFullyInited;
	}


	public OnDragEvent getOnDragEvent() {
		return mOnDragEvent;
	}

	public void setOnDragEvent(OnDragEvent mOnDragEvent) {
		this.mOnDragEvent = mOnDragEvent;
	}

	@Override
	public void onDragRightEdge(AppIcon icon) {
		if (getOnDragEdgeEvent() != null)
			getOnDragEdgeEvent().onDragRightEdge(icon);
	}

	@Override
	public void onDragLeftEdge(AppIcon icon) {
		if (getOnDragEdgeEvent() != null)
			getOnDragEdgeEvent().onDragLeftEdge(icon);
	}

	public OnDragEdgeEvent getOnDragEdgeEvent() {
		return mOnDragEdgeEvent;
	}

	public void setOnDragEdgeEvent(OnDragEdgeEvent mOnDragEdgeEvent) {
		this.mOnDragEdgeEvent = mOnDragEdgeEvent;
	}
	
	public void setFilter(String f) {
		if (LA != null) {
			LA.setFilter(f);
		}
	}

	public OnInitState getOnInitState() {
		return mOnInitState;
	}

	public void setOnInitState(OnInitState mOnInitState) {
		this.mOnInitState = mOnInitState;
	}
	
	public boolean isDragging() {
		return isDragging;
	}

	public static class AllAppsAdapter extends BaseAdapter {
		
		private ArrayList<AppInfo> items, itemsFiltered = new ArrayList<AppInfo>();
		private ArrayList<AppInfo> fullAllItems;
		private LayoutInflater la;
		private AllAppsGrid grid;
		private String filter = null;
		
		public AllAppsAdapter(Context ctx, ArrayList<AppInfo> allItems, AllAppsGrid g, ArrayList<AppInfo> fullAllItems) {
			items = allItems;
			la = LayoutInflater.from(ctx);
			grid = g;
			this.fullAllItems = fullAllItems;
		}
		
		public void setFilter(String f) {
			filter = f;
			itemsFiltered.clear();
			if (f != null) {
				for(AppInfo ai : items) {
					if (ai.getLabel().toLowerCase(Locale.getDefault()).contains(f.toLowerCase(Locale.getDefault()))) {
						itemsFiltered.add(ai);
					}
				}
			}
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return filter == null ? items.size() : itemsFiltered.size();
		}

		@Override
		public AppInfo getItem(int position) {
			return filter == null ? items.get(position) : itemsFiltered.get(position);
		}
		
		public AppInfo getItem(ComponentName cn) {
			AppInfo res = null;
			if (items != null && items.size() > 0) {
				for(AppInfo i : items) {
					if (i.getComponentName().equals(cn)) {
						res = i;
						break;
					}
				}
			}
			return res;
		}
		
		/**
		 * form all installed in system apps
		 * @param cn
		 * @return
		 */
		public ArrayList<AppInfo> getItems(final ComponentName cn) {
			ArrayList<AppInfo> res = new ArrayList<AppInfo>();
			if (this.fullAllItems != null && this.fullAllItems.size() > 0) {
				AppInfo foundCn = null;
				for(AppInfo i : this.fullAllItems) {
					if (cn.equals(i.getComponentName())) {
						foundCn = i;
						//Console.log("FOUND - best match: " + i);
						continue;
					}
					if (i.getComponentName().getPackageName().equals(cn.getPackageName())) {
						//Console.log("FOUND [" + cn.getPackageName() + "]: " + i.getComponentName());
						res.add(i);
					}
				}
				if (foundCn != null)
					res.add(0, foundCn);
			}
			return res;
		}
		
		public AppInfo getItem(String pkg, String cls) {
			AppInfo res = null;
			if (items != null && items.size() > 0) {
				for(AppInfo i : items) {
					if (i.getComponentName().getPackageName().equals(pkg)) {
						if (i.getComponentName().getClassName() != null && cls != null && i.getComponentName().getClassName().equals(cls)) {
							res = i;
							break;
						}
					}
				}
			}
			return res;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo item = getItem(position);
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = la.inflate(R.layout.item_all_apps, grid, false);
				holder = new ViewHolder((AppIcon) convertView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.setup(item);
			return convertView;
		}
		
		private class ViewHolder {
			private AppIcon t;
			public ViewHolder(AppIcon txt) {
				t = txt;
			}
			public void setup(AppInfo i) {
				t.setText(i.getLabel());
				t.setCompoundDrawablesWithIntrinsicBounds(null, i.getIcon(), null, null);
				t.setAppInfo(i);
			}
		}
	}

}
