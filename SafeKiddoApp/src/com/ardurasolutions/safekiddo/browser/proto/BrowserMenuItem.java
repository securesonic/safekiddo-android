package com.ardurasolutions.safekiddo.browser.proto;

import android.view.View;

public class BrowserMenuItem {
	private String label;
	private int icon = 0;
	private View.OnClickListener onClick;
	private View view;
	private int id;
	
	public BrowserMenuItem(String l, int i, View.OnClickListener oc, int idx) {
		setLabel(l);
		setIcon(i);
		setOnClick(oc);
		setId(idx);
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public View.OnClickListener getOnClick() {
		return onClick;
	}
	public void setOnClick(View.OnClickListener onClick) {
		this.onClick = onClick;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}
	
	public String toString() {
		return "{label=" + getLabel() + 
				", id=" + getId() + 
				", onClick=" + (getOnClick() != null) + 
				", view=" + (getView() != null) + 
				"}";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
