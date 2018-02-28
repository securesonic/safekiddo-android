package com.ardurasolutions.safekiddo.proto.network;

import java.util.HashMap;
import java.util.Map;

public interface HeadersHandler {
	
	public HashMap<String, String> getInHeaders(ConnectionParams cp);
	public void getOutHeaders(Map<String, String> h);

}
