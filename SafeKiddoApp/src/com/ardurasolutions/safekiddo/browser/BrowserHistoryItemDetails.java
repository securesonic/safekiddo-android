package com.ardurasolutions.safekiddo.browser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.DateTime;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserHistory;
import com.hv.styleddialogs.proto.BasicDialog;

public class BrowserHistoryItemDetails extends BasicDialog {
	
	private View content;
	private BrowserHistory mBrowserHistory;
	private EditText editTitle, editURL;
	private TextView textDate;

	@Override
	public View getCustomContent(LayoutInflater inflater, @Nullable ViewGroup container) {
		content = inflater.inflate(R.layout.dialog_history_item_details, container, false);
		
		editTitle = (EditText) content.findViewById(R.id.editTitle);
		editURL = (EditText) content.findViewById(R.id.editURL);
		textDate = (TextView) content.findViewById(R.id.textDate);
		
		return content;
	}
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT > 10)
			setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public void onToolbarReady(final Toolbar toolbar) {
		toolbar.post(new Runnable() {
			@Override
			public void run() {
				// INFO hack method
				((TextView) toolbar.getChildAt(0)).setTextColor(0xFFFFFFFF);
			}
		});
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		editTitle.setText(mBrowserHistory.browser_history_label);
		editURL.setText(mBrowserHistory.browser_history_url);
		textDate.setText(DateTime.format(mBrowserHistory.browser_history_date, DateTime.FORMAT_FULL));
	}

	public BrowserHistory getBrowserHistory() {
		return mBrowserHistory;
	}

	public BrowserHistoryItemDetails setBrowserHistory(BrowserHistory mBrowserHistory) {
		this.mBrowserHistory = mBrowserHistory;
		return this;
	}

}
