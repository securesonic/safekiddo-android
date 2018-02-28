package com.ardurasolutions.safekiddo.proto;

import java.util.HashMap;

import android.app.Application;

public class ApplicationProto extends Application {
	
	private static ApplicationProto sInstance;
	private HashMap<String, AsyncTaskProto<?,?,?>> tasks = new HashMap<String, AsyncTaskProto<?,?,?>>();
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}

	public void registerTask(String taskTag, AsyncTaskProto<?,?,?> task) {
		tasks.put(taskTag, task);
	}
	
	public void unregisterTask(String taskTag) {
		tasks.remove(taskTag);
	}
	
	public AsyncTaskProto<?,?,?> getRegisteredTask(String tagName) {
		return tasks.get(tagName);
	}
	
	/**
	 * zwraca instancj� aplikacji
	 * musi by� zainicjowane bo inaczej nie by�o by to wywo�ane z plaikacji
	 * @return
	 */
	public static ApplicationProto get() {
		return sInstance;
	}

}
