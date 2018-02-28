package com.ardurasolutions.safekiddo.sql.tables.skeletons;

import com.ardurasolutions.safekiddo.sql.proto.FieldAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableAnnotation;
import com.ardurasolutions.safekiddo.sql.proto.TableSkeleton;

@TableAnnotation(name = "browser_favs")
public class BrowserFavs extends TableSkeleton {
	
	public static final String FIELD_BROWSER_FAVS_ID = "browser_favs_id";
	public static final String FIELD_BROWSER_FAVS_LABEL = "browser_favs_label";
	public static final String FIELD_BROWSER_FAVS_URL = "browser_favs_url";
	public static final String FIELD_BROWSER_FAVS_TYPE = "browser_favs_type";
	public static final String FIELD_BROWSER_FAVS_PARENT = "browser_favs_parent";
	public static final String FIELD_BROWSER_FAVS_POS = "browser_favs_pos";
	
	@FieldAnnotation(isPrimaryKey = true)
	public Long browser_favs_id;
	public String browser_favs_label;
	public String browser_favs_url;
	
	/**
	 * 0 - link<br>
	 * 1 - folder
	 */
	@FieldAnnotation(extra = "DEFAULT 0")
	public Integer browser_favs_type;
	
	public Long browser_favs_parent;
	public Long browser_favs_pos;
	
	/**
	 * do "rysowania" drzewka folderów
	 */
	@FieldAnnotation(virtualField = true)
	public Integer browser_favs_lvl = 0;
	
	public static enum FavsType {
		
		FAV(0),
		FOLDER(1);
		
		private final int id;
		
		FavsType(int id) { 
			this.id = id; 
		}
		public int getValue() { 
			return id; 
		}
	}
	
	@Override
	public String toString() {
		String spaces = "";
		if (browser_favs_lvl > 0) {
			for(int i=0; i<browser_favs_lvl; i++) {
				spaces += "   ";
			}
		} 
		return spaces + browser_favs_label;
	}
}
