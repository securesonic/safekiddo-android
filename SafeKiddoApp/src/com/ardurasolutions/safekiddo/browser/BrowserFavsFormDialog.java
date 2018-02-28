package com.ardurasolutions.safekiddo.browser;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.sql.BrowserLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.BrowserFavsTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserFavs;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserFavs.FavsType;
import com.hv.styleddialogs.proto.BasicDialog;

public class BrowserFavsFormDialog extends BasicDialog {
	
	public static interface OnBrowserFavsFormDialogDismiss {
		public void onBrowserFavsFormDialogDismiss();
	}
	
	private View content;
	private View ceateFolderOverlay, buttonNewFolderIcon, buttonOkFolder, buttonCancelAFolder;
	private View page1, page2, editNewFolderBox, buttonFolderDelete;
	private EditText editNewFolder, editLabel, editUrl;
	private Button buttonFolder;
	private TextView dialogTitleText;
	private View rootView;
	
	private BrowserFavsTable mBrowserFavsTable;
	private ListView list;
	private ArrayAdapter<BrowserFavs> LA;
	private ArrayList<BrowserFavs> items;
	private BrowserFavs editedFolder, selectedFolder, editedItem;
	private OnBrowserFavsFormDialogDismiss mOnBrowserFavsFormDialogDismiss;
	
	private String favLabel, favUrl;
	
	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.os.Build.VERSION.SDK_INT > 10)
			setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mOnBrowserFavsFormDialogDismiss != null)
			mOnBrowserFavsFormDialogDismiss.onBrowserFavsFormDialogDismiss();
	}
	
	private void showFolderWindow() {
		ceateFolderOverlay.setVisibility(View.VISIBLE);
		int height = rootView.getMeasuredHeight();
		if (height < (int) (getResources().getDisplayMetrics().density * 280))
			height = (int) (getResources().getDisplayMetrics().density * 280);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
		ceateFolderOverlay.setLayoutParams(lp);
		editNewFolder.setText(editedFolder != null ? editedFolder.browser_favs_label : "");
		editNewFolder.requestFocus();
		CommonUtils.showKeyboard(getActivity(), editNewFolder);
	}
	
	private void hideFolderWindow() {
		ceateFolderOverlay.setVisibility(View.GONE);
		editNewFolder.setText("");
		editNewFolder.clearFocus();
		editedFolder = null;
		CommonUtils.hideKeyboard(getActivity(), editNewFolder);
		content.requestFocus();
	}

	public String getFavLabel() {
		//favLabel = editLabel.getText().toString();
		return favLabel;
	}

	public BrowserFavsFormDialog setFavLabel(String favLabel) {
		this.favLabel = favLabel;
		return this;
	}

	public String getFavUrl() {
		//favUrl = editUrl.getText().toString();
		return favUrl;
	}

	public BrowserFavsFormDialog setFavUrl(String favUrl) {
		this.favUrl = favUrl;
		return this;
	}

	public BrowserFavs getEditedItem() {
		return editedItem;
	}

	public void setEditedItem(BrowserFavs editedItem) {
		this.editedItem = editedItem;
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final BrowserFavs selItem = LA.getItem(((AdapterView.AdapterContextMenuInfo)menuInfo).position);
		
		menu.add(R.string.label_edit).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				editedFolder = selItem;
				showFolderWindow();
				return false;
			}
		});
		menu.add(R.string.label_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				mBrowserFavsTable.deleteWithSubchilds(selItem);
				items.clear();
				items.addAll(mBrowserFavsTable.getFoldersTree());
				LA.notifyDataSetChanged();
				return false;
			}
		});
	}

	public OnBrowserFavsFormDialogDismiss getOnBrowserFavsFormDialogDismiss() {
		return mOnBrowserFavsFormDialogDismiss;
	}

	public void setOnBrowserFavsFormDialogDismiss(
			OnBrowserFavsFormDialogDismiss mOnBrowserFavsFormDialogDismiss) {
		this.mOnBrowserFavsFormDialogDismiss = mOnBrowserFavsFormDialogDismiss;
	}

	@Override
	public View getCustomContent(LayoutInflater inflater, @Nullable ViewGroup container) {
		mBrowserFavsTable = BrowserLocalSQL.getInstance(getActivity()).getTable(BrowserFavsTable.class);
		content = inflater.inflate(R.layout.dialog_browser_favs, container, false);
		
		page1 = content.findViewById(R.id.form);
		page2 = content.findViewById(R.id.select);
		rootView = content.findViewById(R.id.rootView);
		ceateFolderOverlay = content.findViewById(R.id.ceateFolderOverlay);
		editNewFolderBox = content.findViewById(R.id.editNewFolderBox);
		editNewFolder = (EditText) content.findViewById(R.id.editNewFolder);
		editLabel = (EditText) content.findViewById(R.id.editLabel);
		editUrl = (EditText) content.findViewById(R.id.editUrl);
		list = (ListView) content.findViewById(R.id.list);
		buttonFolder = (Button) content.findViewById(R.id.buttonFolder);
		buttonFolderDelete = content.findViewById(R.id.buttonFolderDelete);
		buttonNewFolderIcon = content.findViewById(R.id.buttonNewFolderIcon);
		buttonOkFolder = content.findViewById(R.id.buttonOkFolder);
		buttonCancelAFolder = content.findViewById(R.id.buttonCancelAFolder);
		dialogTitleText = (TextView) content.findViewById(R.id.dialogTitleText);
		
		items = mBrowserFavsTable.getFoldersTree();
		
		if (getEditedItem() != null && getEditedItem().browser_favs_type == FavsType.FOLDER.getValue()) {
			content.findViewById(R.id.textView2).setVisibility(View.GONE);
			editUrl.setVisibility(View.GONE);
		}
		
		final int itemLeftmargin = getResources().getDimensionPixelSize(R.dimen.browser_favs_tree_ident);
		
		LA = new ArrayAdapter<BrowserFavs>(getActivity(), R.layout.item_browser_favs_tree, items) {
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_browser_favs_tree, list, false);
				
				BrowserFavs item = items.get(position);
				TextView txt = (TextView) convertView.findViewById(R.id.text1);
				txt.setText(item.browser_favs_label);
				
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) txt.getLayoutParams();
				lp.setMargins(item.browser_favs_id == 0L ? 0 : (itemLeftmargin * (item.browser_favs_lvl + 1)), 0, 0, 0);
				txt.setLayoutParams(lp);
				
				return convertView;
			}
		};
		list.setAdapter(LA);
		registerForContextMenu(list);
		
		content.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){ 
					if (page2.getVisibility() == View.VISIBLE) {
						if (ceateFolderOverlay.getVisibility() != View.VISIBLE) {
							page1.setVisibility(View.VISIBLE);
							page2.setVisibility(View.GONE);
							buttonNewFolderIcon.setVisibility(View.INVISIBLE);
							dialogTitleText.setText(R.string.label_fav_title);
						} else {
							hideFolderWindow();
						}
					} else {
						dismiss();
					}
					return true;
				}
				return false;
			}
		});
		editNewFolder.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){ 
					editNewFolderBox.setVisibility(View.GONE);
					CommonUtils.hideKeyboard(getActivity(), editNewFolder);
					return true;
				}
				return false;
			}
		});
		
		buttonFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				page1.setVisibility(View.GONE);
				page2.setVisibility(View.VISIBLE);
				buttonNewFolderIcon.setVisibility(View.VISIBLE);
				dialogTitleText.setText(R.string.label_select_folder);
			}
		});

		content.findViewById(R.id.buttonSelectFolder).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = list.getCheckedItemPosition();
				if (pos != ListView.INVALID_POSITION) {
					selectedFolder = items.get(pos);
					buttonFolder.setText(selectedFolder.browser_favs_label);
					page1.setVisibility(View.VISIBLE);
					page2.setVisibility(View.GONE);
					buttonNewFolderIcon.setVisibility(View.INVISIBLE);
					dialogTitleText.setText(R.string.label_fav_title);
				} else {
					Toaster.showMsg(getActivity(), R.string.toast_select_dir);
				}
			}
		});
		
		content.findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		content.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (editLabel.getText().toString().trim().length() == 0) {
					Toaster.showMsg(getActivity(), R.string.toast_fill_fields);
					editLabel.requestFocus();
					return;
				}
				
				if (getEditedItem() != null && getEditedItem().browser_favs_type == FavsType.FAV.getValue() && editUrl.getText().toString().trim().length() == 0) {
					Toaster.showMsg(getActivity(), R.string.toast_fill_fields);
					editUrl.requestFocus();
					return;
				}
				
				if (getEditedItem() != null) {
					getEditedItem().browser_favs_label = editLabel.getText().toString();
					getEditedItem().browser_favs_url = editUrl.getText().toString();
					getEditedItem().browser_favs_parent = selectedFolder != null ? selectedFolder.browser_favs_id : 0L;
					mBrowserFavsTable.updateLabelUrlParent(getEditedItem());
				} else {
					mBrowserFavsTable.addEntryFav(selectedFolder, getFavLabel(), getFavUrl());
				}
				dismiss();
				CommonUtils.hideKeyboard(getActivity(), editLabel);
			}
		});
		
		buttonFolderDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBrowserFavsTable.deleteWithSubchilds(editedFolder);
				items.clear();
				items.addAll(mBrowserFavsTable.getFoldersTree());
				LA.notifyDataSetChanged();
			}
		});
		
		buttonNewFolderIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editedFolder = null;
				showFolderWindow();
			}
		});
		
		buttonOkFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int pos = list.getCheckedItemPosition();
				if (editNewFolder.getText().toString().trim().length() > 0) {
					editNewFolderBox.setVisibility(View.GONE);
					CommonUtils.hideKeyboard(getActivity(), editNewFolder);
					if (editedFolder != null) {
						editedFolder.browser_favs_label = editNewFolder.getText().toString().trim();
						mBrowserFavsTable.updateLabel(editedFolder);
						editedFolder = null;
					} else {
						BrowserFavs parent = null;
						if (pos != ListView.INVALID_POSITION) {
							parent = items.get(pos);
						}
						mBrowserFavsTable.addEntryFolder(parent, editNewFolder.getText().toString().trim());
					}
					items.clear();
					items.addAll(mBrowserFavsTable.getFoldersTree());
					LA.notifyDataSetChanged();
					hideFolderWindow();
				} else {
					Toaster.showMsg(getActivity(), R.string.toast_fill_name);
				}
			}
		});
		
		buttonCancelAFolder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideFolderWindow();
			}
		});
		
		View.OnKeyListener editBack = new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){ 
					dismiss();
					return true;
				} else
					return false;
			}
		};
		
		if (getEditedItem() != null) {
			setFavLabel(getEditedItem().browser_favs_label);
			setFavUrl(getEditedItem().browser_favs_url);
		}
		
		editLabel.setText(getFavLabel());
		editUrl.setText(getFavUrl());
		if (getEditedItem() != null && getEditedItem().browser_favs_parent != null && getEditedItem().browser_favs_parent > 0L) {
			BrowserFavs perent = mBrowserFavsTable.getRow(getEditedItem().browser_favs_parent, BrowserFavs.class);
			if (perent != null) 
				buttonFolder.setText(perent.browser_favs_label);
		}
		
		editLabel.setOnKeyListener(editBack);
		editUrl.setOnKeyListener(editBack);
		
		return content;
	}

	@Override
	public void onToolbarReady(Toolbar toolbar) {
		
	}
	
}
