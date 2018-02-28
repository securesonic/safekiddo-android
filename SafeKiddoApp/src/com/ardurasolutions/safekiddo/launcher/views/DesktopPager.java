package com.ardurasolutions.safekiddo.launcher.views;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

public class DesktopPager extends ViewPager {
	
	private boolean activated = true;

	public DesktopPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DesktopPager(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		try {
			Class<?> viewpager = ViewPager.class;
			Field scroller = viewpager.getDeclaredField("mScroller");
			scroller.setAccessible(true);
			scroller.set(this, new MyScroller(getContext()));
		} catch (Exception e)  {
			e.printStackTrace();
		}
	}
	
	public void activate() {
		activated = true;
	}
	
	public void deactivate() {
		activated = false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (activated) {
			try {
				return super.onInterceptTouchEvent(event);
			} catch (Exception e) {
				return true;
			}
		} else {
			return false;
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			return super.onTouchEvent(event);
		} catch (Exception e) {
			return true;
		}
	}
	
	public class MyScroller extends Scroller {
		public MyScroller(Context context) {
			super(context, new DecelerateInterpolator());
		}
		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, 300);
		}
	}
	
}
