package com.ardurasolutions.safekiddo.proto;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Stable ID is enabled<br>
 * ID is position in list
 * @author Hivedi
 *
 */
public class DefaultAdapter extends CursorAdapter {

	private int itemView;
	private LayoutInflater la;
	protected Context mContext;

	public DefaultAdapter(Context context, Cursor c, int itemRes) {
		super(context, c, false);
		mContext = context;
		itemView = itemRes;
		la = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public boolean isViewEnabled(Cursor curr) {
		return true;
	}
	
	public void onSelectionChange(int selCount) {
		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		if (itemView > 0)
			return la.inflate(itemView, null, false);
		return null;
	}
	
	@Override 
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		if (getFilterQueryProvider() != null) { 
			return getFilterQueryProvider().runQuery(constraint); 
		}
		return getCursor();
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

}
