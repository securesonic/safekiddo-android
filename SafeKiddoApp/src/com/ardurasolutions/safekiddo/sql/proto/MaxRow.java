package com.ardurasolutions.safekiddo.sql.proto;

public class MaxRow {
	
	private Long timestamp;
	private Long id;
	
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String toString() {
		return "{id:" + id + ", timestamp:" + timestamp +"}";
	}

}
