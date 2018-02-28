package com.ardurasolutions.safekiddo.launcher.proto;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;

public class AppInfo {
	
	private String label;
	private ComponentName componentName;
	private Drawable icon;
	private Bitmap shadow;
	
	public AppInfo(ComponentName cn) {
		setComponentName(cn);
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public ComponentName getComponentName() {
		return componentName;
	}
	public void setComponentName(ComponentName componentName) {
		this.componentName = componentName;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public Bitmap getShadow() {
		return shadow;
	}

	public void setShadow(Bitmap shadow) {
		this.shadow = shadow;
	}
	
	@Override
	public String toString() {
		return "{label=" + getLabel() + ", component=" + getComponentName() + "}";
	}
	
	public static AppInfo fromResolveInfo(ResolveInfo iApp, PackageManager pm, Resources res) {
		AppInfo app = new AppInfo(new ComponentName(iApp.activityInfo.applicationInfo.packageName, iApp.activityInfo.name));
		app.setLabel(iApp.loadLabel(pm).toString());
		Drawable icon = iApp.activityInfo.loadIcon(pm);
		
		if (icon != null) {
			int iconSize = res.getDimensionPixelSize(R.dimen.all_apps_icon_size);
			Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
			if (bitmap.getWidth() != iconSize && bitmap.getHeight()!= iconSize ){
				bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
				icon = new BitmapDrawable(res, bitmap);
			}
			app.setIcon(icon);
			app.setShadow(CommonUtils.makeBitmapShadow(bitmap));
		}
		return app;
	}
	
	public static AppInfo fromIntent(Intent it, PackageManager pm, Resources res) {
		return fromResolveInfo(pm.resolveActivity(it, 0), pm, res);
	}
	
	public boolean isEqualTo(String pkg, String className) {
		return 
				pkg.equals(componentName.getPackageName()) && className.equals(componentName.getClassName());
	}
	
//	public Drawable getIconClone(Context ctx) {
//		return getIcon().getConstantState().newDrawable(ctx.getResources());
//	}

}
