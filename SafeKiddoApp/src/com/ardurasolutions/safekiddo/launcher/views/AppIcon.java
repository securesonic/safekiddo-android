package com.ardurasolutions.safekiddo.launcher.views;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout.LayoutParams;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.Fonts;
import com.ardurasolutions.safekiddo.launcher.proto.AppInfo;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEdgeEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDrawIcon;
import com.ardurasolutions.safekiddo.launcher.proto.OnDropIcon;
import com.ardurasolutions.safekiddo.proto.view.TextViewHv;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.DesktopConfigTable;
import com.hv.console.Console;

public class AppIcon extends TextViewHv {
	
	private static final float PRESS_ALPHA = 0.4f;
	public static final int DRAG_BORDER_TIME = 1000;
	public static final int DRAG_BORDER_EDGE_DIVIDER = 3;
	
	private AppInfo mAppInfo;
	private boolean showLabel;
	private int drawablePadd;
	private Long dbId = null;
	
	private int savedDownX = -1, savedDownY = -1;
	private boolean isDragging = false;
	private DragView mDragView;
	private Bitmap b;
	public ArrayList<View> dropTargets = new ArrayList<View>();
	private View currentDropTarget = null;
	private View deleteView;
	private OnDragEdgeEvent mOnDragEdgeEvent;
	private OnDragEvent mOnDragEvent;

	public AppIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public AppIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AppIcon(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		drawablePadd = getCompoundDrawablePadding();
		
		post(new Runnable() {
			@Override
			public void run() {
				try {
					mOnDragEdgeEvent = (OnDragEdgeEvent) getParent();
				} catch (ClassCastException e) { }
				try {
					mOnDragEvent = (OnDragEvent) getParent();
				} catch (ClassCastException e) { }
			}
		});
	}
	
	@Override
	protected void setCustomFont(Context ctx, AttributeSet attrs) {
		Fonts
			.getInstance(ctx)
			.applyFont(this, Fonts.FontName.CONDENSED);
	}
	
	public void initDandD() {
		setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (startDragging()) {
					if (savedDownX != -1 && savedDownY != -1) {
						if (getParent() instanceof DesktopView) {
							dropTargets.clear();
							dropTargets.addAll(((DesktopView) getParent()).getOtherDropTargets());
							dropTargets.add((DesktopView) getParent());
						}
						createDragObject(savedDownX, savedDownY);
						savedDownX = -1;
						savedDownY = -1;
						setVisibility(View.INVISIBLE);
						
						if (mOnDragEvent != null)
							mOnDragEvent.onStartDrag(AppIcon.this, (DesktopView) getParent());
					}
					return true;
				}
				return false;
			}
		});
		setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickAction();
			}
		});
	}
	
	@SuppressLint("NewApi")
	public void onClickAction() {
		try {
			if (Console.isEnabled())
				Console.logd("RUN: " + getAppInfo());
			
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			i.setComponent(new ComponentName(getAppInfo().getComponentName().getPackageName(), getAppInfo().getComponentName().getClassName()));
			
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(this, 0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
				getContext().startActivity(i, opts.toBundle());
			} else
				getContext().startActivity(i);
			
		} catch (ActivityNotFoundException e) {
			if (Console.isEnabled())
				Console.loge("AppIcon::onClick[ActivityNotFound]", e);
		}
	}
	
	private boolean isOnRightBorder = false, onRightEventFired = false;
	private long isOnRightBorderTime = 0l;
	private boolean isOnLeftBorder = false, onLeftEventFired = false;
	private long isOnLeftBorderTime = 0l;
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getActionMasked()) {
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
					
					boolean isOverDelete = false;
					
					if (getOnDragEdgeEvent() != null) {
						if (event.getRawX() >= getResources().getDisplayMetrics().widthPixels - (getMeasuredWidth() / DRAG_BORDER_EDGE_DIVIDER)) {
							if (!isOnRightBorder)
								isOnRightBorderTime = System.currentTimeMillis();
							isOnRightBorder = true;
						} else {
							isOnRightBorder = false;
							isOnRightBorderTime = 0L;
							onRightEventFired = false;
						}
						
						if (isOnRightBorder && (System.currentTimeMillis() - isOnRightBorderTime) > DRAG_BORDER_TIME) {
							if (!onRightEventFired) {
								getOnDragEdgeEvent().onDragRightEdge(this);
								onRightEventFired = true;
							}
						}
						
						if (event.getRawX() <= (getMeasuredWidth() / DRAG_BORDER_EDGE_DIVIDER)) {
							if (!isOnLeftBorder)
								isOnLeftBorderTime = System.currentTimeMillis();
							isOnLeftBorder = true;
						} else {
							isOnLeftBorder = false;
							isOnLeftBorderTime = 0L;
							onLeftEventFired = false;
						}
						
						if (isOnLeftBorder && (System.currentTimeMillis() - isOnLeftBorderTime) > DRAG_BORDER_TIME) {
							if (!onLeftEventFired) {
								getOnDragEdgeEvent().onDragLeftEdge(this);
								onLeftEventFired = true;
							}
						}
					}
					
					
					if (getDeleteView() != null) {
						if (isPointInsideView(event.getRawX(), event.getRawY(), getDeleteView())) {
							getDeleteView().setBackgroundResource(R.drawable.delete_view_bg_active);
							if (currentDropTarget != null) {
								OnDrawIcon odi = null;
								try {
									odi = (OnDrawIcon) currentDropTarget;
								} catch (ClassCastException e) {}
								
								if (odi != null) {
									odi.onClearicon();
								}
							}
							isOverDelete = true;
						}
					}
					
					if (!isOverDelete) {
						if (getDeleteView() != null) {
							getDeleteView().setBackgroundResource(R.drawable.delete_view_bg);
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
										odi.onDrawIcon(mAppInfo.getShadow(), (int) event.getRawX() - location[0], (int) event.getRawY() - location[1]);
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
					}
					return true;	
				} else {
					return false;
				}
					//break;
				//return true;
			
			case MotionEvent.ACTION_UP:
				onRightEventFired = false;
				setVisibility(View.VISIBLE);
				
				boolean isOverDelete = false;
				
				if (getDeleteView() != null) {
					if (isPointInsideView(event.getRawX(), event.getRawY(), getDeleteView())) {
						isOverDelete = true;
						((ViewGroup) getParent()).removeView(this);
						LocalSQL.getInstance(getContext()).getTable(DesktopConfigTable.class).deleteIcon(this);
					}
				}
				
				View targetViewUp = currentDropTarget;
				if (!isOverDelete) {
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
									if (odi.onDropIcon(mAppInfo, (int) event.getRawX() - location[0], (int) event.getRawY() - location[1], this.getDbId())) {
										if (getParent() instanceof DesktopView)
											((ViewGroup) getParent()).removeView(this);
									}
								}
								break;
							}
						}
						currentDropTarget = null;
					}
				}
				stopDragging(targetViewUp);
				
			break;
			
			case MotionEvent.ACTION_CANCEL:
				onRightEventFired = false;
				setVisibility(View.VISIBLE);
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

	private boolean startDragging() {
		if (isDragging) return false;
		isDragging = true;
		if (getDeleteView() != null) {
			getDeleteView().setBackgroundResource(R.drawable.delete_view_bg);
			getDeleteView().setVisibility(View.VISIBLE);
			getDeleteView().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.delete_view_in));
		}
		return true;
	}
	
	private void stopDragging(View target) {
		isDragging = false;
		if (mDragView != null) {
			mDragView.remove();
			mDragView = null;
		}
		
		if (target != null) {
			OnDrawIcon odi = null;
			try {
				odi = (OnDrawIcon) target;
			} catch (ClassCastException e) {}
			
			if (odi != null) {
				odi.onClearicon();
			}
		}
		
		if (getDeleteView() != null && getDeleteView().getVisibility() == View.VISIBLE) {
			Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.delete_view_out);
			a.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) { }
				@Override
				public void onAnimationRepeat(Animation animation) { }
				@Override
				public void onAnimationEnd(Animation animation) {
					getDeleteView().setVisibility(View.GONE);
				}
			});
			getDeleteView().startAnimation(a);
		}
		
		if (mOnDragEvent != null)
			mOnDragEvent.onStopDrag(this, (DesktopView) target);
	}
	
	private void createDragObject(int x, int y) {
		b = snapshotBitmap(this);
		
		IBinder mWindowToken = this.getWindowToken();
		//IBinder mWindowToken = dropTargets.get(0).getWindowToken();
		
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
	
	private void update() {
		Drawable icon = getAppInfo().getIcon();
		
		if (isShowLabel()) {
			setCompoundDrawablePadding(drawablePadd);
			setLines(1);
			setText(getAppInfo().getLabel());
		} else {
			setText(" ");
			setLines(1);
			setCompoundDrawablePadding(0);
			
			int iconSize = getResources().getDimensionPixelSize(R.dimen.desktop_fast_bar_icon_size);
			
			Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
			if (bitmap.getWidth() != iconSize && bitmap.getHeight()!= iconSize ){
				bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
				icon = new BitmapDrawable(getResources(), bitmap);
			}
		}
		
		setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
	}
	
	public AppIcon addDropTarget(View v) {
		dropTargets.add(v);
		return this;
	}
	
	public AppIcon removeDropTarget(View v) {
		dropTargets.remove(v);
		return this;
	}
	
	public void clearDropTargets() {
		dropTargets.clear();
	}

	public boolean isShowLabel() {
		return showLabel;
	}

	public AppIcon setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
		update();
		return this;
	}

	public AppInfo getAppInfo() {
		return mAppInfo;
	}

	public AppIcon setAppInfo(AppInfo mAppInfo) {
		this.mAppInfo = mAppInfo;
		return this;
	}
	
	@SuppressLint("NewApi")
	private void setAlphaCompat(float a) {
		if (Build.VERSION.SDK_INT < 11) {
			AlphaAnimation alpha = new AlphaAnimation(a, a);
			alpha.setDuration(0); // Make animation instant
			alpha.setFillAfter(true); // Tell it to persist after the animation ends
			startAnimation(alpha);
		} else {
			setAlpha(a);
		}
	}
	
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (isPressed()) {
			setAlphaCompat(PRESS_ALPHA);
		} else //if (!mLockDrawableState) {
			setAlphaCompat(1f);
		//}
	}

	public View getDeleteView() {
		return deleteView;
	}

	public void setDeleteView(View deleteView) {
		this.deleteView = deleteView;
	}
	
	public int getLeftMargin() {
		LayoutParams lp = (LayoutParams) getLayoutParams();
		return lp.leftMargin;
	}
	
	public int getTopMargin() {
		LayoutParams lp = (LayoutParams) getLayoutParams();
		return lp.topMargin;
	}

	public OnDragEdgeEvent getOnDragEdgeEvent() {
		return mOnDragEdgeEvent;
	}

	public void setOnDragEdgeEvent(OnDragEdgeEvent mOnDragEdgeEvent) {
		this.mOnDragEdgeEvent = mOnDragEdgeEvent;
	}

	public Long getDbId() {
		return dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}

}
