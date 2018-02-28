package com.ardurasolutions.safekiddo.proto;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;

@SuppressLint("InflateParams")
public class PopupWindowMenu extends PopupWindow {
	
	private View popupView;
	private ListView LV;
	private PopupMenuAdapter LA;
	private int iconSize = 32;
	
	public PopupWindowMenu(Context ctx) {
		super(ctx);
	}
	
	public PopupWindowMenu(Context ctx, int maxWidth) {
		//super(maxWidth, LayoutParams.WRAP_CONTENT);
		
		super(LayoutInflater.from(ctx).inflate(R.layout.popup_window_menu, null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); // TODO nie działa na androidzie 2.x - coś na to trzeba wymyślić
		
		iconSize = (int) (ctx.getResources().getDisplayMetrics().density * 32);
		
		popupView = getContentView();
		LV = (ListView) popupView.findViewById(R.id.popupList);
		setContentView(popupView);
		setTouchable(true);
		setOutsideTouchable(true);
		getContentView().setFocusableInTouchMode(true);
		
		LA = new PopupMenuAdapter(ctx);
		LV.setAdapter(LA);
	}
	
	public PopupWindowMenu addItem(PopupWindowMenuItem item) {
		LA.addItem(item);
		return this;
	}
	
	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		//LV.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		LV.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		setWidth(LV.getMeasuredWidth() + iconSize);
		super.showAsDropDown(anchor, xoff, yoff);
		
	}
	
	public static class PopupWindowMenuItem {
		private String label;
		private Drawable icon;
		private View.OnClickListener onClick;
		
		public PopupWindowMenuItem() {}
		public PopupWindowMenuItem(String l) {
			label = l;
		}
		
		public String getLabel() {
			return label;
		}
		public PopupWindowMenuItem setLabel(String label) {
			this.label = label;
			return this;
		}
		public Drawable getIcon() {
			return icon;
		}
		public PopupWindowMenuItem setIcon(Drawable icon) {
			this.icon = icon;
			return this;
		}
		public View.OnClickListener getOnClick() {
			return onClick;
		}
		public PopupWindowMenuItem setOnClick(View.OnClickListener onClick) {
			this.onClick = onClick;
			return this;
		}
	}
	
	private class PopupMenuAdapter extends BaseAdapter {
		
		private ArrayList<PopupWindowMenuItem> items = new ArrayList<PopupWindowMenuItem>();
		private LayoutInflater la;
		
		public PopupMenuAdapter(Context ctx) {
			la = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public PopupWindowMenuItem getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final PopupWindowMenuItem item = getItem(position);
			
			if (convertView == null) {
				convertView = la.inflate(R.layout.item_popup_window_menu_dark, parent, false);
			}
			
			((TextView)convertView).setText(item.getLabel());
			((TextView)convertView).setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
			
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (item.getOnClick() != null)
						item.getOnClick().onClick(v);
					dismiss();
				}
			});
			
			return convertView;
		}
		
		public void addItem(PopupWindowMenuItem item) {
			items.add(item);
			notifyDataSetChanged();
		}
		
	}

}
