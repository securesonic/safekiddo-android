package com.ardurasolutions.safekiddo.launcher.proto;

import java.util.ArrayList;

import android.content.Context;

import com.ardurasolutions.safekiddo.launcher.views.AllAppsGrid.AllAppsAdapter;
import com.ardurasolutions.safekiddo.launcher.views.DesktopView;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.DesktopConfigTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.DesktopConfig;

public class DesktopHelper {
	
	public static boolean loadConfig(DesktopView desk, AllAppsAdapter aaa) {
		String tag = (String) desk.getTag();
		if (tag != null) {
			ArrayList<DesktopConfig> items = LocalSQL.getInstance(desk.getContext()).getTable(DesktopConfigTable.class).getItems(tag);
			
			if (items != null && items.size() > 0) {
				for(DesktopConfig dc : items) {
					AppInfo ai = aaa.getItem(dc.desktop_config_pkg, dc.desktop_config_class);
					if (ai != null)
						desk.addIcon(ai, dc.desktop_config_left, dc.desktop_config_top, dc._id);
				}
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	public static boolean isAnySaveConfig(Context ctx) {
		return LocalSQL.getInstance(ctx).getTable(DesktopConfigTable.class).hasAnyConfig();
	}
	
	public static void removeAllConfigs(Context ctx) {
		LocalSQL.getInstance(ctx)
			.getTable(DesktopConfigTable.class)
			.deleteFully();
	}
	
	/*
	private static class DesktopEntry implements Serializable {
		private static final long serialVersionUID = -5888226565091041759L;
		
		public String pkgName, className;
		public int left, top;
		
		@Override
		public String toString() {
			return "{pkg=" + pkgName + ", class=" + className + ", left=" + left + ", top=" + top  +"}";
		} 
	}
	
	private static class DesktopConfig implements Serializable {
		private static final long serialVersionUID = -3120906612542199930L;
		private ArrayList<DesktopEntry> items = new ArrayList<DesktopEntry>();
		
		public DesktopConfig() {}
		
		public void buildConfig(DesktopView desktop) {
			for(int i=0; i<desktop.getChildCount(); i++) {
				if (desktop.getChildAt(i) instanceof AppIcon) {
					AppIcon ai = (AppIcon) desktop.getChildAt(i);
					DesktopEntry de = new DesktopEntry();
					de.className = ai.getAppInfo().getComponentName().getClassName();
					de.pkgName = ai.getAppInfo().getComponentName().getPackageName();
					de.left = ai.getLeftMargin();
					de.top = ai.getTopMargin();
					items.add(de);
				}
			}
		}
		
		public ArrayList<DesktopEntry> getItems() {
			return items;
		}
		
		@Override
		public String toString() {
			String s = "[";
			if (items.size() > 0) {
				for(DesktopEntry d : items)
					s += d.toString() + ", ";
				s = s.substring(0, s.length()-2);
			}
			s += "]";
			return s;
		}
		
	}
	
	public static void saveConfig(DesktopView desk) {
		try
		{
			File saveTo = new File(desk.getContext().getCacheDir(), desk.getTag() + CONFIG_FILE_EXT);
			DesktopConfig dc = new DesktopConfig();
			dc.buildConfig(desk);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveTo));
			oos.writeObject(dc);
			oos.flush();
			oos.close();
			
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("DesktopHelper::saveConfig", e);
		}
	}
	
	public static boolean loadConfig(DesktopView desk, AllAppsAdapter aaa) {
		File loadFrom = new File(desk.getContext().getCacheDir(), desk.getTag() + CONFIG_FILE_EXT);
		if (loadFrom.exists()) {
			try
			{
				DesktopConfig res = null;
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(loadFrom));
				res = (DesktopConfig) ois.readObject();
				ois.close();
				
				for(DesktopEntry de : res.getItems()) {
					AppInfo ai = aaa.getItem(de.pkgName, de.className);
					if (ai != null)
						desk.addIcon(ai, de.left, de.top);
				}
				
			} catch (Exception e) {
				if (Console.isEnabled())
					Console.loge("DesktopHelper::loadConfig", e);
			}
			return true;
		}
		return false;
	}
	
	public static boolean isAnySaveConfig(Context ctx) {
		boolean res = false;
		
		File c = ctx.getCacheDir();
		File[] l = c.listFiles();
		if (l != null && l.length > 0) {
			for(File sf : l) {
				if (sf.getName().endsWith(CONFIG_FILE_EXT)) {
					res = true;
					break;
				}
			}
		}
		
		return res;
	}
	
	public static void removeAllConfigs(Context ctx) {
		File c = ctx.getCacheDir();
		File[] l = c.listFiles();
		if (l != null && l.length > 0) {
			for(File sf : l) {
				if (sf.getName().endsWith(CONFIG_FILE_EXT)) {
					sf.delete();
				}
			}
		}
	}*/

}
