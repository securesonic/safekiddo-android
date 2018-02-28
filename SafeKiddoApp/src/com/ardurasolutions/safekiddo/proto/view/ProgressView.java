package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ardurasolutions.safekiddo.R;

public class ProgressView extends View {
	
	private int progress = 0;
	private Paint mPaint, mPaintBg;
	
	private void init() {
		mPaint = new Paint();
		mPaint.setColor(getResources().getColor(R.color.sk_orange));
		mPaint.setStrokeWidth(0);
		
		mPaintBg = new Paint();
		mPaintBg.setColor(getResources().getColor(R.color.sk_white));
		mPaintBg.setStrokeWidth(0);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int wx = (int)((canvas.getWidth() / 100D) * getProgress());
		if (wx > 0) {
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaintBg);
			canvas.drawRect(0, 0, wx, canvas.getHeight(), mPaint);
		}
	}

}
