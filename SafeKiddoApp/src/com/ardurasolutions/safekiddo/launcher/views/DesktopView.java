package com.ardurasolutions.safekiddo.launcher.views;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.launcher.proto.AppInfo;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEdgeEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDragEvent;
import com.ardurasolutions.safekiddo.launcher.proto.OnDrawIcon;
import com.ardurasolutions.safekiddo.launcher.proto.OnDropIcon;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.DesktopConfigTable;

public class DesktopView extends RelativeLayout implements OnDropIcon, OnDrawIcon, OnDragEdgeEvent, OnDragEvent {
	
	public static final float ITEM_HEIGHT_RATIO = 1.3f;
	
	private Bitmap iconShadow = null;
	private int iconShadowX = -1, iconShadowY = -1;
	private Paint iconShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint iconShadowPaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int iconWidth = 0, colCount = 0, iconHeight = 0;
	private boolean showOnlyIcon = false;
	private boolean singleLine = false;
	private int iconSize = 48;
	private ArrayList<View> otherDropTargets = new ArrayList<View>();
	private View deleteView;
	private OnDragEdgeEvent mOnDragEdgeEvent;
	private OnDragEvent mOnDragEvent;

	public DesktopView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public DesktopView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DesktopView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		//gridPaint.setAlpha(150);
		gridPaint.setStyle(Style.STROKE);
		gridPaint.setColor(0x99FFFFFF);
		
		iconShadowPaintBg.setStyle(Style.FILL_AND_STROKE);
		iconShadowPaintBg.setColor(0x33ffffff);
		
		//iconShadowPaint.setStyle(Style.FILL_AND_STROKE);
		//iconShadowPaint.setColor(0x00000000);
		
		if (!isInEditMode())
			iconSize = getResources().getDimensionPixelSize(R.dimen.all_apps_item_size);
		
		calculateSizes();
		
		 post(new Runnable() {
			@Override
			public void run() {
				calculateSizes();
			}
		 });
	}
	
	private void calculateSizes() {
		int width = getMeasuredWidth();
		if (width > 0) {
			colCount = width / getIconSize();
			iconWidth = width / colCount;
			iconHeight = isSingleLine() ? getMeasuredHeight() - 1 : (isShowOnlyIcon() ? iconWidth : (int) (iconWidth * ITEM_HEIGHT_RATIO));
		}
	}
	
	public int getMaxRowsCount() {
		return getMeasuredHeight() / iconHeight;
	}
	
	public boolean isViewInPos(int x, int y) {
		return getChildAtPos(x, y) != null;
	}
	
	/**
	 * 
	 * @param item - item from all apps adapter
	 * @param r - liczone od dołu
	 * @param c - liczone od lewej
	 */
	public void addIconAndSave(AppInfo item, int r, int c, boolean withAnimation) {
		if (item != null) {
			int height = getMeasuredHeight();
			Rect boxRect = new Rect(c * iconWidth, height - ((r + 1) * iconHeight), (c + 1) * iconWidth, height - (r * iconHeight));
			
			final AppIcon child = (AppIcon) LayoutInflater.from(getContext()).inflate(R.layout.item_all_apps, this, false);
			child.setAppInfo(item);
			child.setText(item.getLabel());
			child.setClickable(true);
			child.setShowLabel(!isShowOnlyIcon());
			child.addDropTarget(this);
			child.setDeleteView(getDeleteView());
			child.initDandD();
			if (otherDropTargets.size() > 0) {
				for(int i=0; i<otherDropTargets.size(); i++) {
					child.addDropTarget(otherDropTargets.get(i));
				}
			}
			
			LayoutParams lp = new LayoutParams(iconWidth, iconHeight);
			lp.setMargins(boxRect.left, boxRect.top, 0, 0);
			child.setLayoutParams(lp);
			
			LocalSQL.getInstance(getContext()).getTable(DesktopConfigTable.class).saveIcon(this, child);
			
			addView(child);
			
			if (withAnimation) {
				Animation showAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.desktop_icon_show);
				showAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						child.clearAnimation();
					}
					@Override
					public void onAnimationRepeat(Animation animation) { }
				});
				child.startAnimation(showAnimation);
			}
		}
	}
	
	public void addIcon(AppInfo drgItem, int l, int t, Long dbId) {
		AppIcon child = (AppIcon) LayoutInflater.from(getContext()).inflate(R.layout.item_all_apps, this, false);
		child.setAppInfo(drgItem);
		child.setText(drgItem.getLabel());
		child.setClickable(true);
		child.setShowLabel(!isShowOnlyIcon());
		child.addDropTarget(this);
		child.setDeleteView(getDeleteView());
		child.initDandD();
		child.setDbId(dbId);
		if (otherDropTargets.size() > 0) {
			for(int i=0; i<otherDropTargets.size(); i++) {
				child.addDropTarget(otherDropTargets.get(i));
			}
		}
		
		LayoutParams lp = new LayoutParams(iconWidth, iconHeight);
		lp.setMargins(l, t, 0, 0);
		child.setLayoutParams(lp);
		
		addView(child);
	}
	
	@Override
	public boolean onDropIcon(AppInfo drgItem, int x, int y, Long dbId) {
		// FIX: Issue #9
		if (iconWidth > 0 && iconHeight > 0) {
			int height = getMeasuredHeight();
			int col = x / iconWidth;
			int row = (height - y) / iconHeight;
			Rect boxRect = new Rect(col * iconWidth, height - ((row + 1) * iconHeight), (col + 1) * iconWidth, height - (row * iconHeight));
			
			if (boxRect.top > 0) {
				
				View childAtPos = getChildAtPos(x, y);
				if (childAtPos == null) {
					AppIcon child = (AppIcon) LayoutInflater.from(getContext()).inflate(R.layout.item_all_apps, this, false);
					child.setAppInfo(drgItem);
					child.setText(drgItem.getLabel());
					child.setClickable(true);
					child.setShowLabel(!isShowOnlyIcon());
					child.addDropTarget(this);
					child.setDeleteView(getDeleteView());
					child.initDandD();
					child.setDbId(dbId);
					
					if (otherDropTargets.size() > 0) {
						for(int i=0; i<otherDropTargets.size(); i++) {
							child.addDropTarget(otherDropTargets.get(i));
						}
					}
					
					LayoutParams lp = new LayoutParams(iconWidth, iconHeight);
					lp.setMargins(boxRect.left, boxRect.top, 0, 0);
					child.setLayoutParams(lp);
					
					// INFO
					
					LocalSQL.getInstance(getContext())
						.getTable(DesktopConfigTable.class)
						.saveIcon(this, child);
					
					addView(child);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onDrawIcon(Bitmap b, int x, int y) {
		iconShadow = b;
		iconShadowX = x;
		iconShadowY = y;
		invalidate();
	}
	
	@Override
	public void onClearicon() {
		iconShadow = null;
		iconShadowX = -1;
		iconShadowY = -1;
		invalidate();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		final int height = getMeasuredHeight();
		// final int width = getMeasuredWidth();
		
		// TODO error: divide by zero
		if (iconShadow != null && iconWidth > 0 && iconHeight > 0) {
			int x = iconShadowX;
			int y = iconShadowY;
			int col = x / iconWidth;
			int row = (height - y) / iconHeight;
			
			Rect boxRect = new Rect(col * iconWidth, height - ((row + 1) * iconHeight), (col + 1) * iconWidth, height - (row * iconHeight));
			View childAtPos = getChildAtPos(x, y);

			if (boxRect.top > 0 && (childAtPos == null || (childAtPos != null && childAtPos.getVisibility() != View.VISIBLE))) {
				canvas.drawRect(boxRect, iconShadowPaintBg);
				canvas.drawBitmap(iconShadow, boxRect.centerX() - (iconShadow.getWidth() / 2), boxRect.centerY() - (iconShadow.getHeight() / 2), iconShadowPaint);
			}
		}
	}
	
	public View getChildAtPos(int x, int y) {
		View res = null;
		
		for(int i=0; i<getChildCount(); i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			Rect r = new Rect(lp.leftMargin, lp.topMargin, lp.leftMargin + child.getMeasuredWidth(), lp.topMargin + child.getMeasuredHeight());
			if (r.contains(x, y)) {
				res = child;
				break;
			}
		}
		
		return res;
	}
	
	public DesktopView addOtherDropTarget(View v) {
		otherDropTargets.add(v);
		return this;
	}
	
	public DesktopView addOtherDropTargets(ArrayList<View> vl) {
		otherDropTargets.addAll(vl);
		return this;
	}
	
	public DesktopView removeOtherDropTarget(View v) {
		otherDropTargets.remove(v);
		return this;
	}

	public boolean isShowOnlyIcon() {
		return showOnlyIcon;
	}

	public void setShowOnlyIcon(boolean showOnlyIcon) {
		this.showOnlyIcon = showOnlyIcon;
		calculateSizes();
	}

	public int getIconSize() {
		return iconSize;
	}

	public void setIconSize(int iconSize) {
		this.iconSize = iconSize;
		calculateSizes();
	}

	public boolean isSingleLine() {
		return singleLine;
	}

	public void setSingleLine(boolean singleLine) {
		this.singleLine = singleLine;
	}

	public View getDeleteView() {
		return deleteView;
	}

	public void setDeleteView(View deleteView) {
		this.deleteView = deleteView;
	}
	
	public ArrayList<View> getOtherDropTargets() {
		return otherDropTargets;
	}
	
	public void clearOtherTargets() {
		getOtherDropTargets().clear();
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

	@Override
	public void onStartDrag(AppIcon appIcon, DesktopView dv) {
		if (getOnDragEvent() != null)
			getOnDragEvent().onStartDrag(appIcon, this);
	}

	@Override
	public void onStopDrag(AppIcon appIcon, DesktopView dv) {
		if (getOnDragEvent() != null)
			getOnDragEvent().onStopDrag(appIcon, this);
	}

	public OnDragEvent getOnDragEvent() {
		return mOnDragEvent;
	}

	public void setOnDragEvent(OnDragEvent mOnDragEvent) {
		this.mOnDragEvent = mOnDragEvent;
	}

	public ArrayList<AppIcon> findIcons(String pkg, String className) {
		ArrayList<AppIcon> res = new ArrayList<AppIcon>();
		
		for(int i=0; i<getChildCount(); i++) {
			AppIcon icon = (AppIcon) getChildAt(i);
			if (icon.getAppInfo().isEqualTo(pkg, className)) {
				res.add(icon);
			}
		}
		
		return res;
	}
	
	public ArrayList<AppIcon> getIcons() {
		ArrayList<AppIcon> res = new ArrayList<AppIcon>();
		for(int i=0; i<getChildCount(); i++) {
			res.add((AppIcon) getChildAt(i));
		}
		return res;
	}
}
