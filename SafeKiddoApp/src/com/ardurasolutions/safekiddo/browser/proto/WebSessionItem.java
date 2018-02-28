package com.ardurasolutions.safekiddo.browser.proto;

import java.io.Serializable;

import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.WebFragmentItem;

public class WebSessionItem implements Serializable {
	
	public static final String KEY_FORDE_RELOAD = "force_reload";

	private static final long serialVersionUID = -4678740425636285608L;
	
	private Long lastAccessTime;
	private Integer id;
	private String title = null, url;
	private SerialBitmap thumb;
	private boolean isFragmentInitialized = false;
	private boolean forceReloadAfterRestore = false;
	
	public boolean isFragmentInitialized() {
		return this.isFragmentInitialized;
	}
	
	public WebFragmentItem toWebFragmentItem() {
		WebFragmentItem res = new WebFragmentItem();
		
		res.setLastAccessTime(lastAccessTime);
		res.setId(id);
		res.setTitle(title);
		res.setUrl(url);
		res.setThumb(thumb.getBitmap());
		res.setForceReloadAfterRestore(forceReloadAfterRestore);
		
		return res;
	}
	
	public static WebSessionItem fromWebFragmentItem(WebFragmentItem wfi) {
		WebSessionItem res = new WebSessionItem();
		
		res.lastAccessTime = wfi.getLastAccessTime();
		res.id = wfi.getId();
		res.title = wfi.getTitle();
		res.url = wfi.getUrl();
		res.thumb = new SerialBitmap(wfi.getThumb());
		res.isFragmentInitialized = wfi.isFragmentExists();
		res.forceReloadAfterRestore = wfi.isForceReloadAfterRestore();
		
		return res;
	}
	
	

}
