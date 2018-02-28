package com.ardurasolutions.safekiddo.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;

public class HistoryStack {
	
	private ArrayList<Activity> historyStack = new ArrayList<Activity>();
	
	private static HistoryStack instance;
	public static synchronized HistoryStack getInstance() {
		if (instance == null) 
			instance = new HistoryStack();
		return instance;	
	}
	
	public void clear(Class<?> skipActivity) {
		Iterator<Activity> i = historyStack.iterator();
		while(i.hasNext()) {
			Activity a = i.next();
			if ((a != null && skipActivity != null) && a.getClass().equals(skipActivity)) {continue;}
			if (a != null && !a.isFinishing()) {
				a.finish();
				if (skipActivity != null)
					i.remove();
			}
		}
		if (skipActivity == null)
			historyStack.clear();
	}
	
	public void clear() {
		clear(null);
	}
	
	public void remove(Activity a, boolean finishActivity) {
		if (historyStack.contains(a)) {
			if (finishActivity) historyStack.get(historyStack.indexOf(a)).finish();
			historyStack.remove(a);
		}
	}
	
	public void remove(Activity[] list) {
		List<Activity> aList = Arrays.asList(list);
		Iterator<Activity> i = historyStack.iterator();
		while(i.hasNext()) {
			Activity a = i.next();
			if (a != null && aList.contains(a.getClass())) {
				if (a != null && !a.isFinishing()) {
					a.finish();
					i.remove();
				}
			}
		}
	}
	
	public boolean contains(Activity activity) {
		Iterator<Activity> i = historyStack.iterator();
		while(i.hasNext()) {
			if (i.next().getClass().equals(activity.getClass())) return true;
		}
		return false;
	}
	
	public HistoryStack add(Activity a) {
		historyStack.add(a);
		return this;
	}

}
