package com.ardurasolutions.safekiddo.proto.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NameValuePair;

import com.ardurasolutions.safekiddo.helpers.WSHelper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.hv.console.Console;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class BasicRequest {
	
	public static final String HEADER_SET_COOKIE = "Set-Cookie";
	public static final String HEADER_COOKIE = "Cookie";
	public static final String HEADER_USER_AGENT = "User-Agent";
	public static final String HEADER_APP_VERSION = "App-Version";
	public static final String HEADER_DEVICE_TIME = "Device-Time";
	public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	public static final String HEADER_VALUE_ACCEPT_ENCODING = "gzip";
	public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	public static final String HEADER_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
	public static final String HEADER_ETAG = "ETag";
	public static final String ENCODING = "UTF-8";
	
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final char NAME_VALUE_SEPARATOR = '=';
	private static final String SET_COOKIE_SEPARATOR="; ";
	
	protected ConnectionParams mConnectionParams;
	
	private SessionHandler mSessionHandler;
	private HeadersHandler mHeadersHandler;
	private RequestError mRequestError = null;
	
	public BasicRequest(String url) {
		mConnectionParams = new ConnectionParams(url);
		mConnectionParams.useOutCookies();
		mConnectionParams.useOutHeaders();
		mConnectionParams.useGZIP();
	}
	
	public ConnectionParams getConnectionParams() {
		return mConnectionParams;
	}
	
	public SessionHandler getSessionHandler() {
		return mSessionHandler;
	}
	
	public BasicRequest setSessionHandler(SessionHandler mSessionHandler) {
		this.mSessionHandler = mSessionHandler;
		return this;
	}
	
	public HeadersHandler getHeadersHandler() {
		return mHeadersHandler;
	}

	public BasicRequest setHeadersHandler(HeadersHandler mHeadersHandler) {
		this.mHeadersHandler = mHeadersHandler;
		return this;
	}
	
	public BasicRequest addPostParam(String key, String value) {
		mConnectionParams.addPost(key, value);
		return this;
	}
	
	public BasicRequest usePOST() {
		mConnectionParams.usePOST();
		return this;
	}
	
	public boolean hasError() {
		return mRequestError != null;
	}
	
	public RequestError getError() {
		return mRequestError;
	}
	
	public BasicRequest executeSafe() {
		try {
			execute();
		} catch (SSLHandshakeException e) {
			mRequestError = RequestError.SSL;
			if (Console.isEnabled())
				Console.loge("BasicRequest :: executeSafe[SSLHandshake]", e);
		} catch(UnknownHostException e) {
			mRequestError = RequestError.UNKNOWN_HOST;
			if (Console.isEnabled())
				Console.loge("BasicRequest :: executeSafe[UnknownHost]", e);
		} catch (InterruptedIOException e) {
			mRequestError = RequestError.INTERRUPTED;
			if (Console.isEnabled())
				Console.loge("BasicRequest :: executeSafe[InterruptedIO]", e);
		} catch (IOException e) {
			if (e.toString().contains("was not verified")) {
				mRequestError = RequestError.SSL;
			} else {
				mRequestError = RequestError.IO;
			}
			if (Console.isEnabled())
				Console.loge("BasicRequest :: executeSafe[IO]", e);
		} catch (Exception e) {
			mRequestError = RequestError.UNKNOWN;
			if (Console.isEnabled())
				Console.loge("BasicRequest :: executeSafe", e);
		}
		return this;
	}
	
	public Response execute() throws IOException {
		long t1 = System.currentTimeMillis();
		
		OkHttpClient client = new OkHttpClient();
		client.setFollowSslRedirects(true);
		if (mConnectionParams.getNoProxy())
			client.setProxy(Proxy.NO_PROXY);
		
		Request.Builder requestBuilder = new Request.Builder().url(WSHelper.encodeUrl(WSHelper.decodeUrl(mConnectionParams.getUrl())));
		
		if (mHeadersHandler != null) {
			HashMap<String, String> inH = mHeadersHandler.getInHeaders(mConnectionParams);
			if (inH != null)
				mConnectionParams.addInHeaders(inH);
		}
		
		if (mConnectionParams.getInHeaders() != null) {
			Iterator<String> headerNames = mConnectionParams.getInHeaders().keySet().iterator();
			while(headerNames.hasNext()) {
				String headerName = headerNames.next();
				if (mConnectionParams.getInHeaders().get(headerName) != null) // skip null values
					requestBuilder.addHeader(headerName, mConnectionParams.getInHeaders().get(headerName));
			}
		}
		
		if (mSessionHandler != null) {
			String cName = mSessionHandler.getSessionCookieName();
			String cValue = mSessionHandler.getSessionCookieValue();
			if (cName != null && cValue != null) {
				mConnectionParams.addInCookie(cName, cValue);
			}
		}
		
		if (mConnectionParams.getInCookies() != null) {
			StringBuffer cookieStringBuffer = new StringBuffer();
			
			Iterator<String> cookieNames = mConnectionParams.getInCookies().keySet().iterator();
			while(cookieNames.hasNext()) {
				String cookieName = cookieNames.next();
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append("=");
				cookieStringBuffer.append(mConnectionParams.getInCookies().get(cookieName));
				if (cookieNames.hasNext()) 
					cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
			}
			
			requestBuilder.addHeader(HEADER_COOKIE, cookieStringBuffer.toString());
		}
		
		if (mConnectionParams.getPost() != null) {
			try {
				RequestBody body = RequestBody.create(MediaType.parse(HEADER_FORM_URLENCODED), getQuery(mConnectionParams.getPost(), mConnectionParams.getEncoding()));
				requestBuilder.post(body);
			} catch (UnsupportedEncodingException e) {
				// igonre
			} catch (Exception e) {
				
			}
		}
		
		if (Console.isEnabled())
			Console.log(mConnectionParams.toString());
		
		Response response = client.newCall(requestBuilder.build()).execute();
		mConnectionParams.setResponse(response);
		
		if (mConnectionParams.getOutCookies() != null) {
			String headerName = null;
			for (int i=1; (headerName = response.headers().name(i)) != null; i++) {
				if (headerName.equalsIgnoreCase(HEADER_SET_COOKIE)) {
					StringTokenizer st = new StringTokenizer(response.headers().value(i), COOKIE_VALUE_DELIMITER);
					if (st.hasMoreTokens()) {
						String token  = st.nextToken();
						if (token.indexOf(NAME_VALUE_SEPARATOR) != -1) {
							String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
							String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
							mConnectionParams.getOutCookies().put(name, value);
						} else {
							mConnectionParams.getOutCookies().put(token, null);
						}
					}
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.indexOf(NAME_VALUE_SEPARATOR) != -1) {
							mConnectionParams.getOutCookies().put(
								token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(Locale.getDefault()),
								token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length())
							);
						} else {
							mConnectionParams.getOutCookies().put(token, null);
						}
					}
				}
			}
		}
		
		if (mSessionHandler != null) {
			String cName = mSessionHandler.getSessionCookieName();
			if (mConnectionParams.getOutCookies() != null && cName != null) {
				if (mConnectionParams.getOutCookies().containsKey(cName)) {
					mSessionHandler.onSaveSession(mConnectionParams.getOutCookies().get(cName));
				}
			}
		}
		
		if (mConnectionParams.getOutHeaders() != null) {
			String headerName = null;
			for (int i=0; (headerName = response.headers().name(i)) != null; i++) {
				if (headerName != null)
					mConnectionParams.getOutHeaders().put(headerName, response.headers().value(i));
			}
			
			if (mHeadersHandler != null) {
				mHeadersHandler.getOutHeaders(mConnectionParams.getOutHeaders());
			}
		}
		
		if (Console.isEnabled())
			Console.logd("REQUEST TIME: " + (System.currentTimeMillis() - t1) + "ms");
		
		return response;
	}
	
	/**
	 * if <i>getResponse()</i> is not null<br>
	 * use this function, its handle GZIP compression
	 * @return <b>InputStream</b> or <b>null</b>
	 */
	public InputStream getStream() {
		if (getResponse() != null) {
			
			if (mConnectionParams.getOutHeaders() != null && mConnectionParams.getOutHeaders().containsKey(HEADER_CONTENT_ENCODING)) {
				if (mConnectionParams.getOutHeaders().get(HEADER_CONTENT_ENCODING).equalsIgnoreCase(HEADER_VALUE_ACCEPT_ENCODING)) {
					try {
						return new GZIPInputStream(getResponse().body().byteStream());
					} catch (IOException e) {
						return getResponse().body().byteStream();
					}
				} else
					return getResponse().body().byteStream();
				
			} else
			
				return getResponse().body().byteStream();
		} else
			return null;
	}
	
	/**
	 * if execute is called not null
	 * @return <b>Response</b> or <b>null</b>
	 */
	public Response getResponse() {
		return mConnectionParams.getResponse();
	}
	
	/**
	 * call after <i>execute()</i>, if <i>getStream()</i> is not null
	 * @return <b>JsonObject</b> or <b>null</b>
	 */
	public JsonObject getJson() {
		InputStream instream = getStream();
		if (instream != null) {
			try {
				return new GsonBuilder()
					.serializeNulls()
					.create()
					.fromJson(new JsonReader(new InputStreamReader(instream)), JsonObject.class);
			} catch(JsonIOException e) {
				
			} catch (JsonSyntaxException e) {
				
			} catch (Exception e) {
				
			}
			return null;
		} else {
			return null;
		}
	}
	
	public static String getQuery(List<NameValuePair> params, String encoding) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		
		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");
			
			result.append(URLEncoder.encode(pair.getName(), encoding));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), encoding));
		}
		
		return result.toString();
	}

}
