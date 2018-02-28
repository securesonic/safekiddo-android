package com.ardurasolutions.safekiddo.browser.proto;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.browser.proto.WebFragmentsManager.WebFragmentItem;

public class WebTabsAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater la;
	private GridView lv;
	
	public WebTabsAdapter(Context ctx, GridView l) {
		mContext = ctx;
		lv = l;
		la = LayoutInflater.from(mContext);
	}
	
	public ArrayList<WebFragmentItem> getAllItems() {
		return null;
	}
	
	public View.OnClickListener onDeleteClick(final WebFragmentItem item) {
		return null;
	}

	@Override
	public int getCount() {
		return getAllItems().size();
	}

	@Override
	public WebFragmentItem getItem(int position) {
		return getAllItems().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		WebFragmentItem item = getItem(position);
		if (convertView == null) {
			convertView = la.inflate(R.layout.item_browser_tab, lv, false);
			holder = new ViewHolder(
				(TextView) convertView.findViewById(R.id.pageTitle), 
				(ImageView) convertView.findViewById(R.id.pageThumb),
				(ImageView) convertView.findViewById(R.id.pageClose)
			);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.setup(item, onDeleteClick(item));
		
		return convertView;
	}
	
	private class ViewHolder {
		TextView title;
		ImageView img, pageClose;
		
		public ViewHolder(TextView t, ImageView i, ImageView c) {
			title = t;
			img = i;
			pageClose = c;
		}
		
		public void setup(WebFragmentItem item, View.OnClickListener ocl) {
			String t = item.getTitle();
			if (t == null) {
				if (item.getUrl() == null || (item.getUrl() != null && item.getUrl().equals("about:blank"))) {
					t = mContext.getResources().getString(R.string.label_empty_page);
				} else
					t = item.getUrl();
			}
			
			title.setText(t);
			img.setImageBitmap(item.getThumb());
			pageClose.setOnClickListener(ocl);
		}
		
	}

}
