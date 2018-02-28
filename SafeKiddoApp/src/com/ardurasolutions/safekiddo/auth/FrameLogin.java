package com.ardurasolutions.safekiddo.auth;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.proto.AuthOperation;
import com.ardurasolutions.safekiddo.auth.proto.AuthOperation.OnAuthOperationSuccess;
import com.ardurasolutions.safekiddo.auth.proto.BasicUserOperation.OnError;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation.OnFetchChildSuccess;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Toaster;

public class FrameLogin extends Fragment {
	
	public static interface OnAuthSuccess {
		public void onAuthSuccess(ArrayList<ChildElement> childs, String userName);
	}
	
	private View buttonRight, progressOverlay;
	private EditText editLogin, editPass;
	private OnAuthSuccess mOnAuthSuccess;
	private String savedUserName = null;
	//private SafeKiddoUserAuth skAuth;
	private UserOperations mUserOperations;
	
	public FrameLogin() {}
	
	private void overlay(boolean show) {
		if (show) {
			if (progressOverlay.getVisibility() == View.GONE) {
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
			}
		} else {
			if (progressOverlay.getVisibility() == View.VISIBLE) {
				Animation a = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_layer);
				a.setAnimationListener(new AnimationListener() {
					@Override public void onAnimationStart(Animation animation) { }
					@Override
					public void onAnimationEnd(Animation animation) {
						progressOverlay.setVisibility(View.GONE);
						progressOverlay.clearAnimation();
					}
					@Override public void onAnimationRepeat(Animation animation) { }
				});
				progressOverlay.startAnimation(a);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_frame_login, container, false);
		
		//setSavedUserName(UserHelper.getUserName(getActivity()));
		
		buttonRight = v.findViewById(R.id.buttonRight);
		progressOverlay = v.findViewById(R.id.progressOverlay);
		editLogin = (EditText) v.findViewById(R.id.editLogin);
		editPass = (EditText) v.findViewById(R.id.editPass);
		
		editLogin.setText(getSavedUserName());
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mOnAuthSuccess = (OnAuthSuccess) activity;
		} catch (ClassCastException e) {}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		buttonRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (editLogin.getText().toString().trim().equals("")) {
					Toaster.showMsg(getActivity(), R.string.toast_enter_login);
					editLogin.requestFocus();
					return;
				}
				
				if (editPass.getText().toString().trim().equals("")) {
					Toaster.showMsg(getActivity(), R.string.toast_enter_pass);
					editPass.requestFocus();
					return;
				}
				
				CommonUtils.hideKeyboard(getActivity(), editPass);
				
				overlay(true);
				
				final OnError mOnError = new OnError() {
					@Override
					public void onError(int errorCode, Object extraData) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toaster.showMsg(getActivity(), R.string.toast_auth_error);
								overlay(false);
							}
						});
					}
				};
				
				mUserOperations = new UserOperations()
				.addOperation(
					new AuthOperation(getActivity())
						.setUserName(editLogin.getText().toString())
						.setUserPass(editPass.getText().toString())
						.setOnAuthOperationSuccess(new OnAuthOperationSuccess() {
							@Override
							public void onAuthOperationSuccess(String pin) {
								
							}
						})
						.setOnError(mOnError)
				)
				.addOperation(
					new FetchChildsOperation(getActivity())
						.setOnFetchChildSuccess(new OnFetchChildSuccess() {
							@Override
							public void onFetchChildSuccess(String currentUuid, final ArrayList<ChildElement> childs, ChildElement currentChild) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										if (mOnAuthSuccess != null)
											mOnAuthSuccess.onAuthSuccess(childs, editLogin.getText().toString());
									}
								});
							}
						})
						.setOnError(mOnError)
				);
				mUserOperations.execute();
				
				/*skAuth = new SafeKiddoUserAuth(getActivity());
				skAuth.setOnAuthSuccess(new SafeKiddoUserAuth.OnAuthSuccess() {
					@Override
					public void onAuthSuccess(FetchChildsResult result) {
						if (mOnAuthSuccess != null)
							mOnAuthSuccess.onAuthSuccess(result.getChilds(), editLogin.getText().toString());
						//skAuth = null;
					}
				})
				.setOnAuthError(new SafeKiddoUserAuth.OnAuthError() {
					@Override
					public void onAuthError() {
						//skAuth = null;
						Toaster.showMsg(getActivity(), R.string.toast_auth_error);
						//progressOverlay.setVisibility(View.GONE);
						overlay(false);
					}
				})
				.setUserName(editLogin.getText().toString())
				.setUserPass(editPass.getText().toString())
				.startAuth();*/
			}
		});
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (mUserOperations != null)
			mUserOperations.interrupt();
		/*if (skAuth != null)
			skAuth.cancelRequest();*/
	}

	public String getSavedUserName() {
		return savedUserName;
	}

	public FrameLogin setSavedUserName(String savedUserName) {
		this.savedUserName = savedUserName;
		return this;
	}

}
