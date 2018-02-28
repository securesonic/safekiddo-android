package com.ardurasolutions.safekiddo.auth.proto;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Build;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class SetCurrentChildOperation extends BasicUserOperation {
	
	public static final String KEY_JSON_SUCCESS = "success";
	public static final String KEY_JSON_CURRENT_UUID = "current_uuid";
	public static final String KEY_JSON_DEVICE_UUID = "device_uuid";
	public static final String KEY_JSON_ERROR = "error";
	public static final String KEY_JSON_LOGIN = "login";
	
	public static final int ERROR_RESPONSE_NULL = 1;
	public static final int ERROR_UNKNOWN = 2;
	public static final int ERROR_MAX_DEVICES = 3;
	public static final int ERROR_LOGET_OUT = 4;
	
	private Context mContext;
	private ChildElement child;
	
	public SetCurrentChildOperation(Context ctx) {
		mContext = ctx;
	}

	@Override
	public int execute() {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("child_uuid", child.getUuid()));
		post.add(new BasicNameValuePair("device_label", Build.MODEL));
		
		Config cfg = Config.getInstance(mContext);
		String deviceUuid = cfg.load(Config.KeyNames.DEVICE_UUID, "");
		if (deviceUuid.length() > 0)
			post.add(new BasicNameValuePair("device_uuid", deviceUuid));
		
		BasicRequest br = new BasicRequest(Constants.getChildSetUrl());
		br.setSessionHandler(new AppSessionHandler(mContext));
		br.setHeadersHandler(new AppHeadersHandler(mContext));
		br.getConnectionParams().setPost(post);
		br.executeSafe();
		JsonObject resp = br.getJson();
		
		//if (Console.isEnabled())
		//	Console.logd("SetCurrentChildOperation JSON: " + resp);
		
		
		if (resp != null && resp.has(KEY_JSON_SUCCESS) && resp.get(KEY_JSON_SUCCESS).getAsBoolean()) {
			
			if (resp.has(KEY_JSON_DEVICE_UUID)) {
				String newDeviceUUid = resp.get(KEY_JSON_DEVICE_UUID).getAsString();
				cfg.save(Config.KeyNames.DEVICE_UUID, newDeviceUUid);
			}
			
		} else {
			
			if (resp != null) {
				if (resp.has(KEY_JSON_ERROR)) {
					if (resp.get(KEY_JSON_ERROR).getAsInt() == 1) {
						return ERROR_MAX_DEVICES;
					} else
						return ERROR_UNKNOWN;
				} else
					return ERROR_UNKNOWN;
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
								return ERROR_RESPONSE_NULL;
							}
						} catch(JsonIOException e) {
							
						} catch (JsonSyntaxException e) {
							
						} catch (Exception e) {
							
						}
						
					} else {
						return ERROR_RESPONSE_NULL;
					}
				} else {
					return ERROR_RESPONSE_NULL;
				}
				
				return ERROR_RESPONSE_NULL;
			}
			
		}
		return 0;
	}
	
	public SetCurrentChildOperation setChild(ChildElement ch) {
		this.child = ch;
		return this;
	}

	@Override
	public Object getErrorExtra() {
		return null;
	}

}
