package com.ardurasolutions.safekiddo.launcher.proto;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.launcher.views.DesktopView;

public class DesktopsAdapter extends PagerAdapter {
	
	public static interface OnPageRemoveCreate {
		public void onPageCreate(DesktopView desktop);
		public void onPageRemove(DesktopView desktop);
	}
	
	private ArrayList<DesktopView> desktops = new ArrayList<DesktopView>();
	private LayoutInflater la;
	private OnPageRemoveCreate mOnPageRemoveCreate;
	
	public DesktopsAdapter(Context ctx) {
		la = LayoutInflater.from(ctx);
	}
	
	@Override
	public int getCount() {
		return 3;//desktops.size();
	}
	
	public ArrayList<DesktopView> getDesktops() {
		return desktops;
	}
	
	public ArrayList<View> getDesktops(DesktopView skipDesktop) {
		ArrayList<View> res = new ArrayList<View>();
		for(DesktopView d : desktops) {
			if (d.equals(skipDesktop)) continue;
			res.add(d);
		}
		return res;
	}
	
	@Override
	public Object instantiateItem(View collection, final int position) {
		final DesktopView desktop = (DesktopView) la.inflate(R.layout.item_desktop, (ViewPager) collection, false);
		desktop.setTag("page_" + position);
		desktop.post(new Runnable() {
			@Override
			public void run() {
				if (getOnPageRemoveCreate() != null)
					getOnPageRemoveCreate().onPageCreate(desktop);
			}
			
		});
		
		desktops.add(desktop);
		
		((ViewPager) collection).addView(desktop, 0);
		return desktop;
	}
	
	@Override
	public void destroyItem(View collection, int position, Object view) {
		if (getOnPageRemoveCreate() != null)
			getOnPageRemoveCreate().onPageRemove((DesktopView) view);
		desktops.remove(view);
		((ViewPager) collection).removeView((View) view);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==((View)object);
	}

	public OnPageRemoveCreate getOnPageRemoveCreate() {
		return mOnPageRemoveCreate;
	}

	public void setOnPageRemoveCreate(OnPageRemoveCreate mOnPageRemoveCreate) {
		this.mOnPageRemoveCreate = mOnPageRemoveCreate;
	}

}
