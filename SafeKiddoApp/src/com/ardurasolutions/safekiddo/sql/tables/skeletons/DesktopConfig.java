package com.ardurasolutions.safekiddo.sql.tables.skeletons;

import com.ardurasolutions.safekiddo.launcher.views.AppIcon;
import com.ardurasolutions.safekiddo.sql.proto.FieldAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableSkeleton;

@TableAnnotation(name = "desktop_config")
public class DesktopConfig extends TableSkeleton {
	
	public static final String FIELD_DESKTOP_CONFIG_ID = "desktop_config_id";
	public static final String FIELD_DESKTOP_CONFIG_PKG = "desktop_config_pkg";
	public static final String FIELD_DESKTOP_CONFIG_CLASS = "desktop_config_class";
	public static final String FIELD_DESKTOP_CONFIG_TOP = "desktop_config_top";
	public static final String FIELD_DESKTOP_CONFIG_LEFT = "desktop_config_left";
	public static final String FIELD_DESKTOP_CONFIG_TAG = "desktop_config_tag";
	
	@FieldAnnotation(isPrimaryKey = true)
	public Long desktop_config_id;
	public String desktop_config_pkg;
	public String desktop_config_class;
	public Integer desktop_config_top;
	public Integer desktop_config_left;
	public String desktop_config_tag;
	
	public static DesktopConfig from(AppIcon ai, String tag) {
		DesktopConfig res = new DesktopConfig();
		
		res.desktop_config_pkg = ai.getAppInfo().getComponentName().getPackageName();
		res.desktop_config_class = ai.getAppInfo().getComponentName().getClassName();
		res.desktop_config_top = ai.getTopMargin();
		res.desktop_config_left = ai.getLeftMargin();
		res.desktop_config_tag = tag;
		
		return res;
	}
	
	/**
	 * comapres only <b>desktop_config_pkg</b> and <b>desktop_config_class</b>
	 */
	@Override
	public boolean equals(Object o) {
		DesktopConfig compareTo = (DesktopConfig) o;
		return compareTo.desktop_config_pkg.equals(desktop_config_pkg) && compareTo.desktop_config_class.equals(desktop_config_class);
	}
	
}
