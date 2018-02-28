package com.ardurasolutions.safekiddo.sql.proto;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Intent;
import android.os.Bundle;

import com.ardurasolutions.safekiddo.helpers.TextUtils;

public class TableSkeleton implements Cloneable {
	
	public static final String FIELD_ID = "_id";
	public static final String FIELD_SYNC_IS_CHANGED = "sync_isChanged";
	public static final String FIELD_SYNC_IS_DELETED = "sync_isDeleted";
	public static final String FIELD_SYNC_IS_REJECTED = "sync_isRejected";
	public static final String FIELD_SERVER_TIMESTAMP = "server_timestamp";
	public static final String FIELD_DEVICE_TIMESTAMP = "device_timestamp";
	public static final String FIELD_ENABLE = "enable";
	
	public Long _id;
	@FieldAnnotation(extra = "DEFAULT 2")
	public int sync_isChanged = 2;
	@FieldAnnotation(extra = "DEFAULT 0")
	public int sync_isDeleted = 0;
	@FieldAnnotation(extra = "DEFAULT 0", onlyLocal = true)
	public int sync_isRejected = 0;
	@FieldAnnotation(extra = "DEFAULT NULL")
	public Long server_timestamp = null;
	@FieldAnnotation(extra = "DEFAULT NULL")
	public Long device_timestamp = null;
	@FieldAnnotation(extra = "DEFAULT 1")
	public Integer enable = 1;
	
	public static String getCreateString(Class<?> c) {
		StringBuilder res = new StringBuilder();
		
		res.append("CREATE TABLE IF NOT EXISTS " + TableSkeleton.getTableName(c) + "(\n_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL \n");
		for(Field f : c.getFields()) {
			if (f.getName().equals("_id")) continue;
			
			// INFO : pominięcie virtualnych pól oraz satycznych oznaczajacych nazwy kolumn
			FieldAnnotation fa = f.getAnnotation(FieldAnnotation.class);
			if (fa != null && fa.virtualField()) continue;
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
			
			res.append(", " + f.getName() + " ");
			if (f.getType().equals(String.class)) {
				res.append("TEXT");
			} else if (f.getType().equals(Integer.class) || f.getType().equals(Long.class)){
				res.append("INTEGER");
			} else if (f.getType().equals(int.class)){
				res.append("INT");
			} else if (f.getType().equals(Double.class)){
				res.append("DOUBLE");
			} else if (f.getType().equals(byte[].class)){
				res.append("BLOB");
			} else {
				res.append("TEXT");
			}
			
			if (fa != null) {
				res.append(" " + fa.extra());
			}
			res.append(" \n");
		}
		res.append(")");
		
		return res.toString();
	}
	
	public static String getPrimaryFieldName(Class<?> cl) {
		for(Field f : cl.getFields()) {
			FieldAnnotation fa = f.getAnnotation(FieldAnnotation.class);
			if (fa != null && fa.isPrimaryKey() && !fa.onlyLocal() && !fa.isGeneratedField()) {
				return f.getName();
			}
		}
		return null;
	}
	
	public String getPrimaryFieldName() {
		return getPrimaryFieldName(this.getClass());
	}
	
	public static String getTableName(Class<?> c) {
		TableAnnotation ta = c.getAnnotation(TableAnnotation.class);
		return ta == null ? "" : ta.name();
	}
	
	public String toString() {
		String res = "{";
		try {
			for(Field f : this.getClass().getFields()) {
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				if (f.get(this) == null) {
					res += f.getName() + "=NULL, ";
				} else {
					res += f.getName() + "=" + f.get(this).toString() + ", ";
				}
			}
			res = res.length() > 2 ? res.substring(0, res.length()-2) + "}" : "}";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public Bundle toBundle(LinkedHashMap<String, String> map) {
		Bundle res = new Bundle();
		
		try {
			for(Field f : this.getClass().getFields()) {
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				if (f.getType().equals(Double.class)) {
					res.putDouble(f.getName(), f.getDouble(this));
				} else if (f.getType().equals(Long.class)) {
					res.putLong(f.getName(), f.getLong(this));
				} else if (f.getType().equals(Integer.class)) {
					res.putInt(f.getName(), f.getInt(this));
				} else if (f.getType().equals(String.class)) {
					res.putString(f.getName(), f.get(this) != null ? f.get(this).toString() : null);
				} else {
					res.putString(f.getName(), f.get(this) != null ? f.get(this).toString() : null);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static String getFieldsComa(Class<?> cl) {
		return TextUtils.join(getFieldsArray(cl), ",");
	}
	
	public static String[] getFields(Class<?> cl) {
		ArrayList<String> res = getFieldsArray(cl);
		return (String [])res.toArray(new String[res.size()]);
	}
	
	public static ArrayList<String> getFieldsArray(Class<?> cl) {
		ArrayList<String> res = new ArrayList<String>();
		for(Field f : cl.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
			String fName = f.getName();
			if (fName.equals("_id")) continue; // fName.equals("server_timestamp")
			if (f.getAnnotation(FieldAnnotation.class) != null && f.getAnnotation(FieldAnnotation.class).onlyLocal()) continue;
			if (f.getAnnotation(FieldAnnotation.class) != null && f.getAnnotation(FieldAnnotation.class).virtualField()) continue;
			res.add(fName);
		}
		return res;
	}
	
	/**
	 * get all local fileds - not system fields like sync_isRejected
	 * @param cl
	 * @return
	 */
	public static ArrayList<String> getFieldsGeneratedArray(Class<?> cl) {
		ArrayList<String> res = new ArrayList<String>();
		for(Field f : cl.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
			String fName = f.getName();
			FieldAnnotation fa = f.getAnnotation(FieldAnnotation.class);
			if (fa == null) continue;
			if (!fa.onlyLocal()) continue;
			if (!fa.isGeneratedField()) continue;
			if (fa.virtualField()) continue;
			res.add(fName);
		}
		return res;
	}
	
	public static Intent saveToIntent(Intent it, Object o) {
		if (it == null || o == null) return it;
		try {
			Class<?> c = o.getClass();
			for(Field fx : c.getFields()) {
				Field f = c.getField(fx.getName());
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				
				if (f.get(o) == null) continue;
				
				if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
					it.putExtra(f.getName(), (Double)f.get(o));
				} else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
					it.putExtra(f.getName(), (Long)f.get(o));
				} else if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
					it.putExtra(f.getName(), (Integer)f.get(o));
				} else if (f.getType().equals(String.class)) {
					it.putExtra(f.getName(), (String)f.get(o));
				} else {
					it.putExtra(f.getName(), f.get(o) != null ? f.get(o).toString() : null);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return it;
	}
	
	public Intent saveToIntent(Intent it) {
		if (it == null) return it;
		try {
			Class<?> c = this.getClass();
			for(Field fx : c.getFields()) {
				Field f = c.getField(fx.getName());
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				
				if (f.get(this) == null) continue;
				
				if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
					it.putExtra(f.getName(), (Double)f.get(this));
				} else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
					it.putExtra(f.getName(), (Long)f.get(this));
				} else if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
					it.putExtra(f.getName(), (Integer)f.get(this));
				} else if (f.getType().equals(String.class)) {
					it.putExtra(f.getName(), (String)f.get(this));
				} else {
					it.putExtra(f.getName(), f.get(this) != null ? f.get(this).toString() : null);
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return it;
	}
	
	public static <T> T readFromIntent(Intent it, Class<T> c) {
		if (it == null) return null;
		T res = null;
		try {
			res = (T)c.newInstance();
			for(Field fx : c.getFields()) {
				Field f = c.getField(fx.getName());
				f.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				
				if (!it.hasExtra(f.getName())) {
					if (!f.getType().isPrimitive())
						f.set(res, null); 
					continue;
				}
				
				if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
					f.set(res, Double.valueOf(it.getDoubleExtra(f.getName(), 0D)));
				} else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
					f.set(res, Long.valueOf(it.getLongExtra(f.getName(), 0L)));
				} else if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
					f.set(res, Integer.valueOf(it.getIntExtra(f.getName(), 0)));
				} else if (f.getType().equals(String.class)) {
					f.set(res, it.getStringExtra(f.getName()));
				} else {
					// TODO unsuported type
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * return null when fails
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			//
		}
		return null;
	}
	
	/**
	 * copy values from object to "this"
	 * @param val - another class like TableSkeleton subclass
	 * @return tue if success false otherwise
	 */
	public boolean copyValuesFrom(Object val) {
		for(Field f : val.getClass().getFields()) {
			Field thisField = null;
			try {
				thisField = this.getClass().getField(f.getName());
				thisField.setAccessible(true);
				if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) continue;
				thisField.set(this, f.get(val));
			} catch(NoSuchFieldException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
}
