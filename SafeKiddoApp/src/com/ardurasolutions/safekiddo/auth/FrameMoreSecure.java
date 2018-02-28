package com.ardurasolutions.safekiddo.auth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.ardurasolutions.safekiddo.R;

public class FrameMoreSecure extends Fragment {
	
	public static interface OnMoreSecureSelect {
		public void onMoreSecureSelect(boolean isChecked, boolean useLauncher);
		public void onMoreSecureSelectBack(boolean isChecked, boolean useLauncher);
	}
	
	private OnMoreSecureSelect mOnMoreSecureSelect;
	private CheckBox moreSecureCheckbox, useLauncherCheckbox;
	private boolean isMoreSecure = false;
	private boolean isUseLauncher = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_frame_more_secure, container, false);
		
		moreSecureCheckbox = (CheckBox) v.findViewById(R.id.moreSecureCheckbox);
		useLauncherCheckbox = (CheckBox) v.findViewById(R.id.useLauncherCheckbox);
		
		v.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnMoreSecureSelect != null)//moreSecureCheckbox.isChecked()
					mOnMoreSecureSelect.onMoreSecureSelect(true, useLauncherCheckbox.isChecked());
			}
		});
		v.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnMoreSecureSelect != null)//moreSecureCheckbox.isChecked()
					mOnMoreSecureSelect.onMoreSecureSelectBack(true, useLauncherCheckbox.isChecked());
			}
		});
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnMoreSecureSelect = (OnMoreSecureSelect) activity;
		} catch (ClassCastException e) {}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		moreSecureCheckbox.setChecked(isMoreSecure);
		useLauncherCheckbox.setChecked(isUseLauncher);
	}
	
	public FrameMoreSecure setMoreSecure(boolean ms) {
		isMoreSecure = ms;
		return this;
	}
	
	public FrameMoreSecure setUseLauncher(boolean ms) {
		isUseLauncher = ms;
		return this;
	}

}
