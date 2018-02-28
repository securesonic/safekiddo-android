package com.ardurasolutions.safekiddo.proto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.hv.styleddialogs.proto.BasicDialog;

public class DialogProgress extends BasicDialog {

	@Override
	public View getCustomContent(LayoutInflater inflater, @Nullable ViewGroup container) {
		View logoutProgressView = inflater.inflate(R.layout.dialog_progress, container, false);
		((TextView) logoutProgressView.findViewById(R.id.text)).setText(R.string.label_logout_progress);
		return logoutProgressView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.StyledDialogsThemeFullRound);
	}

	@Override
	public void onToolbarReady(Toolbar toolbar) {
		
	}

}
