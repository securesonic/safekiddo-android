package com.ardurasolutions.safekiddo.auth;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.activities.FirstRunActivity;
import com.ardurasolutions.safekiddo.auth.FrameLogin.OnAuthSuccess;
import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.UserHelper;

public class AuthLogin extends FragmentActivity implements OnAuthSuccess {
	
	public static final String KEY_FOR_RESULT = "for_result";
	
	private boolean isForResult = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth_login);
		
		isForResult = getIntent() != null && getIntent().getBooleanExtra(KEY_FOR_RESULT, false) ? true : false;
	}

	@Override
	public void onAuthSuccess(ArrayList<ChildElement> childs, String userName) {
		UserHelper.saveUserName(this, userName);
		
		Config
			.getInstance(this)
			.save(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, false);
		
		if (!isForResult) {
			Intent it = new Intent(this, AuthPreview.class);
			ArrayList<String> params = new ArrayList<String>();
			
			for(ChildElement c : childs) {
				params.add(c.toJSON());
			}
			
			it.putExtra(AuthPreview.KEY_CHILDS, params);
			startActivity(it);
			finish();
			if (FirstRunActivity.getInstance() != null)
				FirstRunActivity.getInstance().finish();
		} else {
			setResult(Activity.RESULT_OK);
			finish();
		}
	}

}
