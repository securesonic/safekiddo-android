package com.ardurasolutions.safekiddo.proto.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.squareup.okhttp.Response;

public class ConnectionParams {
	private String url;
	private List<NameValuePair> post;
	private Map<String, String> inCookies;
	private Map<String, String> outCookies;
	private Map<String, String> inHeaders;
	private Map<String, String> outHeaders;
	private Response mResponse;
	
	private Boolean noProxy = Boolean.TRUE;
	private String encoding = BasicRequest.ENCODING;
	
	public ConnectionParams() {}
	
	public ConnectionParams(String urlString) {
		setUrl(urlString);
	}
	
	public ConnectionParams addInHeader(String name, String value) {
		if (inHeaders == null)
			inHeaders = new HashMap<String, String>();
		inHeaders.put(name, value);
		return this;
	}
	
	public ConnectionParams addInHeaders(HashMap<String, String> h) {
		if (h != null) {
			if (inHeaders == null)
				inHeaders = new HashMap<String, String>();
			inHeaders.putAll(h);
		}
		return this;
	}
	
	public ConnectionParams addInCookie(String name, String value) {
		if (inCookies == null)
			inCookies = new HashMap<String, String>();
		inCookies.put(name, value);
		return this;
	}
	
	public ConnectionParams addPost(String name, String value) {
		if (post == null)
			post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair(name, value));
		return this;
	}
	
	/**
	 * create empty post if not set
	 * @return self
	 */
	public ConnectionParams usePOST() {
		if (post == null)
			post = new ArrayList<NameValuePair>();
		return this;
	}
	
	public ConnectionParams useGZIP() {
		if (inHeaders == null)
			inHeaders = new HashMap<String, String>();
		inHeaders.put(BasicRequest.HEADER_ACCEPT_ENCODING, BasicRequest.HEADER_VALUE_ACCEPT_ENCODING);
		return this;
	}
	
	public ConnectionParams useOutHeaders() {
		if (outHeaders == null)
			outHeaders = new HashMap<String, String>();
		return this;
	}
	
	public ConnectionParams useOutCookies() {
		if (outCookies == null)
			outCookies = new HashMap<String, String>();
		return this;
	}
	
	public String getUrl() {
		return url;
	}
	public ConnectionParams setUrl(String url) {
		this.url = url;
		return this;
	}
	public List<NameValuePair> getPost() {
		return post;
	}
	public ConnectionParams setPost(List<NameValuePair> post) {
		this.post = post;
		return this;
	}
	public Map<String, String> getInCookies() {
		return inCookies;
	}
	public ConnectionParams setInCookies(Map<String, String> inCookies) {
		this.inCookies = inCookies;
		return this;
	}
	public Map<String, String> getOutCookies() {
		return outCookies;
	}
	public ConnectionParams setOutCookies(Map<String, String> outCookies) {
		this.outCookies = outCookies;
		return this;
	}
	public Boolean getNoProxy() {
		return noProxy;
	}
	public ConnectionParams setNoProxy(Boolean noProxy) {
		this.noProxy = noProxy;
		return this;
	}
	public String getEncoding() {
		return encoding;
	}
	public ConnectionParams setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}
	public Map<String, String> getInHeaders() {
		return inHeaders;
	}
	public ConnectionParams setInHeaders(Map<String, String> inHeaders) {
		this.inHeaders = inHeaders;
		return this;
	}
	public Map<String, String> getOutHeaders() {
		return outHeaders;
	}
	public ConnectionParams setOutHeaders(Map<String, String> outHeaders) {
		this.outHeaders = outHeaders;
		return this;
	}
	public Response getResponse() {
		return mResponse;
	}
	public ConnectionParams setResponse(Response mResponse) {
		this.mResponse = mResponse;
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
			.append("REQUEST: " + getUrl() + "\n");
		
		if (getInHeaders() != null && getInHeaders().size() > 0)
			sb.append("    InHeaders: " + prntMap(getInHeaders()) + "\n");
		
		if (getOutHeaders() != null && getOutHeaders().size() > 0)
			sb.append("    OutHeaders: " + prntMap(getOutHeaders()) + "\n");
		
		if (getInCookies() != null && getInCookies().size() > 0)
			sb.append("    InCookies: " + prntMap(getInCookies()) + "\n");
		
		if (getOutCookies() != null && getOutCookies().size() > 0)
			sb.append("    InCookies: " + prntMap(getOutCookies()) + "\n");
		
		if (getPost() != null && getPost().size() > 0)
			sb.append("    POST: " + prntNVP(getPost()) + "\n");
		
		return sb.toString();
	}
	
	private String prntMap(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		
		if (map.size() > 0) {
			
			sb.append("{\n");
			for(String key : map.keySet()) {
				sb.append("      ")
				.append(key)
				.append("=")
				.append(map.get(key))
				.append("\n");
			}
			sb.append("    }\n");
			
		}
		
		return sb.toString();
	}
	
	private String prntNVP(List<NameValuePair> nvp) {
		StringBuilder sb = new StringBuilder();
		
		if (nvp.size() > 0) {
			
			sb.append("{\n");
			for(NameValuePair entry : nvp) {
				sb.append("      ")
				.append(entry.getName())
				.append("=")
				.append(entry.getValue())
				.append("\n");
			}
			sb.append("    }\n");
			
		}
		
		return sb.toString();
	}
}
