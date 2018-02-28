package com.ardurasolutions.safekiddo.auth.proto;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

import android.content.Context;

import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.WSHelper.ErrorCode;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;

public class LockScreenOperation extends BasicUserOperation {
	
	public static final String KEY_JSON_LOGIN = "login";
	
	public static final int ERROR_READ_STREAM = 1;
	public static final int ERROR_RESPONSE = 2;
	
	public static interface OnLockScreenResult {
		public void onLockScreenResultHtml(String html);
		public void onLockScreenResultLogout();
	}
	
	private Context mContext;
	private ErrorCode mErrorCode;
	private String url;
	private OnLockScreenResult mOnLockScreenResult;
	
	public LockScreenOperation(Context ctx) {
		mContext = ctx;
	}

	@Override
	public int execute() {
		
		HashMap<String, String> extraInHeaders = new HashMap<String, String>();
		extraInHeaders.put(Constants.CHILD_BLOCK_HEADER_CODE, Integer.toString(getErrorCode().getValue()));
		extraInHeaders.put(Constants.CHILD_BLOCK_HEADER_URL, getUrl());
		
		BasicRequest br = new BasicRequest(Constants.getBlockUrl());
		br.setSessionHandler(new AppSessionHandler(mContext));
		br.setHeadersHandler(new AppHeadersHandler(mContext));
		br.getConnectionParams().addInHeaders(extraInHeaders);
		br.executeSafe();
		
		String content = "";
		InputStream is = br.getStream();
		
		if (br.getResponse() == null ) {
			
			return ERROR_RESPONSE;
			
		} else {

			int respCode = br.getResponse().code();
			switch(respCode) {
				default:
					content = is != null ? CommonUtils.streamToString(is, 8 * 1024) : "";
					
					if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED && getOnLockScreenResult() != null) {
						Config
							.getInstance(mContext)
							.save(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, true);
						getOnLockScreenResult().onLockScreenResultLogout();
					}
				break;
				case HttpURLConnection.HTTP_OK:
					content = is != null ? CommonUtils.streamToString(is, 8 * 1024) : "";
				break;
			}
			
			if (getOnLockScreenResult() != null)
				getOnLockScreenResult().onLockScreenResultHtml(content);
		}
		
		return 0;
	}

	@Override
	public Object getErrorExtra() {
		return null;
	}

	public ErrorCode getErrorCode() {
		return mErrorCode;
	}

	public LockScreenOperation setErrorCode(ErrorCode mErrorCode) {
		this.mErrorCode = mErrorCode;
		return this;
	}
	
	public String getUrl() {
		return url;
	}

	public LockScreenOperation setUrl(String u) {
		this.url = u;
		return this;
	}

	public OnLockScreenResult getOnLockScreenResult() {
		return mOnLockScreenResult;
	}

	public LockScreenOperation setOnLockScreenResult(OnLockScreenResult mOnLockScreenResult) {
		this.mOnLockScreenResult = mOnLockScreenResult;
		return this;
	}

}
