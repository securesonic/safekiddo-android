package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ardurasolutions.safekiddo.R;

public class CircleProgressBar extends RelativeLayout {
	
	private ImageView img1, img2;
	private RotateAnimation anim, anim2;
	private int duration = 900;

	public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public CircleProgressBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		img1 = new ImageView(this.getContext());
		img2 = new ImageView(this.getContext());
		
		img1.setImageResource(R.drawable.circle_animation_1);
		img2.setImageResource(R.drawable.circle_animation_2);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		img1.setLayoutParams(lp);
		img2.setLayoutParams(lp);
		
		addView(img1);
		addView(img2);
		
		anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setRepeatMode(Animation.RESTART);
		anim.setDuration(getAnimDuration());
		
		anim2 = new RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim2.setInterpolator(new LinearInterpolator());
		anim2.setRepeatCount(Animation.INFINITE);
		anim2.setRepeatMode(Animation.RESTART);
		anim2.setDuration(getAnimDuration());
		
		animStart();
	}
	
	public void animStart(){
		img1.startAnimation(anim);
		img2.startAnimation(anim2);
	}
	
	public void animStop() {
		img1.clearAnimation();
		img2.clearAnimation();
	}

	public int getAnimDuration() {
		return duration;
	}
	
	public void resetAnimation() {
		animStop();
		animStart();
	}

	public void setAnimDuration(int duration) {
		this.duration = duration;
		resetAnimation();
	}

}
