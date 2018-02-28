package com.ardurasolutions.safekiddo.browser.proto;

import java.util.ArrayList;

public class UrlObserver {
	
	private ArrayList<String> igoneUrl = new ArrayList<String>();
	
	public static interface OnUrlChange {
		public void onUrlChanged(String url, String debugParam);
	}
	
	private String baseUrl = null;
	private String debugParam = "";
	private OnUrlChange mOnUrlChange;
	
	public UrlObserver(String url, OnUrlChange uc) {
		this.baseUrl = url;
		this.mOnUrlChange = uc;
		if (!isIgonredUrl(this.baseUrl) && getOnUrlChange() != null)
			getOnUrlChange().onUrlChanged(this.baseUrl, "constructor");
	}
	
	public UrlObserver updateUrlForce(String url, String dp) {
		this.baseUrl = url;
		this.debugParam = "FORCE " + dp;
		if (!isIgonredUrl(this.baseUrl) && getOnUrlChange() != null)
			getOnUrlChange().onUrlChanged(this.baseUrl, this.debugParam);
		return this;
	}
	
	
	public UrlObserver updateUrl(String url, String dp) {
		if (this.baseUrl != null && url == null) {
			this.baseUrl = url;
			this.debugParam = dp;
			if (!isIgonredUrl(this.baseUrl) && getOnUrlChange() != null)
				getOnUrlChange().onUrlChanged(this.baseUrl, this.debugParam);
			return this;
		}
		if (!url.equals(this.baseUrl)) {
			this.baseUrl = url;
			this.debugParam = dp;
			if (!isIgonredUrl(this.baseUrl) && getOnUrlChange() != null)
				getOnUrlChange().onUrlChanged(this.baseUrl, this.debugParam);
		}
		return this;
	}
	
	public boolean isIgonredUrl(String url) {
		if (igoneUrl.size() == 0) return false;
		if (url != null)
			return igoneUrl.contains(url);
		else
			return false;
	}
	
	public UrlObserver addIgnoreUrl(String url) {
		igoneUrl.add(url);
		return this;
	}
	
	public UrlObserver delIgnoreUrl(String url) {
		igoneUrl.remove(url);
		return this;
	}

	public OnUrlChange getOnUrlChange() {
		return mOnUrlChange;
	}

	public UrlObserver setOnUrlChange(OnUrlChange mOnUrlChange) {
		this.mOnUrlChange = mOnUrlChange;
		return this;
	}

}
