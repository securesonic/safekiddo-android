package com.ardurasolutions.safekiddo.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.hv.console.Console;

/**
 * Utility class for setting WebKit proxy used by Android WebView
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProxySystem {

	static final int PROXY_CHANGED = 193;

	private static Object getDeclaredField(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		Object out = f.get(obj);
		return out;
	}

	public static Object getRequestQueue(Context ctx) throws Exception {
		Object ret = null;
		Class networkClass = Class.forName("android.webkit.Network");
		if (networkClass != null) {
			Object networkObj = invokeMethod(networkClass, "getInstance", new Object[] { ctx },
					Context.class);
			if (networkObj != null) {
				ret = getDeclaredField(networkObj, "mRequestQueue");
			}
		}
		return ret;
	}

	private static Object invokeMethod(Object object, String methodName, Object[] params,
			Class... types) throws Exception {
		Object out = null;
		Class c = object instanceof Class ? (Class) object : object.getClass();
		if (types != null) {
			Method method = c.getMethod(methodName, types);
			out = method.invoke(object, params);
		} else {
			Method method = c.getMethod(methodName);
			out = method.invoke(object);
		}
		return out;
	}

	public static void resetProxy(Context ctx) throws Exception {
		Object requestQueueObject = getRequestQueue(ctx);
		if (requestQueueObject != null) {
			setDeclaredField(requestQueueObject, "mProxyHost", null);
		}
	}

	private static void setDeclaredField(Object obj, String name, Object value)
			throws SecurityException, NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Field f = obj.getClass().getDeclaredField(name);
		f.setAccessible(true);
		f.set(obj, value);
	}

	
	/* SYSTEM PROXY ------------------------------------------------ */
	public static WifiConfiguration getCurrentWifiConfiguration(WifiManager manager) {
		if (!manager.isWifiEnabled()) 
			return null;
		
		List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
		WifiConfiguration configuration = null;
		int cur = manager.getConnectionInfo().getNetworkId();
		for (int i = 0; i < configurationList.size(); ++i)
		{
			WifiConfiguration wifiConfiguration = configurationList.get(i);
			if (wifiConfiguration.networkId == cur)
				configuration = wifiConfiguration;
		}
		
		return configuration;
	}
	
	@SuppressWarnings("unused")
	private static boolean setWiFiProxyRes(Context ctx, String proxyIp, int proxyPort) {
		try {
			setWiFiProxy(ctx, proxyIp, proxyPort);
			return true;
		} catch (SecurityException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[SecurityException]", e);
		} catch (IllegalArgumentException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[IllegalArgumentException]", e);
		} catch (NoSuchFieldException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[NoSuchFieldException]", e);
		} catch (IllegalAccessException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[IllegalAccessException]", e);
		} catch (ClassNotFoundException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[ClassNotFoundException]", e);
		} catch (NoSuchMethodException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[NoSuchMethodException]", e);
		} catch (InstantiationException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[InstantiationException]", e);
		} catch (InvocationTargetException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[InvocationTargetException]", e);
		} catch (UnknownHostException e) {
			if (Console.isEnabled())
				Console.loge("setWiFiProxyRes[UnknownHostException]", e);
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private static void usetWiFiProxySingle(WifiManager manager, WifiConfiguration config, String proxyIp, int proxyPort)  throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, UnknownHostException {
		
		Object linkProperties = getField(config, "linkProperties");

		if (linkProperties == null) {
			if (Console.isEnabled())
				Console.logw("usetWiFiProxySingle no linkProperties object");
			return;
		}
		
		Object mHttpProxy = getDeclaredField(linkProperties, "mHttpProxy");
		if (mHttpProxy != null) {
			String mHost = (String)getDeclaredField(mHttpProxy, "mHost");
			Integer mPort = (Integer)getDeclaredField(mHttpProxy, "mPort");
			
			if (mHost.equals(proxyIp) && proxyPort == mPort) {
				
				//get the setHttpProxy method for LinkProperties
				Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
				Class[] setHttpProxyParams = new Class[1];
				setHttpProxyParams[0] = proxyPropertiesClass;
				Class lpClass = Class.forName("android.net.LinkProperties");
				Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
				setHttpProxy.setAccessible(true);
				
				//pass null as the proxy
				Object[] params = new Object[1];
				params[0] = null;
				setHttpProxy.invoke(linkProperties, params);
				
				setProxySettings("NONE", config);
				
				manager.updateNetwork(config);
			} else {
				// other wifi proxy settings
				return;
			}
		}
	}
	
	public static void usetWiFiProxy(Context ctx, String proxyIp, int proxyPort)  throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, UnknownHostException {
		/*WifiManager manager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		for(WifiConfiguration config : manager.getConfiguredNetworks()) {
			usetWiFiProxySingle(manager, config,proxyIp, proxyPort);
		}
		manager.disconnect();
		manager.reconnect();*/
	}
	
	public static void usetWiFiProxySafe(Context ctx, String proxyIp, int proxyPort) {
		try {
			usetWiFiProxy(ctx, proxyIp, proxyPort);
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		} catch (InstantiationException e) {
		} catch (InvocationTargetException e) {
		} catch (UnknownHostException e) {
		}
	}
	
	public static void setWiFiProxy(Context ctx, String proxyIp, int proxyPort) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, UnknownHostException {
		if (Constants.DEV_DISABLE_PROXY_SET) return;
		
		if (Build.VERSION.SDK_INT <= 14) return;
		
		WifiManager manager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration config = getCurrentWifiConfiguration(manager);
		if (config == null) {
			if (Console.isEnabled())
				Console.logw("setWiFiProxy - no curent WiFi config");
			return;
		}
		
		Object linkProperties = getField(config, "linkProperties");

		if (linkProperties == null) {
			if (Console.isEnabled())
				Console.logw("setWiFiProxy no linkProperties object");
			return;
		}
		
		Object mHttpProxy = getDeclaredField(linkProperties, "mHttpProxy");
		if (mHttpProxy != null) {
			String mHost = (String)getDeclaredField(mHttpProxy, "mHost");
			Integer mPort = (Integer)getDeclaredField(mHttpProxy, "mPort");
			
			if (mHost.equals(proxyIp) && proxyPort == mPort) {
				if (Console.isEnabled())
					Console.logi("setWiFiProxy - proxy is ok");
				return;
			}
		}
		
		Class<?> proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
		Class<?>[] setHttpProxyParams = new Class[1];
		setHttpProxyParams[0] = proxyPropertiesClass;
		Class<?> lpClass = Class.forName("android.net.LinkProperties");
		Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
		setHttpProxy.setAccessible(true);
		
		//get ProxyProperties constructor
		Class<?>[] proxyPropertiesCtorParamTypes = new Class[3];
		proxyPropertiesCtorParamTypes[0] = String.class;
		proxyPropertiesCtorParamTypes[1] = int.class;
		proxyPropertiesCtorParamTypes[2] = String.class;
		
		Constructor<?> proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);
		
		//create the parameters for the constructor
		Object[] proxyPropertiesCtorParams = new Object[3];
		proxyPropertiesCtorParams[0] = proxyIp;
		proxyPropertiesCtorParams[1] = proxyPort;
		proxyPropertiesCtorParams[2] = null;
		
		//create a new object using the params
		Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);
		
		//pass the new object to setHttpProxy
		Object[] params = new Object[1];
		params[0] = proxySettings;
		setHttpProxy.invoke(linkProperties, params);
		
		setProxySettings("STATIC", config);
		
		//save the settings
		manager.updateNetwork(config);
		manager.disconnect();
		manager.reconnect();
	}
	
	public static Object getField(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field f = obj.getClass().getField(name);
		Object out = f.get(obj);
		return out;
	}
	
	public static void setEnumField(Object obj, String value, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		Field f = obj.getClass().getField(name);
		f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
	}

	public static Object getEnumField(Object obj, String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
		return obj.getClass().getField(name).get(obj);
	}

	public static void setProxySettings(String assign , WifiConfiguration wifiConf) throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException{
		setEnumField(wifiConf, assign, "proxySettings");     
	}
	public static void setupWiFiProxy(Context ctx) {
		//setWiFiProxyRes(ctx.getApplicationContext(), "127.0.0.1", Constants.LOCAL_PROXY_PORT);
	}

}