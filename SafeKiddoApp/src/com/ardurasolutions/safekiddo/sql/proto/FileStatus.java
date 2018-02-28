package com.ardurasolutions.safekiddo.sql.proto;

public enum FileStatus {
	
	OK,
	TO_DOWNLOAD,
	TO_UPLOAD,
	TO_DELETE;
	
	public int toint() {
		switch(this) {
			default: 
			case OK:          return 0;
			case TO_DOWNLOAD: return 1;
			case TO_UPLOAD:   return 2;
			case TO_DELETE:   return 3;
		}
	}
	
	public static FileStatus valueOf(int v) {
		switch(v) {
			default:
			case 0: return OK;
			case 1: return TO_DOWNLOAD;
			case 2: return TO_UPLOAD;
			case 3: return TO_DELETE;
		}
	}

}
