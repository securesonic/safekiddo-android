package com.ardurasolutions.safekiddo.proto.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Fonts;

public class AutoCompleteTextViewHv extends AutoCompleteTextView {
	
	public static interface OnRightIconClick {
		public void onIconClick();
	}
	
	public static interface OnKeyboardEvent {
		public void onKeyboardEvent(boolean isShowing, boolean fromPreIme);
	}
	
	public static interface OnKeyboardBack {
		public void onKeyboardBack();
	}
	
	private Drawable dRight;
	private int rightButtonImgRes = R.drawable.ic_action_remove_small;
	private OnRightIconClick mOnRightIconClick;
	private OnKeyboardEvent mOnKeyboardEvent;
	private OnKeyboardBack mOnKeyboardBack;
	//private boolean onceDisableKeyEvent = false;
	private String displayText = "";

	public AutoCompleteTextViewHv(Context context) {
		super(context);
		if (!isInEditMode())
			setCustomFont(context, null);
	}

	public AutoCompleteTextViewHv(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode())
			setCustomFont(context, attrs);
	}

	public AutoCompleteTextViewHv(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode())
			setCustomFont(context, attrs);
	}
	
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getOnKeyboardBack() != null)
				getOnKeyboardBack().onKeyboardBack();
			return false;
		}
		return super.onKeyPreIme(keyCode, event);
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		setRightIcon(focused ? R.drawable.ic_action_remove_small : 0);
		if (focused) {
			if (getOnKeyboardEvent() != null)
				getOnKeyboardEvent().onKeyboardEvent(true, false);
			CommonUtils.showKeyboard(getContext(), this);
		} else {
			if (getOnKeyboardEvent() != null)
				getOnKeyboardEvent().onKeyboardEvent(false, false);
		}
	}
	
	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
	}

	public void setRightIcon(int resId) {
		rightButtonImgRes = resId;
		if (rightButtonImgRes > 0) {
			setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], null, getResources().getDrawable(rightButtonImgRes), null);
			setPadding(getPaddingLeft(), getPaddingTop(), 0, getPaddingBottom());
		} else {
			setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[0], null, null, null);
			setPadding(getPaddingLeft(), getPaddingTop(), (int) (getResources().getDisplayMetrics().density * 10), getPaddingBottom());
		}
		dRight = resId == 0 ? null : getCompoundDrawables()[2];
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_UP) {
			if (dRight != null && (int) event.getX() >= getMeasuredWidth() - dRight.getBounds().width() - 10) {
				//performClick();
				if (getOnRightIconClick() != null) {
					getOnRightIconClick().onIconClick();
				}
				event.setAction(MotionEvent.ACTION_CANCEL);
			}
		}
		
		return super.onTouchEvent(event);
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
	
	@Override
	protected void finalize() throws Throwable {
		dRight = null;
		super.finalize();
	}

	public OnRightIconClick getOnRightIconClick() {
		return mOnRightIconClick;
	}

	public void setOnRightIconClick(OnRightIconClick mOnRightIconClick) {
		this.mOnRightIconClick = mOnRightIconClick;
	}

	public OnKeyboardEvent getOnKeyboardEvent() {
		return mOnKeyboardEvent;
	}

	public void setOnKeyboardEvent(OnKeyboardEvent mOnKeyboardEvent) {
		this.mOnKeyboardEvent = mOnKeyboardEvent;
	}

	public OnKeyboardBack getOnKeyboardBack() {
		return mOnKeyboardBack;
	}

	public void setOnKeyboardBack(OnKeyboardBack mOnKeyboardBack) {
		this.mOnKeyboardBack = mOnKeyboardBack;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String displayText) {
		this.displayText = displayText;
	}
	
	public void copyTextToDisplayText() {
		setDisplayText(getText().toString());
	}

}
