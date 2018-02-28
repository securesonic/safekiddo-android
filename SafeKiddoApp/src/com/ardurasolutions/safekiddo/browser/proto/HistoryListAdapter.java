package com.ardurasolutions.safekiddo.browser.proto;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.DateTime;
import com.ardurasolutions.safekiddo.proto.DefaultAdapter;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserHistory;
import com.ardurasolutions.safekiddo.sql.utils.DBUtils;

public class HistoryListAdapter extends DefaultAdapter implements StickyListHeadersAdapter {
	
	private LayoutInflater inflater;

	public HistoryListAdapter(Context context) {
		super(context, null, R.layout.item_browser_history);
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		BrowserHistory item = DBUtils.currToObj(cursor, BrowserHistory.class);
		((TextView)view.findViewById(R.id.itemTitle)).setText(item.browser_history_label);
		((TextView)view.findViewById(R.id.itemSubTitle)).setText(item.browser_history_url);
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderHolder h;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_history_header, parent, false);
			h = new HeaderHolder((TextView) convertView.findViewById(R.id.headerText));
			convertView.setTag(h);
		} else {
			h = (HeaderHolder) convertView.getTag();
		}
		
		Cursor c = getCursor();
		c.moveToPosition(position);
		
		BrowserHistory bh = DBUtils.currToObj(c, BrowserHistory.class);
		h.setup(bh);
		
		return convertView;
	}
	
	@Override
	public long getItemId(int position) {
		Cursor c = getCursor();
		c.moveToPosition(position);
		BrowserHistory bh = DBUtils.currToObj(c, BrowserHistory.class);
		return bh._id;
	}

	@Override
	public long getHeaderId(int position) {
		Cursor c = getCursor();
		c.moveToPosition(position);
		
		BrowserHistory bh = DBUtils.currToObj(c, BrowserHistory.class);
		String s = DateTime.format(bh.browser_history_date, DateTime.FORMAT_YMD);
		
		return s.hashCode();
	}
	
	private static class HeaderHolder {
		private TextView txt;
		
		public HeaderHolder(TextView t) {
			txt = t;
		}
		
		public void setup(BrowserHistory bh){
			txt.setText(DateTime.format(bh.browser_history_date, DateTime.FORMAT_YMD));
		}
	}

}
