package com.ardurasolutions.safekiddo.browser.proto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ardurasolutions.safekiddo.browser.BrowserFrameFragment;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.WebFragmentItem.OnUpdateWebFragmentItem;
import com.ardurasolutions.safekiddo.helpers.DateTime;
import com.hv.console.Console;

public class WebFragmentsManager {
	
	public static interface OnFragmentDestroy {
		public void onFragmentDestroy(WebFragmentItem item);
	}
	
	public static interface OnFragmentUpdate {
		public void onFragmentUpdate(WebFragmentItem item);
		public void onShowFragment(BrowserFrameFragment wfi);
		public void onHideFragment(BrowserFrameFragment wfi);
	}
	
	private class ComparatorByTime implements Comparator<WebFragmentItem> {
		@Override
		public int compare(WebFragmentItem o1, WebFragmentItem o2) {
			return o1.getLastAccessTime().compareTo(o2.getLastAccessTime());
		}
	}
	private class ComparatorById implements Comparator<WebFragmentItem> {
		@Override
		public int compare(WebFragmentItem o1, WebFragmentItem o2) {
			return o1.getId().compareTo(o2.getId());
		}
	}
	
	private static final int MAX_EXISTSNG_FRAGMENTS = 5;
	private static final String FRAGMENT_TAG_PREFIX = "idx_";
	private static final String WEB_CACHE = "web_cache";
	private static final String SESSIONS_FILE = "sessions.data";
	public static final String SAVE_RESTORE_STATE_FILE_KEY = "cache_";
	
	private ComparatorByTime mComparatorByTime = new ComparatorByTime();
	private ComparatorById mComparatorById = new ComparatorById();
	public ArrayList<WebFragmentItem> items = new ArrayList<WebFragmentItem>();
	private Integer idInc = 1;
	private OnFragmentDestroy mOnFragmentDestroy;
	private OnFragmentUpdate mOnFragmentUpdate;
	private OnUpdateWebFragmentItem mOnUpdateWebFragmentItem;
	private FragmentManager mFragmentManager;
	private int fragmentFrameId = 0;
	private WebFragmentItem topItem = null;
	private File saveWebViewStateDir = Environment.getExternalStorageDirectory(); //new File(Environment.getExternalStorageDirectory(), "");
	
	public static File getWebCacheDir(File cacheDir) {
		return new File(cacheDir, WEB_CACHE);
	}
	
	public WebFragmentsManager(File cacheDir) {
		
		saveWebViewStateDir = getWebCacheDir(cacheDir);
		if (!saveWebViewStateDir.exists())
			saveWebViewStateDir.mkdir();
		
		mOnUpdateWebFragmentItem = new OnUpdateWebFragmentItem() {
			@Override
			public void onWebFragmentItem(WebFragmentItem i) {
				if (getOnFragmentUpdate() != null)
					getOnFragmentUpdate().onFragmentUpdate(i);
			}
		};
	}
	
	public WebFragmentItem registerNewFragment(BrowserFrameFragment wf) {
		hideAllFragments(null);
		WebFragmentItem item = new WebFragmentItem();
		item.setWebFragment(wf);
		item.setId(idInc);
		item.setOnUpdateWebFragmentItem(mOnUpdateWebFragmentItem);
		item.updateLastAccessTime();
		wf.setWebFragmentItem(item);
		
		items.add(item);
		idInc = idInc + 1;
		sortList();
		optimize();
		
		loadFragment(wf, item.getId());
		topItem = item;
		
		if (mOnFragmentUpdate != null)
			mOnFragmentUpdate.onShowFragment(wf);
		
		return item;
	}
	
	public void optimize() {
		sortList();
		ArrayList<WebFragmentItem> e = items;
		if (e.size() > MAX_EXISTSNG_FRAGMENTS) {
			
			// poukładnie od najstarszej do najnowszej
			for(int i=0; i<e.size(); i++) {
				WebFragmentItem itemD = items.get(i);
				if (!itemD.isFragmentExists()) continue;
				if (i >= e.size() - MAX_EXISTSNG_FRAGMENTS) break;
				
				if (mOnFragmentDestroy != null && itemD.isFragmentExists()) {
					mOnFragmentDestroy.onFragmentDestroy(itemD);
				}
				
				saveWebViewState(itemD);
				
				itemD.destroyFragment();
			}
		}
	}
	
	private void saveWebViewState(WebFragmentItem itemD) {
		Bundle webViewState = itemD != null && itemD.isFragmentExists() ? itemD.getWebFragment().getWebViewState() : null;
		if (webViewState != null) {
			WebViewHv.saveStateToFile(
				new File(saveWebViewStateDir, SAVE_RESTORE_STATE_FILE_KEY + itemD.getId()), 
				webViewState
			);
			itemD.setForceReloadAfterRestore(WebViewHv.isForceReloadAfterRestore(webViewState));
		}
	}
	
	public void unregisterFragment(WebFragmentItem ix) {
		for(WebFragmentItem i : items) {
			if (i.getId() == ix.getId()) {
				items.remove(i);
				break;
			}
		}
		sortList();
	}
	
	/**
	 * zamykna fragment i usuwa cach z nim powiązany
	 * @param i
	 * @return WebFragmentsManager instance
	 */
	public WebFragmentsManager closeFragment(WebFragmentItem i) {
		
		new File(saveWebViewStateDir, SAVE_RESTORE_STATE_FILE_KEY + i.getId()).delete();
		
		if (i.isFragmentExists()) {
			BrowserFrameFragment f = findWebFragment(i);
			if (f != null)
				getFragmentManager().beginTransaction().remove(f).commit();
			unregisterFragment(i);
		} else {
			unregisterFragment(i);
		}
		
		if (items.size() > 0) {
			topItem = items.get(0);
			loadFragment(topItem);
		} else {
			topItem = null;
		}
		
		return this;
	}
	
	public WebFragmentsManager closeTopFragment() {
		WebFragmentItem i = topItem;
		if (i != null) {
			closeFragment(i);
		}
		return this;
	}
	
	public WebFragmentItem getTopItem() {
		return topItem;
	}
	
	private void sortList() {
		if (items.size() > 0)
			Collections.sort(items, mComparatorByTime);
	}
	
	public int getItemsCount() {
		return items.size();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<WebFragmentItem> getOrderedList() {
		ArrayList<WebFragmentItem> res = (ArrayList<WebFragmentItem>) items.clone();
		Collections.sort(res, mComparatorById);
		return res;
	}
	
	public void updateLastActivity(WebFragmentItem i) {
		i.setLastAccessTime(System.currentTimeMillis());
		sortList();
	}
	
	public ArrayList<WebFragmentItem> getExistingFragments() {
		ArrayList<WebFragmentItem> res = new ArrayList<WebFragmentItem>();
		
		for(WebFragmentItem i : items) {
			if (i.isFragmentExists())
				res.add(i);
		}
		
		return res;
	}
	
	public WebFragmentItem findById(int idx) {
		for(WebFragmentItem i : items) {
			if (i.getId() == idx) {
				return i;
			}
		}
		return null;
	}
	
	public OnFragmentDestroy getOnFragmentDestroy() {
		return mOnFragmentDestroy;
	}

	public WebFragmentsManager setOnFragmentDestroy(OnFragmentDestroy mOnFragmentDestroy) {
		this.mOnFragmentDestroy = mOnFragmentDestroy;
		return this;
	}

	public OnFragmentUpdate getOnFragmentUpdate() {
		return mOnFragmentUpdate;
	}

	public WebFragmentsManager setOnFragmentUpdate(OnFragmentUpdate mOnFragmentUpdate) {
		this.mOnFragmentUpdate = mOnFragmentUpdate;
		return this;
	}

	public FragmentManager getFragmentManager() {
		return mFragmentManager;
	}

	public WebFragmentsManager setFragmentManager(FragmentManager mFragmentManager) {
		this.mFragmentManager = mFragmentManager;
		return this;
	}

	public int getFragmentFrameId() {
		return fragmentFrameId;
	}

	public WebFragmentsManager setFragmentFrameId(int fragmentFrameId) {
		this.fragmentFrameId = fragmentFrameId;
		return this;
	}
	
	///////
	
	public WebFragmentsManager swithToFragment(WebFragmentItem i) {
		i.updateLastAccessTime();
		optimize();
		hideAllFragments(i.getWebFragment());
		loadFragment(i);
		return this;
	}
	
	public WebFragmentsManager loadFragment(WebFragmentItem i) {
		if (i.isFragmentExists()) {
			showFragment(i.getWebFragment());
		} else {
			
			// load state from cache
			/*
			byte[] state = null;
			File f = new File(saveWebViewStateDir, SAVE_RESTORE_STATE_FILE_KEY + i.getId());
			if (f.exists()) {
				try {
					state = new byte[(int) f.length()];
					FileInputStream fs = new FileInputStream(f);
					fs.read(state);
					fs.close();
				} catch (FileNotFoundException e) {
					if (Console.isEnabled())
						Console.loge("loadFragment[FileNotFound]", e);
				} catch (IOException e) {
					if (Console.isEnabled())
						Console.loge("loadFragment[IO", e);
				}
			}*/
			
			Bundle state = WebViewHv.loadStateFromFile(new File(saveWebViewStateDir, SAVE_RESTORE_STATE_FILE_KEY + i.getId()));
			//Console.logi("state: " + state);
			BrowserFrameFragment wf = new BrowserFrameFragment()
				.setUrlToLoadAfterInit(i.getUrl())
				.setWebFragmentItem(i)
				.setRestoredState(state);
			i.setWebFragment(wf);
			
			loadFragment(wf, i.getId());
		}
		topItem = i;
		return this;
	}
	
	/**
	 * removes fragment from frame and destroy from item (set null)
	 * @param i
	 * @return
	 */
	public WebFragmentsManager unLoadFragment(WebFragmentItem i) {
		Fragment f = findWebFragment(i);
		if (f != null)
			getFragmentManager().beginTransaction().remove(f).commit();
		i.destroyFragment();
		return this;
	}
	
	private WebFragmentsManager loadFragment(BrowserFrameFragment wf, int idx) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(fragmentFrameId, wf, FRAGMENT_TAG_PREFIX + idx);
		ft.commit();
		if (mOnFragmentUpdate != null)
			mOnFragmentUpdate.onShowFragment(wf);
		return this;
	}
	
	public WebFragmentsManager showFragment(BrowserFrameFragment wf) {
		getFragmentManager()
			.beginTransaction()
			.show(wf)
			.commit();
		if (mOnFragmentUpdate != null)
			mOnFragmentUpdate.onShowFragment(wf);
		return this;
	}
	
	public WebFragmentsManager hideAllFragments(BrowserFrameFragment exclude) {
		List<Fragment> fl = getFragmentManager().getFragments();
		if (fl != null && fl.size() > 0) {
			FragmentTransaction ftx = getFragmentManager().beginTransaction();
			for(Fragment ff : fl) {
				if (ff != null) {
					if (exclude != null && exclude.equals(ff)) continue;
					ftx.hide(ff);
					if (mOnFragmentUpdate != null)
						mOnFragmentUpdate.onHideFragment((BrowserFrameFragment) ff);
				}
			}
			if (!ftx.isEmpty()) ftx.commit();
		}
		return this;
	}
	
	public BrowserFrameFragment findWebFragmentById(int idx) {
		Fragment f = getFragmentManager().findFragmentByTag(FRAGMENT_TAG_PREFIX + idx);
		if (f instanceof BrowserFrameFragment) {
			return (BrowserFrameFragment) f;
		}
		return null;
	}
	
	public BrowserFrameFragment findWebFragment(WebFragmentItem i) {
		return findWebFragmentById(i.getId());
	}
	
	/**
	 * 
	 * @return true if any tabs are restored
	 */
	public boolean restoreSession() {
		boolean res = false;
		
		File sessions = new File(saveWebViewStateDir, SESSIONS_FILE);
		if (sessions.exists() && sessions.length() > 0L) {
			try {
				FileInputStream fs = new FileInputStream(sessions);
				
				ObjectInputStream in = new ObjectInputStream(fs);
				int maxId = 1;
				Object dRes = in.readObject();
				
				if (dRes.getClass().getName().equals(ArrayList.class.getName())) {
					
					List<?> saveItems = (List<?>) dRes;
					for(Object i : saveItems) {
						WebSessionItem ii = (WebSessionItem) i;
						WebFragmentItem wfi = ii.toWebFragmentItem().setOnUpdateWebFragmentItem(mOnUpdateWebFragmentItem);
						
						if (ii.isFragmentInitialized()) {
							loadFragment(wfi);
						}
						
						this.items.add(wfi);
						
						if (wfi.getId() > maxId) maxId = wfi.getId();
					}
					
					res = this.items.size() > 0;
					
					idInc = maxId + 1;
					sortList();
					optimize();
				}
				in.close();
				fs.close();
				
			} catch (FileNotFoundException e) {
				if (Console.isEnabled())
					Console.loge("WebFragmentsManager :: restoreSession[FileNotFound]", e);
			} catch (IOException e) {
				if (Console.isEnabled())
					Console.loge("WebFragmentsManager :: restoreSession[IO]", e);
			} catch (ClassNotFoundException e) {
				if (Console.isEnabled())
					Console.loge("WebFragmentsManager :: restoreSession[ClassNotFound]", e);
			}
		}
		
		return res;
	}
	
	public void saveSession() {
		File sessions = new File(saveWebViewStateDir, SESSIONS_FILE);
		sessions.delete();
		try {
			FileOutputStream fs = new FileOutputStream(sessions);
			
			List<WebSessionItem> saveItems = new ArrayList<WebSessionItem>();
			
			for(WebFragmentItem i : items) {
				if (i.getUrl() == null) continue;
				if (i.getUrl().equals("about:blank")) continue;
				saveWebViewState(i);
				saveItems.add(WebSessionItem.fromWebFragmentItem(i));
			}
			
			ObjectOutputStream out = new ObjectOutputStream(fs);
			out.writeObject(saveItems);
			out.close();
			fs.close();
			
		} catch (FileNotFoundException e) {
			if (Console.isEnabled())
				Console.loge("WebFragmentManager :: saveSession[FileNotFound]", e);
		} catch (UnsupportedEncodingException e) {
			if (Console.isEnabled())
				Console.loge("WebFragmentManager :: saveSession[UnsupportedEncoding]", e);
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("WebFragmentManager :: saveSession[IO]", e);
		}
	}

	public static class WebFragmentItem {
		
		public static interface OnUpdateWebFragmentItem {
			public void onWebFragmentItem(WebFragmentItem i);
		}
		
		private BrowserFrameFragment mWebFragment;
		private Long lastAccessTime;
		private Integer id;
		private String title = null, url;
		private OnUpdateWebFragmentItem mOnUpdateWebFragmentItem;
		private Bitmap thumb;
		private boolean forceReloadAfterRestore = false;
		
		public BrowserFrameFragment getWebFragment() {
			return mWebFragment;
		}
		public void setWebFragment(BrowserFrameFragment mWebFragment) {
			this.mWebFragment = mWebFragment;
		}
		public Long getLastAccessTime() {
			return lastAccessTime;
		}
		public WebFragmentItem setLastAccessTime(Long lastAccessTime) {
			this.lastAccessTime = lastAccessTime;
			if (getOnUpdateWebFragmentItem() != null)
				getOnUpdateWebFragmentItem().onWebFragmentItem(this);
			return this;
		}
		public WebFragmentItem updateLastAccessTime() {
			setLastAccessTime(System.currentTimeMillis());
			return this;
		}
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = Integer.valueOf(id);
		}
		public boolean isFragmentExists() {
			return getWebFragment() != null;
		}
		public void destroyFragment() {
			setWebFragment(null);
			if (getOnUpdateWebFragmentItem() != null)
				getOnUpdateWebFragmentItem().onWebFragmentItem(this);
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
			if (getOnUpdateWebFragmentItem() != null)
				getOnUpdateWebFragmentItem().onWebFragmentItem(this);
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
			if (getOnUpdateWebFragmentItem() != null)
				getOnUpdateWebFragmentItem().onWebFragmentItem(this);
		}
		public OnUpdateWebFragmentItem getOnUpdateWebFragmentItem() {
			return mOnUpdateWebFragmentItem;
		}
		public WebFragmentItem setOnUpdateWebFragmentItem(OnUpdateWebFragmentItem mOnUpdateWebFragmentItem) {
			this.mOnUpdateWebFragmentItem = mOnUpdateWebFragmentItem;
			return this;
		}
		
		@Override
		public String toString() {
			return "{time=" + DateTime.format(getLastAccessTime(), DateTime.FORMAT_FULL) + ", url=" + getUrl() + "}";
		}
		public Bitmap getThumb() {
			return thumb;
		}
		public WebFragmentItem setThumb(Bitmap thumb) {
			this.thumb = thumb;
			return this;
		}
		public boolean isForceReloadAfterRestore() {
			return forceReloadAfterRestore;
		}
		public void setForceReloadAfterRestore(boolean forceReloadAfterRestore) {
			this.forceReloadAfterRestore = forceReloadAfterRestore;
		}
	}

}
