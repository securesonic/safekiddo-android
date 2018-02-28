package com.ardurasolutions.safekiddo.launcher.proto;

import com.ardurasolutions.safekiddo.launcher.views.AppIcon;
import com.ardurasolutions.safekiddo.launcher.views.DesktopView;

public interface OnDragEvent {
	
	public void onStartDrag(AppIcon appIcon, DesktopView dv);
	public void onStopDrag(AppIcon appIcon, DesktopView dv);

}
