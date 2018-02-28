package com.ardurasolutions.safekiddo.sql.tables.skeletons;

import com.ardurasolutions.safekiddo.sql.proto.FieldAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableSkeleton;

@TableAnnotation(name = "browser_history")
public class BrowserHistory extends TableSkeleton {
	
	public static final String FIELD_BROWSER_HISTORY_ID = "browser_history_id";
	public static final String FIELD_BROWSER_HISTORY_LABEL = "browser_history_label";
	public static final String FIELD_BROWSER_HISTORY_URL = "browser_history_url";
	public static final String FIELD_BROWSER_HISTORY_DATE = "browser_history_date";
	
	@FieldAnnotation(isPrimaryKey = true)
	public Long browser_history_id;
	public String browser_history_label;
	public String browser_history_url;
	public Long browser_history_date;
}
