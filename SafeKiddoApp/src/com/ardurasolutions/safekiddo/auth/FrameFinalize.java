package com.ardurasolutions.safekiddo.auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.ardurasolutions.safekiddo.R;

public class FrameFinalize extends Fragment {
	
	public static interface OnFinalizeSelect {
		public void onFinalizeSelect();
		public void onFinalizeSelectBack();
	}
	
	private OnFinalizeSelect mOnFinalizeSelect;
	private boolean installLauncher = true;
	private View textView2, progressOverlay;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_frame_finalize, container, false);
		
		textView2 = v.findViewById(R.id.textView2);
		progressOverlay = v.findViewById(R.id.progressOverlay);
		
		v.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				progressOverlay.setVisibility(View.VISIBLE);
				Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.show_layer);
				a.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						progressOverlay.clearAnimation();
					}
					@Override public void onAnimationRepeat(Animation animation) { }
				});
				progressOverlay.startAnimation(a);
				if (mOnFinalizeSelect != null)
					mOnFinalizeSelect.onFinalizeSelect();
			}
		});
		v.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnFinalizeSelect != null)
					mOnFinalizeSelect.onFinalizeSelectBack();
			}
		});
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnFinalizeSelect = (OnFinalizeSelect) activity;
		} catch (ClassCastException e) {}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		textView2.setVisibility(isInstallLauncher() ? View.VISIBLE : View.INVISIBLE);
	}

	public boolean isInstallLauncher() {
		return installLauncher;
	}

	public FrameFinalize setInstallLauncher(boolean installLauncher) {
		this.installLauncher = installLauncher;
		return this;
	}

}
