package com.ardurasolutions.safekiddo.auth.proto;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AuthOperation extends BasicUserOperation {
	
	public static final int ERROR_RESPONSE_NULL = 1;
	public static final int ERROR_NO_PIN = 2;
	public static final int ERROR_SUCCESS_FALSE = 3;
	
	public static final String KEY_JSON_SUCCESS = "success";
	public static final String KEY_JSON_PIN = "pin";
	
	public static interface OnAuthOperationSuccess {
		public void onAuthOperationSuccess(String pin);
	}
	
	private String userName, userPass;
	private Context mContext;
	private OnAuthOperationSuccess mOnAuthOperationSuccess;
	
	public AuthOperation(Context ctx) {
		mContext = ctx;
	}

	@Override
	public int execute() {
		
		List<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair(Constants.AUTH_KEY_USERNAME, getUserName()));
		post.add(new BasicNameValuePair(Constants.AUTH_KEY_PASSWORD, getUserPass()));
		
		BasicRequest br = new BasicRequest(Constants.getAuthUrl());
		br.getConnectionParams().setPost(post);
		br.setSessionHandler(new AppSessionHandler(mContext));
		br.setHeadersHandler(new AppHeadersHandler(mContext));
		br.executeSafe();
		JsonObject resp = br.getJson();
		
		if (resp == null) {
			
			return ERROR_RESPONSE_NULL;
			
		} else {
			JsonElement isSuccess = resp.get(KEY_JSON_SUCCESS);
			if (isSuccess.isJsonPrimitive() && isSuccess.getAsBoolean()) {
				
				JsonElement jsonPin = resp.get(KEY_JSON_PIN);
				if (jsonPin.isJsonPrimitive() && jsonPin.getAsString() != null && jsonPin.getAsString().length() > 0) {
					Config
						.getInstance(mContext)
						.save(Config.KeyNames.USER_PIN, jsonPin.getAsString());
					
					if (getOnAuthOperationSuccess() != null)
						getOnAuthOperationSuccess().onAuthOperationSuccess(jsonPin.getAsString());
					
				} else {
					return ERROR_NO_PIN;
				}
				
			} else {
				return ERROR_SUCCESS_FALSE;
			}
		}
		
		return 0;
	}

	public String getUserName() {
		return userName;
	}

	public AuthOperation setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public String getUserPass() {
		return userPass;
	}

	public AuthOperation setUserPass(String userPass) {
		this.userPass = userPass;
		return this;
	}

	public OnAuthOperationSuccess getOnAuthOperationSuccess() {
		return mOnAuthOperationSuccess;
	}

	public AuthOperation setOnAuthOperationSuccess(OnAuthOperationSuccess mOnAuthOperationSuccess) {
		this.mOnAuthOperationSuccess = mOnAuthOperationSuccess;
		return this;
	}

	@Override
	public Object getErrorExtra() {
		return null;
	}

}
