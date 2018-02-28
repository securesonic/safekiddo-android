package com.ardurasolutions.safekiddo.services;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.nio.charset.Charset;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ardurasolutions.safekiddo.browser.interfaces.OnProxyServerStarts;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.Network;
import com.ardurasolutions.safekiddo.helpers.WSHelper;
import com.ardurasolutions.safekiddo.helpers.WSHelper.CheckResult;
import com.ardurasolutions.safekiddo.helpers.WSHelper.UserActionType;
import com.ardurasolutions.safekiddo.proto.LocalServiceBinder;
import com.hv.console.Console;

public class ProxyService extends Service {
	
	private final IBinder mBinder = new LocalServiceBinder<ProxyService>(this);
	private HttpProxyServer server;
	private boolean isStartRunning = false, isRuned = false;
	//private ChildElement mChildElement;
	private BroadcastReceiver mChildProfileChanged, mSafekiddoRemove;
	private OnProxyServerStarts mOnProxyServerStarts;

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public void onCreate() { 
		super.onCreate();
		if (Console.isEnabled())
			Console.logi("Proxy service create...");
		
		mChildProfileChanged = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logi("Get refresh child profile");
				//mChildElement = UserHelper.getCurrentChildProfile(ProxyService.this);
			}
		};
		mSafekiddoRemove = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Console.isEnabled())
					Console.logi("SafeKiddo remove in proxy service");
				stopSelf();
			}
		};
		registerReceiver(mChildProfileChanged, new IntentFilter(Constants.BRODCAST_CHILD_PROFILE_CHANGED));
		registerReceiver(mSafekiddoRemove, new IntentFilter(Constants.BRODCAST_SAFEKIDDO_REMOVE));
	}
	
	public boolean isStartRunning() {
		return isStartRunning;
	}
	
	public boolean isRuned() {
		return isRuned;
	}
	
	private Thread proxyThread = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isStartRunning) {
			if (Console.isEnabled())
				Console.logi("Server is running now");
			return START_STICKY;
		}
		
		//new Thread(new Runnable() {
			//@Override
			//public void run() {
		
		proxyThread = new Thread() {
			@Override
			public void run() {
				
				int localPort = Config.getInstance(ProxyService.this).load(Config.KeyNames.LOCAL_PROXY_PORT, Constants.LOCAL_PROXY_PORT);
				// INFO try find first free port
				if (Network.isLocalPortisFree(localPort)) {
					
					Config.getInstance(ProxyService.this).save(Config.KeyNames.LOCAL_PROXY_PORT, localPort);
					
				} else {
					localPort = Network.findFreeLocalPort();
					if (Console.isEnabled())
						Console.logi("PROXY PORT : " + localPort);
					Config.getInstance(ProxyService.this).save(Config.KeyNames.LOCAL_PROXY_PORT, localPort);
					sendBroadcast(new Intent().setAction(Constants.BRODCAST_LOCAL_PROXY_PORT));
				}
				
				isStartRunning = true;
				HttpProxyServerBootstrap bs = DefaultHttpProxyServer.bootstrap()
					.withPort(localPort)
					
					.withFiltersSource(new HttpFiltersSourceAdapter() {
						
						@Override
						public DefaultFullHttpResponse gatewayTimeoutResponse() {
							String body = "";
							try {
								body = CommonUtils.streamToString(getResources().getAssets().open("html/gateway_timeout.html"), 1024 * 8);
							} catch (IOException e) {
								if (Console.isEnabled())
									Console.loge("gatewayTimeoutResponse[IO]", e);
							}
							
							byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
							
							DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
							response.content().writeBytes(bytes);
							response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
							response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
							response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
							
							return response;
						};
						
						@Override
						public DefaultFullHttpResponse badGatewayResponse(HttpRequest request) {
							String body = "";
							try {
								body = CommonUtils.streamToString(getResources().getAssets().open("html/bad_gateway.html"), 1024 * 8);
							} catch (IOException e) {
								if (Console.isEnabled())
									Console.loge("badGatewayResponse[IO]", e);
							}
							
							body = body.replaceAll("#URL#", request.getUri());
							byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
							
							DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
							response.content().writeBytes(bytes);
							response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
							response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
							response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
							
							return response;
						}
						
						public HttpFilters filterRequest(HttpRequest originalRequest, io.netty.channel.ChannelHandlerContext ctx, HttpRequest request) {
							
							// NO INTERNET CONN
							if (!CommonUtils.isOnline(ProxyService.this)) {
								return new org.littleshoot.proxy.HttpFiltersAdapter(originalRequest) {
									@Override
									public HttpResponse requestPre(HttpObject httpObject) {
										String body = "";
										try {
											body = CommonUtils.streamToString(getResources().getAssets().open("html/no_internet.html"), 1024 * 8);
										} catch (IOException e) {
											if (Console.isEnabled())
												Console.loge("filterRequest[IsOnline=FALSE]", e);
										}
										
										byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
										
										DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
										response.content().writeBytes(bytes);
										response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
										response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
										response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
										
										return response;
									}
								};
							}
							
							final String checkUrl = request.getUri();
							
							// INFO url po jakim sprawdza się czy server proxy chodzi
							if (checkUrl.equals("http://127.0.0.1/test")) {
								Console.logd("local check");
								return new org.littleshoot.proxy.HttpFiltersAdapter(originalRequest) {
									@Override
									public HttpResponse requestPre(HttpObject httpObject) {
										byte[] bytes = "SAFEKIDDO PROXY OK".getBytes(Charset.forName("UTF-8"));
										DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
										response.content().writeBytes(bytes);
										response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
										response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
										response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
										return response;
									}
								};
							}
							
							// INFO jeżeli nie http (czyli https) to puszczaj - nie blokuj
							if (!checkUrl.startsWith("http://")) {
								if (Console.isEnabled())
									Console.log("PROXY SKIP HTTPS URL: " + checkUrl);
								return super.filterRequest(originalRequest, ctx, request);
							}
							
							// INFO jeżeli url z domeny safekiddo to nie sprawdzaj
							if (checkUrl.startsWith(Constants.getBaseUrl())) {
								if (Console.isEnabled())
									Console.log("PROXY SKIP URL: " + checkUrl);
								return super.filterRequest(originalRequest, ctx, request);
							}
							
							//final WSHelper.ErrorCode allowedUrl = WSHelper.isUrlAllowed(checkUrl, UserActionType.NO_LOG_REQUEST, ProxyService.this);
							final CheckResult mCheckResult = WSHelper.checkUrl(checkUrl, UserActionType.NO_LOG_REQUEST, ProxyService.this);
							
							if (!mCheckResult.isSuccess()) {
								
								switch(mCheckResult.getErrorCode()) {
									default:
										return new org.littleshoot.proxy.HttpFiltersAdapter(originalRequest) {
											@Override
											public HttpResponse requestPre(HttpObject httpObject) {
												DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
												response.headers().add(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE + ", no-store, must-revalidate");
												response.headers().add(HttpHeaders.Names.PRAGMA, HttpHeaders.Values.NO_CACHE);
												response.headers().add(HttpHeaders.Names.EXPIRES, "0");
												return response;
											}
										};
									case EMPTY:
										return new org.littleshoot.proxy.HttpFiltersAdapter(originalRequest) {
											@Override
											public HttpResponse requestPre(HttpObject httpObject) {
												String body = "";
												try {
													body = CommonUtils.streamToString(getResources().getAssets().open("html/no_ws.html"), 1024 * 8);
												} catch (IOException e) {
													if (Console.isEnabled())
														Console.loge("filterRequest[ErrorCode=EMPTY]", e);
												}
												
												byte[] bytes = body.getBytes(Charset.forName("UTF-8"));
												
												DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
												response.content().writeBytes(bytes);
												response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
												response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
												response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
												
												return response;
											}
										};
								}
								
							} else {
								return super.filterRequest(originalRequest, ctx, request);
							}
						}
					
					});
					
				// 07-28 09:30:08.481: W/System.err(2610): Caused by: java.net.BindException: bind failed: EADDRINUSE (Address already in use)

				try {
					server = bs.start();
					isStartRunning = true;
					isRuned = true;
					if (getOnProxyServerStarts() != null)
						getOnProxyServerStarts().onProxyServerStarts();
					if (Console.isEnabled())
						Console.logi("Proxy server starts at port " + localPort);
				} catch (Exception e) {
					if (Console.isEnabled())
						Console.loge("PROXY SERVER STARTS ERROR on port " + localPort, e);
					isStartRunning = false;
					isRuned = false;
					stopSelf();
				}
			//}
			}
		};
		
		proxyThread.start();
		//}).start();
		return START_STICKY;
	}
	
	public void stopServer() {
		isStartRunning = false;
		isRuned = false;
		if (server != null) {
			proxyThread.interrupt();
			proxyThread = null;
		}
		//if (server != null) server.stop();
		server = null;
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		stopSelf();
	}
	
	@Override
	public void onDestroy() {
		stopServer();
		
		if (mChildProfileChanged != null)
			unregisterReceiver(mChildProfileChanged);
		
		if (mSafekiddoRemove != null)
			unregisterReceiver(mSafekiddoRemove);
		
		if (Console.isEnabled())
			Console.logi("Proxy server destroy");
		super.onDestroy();
	}

	public OnProxyServerStarts getOnProxyServerStarts() {
		return mOnProxyServerStarts;
	}

	public void setOnProxyServerStarts(OnProxyServerStarts mOnProxyServerStarts) {
		this.mOnProxyServerStarts = mOnProxyServerStarts;
	}

}
