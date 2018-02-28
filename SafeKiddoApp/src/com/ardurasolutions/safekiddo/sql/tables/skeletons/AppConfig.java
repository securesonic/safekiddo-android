package com.ardurasolutions.safekiddo.sql.tables.skeletons;

import com.ardurasolutions.safekiddo.sql.proto.FieldAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableSkeleton;

@TableAnnotation(name = "app_config")
public class AppConfig extends TableSkeleton {
	
	public static final String FIELD_APP_CONFIG_ID = "app_config_id";
	public static final String FIELD_APP_CONFIG_KEY = "app_config_key";
	public static final String FIELD_APP_CONFIG_VALUE = "app_config_value";
	
	@FieldAnnotation(isPrimaryKey = true)
	public Long app_config_id;
	public String app_config_key;
	public String app_config_value;
	
}
