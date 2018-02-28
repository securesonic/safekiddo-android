package com.ardurasolutions.safekiddo.proto;

import android.os.Binder;

public class LocalServiceBinder<T> extends Binder {
	
	private T mInstance;
	
	public LocalServiceBinder(T serviceClass) {
		mInstance = serviceClass;
	}
	
	public T getService() {
		return mInstance;
	}

}
