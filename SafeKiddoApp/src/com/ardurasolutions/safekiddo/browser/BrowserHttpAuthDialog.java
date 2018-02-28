package com.ardurasolutions.safekiddo.browser;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ardurasolutions.safekiddo.R;
import com.hv.styleddialogs.proto.BasicDialog;

public class BrowserHttpAuthDialog extends BasicDialog {

	public static interface OnAuthEvent {
		public void onAuthProcess(String login, String pass);
		public void onAuthCancel();
	}
	
	private OnAuthEvent mOnAuthEvent;
	private EditText loginEdit;
	private EditText passEdit;
	
	public BrowserHttpAuthDialog() {
		
		setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (getOnAuthEvent() != null) 
					getOnAuthEvent().onAuthProcess(loginEdit.getText().toString(), passEdit.getText().toString());
				dialog.dismiss();
			}
		});
		setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (getOnAuthEvent() != null) 
					getOnAuthEvent().onAuthCancel();
				dialog.dismiss();
			}
		});
	}

	public OnAuthEvent getOnAuthEvent() {
		return mOnAuthEvent;
	}

	public void setOnAuthEvent(OnAuthEvent mOnAuthEvent) {
		this.mOnAuthEvent = mOnAuthEvent;
	}

	@Override
	public View getCustomContent(LayoutInflater inflater, @Nullable ViewGroup container) {
		View content = inflater.inflate(R.layout.dialog_http_auth, container, false);
		loginEdit = (EditText) content.findViewById(R.id.editText1);
		passEdit = (EditText) content.findViewById(R.id.editText2);
		return content;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getResources().getString(R.string.label_http_auth_title));
	}
	
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getDialog().setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (getOnAuthEvent() != null) 
					getOnAuthEvent().onAuthCancel();
			}
		});
	}

	@Override
	public void onToolbarReady(Toolbar toolbar) {
		
	}

}
