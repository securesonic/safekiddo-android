package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewHv extends ListView {

	public ListViewHv(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		try {
			super.dispatchDraw(canvas);
		} catch (IndexOutOfBoundsException e) {
			
		}
	}

}