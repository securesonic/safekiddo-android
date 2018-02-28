package com.ardurasolutions.safekiddo.sql.tables.skeletons;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

import com.ardurasolutions.safekiddo.sql.proto.FieldAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableSkeleton;

@TableAnnotation(name = "all_apps")
public class AllApps extends TableSkeleton {
	
	public static final String FIELD_ALL_APPS_ID = "all_apps_id";
	public static final String FIELD_ALL_APPS_PACKAGE = "all_apps_package";
	public static final String FIELD_ALL_APPS_CLASS = "all_apps_class";
	public static final String FIELD_ALL_APPS_LABEL = "all_apps_label";
	public static final String FIELD_ALL_APPS_IS_BLOCKED = "all_apps_is_blocked";
	public static final String FIELD_ALL_APPS_ICON = "all_apps_icon";
	
	@FieldAnnotation(isPrimaryKey = true)
	public Long all_apps_id;
	public String all_apps_label;
	public String all_apps_package;
	public String all_apps_class;
	/**
	 * blocked = 1<br>
	 * not blocked = 0
	 */
	@FieldAnnotation(extra = "DEFAULT 0")
	public Integer all_apps_is_blocked = 0;
	@FieldAnnotation(virtualField = true)
	public Drawable all_apps_icon;
	
	public AllApps() {}
	
	public AllApps(String pkg, String cls) {
		all_apps_package = pkg;
		all_apps_class = cls;
	}
	
	public AllApps(ComponentName cn) {
		all_apps_package = cn.getPackageName();
		all_apps_class = cn.getClassName();
	}
	
	public boolean isBlocked() {
		return all_apps_is_blocked == null ? false : all_apps_is_blocked != 0;
	}
	
	@Override
	public boolean equals(Object o) {
		AllApps compareTo = (AllApps) o;
		return 
			all_apps_package.equals(compareTo.all_apps_package) && 
			all_apps_class.equals(compareTo.all_apps_class);
	}
	
	@Override
	public String toString() {
		return "{" + all_apps_package + "/" + all_apps_class+ "}";
	}
	
}
