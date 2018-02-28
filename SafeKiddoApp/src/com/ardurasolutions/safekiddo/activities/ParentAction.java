package com.ardurasolutions.safekiddo.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.AuthLogin;

public class ParentAction extends Activity {
	
	private Button buttonRight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parent_action);
		
		buttonRight = (Button) findViewById(R.id.buttonRight);
		
		//if (Config.getInstance(this).load(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, false)) {
			buttonRight.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent it = new Intent(ParentAction.this, AuthLogin.class);
					it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					startActivity(it);
					finish();
				}
			});
		//}
	}

}
