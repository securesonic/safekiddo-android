package com.ardurasolutions.safekiddo.proto.network;

public interface SessionHandler {
	/**
	 * name of cookie
	 * @return
	 */
	public abstract String getSessionCookieName();
	
	/**
	 * read value from store (previously saved)
	 * @return
	 */
	public abstract String getSessionCookieValue();
	
	/**
	 * save value to store
	 * @param value
	 */
	public abstract void onSaveSession(String value);

}
