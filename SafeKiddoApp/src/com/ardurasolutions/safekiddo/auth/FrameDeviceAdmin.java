package com.ardurasolutions.safekiddo.auth;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;

public class FrameDeviceAdmin extends Fragment {
	
	public static interface OnDeviceAdminInstall {
		public void onDeviceAdminInstall();
		public void onDeviceAdminInstallBack();
	}
	
	private OnDeviceAdminInstall mOnDeviceAdminInstall;
	private DevicePolicyManager mDPM; 
	private ComponentName mDeviceAdmin;
	static final int RESULT_ENABLE = 1;
	private TextView textView1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_frame_devce_admin, container, false);
		
		textView1 = (TextView) v.findViewById(R.id.textView1);
		
		v.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDPM.isAdminActive(mDeviceAdmin)) {
					if (mOnDeviceAdminInstall != null)
						mOnDeviceAdminInstall.onDeviceAdminInstall();
				} else {
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN); 
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,  mDeviceAdmin); 
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.label_da_install_info)); 
					startActivityForResult(intent, RESULT_ENABLE); 
				}
			}
		});
		
		v.findViewById(R.id.buttonLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnDeviceAdminInstall != null)
					mOnDeviceAdminInstall.onDeviceAdminInstallBack();
			}
		});
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnDeviceAdminInstall = (OnDeviceAdminInstall) activity;
		} catch (ClassCastException e) {}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mDPM = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(getActivity(), SKAdmin.class);
		textView1.setText(mDPM.isAdminActive(mDeviceAdmin) ? R.string.label_da_info_installed : R.string.label_da_info);
	}
	
	 @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_ENABLE && resultCode == Activity.RESULT_OK) {
			if (mOnDeviceAdminInstall != null)
				mOnDeviceAdminInstall.onDeviceAdminInstall();
		}
	}

}
