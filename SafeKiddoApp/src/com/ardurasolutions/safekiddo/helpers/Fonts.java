package com.ardurasolutions.safekiddo.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class Fonts {
	
	public static enum FontName {
		//DEFAULT(2), 
		THIN(0), 
		LIGHT(1), 
		REGULAR(2), 
		BOLD(3),
		CONDENSED(4);
		
		private int id = 0;
		FontName(int idx) {
			this.id = idx;
		}
		
		public int getValue() {
			return id;
		}
		
		public static FontName fromInt(int val) {
			FontName res = FontName.REGULAR;
			for(FontName f : FontName.values()) {
				if (f.getValue() == val) {
					res = f;
					break;
				}
			}
			return res;
		}
	};
	
	private static Fonts mInstance;
	public static synchronized Fonts getInstance(Context ctx) {
		if (mInstance == null)
			mInstance = new Fonts(ctx);
		return mInstance;
	}
	
	private Typeface fontThin, fontLight, fontRegular, fontBold, condensed;// = Typeface.createFromAsset(getAssets(), fontPath);
	
	public Fonts(Context ctx) {
		fontThin = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Thin.ttf");
		fontLight = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Light.ttf");
		fontRegular = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Regular.ttf"); 
		fontBold = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Bold.ttf");
		condensed = Typeface.createFromAsset(ctx.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
	}
	
	public Typeface getTypeface(FontName fn) {
		Typeface tf = fontThin;
		switch(fn) {
			default:
			//case DEFAULT: tf = fontLight; break;
			case THIN: tf = fontThin; break;
			case LIGHT: tf = fontLight; break;
			case REGULAR: tf = fontRegular; break;
			case BOLD: tf = fontBold; break;
			case CONDENSED: tf = condensed; break;
		}
		return tf;
	}
	
	public Fonts applyFont(TextView txt, FontName fn) {
		txt.setTypeface(getTypeface(fn));
		return this;
	}

}
