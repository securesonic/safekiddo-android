package com.ardurasolutions.safekiddo.proto.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.ardurasolutions.safekiddo.R;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };
    
    private static final int[] DISABLED_STATE_SET = {
        -android.R.attr.state_enabled
    };
    
    private boolean checked = false;

    @SuppressLint("NewApi")
    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckableLinearLayout);
        checked = ta.getBoolean(R.styleable.CheckableLinearLayout_marked, false);
        ta.recycle();
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CheckableLinearLayout);
        checked = ta.getBoolean(R.styleable.CheckableLinearLayout_marked, false);
        ta.recycle();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	refreshDrawableState();
    }

    public CheckableLinearLayout(Context context) {
        super(context);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }
    
    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
        
        refreshDrawableState();
    
        //Propagate to childs
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if(child instanceof Checkable) {
                ((Checkable)child).setChecked(checked);
            }
        }
    }
    
    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isEnabled()) {
	        if (isChecked()) {
	            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
	        }
        } else {
        	mergeDrawableStates(drawableState, DISABLED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public void toggle() {
        this.checked = !this.checked;
    }
}
