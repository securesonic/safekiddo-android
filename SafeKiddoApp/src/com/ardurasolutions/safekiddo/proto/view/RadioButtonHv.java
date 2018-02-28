package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.Fonts;

public class RadioButtonHv extends RadioButton {

	public RadioButtonHv(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode())
			setCustomFont(context, attrs);
	}
	
	public RadioButtonHv(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode())
			setCustomFont(context, attrs);
	}
	
	public RadioButtonHv(Context context) {
		super(context);
		if (!isInEditMode())
			setCustomFont(context, null);
	}
	
	private void setCustomFont(Context ctx, AttributeSet attrs) {
		Fonts f = Fonts.getInstance(ctx);
		if (attrs != null) {
			TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewHv);
			int font = a.getInt(R.styleable.TextViewHv_font, getResources().getInteger(R.integer.default_font));
			switch(font) {
				default: 
				case 0: f.applyFont(this, Fonts.FontName.THIN); break;
				case 1: f.applyFont(this, Fonts.FontName.LIGHT); break;
				case 2: f.applyFont(this, Fonts.FontName.REGULAR); break;
				case 3: f.applyFont(this, Fonts.FontName.BOLD); break;
			}
			a.recycle();
		} else {
			f.applyFont(this, Fonts.FontName.fromInt(getResources().getInteger(R.integer.default_font)));
		}
	}

}
