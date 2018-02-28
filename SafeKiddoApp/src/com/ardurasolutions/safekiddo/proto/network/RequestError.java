package com.ardurasolutions.safekiddo.proto.network;

public enum RequestError {
	
	UNKNOWN(-1),
	UNKNOWN_HOST(1),
	IO(2),
	SSL(3),
	INTERRUPTED(4);
	
	private int id = 0;
	
	RequestError(int idx) {
		id = idx;
	}
	
	public int getValue() {
		return id;
	}

}
