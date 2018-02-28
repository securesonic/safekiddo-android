package com.ardurasolutions.safekiddo.launcher.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class DesktopPagerIndicator extends View {
	
	private Paint spotPaint, spotSelectedPaint;
	private int points = 0;
	private int selectedPoint = 0;

	public DesktopPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	public DesktopPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DesktopPagerIndicator(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		spotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		spotPaint.setColor(0x11FFFFFF);
		spotPaint.setStyle(Style.FILL_AND_STROKE);
		
		spotSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		spotSelectedPaint.setColor(0x55FFFFFF);
		spotSelectedPaint.setStyle(Style.FILL_AND_STROKE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (getPoints() > 0) {
			int width = getMeasuredWidth();
			int height = getMeasuredHeight();
			int height_2 = (height / 2);
			int spotSize = height + height_2;
			int startPos = (width / 2) - ((spotSize * (getPoints() - 1)) / 2);
			
			for(int i=0; i<getPoints(); i++) {
				canvas.drawCircle(startPos, height_2, height_2, i == getSelectedPoint() ? spotSelectedPaint : spotPaint);
				startPos += spotSize;
			}
		}
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getSelectedPoint() {
		return selectedPoint;
	}

	public void setSelectedPoint(int selectedPoint) {
		this.selectedPoint = selectedPoint;
		invalidate();
	}

}
