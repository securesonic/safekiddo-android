package com.ardurasolutions.safekiddo.sql.utils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import android.database.Cursor;

import com.google.gson.JsonArray;
import com.hv.console.Console;

public class DBUtils {
	
	public static LinkedHashMap<String, String> currToObj(Cursor cur) {
		LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
		for(int i=0; i<cur.getColumnCount(); i++) {
			res.put(cur.getColumnName(i), cur.isNull(i) ? null : cur.getString(i));
		}
		return res;
	}
	
	/**
	 * Convert cursor to object of typed class
	 * @param cur - opened and setted cursor
	 * @param c - class name
	 * @return object of typed class
	 */
	public static <T> T currToObj(Cursor cur, Class<T> c) {
		T res = null;
		try {
			res = c.newInstance();
			for(Field f : c.getFields()) {
				int idx = cur.getColumnIndex(f.getName());
				if (idx > -1) {
					Field ff = c.getField(f.getName());
					ff.setAccessible(true);
					
					if (cur.isNull(idx)) {
						if (!ff.getType().isPrimitive())// INFO : czasem moze trafić sie ze pole jest typu primitive to nie ma jak null usatwić
							ff.set(res, null);
						continue;
					}
					
					if (ff.getType().equals(String.class)) {
						ff.set(res, cur.getString(idx));
					} else if (ff.getType().equals(Integer.class) || ff.getType().equals(int.class)) {
						ff.set(res, cur.getInt(idx));
					} else if (ff.getType().equals(Long.class) || ff.getType().equals(long.class)) {
						ff.set(res, cur.getLong(idx));
					} else if (ff.getType().equals(Double.class) || ff.getType().equals(double.class)) {
						ff.set(res, cur.getDouble(idx));
					} else if (ff.getType().equals(byte[].class) || ff.getType().equals(Byte[].class)) {
						ff.set(res, cur.getBlob(idx));
					} else {
						ff.set(res, cur.getString(idx));
					}
				} else {
					//Helper.logd("no column name: " + f.getName());
				}
			}
		} catch (IllegalAccessException e) {
			if (Console.isEnabled()) 
				Console.loge("currToObj[IllegalAccessException]", e);
		} catch (InstantiationException e) {
			if (Console.isEnabled()) 
				Console.loge("currToObj[InstantiationException]", e);
		} catch (SecurityException e) {
			if (Console.isEnabled()) 
				Console.loge("currToObj[SecurityException]", e);
		} catch (NoSuchFieldException e) {
			if (Console.isEnabled()) 
				Console.loge("currToObj[NoSuchFieldException]", e);
		}
		return res;
	}
	
	public static LinkedHashMap<String, String> buildObject(String fields, JsonArray data) {
		LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();
		Integer i = 0;
		for(String field : fields.split(",")) {
			res.put(field, data.get(i).isJsonNull() ? null : data.get(i).getAsString());
			i++;
		}
		return res;
	}
	
	/**
	 * Konwertuje LinkedMap do obiektu klasy
	 * @param map
	 * @param objClass
	 * @return
	 */
	public static <T> T mapToObject(LinkedHashMap<String, String> map, Class<T> objClass) {
		T res = null;
		try {
			res = (T)objClass.newInstance();
			for(Field fx : objClass.getFields()) {
				Field f = objClass.getField(fx.getName());
				f.setAccessible(true);
				
				if (!map.containsKey(f.getName()) || map.get(f.getName()) == null) continue;
				
				if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
					f.set(res, Double.valueOf(map.get(f.getName()).toString()));
				} else if (f.getType().equals(Float.class) || f.getType().equals(float.class)) {
					f.set(res, Double.valueOf(map.get(f.getName()).toString()).floatValue());
				} else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
					f.set(res, Double.valueOf(map.get(f.getName()).toString()).longValue());
				} else if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
					f.set(res, Double.valueOf(map.get(f.getName()).toString()).intValue());
				} else if (f.getType().equals(String.class)) {
					f.set(res, map.get(f.getName()).toString());
				} else {
					f.set(res, map.get(f.getName()));
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return res;
	}

}
