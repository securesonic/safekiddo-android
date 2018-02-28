package com.ardurasolutions.safekiddo.auth.proto;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import android.content.Context;

import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.proto.network.AppHeadersHandler;
import com.ardurasolutions.safekiddo.proto.network.AppSessionHandler;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class FetchChildsOperation extends BasicUserOperation {
	
	public static final String KEY_JSON_SUCCESS = "success";
	public static final String KEY_JSON_CHILDS = "childs";
	public static final String KEY_JSON_LOGIN = "login";
	public static final String KEY_JSON_CURRENT_UUID = "current_uuid";
	public static final String KEY_JSON_DEVICE_UUID = "device_uuid";
	public static final String KEY_JSON_ERROR = "error";
	
	public static final int ERROR_RESPONSE_NULL = 1;
	public static final int ERROR_LOGET_OUT = 2;
	public static final int ERROR_SUCCESS_FALSE = 3;
	@Deprecated
	public static final int ERROR_NO_CHILDS = 4;
	
	public static interface OnFetchChildSuccess {
		public void onFetchChildSuccess(String currentUuid, ArrayList<ChildElement> childs, ChildElement currentChild);
	}
	
	private Context mContext;
	private OnFetchChildSuccess mOnFetchChildSuccess;
	
	public FetchChildsOperation(Context ctx) {
		mContext = ctx;
	}

	@Override
	public int execute() {
		
		BasicRequest br = new BasicRequest(Constants.getAuthFetchChildsUrl());
		br.setSessionHandler(new AppSessionHandler(mContext));
		br.setHeadersHandler(new AppHeadersHandler(mContext));
		br.getConnectionParams();  //INFO w kolejnej wersji nie ma już POST
		br.executeSafe();
		JsonObject respChilds = br.getJson();
		
		if (respChilds != null) {
			
			JsonElement isSuccessChilds = respChilds.get(KEY_JSON_SUCCESS);
			if (isSuccessChilds!= null && isSuccessChilds.isJsonPrimitive() && isSuccessChilds.getAsBoolean()) {
				
				JsonElement currentuidJson = respChilds.get(KEY_JSON_CURRENT_UUID);
				String currentUuid = currentuidJson != null && !currentuidJson.isJsonNull() ? currentuidJson.getAsString() : null; // respChilds.get(KEY_JSON_CURRENT_UUID).isJsonNull() ? null :  .getAsString();
				JsonElement childs = respChilds.get(KEY_JSON_CHILDS);
				
				if (childs != null && childs.isJsonArray()) {
					JsonArray childArray = childs.getAsJsonArray();
					if (childArray.size() >= 0) {
						ArrayList<ChildElement> list = new ArrayList<ChildElement>();
						ChildElement current = null;
						if (childArray.size() >= 0) {
							for(int i=0; i<childArray.size(); i++) {
								ChildElement c = ChildElement.fromJsonElement(childArray.get(i).getAsJsonObject());
								if (current == null && currentUuid != null && c.getUuid() != null && c.getUuid().equals(currentUuid))
									current = c;
								list.add(c);
							}
						}
						
						if (getOnFetchChildSuccess() != null)
							getOnFetchChildSuccess().onFetchChildSuccess(currentUuid, list, current);
					} else {
						// no childs
						return ERROR_NO_CHILDS;
					}
				} else {
					// no childs in json (or no array)
					return ERROR_NO_CHILDS;
				}
				
			} else {
				JsonElement isLogin = respChilds.get(KEY_JSON_LOGIN);
				if (isLogin != null && isLogin.isJsonPrimitive() && !isLogin.getAsBoolean()) {
					Config
						.getInstance(mContext)
						.save(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, true);
					return ERROR_LOGET_OUT;
				} else {
					// unknown error
					return ERROR_SUCCESS_FALSE;
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
							return ERROR_RESPONSE_NULL;
						}
					} catch (JsonIOException e) {
						
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
		return 0;
	}

	public OnFetchChildSuccess getOnFetchChildSuccess() {
		return mOnFetchChildSuccess;
	}

	public FetchChildsOperation setOnFetchChildSuccess(OnFetchChildSuccess mOnFetchChildSuccess) {
		this.mOnFetchChildSuccess = mOnFetchChildSuccess;
		return this;
	}

	@Override
	public Object getErrorExtra() {
		return null;
	}

}
