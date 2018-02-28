package com.ardurasolutions.safekiddo.auth.proto;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import android.content.Context;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.HeartBeatHelper;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.hv.console.Console;

public class HearBeatOperation extends BasicUserOperation {
	
	public static final int ERROR_NO_JSON = 1;
	public static final int ERROR_LOGET_OUT = 2;
	public static final int ERROR_NO_DEVICE_UUID = 3;
	
	public static final String KEY_JSON_UNINSTALL = "block_access";
	public static final String KEY_JSON_SUCCESS = "success";
	public static final String KEY_JSON_LOGIN = "login";
	public static final String KEY_POST_DEVICE_UUID = "device_uuid";
	
	public static interface OnHeartBeatResult {
		public void onHeartBeatSuccess();
		public void onHeartBeatUninstall();
	}
	
	private Context mContext;
	private OnHeartBeatResult mOnHeartBeatResult;
	
	public HearBeatOperation(Context ctx) {
		mContext = ctx;
	}

	@Override
	public int execute() {
		Long t1 = System.currentTimeMillis();
		
		HeartBeatHelper.saveLastHBTime(mContext);
		
		String deviceUUID = Config.getInstance(mContext).load(Config.KeyNames.DEVICE_UUID, "");
		if (deviceUUID.length() == 0) {
			if (Console.isEnabled())
				Console.loge("HB ERROR: NO DEVICE UUID");
			return ERROR_NO_DEVICE_UUID;
		}
		
		BasicRequest br = new BasicRequest(Constants.getHeartBeatUrl());
		br.setSessionHandler(new AppSessionHandler(mContext));
		br.setHeadersHandler(new AppHeadersHandler(mContext));
		br.getConnectionParams().usePOST().addPost(KEY_POST_DEVICE_UUID, deviceUUID);
		br.executeSafe();
		JsonObject json = br.getJson();
		
		//if (Console.isEnabled())
		//	Console.logw("HB JSON: " + json);
	
		if (json != null) {
			JsonElement success = json.get(KEY_JSON_SUCCESS);
			if (success != null && success.isJsonPrimitive() && success.getAsBoolean()) {
				if (Console.isEnabled())
					Console.logi("HEAR BEAT SEND SUCCESFULLY");
				
				if (getOnHeartBeatResult() != null)
					getOnHeartBeatResult().onHeartBeatSuccess();
				
			} else {
				if (json.has(KEY_JSON_UNINSTALL) && json.get(KEY_JSON_UNINSTALL).getAsBoolean()) {
					
					if (getOnHeartBeatResult() != null)
						getOnHeartBeatResult().onHeartBeatUninstall();
					
				} else {
					return ERROR_NO_JSON;
				}
				
			}
		} else {
			
			if (br.getResponse() != null && br.getResponse().code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				InputStream es = br.getStream();
				if (es != null) {
					try {
						JsonObject respJson = new GsonBuilder()
							.serializeNulls()
							.create()
							.fromJson(new JsonReader(new InputStreamReader(es)), JsonObject.class);
						JsonElement isLogin = respJson.get(KEY_JSON_LOGIN);
						if (isLogin != null && isLogin.isJsonPrimitive() && !isLogin.getAsBoolean()) {
							Config
								.getInstance(mContext)
								.save(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, true);
							return ERROR_LOGET_OUT;
						} else {
							return ERROR_NO_JSON;
						}
					} catch(JsonIOException e) {
						
					} catch (JsonSyntaxException e) {
						
					} catch (Exception e) {
						
					}
					
				} else {
					return ERROR_NO_JSON;
				}
			} else {
				return ERROR_NO_JSON;
			}
			
			return ERROR_NO_JSON;
		}
		
		if (Console.isEnabled())
			Console.logd("HB TIME: " + (System.currentTimeMillis() - t1) + "ms");
		
		return 0;
	}


	@Override
	public Object getErrorExtra() {
		return null;
	}

	public OnHeartBeatResult getOnHeartBeatResult() {
		return mOnHeartBeatResult;
	}

	public HearBeatOperation setOnHeartBeatResult(OnHeartBeatResult mOnHeartBeatResult) {
		this.mOnHeartBeatResult = mOnHeartBeatResult;
		return this;
	}

}
