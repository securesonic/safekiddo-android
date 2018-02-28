package com.ardurasolutions.safekiddo.activities;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.AuthLogin;
import com.ardurasolutions.safekiddo.auth.proto.BasicUserOperation.OnError;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation;
import com.ardurasolutions.safekiddo.auth.proto.FetchChildsOperation.OnFetchChildSuccess;
import com.ardurasolutions.safekiddo.auth.proto.SetCurrentChildOperation;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations;
import com.ardurasolutions.safekiddo.auth.proto.UserOperations.OnSuccessAllOperations;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.helpers.UserHelper;

public class UserSettingsChildProfile extends ActionBarActivity {
	
	private static final int KEY_RC_AUTH = 1001;
	
	private ListView LV;
	private ArrayAdapter<ChildElement> LA;
	private ArrayList<ChildElement> childs;
	private View progressBar1;
	//private boolean cancelGetList = false;
	private ChildElement currentProfile, selectedProfile;
	private TextView textInfo;
	//private SafeKiddoUserAuth mSafeKiddoUserAuth;
	private UserOperations mUserOperations;
	private MenuItem applyMenuItem;
	private boolean finishPrevActivity = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(0, 0);
		setContentView(R.layout.activity_user_settings_apps_manage);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    
		getSupportActionBar().setTitle(R.string.label_settings_select_profile);
		//getSupportActionBar().setLogo(R.drawable.ic_action_settings);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		LV = (ListView) findViewById(R.id.list);
		textInfo = (TextView) findViewById(R.id.textInfo);
		progressBar1 = findViewById(R.id.progressBar1);
		currentProfile = UserHelper.getCurrentChildProfile(this);
		selectedProfile = UserHelper.getCurrentChildProfile(this);
		
		progressBar1.setVisibility(View.VISIBLE);
		LV.setItemsCanFocus(false);
		LV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		LV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) { }
		});
		
		mUserOperations = new UserOperations();
		mUserOperations.setOnSuccessAllOperations(new OnSuccessAllOperations() {
			@Override
			public void onSuccessAllOperations() {
				
			}
		});
		mUserOperations.addOperation(new FetchChildsOperation(this).setOnFetchChildSuccess(new OnFetchChildSuccess() {
			@Override
			public void onFetchChildSuccess(String currentUuid, ArrayList<ChildElement> childList, ChildElement currentChild) {
				childs = childList;
				selectedProfile = currentChild;
				UserHelper.saveCurrentChildProfile(UserSettingsChildProfile.this, selectedProfile);
				
				LA = new ArrayAdapter<ChildElement>(UserSettingsChildProfile.this, R.layout.item_child_profile, childs) {
					@SuppressLint("ViewHolder")
					@Override
					public View getView(final int position, View convertView, android.view.ViewGroup parent) {
						convertView = LayoutInflater.from(UserSettingsChildProfile.this).inflate(R.layout.item_child_profile, LV, false);
						final ChildElement child = childs.get(position);
						((RadioButton) convertView).setText(child.getName());
						
						if (currentProfile != null && currentProfile.getUuid().equals(child.getUuid())) {
							((RadioButton) convertView).setChecked(true);
						}
						
						((RadioButton) convertView).setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								if (isChecked) {
									selectedProfile = child;
									LA.notifyDataSetChanged();
								}
								LV.setItemChecked(position, isChecked);
							}
						});
						
						return convertView;
					}
				};
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LV.setAdapter(LA);
						if (currentProfile != null && selectedProfile != null) {
							int pos = 0;
							for(ChildElement c : childs) {
								if (c.getUuid().equals(selectedProfile.getUuid())) {
									LV.setItemChecked(pos, true);
									LA.notifyDataSetChanged();
									break;
								}
								pos++;
							}
						}
						progressBar1.setVisibility(View.GONE);
						if (applyMenuItem != null)
							applyMenuItem.setEnabled(true);
					}
				});
			}
		})
		.setOnError(new OnError() {
			@Override
			public void onError(int errorCode, Object extraData) {
				mUserOperations = null;
				switch(errorCode) {
					default:
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//Toaster.showMsg(UserSettingsChildProfile.this, "Błąd pobierania danych");
								progressBar1.setVisibility(View.GONE);
								textInfo.setText(R.string.toast_error_get_childs);
								textInfo.setVisibility(View.VISIBLE);
							}
						});
					break;
					case FetchChildsOperation.ERROR_LOGET_OUT: 
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								sendBroadcast(new Intent().setAction(Constants.BRODCAST_SESSION_EXPIRES));
								Intent it = new Intent(UserSettingsChildProfile.this, AuthLogin.class);
								it.putExtra(AuthLogin.KEY_FOR_RESULT, true);
								startActivityForResult(it, KEY_RC_AUTH);
								finish();
							}
						});
					break;
				}
			}
		}));
		mUserOperations.execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		applyMenuItem = menu.add(R.string.label_apply);
		if (CommonUtils.isTablet(this)) 
			applyMenuItem.setIcon(R.drawable.ic_action_accept_light);
		applyMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				if (selectedProfile == null) {
					Toaster.showMsg(UserSettingsChildProfile.this, R.string.toast_select_child_from_list);
					return false;
				}
				
				progressBar1.setVisibility(View.VISIBLE);
				applyMenuItem.setEnabled(false);
				
				UserOperations up = new UserOperations();
				up.addOperation(
					new SetCurrentChildOperation(UserSettingsChildProfile.this)
					.setChild(selectedProfile)
					.setOnError(new OnError() {
						@Override
						public void onError(final int errorCode, Object extraData) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									int toastMsg = R.string.toast_net_error;
									switch(errorCode) {
										default: break;
										case SetCurrentChildOperation.ERROR_MAX_DEVICES:
											toastMsg = R.string.toast_max_devices;
										break;
									}
									
									Toaster.showMsg(UserSettingsChildProfile.this, toastMsg);
									progressBar1.setVisibility(View.GONE);
									applyMenuItem.setEnabled(true);
								}
							});
						}
					})
				);
				up.setOnSuccessAllOperations(new OnSuccessAllOperations() {
					@Override
					public void onSuccessAllOperations() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								finishPrevActivity = false;
								UserHelper.saveCurrentChildProfile(UserSettingsChildProfile.this, selectedProfile);
								sendBroadcast(new Intent().setAction(Constants.BRODCAST_CHILD_PROFILE_CHANGED));
								setResult(Activity.RESULT_OK);
								finish();
							}
						});
					}
				});
				up.execute();
				return false;
			}
		});
		applyMenuItem.setEnabled(false);
		MenuItemCompat.setShowAsAction(applyMenuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mUserOperations != null) {
			mUserOperations.interrupt();
			mUserOperations = null;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (finishPrevActivity && UserSettings.get() != null)
			UserSettings.get().selfFinish();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		finishPrevActivity = false;
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishPrevActivity = false;
				setResult(Activity.RESULT_OK);
				finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
