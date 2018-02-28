package com.ardurasolutions.safekiddo.helpers;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.UriUtils;

import android.content.Context;
import android.webkit.CookieManager;

import com.ardurasolutions.safekiddo.auth.proto.ChildElement;
import com.ardurasolutions.safekiddo.proto.network.BasicRequest;
import com.ardurasolutions.safekiddo.proto.network.RequestError;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class WSHelper {
	
	public static enum ErrorCode {
		/**
		 * wystpuje wtedy kiedy wtek z żdaniem zostanie przerwany
		 */
		INTERRUPTED(-3),
		
		/**
		 * w systemie nie ma wybranego profilu dziecka
		 */
		NO_CHILD_PROFILE(-2),
		
		/**
		 * w przypadku gdy server nie zwrócił odpowiedniego nagłowka
		 */
		EMPTY(-1),
		
		/**
		 * wszysko ok, strona nie jest blokowana
		 */
		SUCCESS(0),
		
		/**
		 * użytkownik nie ma dostępu do internetu o tej porze
		 */
		INTERNET_ACCESS_FORBIDDEN(100),
		
		/**
		 * bieżący, prywatny profil użytkownika nie daje dostępu do grupy kategorii 
		 * Category-group-* określa grupę kategorii
		 * Used-profile-* określa użyty profil
		 */
		CATEGORY_BLOCKED_CUSTOM(111),
		
		/**
		 * administracyjna blokada grupy kategorii
		 * Category-group-* określa grupę kategorii
		 */
		CATEGORY_BLOCKED_GLOBAL(112),
		
		/**
		 * bieżący, prywatny profil użytkownika nie daje dostępu do URL
		 * Used-profile-* określa użyty profil
		 */
		URL_BLOCKED_CUSTOM(121),
		
		/**
		 * administracyjna blokada URL
		 */
		URL_BLOCKED_GLOBAL(122),
		
		/**
		 * odmowa dostępu ze względu na zbyt niską reputację docelowego hosta (nie zamodelowane w bazie!)
		 * IP-Reputation zawiera wartość liczbową 0-100
		 * + dodatkowe, na razie nie znane informacje
		 */
		IP_REPUTATION_CHECK_FAILED(200),
		
		/**
		 * backendowi nie udało się zidentyfikować profilu właściwego dla podanego użytkownika
		 */
		UNKNOWN_USER(300);
		
		
		private final int id;
		
		ErrorCode(int id) { 
			this.id = id; 
		}
		
		/**
		 * value of enum
		 * @return numric value
		 */
		public int getValue() { 
			return id; 
		}
		
		/**
		 * 
		 * @param val
		 * @return may return null
		 */
		public static ErrorCode valueOf(int val) {
			for(ErrorCode js : ErrorCode.values()) {
				if (js.id == val) return js;
			}
			return null;
		}
		
		/**
		 * 
		 * @param val
		 * @return may return null
		 */
		public static ErrorCode valueOfString(String val) {
			try {
				return valueOf(Integer.valueOf(val));
			} catch(Exception e) {
				if (Console.isEnabled())
					Console.loge("WSHelper::ErrorCode::valueOfString", e);
			}
			return ErrorCode.EMPTY;
		}
		
		public boolean isSuccess() {
			return this == SUCCESS;
		}
	}
	
	public static enum UserActionType {
		/**
		 * 0 - jeśli request ma nie być zalogowany, zaleca się użycie tej wartości, patrz następny punkt
		 */
		NO_LOG_REQUEST (0),
		/**
		 * 1 - request wywołany z inicjatywy użytkownika (a nie np. jako subrequest). 
		 * Backend dla requestów z UserAction == 1 bądź bez tego nagłówka loguje request do bazy logów. Pozostałe requesty nie są logowane
		 */
		LOG_REQUEST(1),
		/**
		 * 2 - użyty w celu odpytania backendu o kategoryzację strony (nie idzie do bazy logów). 
		 * Wymusza ustawienie nagłówków odpowiedzi: Category-group-id i Category-group-name. 
		 * Zwracana jest jedna grupa kategorii. Jeśli url ma więcej niż jedną grupę kategorii 
		 * i wsród nich jest kategoria zablokowana w profilu, wówczas właśnie ta kategoria zostanie zwrócona
		 */
		GET_CATEGORY(2);
		
		private final int id;
		
		UserActionType(int id) { 
			this.id = id; 
		}
		
		public int getValue() { 
			return id; 
		}
	}
	
	public static class CheckRedirectUrlResult {
		private RequestError mRequestError = null;
		private String newUrl = null;
		
		public RequestError getRequestError() {
			return mRequestError;
		}
		public CheckRedirectUrlResult setRequestError(RequestError mRequestError) {
			this.mRequestError = mRequestError;
			return this;
		}
		public String getNewUrl() {
			return newUrl;
		}
		public CheckRedirectUrlResult setNewUrl(String newUrl) {
			this.newUrl = newUrl;
			return this;
		}
		public boolean hasError() {
			return getRequestError() != null;
		}
		
		@Override
		public String toString() {
			return "CheckRedirectUrlResult {error=" + getRequestError() + ", url=" + getNewUrl() + "}";
		}
	}
	
	public static class CheckResult {
		
		public static final int NO_CATEGORY_ID = -1;
		public static final int NO_USED_PROFILE_ID = -1;
		
		private ErrorCode mErrorCode = ErrorCode.EMPTY;
		private Integer categoryGroupId = NO_CATEGORY_ID;
		private String categoryName = "";
		private Integer usedProfileId = NO_USED_PROFILE_ID;
		private String usedProfileName = "";
		private String url = "";

		public ErrorCode getErrorCode() {
			return mErrorCode;
		}

		public CheckResult setErrorCode(ErrorCode mErrorCode) {
			this.mErrorCode = mErrorCode;
			return this;
		}
		
		public boolean isSuccess() {
			return getErrorCode() != null && getErrorCode() == ErrorCode.SUCCESS;
		}
		
		public boolean isEmpty() {
			return getErrorCode() != null && getErrorCode() == ErrorCode.EMPTY;
		}
		
		public boolean isInterrupted() {
			return getErrorCode() != null && getErrorCode() == ErrorCode.INTERRUPTED;
		}
		
		public boolean hasCategoryId() {
			return getCategoryGroupId() != NO_CATEGORY_ID;
		}
		
		public boolean hasUsedProfileId() {
			return getUsedProfileId() != NO_USED_PROFILE_ID;
		}

		public Integer getCategoryGroupId() {
			return categoryGroupId;
		}

		public CheckResult setCategoryGroupId(Integer categoryGroupId) {
			this.categoryGroupId = categoryGroupId;
			return this;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public CheckResult setCategoryName(String categoryName) {
			this.categoryName = categoryName;
			return this;
		}

		public Integer getUsedProfileId() {
			return usedProfileId;
		}

		public CheckResult setUsedProfileId(Integer usedProfileId) {
			this.usedProfileId = usedProfileId;
			return this;
		}

		public String getUsedProfileName() {
			return usedProfileName;
		}

		public CheckResult setUsedProfileName(String usedProfileName) {
			this.usedProfileName = usedProfileName;
			return this;
		}

		public String getUrl() {
			return url;
		}

		public CheckResult setUrl(String url) {
			this.url = url;
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{url=" + url + ", ");
			sb.append("categoryGroupId=" + categoryGroupId + ", ");
			sb.append("categoryName=" + categoryName + ", ");
			sb.append("usedProfileId=" + usedProfileId + ", ");
			sb.append("usedProfileName=" + usedProfileName + ", ");
			sb.append("errorCode=" + mErrorCode + "}");
			return sb.toString();
		}
	}
	
	public static final String HEADER_FIELD_RESULT = "Result";
	public static final String HEADER_FIELD_REQUEST_URL = "Request";
	public static final String HEADER_FIELD_USER_URL = "UserAction"; // 0/1
	public static final String HEADER_FIELD_USER_ID = "UserId"; // 0/1
	public static final String HEADER_FIELD_CATEGORY_GROUP_ID = "Category-group-id"; //"Category-group-id"; // Integer
	public static final String HEADER_FIELD_CATEGORY_GROUP_NAME = "Category-group-name"; // String
	public static final String HEADER_FIELD_USED_PROFILE_ID = "Used-profile-id"; // Integer
	public static final String HEADER_FIELD_USED_PROFILE_NAME = "Used-profile-name"; // String
	
	public static CheckResult checkUrl(String url, UserActionType userActionType, Context ctx) {
		final long t1 = System.currentTimeMillis();
		
		CheckResult res = new CheckResult().setUrl(url);
		
		ChildElement child = UserHelper.getCurrentChildProfile(ctx);
		
		if (child == null) {
			if (Console.isEnabled())
				Console.loge("No child profile ");
			return res.setErrorCode(ErrorCode.NO_CHILD_PROFILE);
		}
		
		// INFO dopuszczamy wszystko co nie jest http/https
		// beda tutaj tez takie rzeczy jak about:blank itp
		if (!url.trim().startsWith("http")) {
			return res.setErrorCode(ErrorCode.SUCCESS);
		}
		
		BasicRequest br = new BasicRequest(Constants.SAFEKIDO_WS_CHECK_URL);
		
		br.getConnectionParams()
			.addInHeader(HEADER_FIELD_REQUEST_URL, encodeUrl(decodeUrl(url)))
			.useGZIP()
			.setNoProxy(true)
			.useOutHeaders();
		
		if (userActionType != null)
			br.getConnectionParams().addInHeader(HEADER_FIELD_USER_URL, Integer.toString(userActionType.getValue()));
		
		br.getConnectionParams().addInHeader(HEADER_FIELD_USER_ID, child.getUuid().toString());
		
		String resValue = null;
		
		try {
			br.execute();
			
			CommonUtils.streamToString(br.getStream(), 8 * 1024);
			Map<String, String> headers = br.getConnectionParams().getOutHeaders();
			resValue = headers.get(HEADER_FIELD_RESULT);
			
			/*
			 * jeżeli url jest w postaci xx.xx.xx.xx (host) a ws server nie odpowie prawidłowo to najprawdopodobniej jest to
			 * przekierowanie do autoryzacji w otwartej sieci więc puszamy takie coś
			 */
			if (resValue == null) {// && TextUtils.isValidIpAdress(url)) {
				if (Console.isEnabled())
					Console.logd("IS AUTH URL?: " + url);
				
				return res.setErrorCode(ErrorCode.SUCCESS);
			}
			
			if (headers.containsKey(HEADER_FIELD_CATEGORY_GROUP_ID)) {
				try {
					res.setCategoryGroupId(Integer.valueOf(headers.get(HEADER_FIELD_CATEGORY_GROUP_ID)));
				} catch (NumberFormatException e) {
					BugSenseHandler.sendException(new Exception("WS_SERVER_RESPONSE=ERROR_CATEGORY, ID: " + headers.get(HEADER_FIELD_CATEGORY_GROUP_ID)));
				}
			}
			
			if (headers.containsKey(HEADER_FIELD_CATEGORY_GROUP_NAME)) {
				res.setCategoryName(headers.get(HEADER_FIELD_CATEGORY_GROUP_NAME));
			}
			
			if (headers.containsKey(HEADER_FIELD_USED_PROFILE_ID)) {
				try {
					res.setUsedProfileId(Integer.valueOf(headers.get(HEADER_FIELD_USED_PROFILE_ID)));
				} catch (NumberFormatException e) {
					BugSenseHandler.sendException(new Exception("WS_SERVER_RESPONSE=ERROR_USED_PROFILE_ID, ID: " + headers.get(HEADER_FIELD_USED_PROFILE_ID)));
				}
			}
			
			if (headers.containsKey(HEADER_FIELD_USED_PROFILE_NAME)) {
				res.setUsedProfileName(headers.get(HEADER_FIELD_USED_PROFILE_NAME));
			}
			
		} catch (InterruptedIOException e) {
			if (Console.isEnabled())
				Console.loge("WSHelper::isUrlAllowed[InterruptedIO]", e);
			return res.setErrorCode(ErrorCode.INTERRUPTED);
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("WSHelper::isUrlAllowed[IO]", e);
			return res.setErrorCode(ErrorCode.EMPTY);
		}
		
		res.setErrorCode(ErrorCode.valueOfString(resValue));
		
		if (Thread.currentThread().isInterrupted()) {
			return res.setErrorCode(ErrorCode.INTERRUPTED);
		}
		
		if (Console.isEnabled()) {
			Console.log("CHECK URL: " + url + ", ErrorCode: " + res.getErrorCode() + ", ErrorCode-RAW: " + resValue + ", userActionType=" + userActionType);
			Console.logd("WS Time: " + (System.currentTimeMillis() - t1) + " ms");
			if (res.isEmpty())
				Console.loge("WS_SERVER_RESPONSE=ErrorCode.EMPTY, URL: " + url);
		}
		
		if (res.isEmpty()) {
			BugSenseHandler.sendException(new Exception("WS_SERVER_RESPONSE=ErrorCode.EMPTY, URL: " + url));
		}
		
		return res;
	}
	
	public static String decodeUrl(String url) {
		try {
			return UriUtils.decode(url, "UTF-8");
		} catch (IllegalArgumentException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return url;
	}
	
	public static String encodeUrl(String url) {
		try {
			return UriUtils.encodeUri(url, "UTF-8");
		} catch (IllegalArgumentException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return url;
	}
	
	/**
	 * 
	 * @param u
	 * @param userAgent
	 * @param mCookieManager
	 * @param outError - return error from request
	 * @return
	 */
	public static CheckRedirectUrlResult checkIsRedirecUrl(String u, String userAgent, CookieManager mCookieManager) {
		CheckRedirectUrlResult res = new CheckRedirectUrlResult().setNewUrl(u);
		BasicRequest br = new BasicRequest(u);
		
		br.getConnectionParams()
			.addInHeader(BasicRequest.HEADER_USER_AGENT, userAgent)
			.addInHeader(BasicRequest.HEADER_COOKIE, mCookieManager.getCookie(u)) // TODO w przypadku youtube dostaje dziwny adres URL (nie do filmu)
			//.addInHeader("accept-language", "pl,en;q=0.8,en-US;q=0.6") // TODO testy
			.useGZIP()
			.setNoProxy(true)
			.useOutHeaders();
		br.executeSafe();
		
		if (br.hasError()) {
			if (Console.isEnabled())
				Console.loge("checkIsRedirecUrl ERROR: " + br.getError());
			res.setNewUrl(null);
			res.setRequestError(br.getError());
			return res;
		}
		
		if (br.getResponse() != null) {
			res.setNewUrl(br.getResponse().request().urlString());
			if (Console.isEnabled() && !u.equals(res.getNewUrl()))
				Console.logd("REDIRECT: " + u + " => " + res.getNewUrl());
		}
		
		return res;
	}
	
	public static class AsyncCheckUrl {
		
		public static interface OnStatusChanged {
			public void onCancel();
			public void onResult(ErrorCode result, String newUrl);
			public void onError(ErrorCode result);
			public void onCheckError(RequestError error);
			public void onUrlRedirected(String newUrl);
		}
		
		private String u;
		private UserActionType uat;
		private Context c;
		private boolean isCanceled = false;
		private OnStatusChanged mOnStatusChanged;
		private Thread mThread;
		private String userAgent;
		private CookieManager mCookieManager;
		
		public AsyncCheckUrl(String url, UserActionType userActionType, Context ctx) {
			u = url;
			uat = userActionType;
			c = ctx;
		}
		
		public AsyncCheckUrl start() {
			
			mThread = new Thread(new Runnable() {
				@Override
				public void run() {
					
					// wykonanie żadania aby sprawdzić prawdziwość URLa i jego redirecty
					CheckRedirectUrlResult checkUrl = checkIsRedirecUrl(u, userAgent, mCookieManager);
					
					if (isCanceled()) return;
					
					if (checkUrl.hasError()) {
						if (getOnStatusChanged() != null)
							getOnStatusChanged().onCheckError(checkUrl.getRequestError());
						return;
					}
					
					if (checkUrl.getNewUrl() == null) {
						if (getOnStatusChanged() != null)
							getOnStatusChanged().onError(ErrorCode.EMPTY);
						return;
					}
					
					String newUrl = TextUtils.parseToNiceUrl(checkUrl.getNewUrl());
					ErrorCode result = checkUrl(newUrl, uat, c).getErrorCode();
					
					if (isCanceled()) {
						
						//if (getOnStatusChanged() != null)
						//	getOnStatusChanged().onCancel();
						
					} else {
						
						if (result.getValue() < 0) {
							if (getOnStatusChanged() != null)
								getOnStatusChanged().onError(result);
						} else {
							if (getOnStatusChanged() != null)
								getOnStatusChanged().onResult(result, newUrl);
						}
						
					}
				}
			});
			mThread.start();
			
			return this;
		}
		
		private synchronized void cencel() {
			isCanceled = true;
			mThread.interrupt();
			if (getOnStatusChanged() != null)
				getOnStatusChanged().onCancel();
		}
		
		private synchronized boolean isCanceled() {
			return isCanceled;
		}
		
		public AsyncCheckUrl cancelCheck() {
			cencel();
			return this;
		}

		public OnStatusChanged getOnStatusChanged() {
			return mOnStatusChanged;
		}

		public AsyncCheckUrl setOnStatusChanged(OnStatusChanged mOnStatusChanged) {
			this.mOnStatusChanged = mOnStatusChanged;
			return this;
		}

		public String getUserAgent() {
			return userAgent;
		}
		public AsyncCheckUrl setUserAgent(String userAgent) {
			this.userAgent = userAgent;
			return this;
		}

		public CookieManager getCookieManager() {
			return mCookieManager;
		}

		public AsyncCheckUrl setCookieManager(CookieManager mCookieManager) {
			this.mCookieManager = mCookieManager;
			return this;
		}
		
	}

}
