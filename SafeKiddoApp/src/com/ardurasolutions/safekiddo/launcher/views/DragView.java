package com.ardurasolutions.safekiddo.launcher.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class DragView extends View {
	
	private Bitmap mBitmap;
	private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
	private WindowManager.LayoutParams mLayoutParams;
	private WindowManager mWindowManager;

	public DragView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DragView(Context context, Bitmap bitmap) {
		super(context);
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mBitmap = bitmap;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0.0f, 0.0f, p);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mBitmap.getWidth(), mBitmap.getHeight());
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mBitmap.recycle();
	}
	
	public void show(IBinder windowToken, int touchX, int touchY) {
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.format = PixelFormat.TRANSLUCENT;
		lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		lp.x = touchX - (mBitmap.getWidth() / 2);
		lp.y = touchY - (mBitmap.getHeight() / 2);
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
		lp.gravity = Gravity.LEFT | Gravity.TOP;
		lp.token = windowToken;
		lp.setTitle("DragView");
		lp.windowAnimations = 0;
		mLayoutParams = lp;
		
		mWindowManager.addView(this, lp);
	}
	
	void move(int touchX, int touchY) {
		WindowManager.LayoutParams lp = mLayoutParams;
		lp.x = touchX - (getMeasuredWidth() / 2);
		lp.y = touchY - (getMeasuredHeight() / 2);
		mWindowManager.updateViewLayout(this, lp);
	}

	void remove() {
		mWindowManager.removeView(this);
	}

}
