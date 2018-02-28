package com.ardurasolutions.safekiddo.auth;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.proto.BasicUserOperation.OnError;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation.OnFetchChildSuccess;
import com.ardurasolutions.safekiddo.auth.proto.SetCurrentChildOperation;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations.OnSuccessAllOperations;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.Toaster;

public class FrameChildProfile extends Fragment {
	
	public static interface OnProfileSelect {
		public void onProfileSelect(ChildElement profileId);
	}
	
	public static interface OnChildListRefresh {
		public void onChildListRefresh(ArrayList<ChildElement> childList);
	}
	
	private RadioGroup profileRadios;
	private OnProfileSelect mOnProfileSelect;
	private OnChildListRefresh mOnChildListRefresh;
	private int selectedProfileId = -1;
	private ArrayList<ChildElement> childs;
	private View progressOverlay;
	
	public FrameChildProfile() {}
	
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
		View v = inflater.inflate(R.layout.fragment_frame_child_profile, container, false);
		
		profileRadios = (RadioGroup) v.findViewById(R.id.profileRadios);
		progressOverlay = v.findViewById(R.id.progressOverlay);
		
		v.findViewById(R.id.buttonRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int idx = profileRadios.indexOfChild(profileRadios.findViewById(profileRadios.getCheckedRadioButtonId()));
				
				if (profileRadios.getCheckedRadioButtonId() == -1) {
					Toaster.showMsg(getActivity(), R.string.toast_select_child_profile);
					return;
				}
				
				//progressOverlay.setVisibility(View.VISIBLE);
				overlay(true);
				final ChildElement selectedChild = getChilds().get(idx);
				
				UserOperations up = new UserOperations();
				up.addOperation(
					new SetCurrentChildOperation(getActivity())
						.setChild(selectedChild)
						.setOnError(new OnError() {
							@Override
							public void onError(final int errorCode, Object extraData) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										overlay(false);
										int toastMsg = R.string.toast_net_error;
										switch(errorCode) {
											default: break;
											case SetCurrentChildOperation.ERROR_MAX_DEVICES:
												toastMsg = R.string.toast_max_devices;
											break;
											case SetCurrentChildOperation.ERROR_LOGET_OUT:
												toastMsg = R.string.toast_auth_error_relogin;
												getActivity().finish();
											break;
										}
										
										Toaster.showMsg(getActivity(), toastMsg);
									}
								});
							}
						})
				);
				up.setOnSuccessAllOperations(new OnSuccessAllOperations() {
					@Override
					public void onSuccessAllOperations() {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mOnProfileSelect != null)
									mOnProfileSelect.onProfileSelect(selectedChild);
							}
						});
					}
				});
				up.execute();
			}
		});
		
		v.findViewById(R.id.refreshList).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				overlay(true);
				
				UserOperations up = new UserOperations();
				up.addOperation(new FetchChildsOperation(getActivity()).setOnFetchChildSuccess(new OnFetchChildSuccess() {
					@Override
					public void onFetchChildSuccess(String currentUuid, ArrayList<ChildElement> childs, ChildElement currentChild) {
						setChilds(childs);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mOnChildListRefresh != null)
									mOnChildListRefresh.onChildListRefresh(getChilds());
								selectedProfileId = -1;
								initChildList();
								overlay(false);
							}
						});
					}
				}).setOnError(new OnError() {
					@Override
					public void onError(final int errorCode, Object extraData) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								switch (errorCode) {
									default: 
										Toaster.showMsg(getActivity(), R.string.toast_net_error);
									break;
//									case FetchChildsOperation.ERROR_NO_CHILDS:
//										setChilds(new ArrayList<ChildElement>());
//										if (mOnChildListRefresh != null)
//											mOnChildListRefresh.onChildListRefresh(getChilds());
//										selectedProfileId = -1;
//										initChildList();
//									break;
								}
								
								overlay(false);
							}
						});
					}
				}));
				up.execute();
			}
		});
		
		TextView textView2 = (TextView) v.findViewById(R.id.textView2);
		String s = getString(R.string.label_select_child_profile_extra);
		s = s.replace("{URL1}", Constants.getPanelUrl());
		s = s.replace("{URL2}", Constants.getPanelUrl().replace("https://", "").replace("http://", ""));
		Spanned spanned = Html.fromHtml(s);
		textView2.setMovementMethod(LinkMovementMethod.getInstance());
		ForegroundColorSpan spans[] = spanned.getSpans(0, spanned.length(), ForegroundColorSpan.class);
		if (spans.length > 0) {
			textView2.setLinkTextColor(spans[0].getForegroundColor());
		}
		textView2.setText(spanned);
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mOnProfileSelect = (OnProfileSelect) activity;
		} catch (ClassCastException e) {}
		
		try {
			mOnChildListRefresh = (OnChildListRefresh) activity;
		} catch (ClassCastException e) {}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initChildList();
	}
	
	private void initChildList() {
		LayoutInflater la = LayoutInflater.from(getActivity());
		
		profileRadios.clearCheck();
		profileRadios.removeAllViews();
		
		if (getChilds() != null) {
			if (getChilds().size() > 0) {
				for(ChildElement c : getChilds()) {
					RadioButton radio = (RadioButton) la.inflate(R.layout.item_child_profile_radio, profileRadios, false);
					radio.setId(c.getId().intValue());
					radio.setText(c.getName());
					radio.setChecked(c.getId().intValue() == selectedProfileId);
					profileRadios.addView(radio);
				}
				if (getChilds().size() == 1) {
					RadioButton rb = (RadioButton) profileRadios.getChildAt(0);
					if (rb != null) {
						rb.setChecked(true);
						selectedProfileId = getChilds().get(0).getId().intValue();
					}
				}
			} else {
				TextView txt = (TextView) la.inflate(R.layout.item_no_child_overlay, profileRadios, false);
				txt.setText("Nie dodano jeszcze konta dziecka");
				profileRadios.addView(txt);
			}
		}
	}
	
	public FrameChildProfile setProfileId(int idx) {
		selectedProfileId = idx;
		return this;
	}

	public ArrayList<ChildElement> getChilds() {
		return childs;
	}

	public FrameChildProfile setChilds(ArrayList<ChildElement> childs) {
		this.childs = childs;
		return this;
	}

}
