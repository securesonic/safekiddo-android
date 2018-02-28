package com.ardurasolutions.safekiddo.browser;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.browser.BrowserFavsFormDialog.OnBrowserFavsFormDialogDismiss;
import com.ardurasolutions.safekiddo.sql.BrowserLocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.BrowserFavsTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.BrowserFavs;

public class BrowserFavsActivity extends ActionBarActivity {
	
	private ArrayList<BrowserFavs> path = new ArrayList<BrowserFavs>();
	private ListView LV;
	private FavsAdapter LA;
	private TextView textPath;
	private TextView listOverlayEmpty;
	private BrowserFavs root = null;
	private BrowserFavs fakeRoot;
	
	private BrowserFavsTable mBrowserFavsTable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browser_favs);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mBrowserFavsTable = BrowserLocalSQL.getInstance(this).getTable(BrowserFavsTable.class);
		fakeRoot = new BrowserFavs();
		fakeRoot.browser_favs_id = 0L;
		root = fakeRoot;
		
		listOverlayEmpty = (TextView) findViewById(R.id.listOverlayEmpty);
		LV = (ListView) findViewById(R.id.list);
		textPath = (TextView) findViewById(R.id.textPath);
		
		LA = new FavsAdapter(mBrowserFavsTable.getItemsArray(null, root), LayoutInflater.from(this));
		
		LV.setAdapter(LA);
		LV.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BrowserFavs item = LA.getItem(position);
				if (item.browser_favs_type == BrowserFavs.FavsType.FOLDER.getValue()) {
					root = item;
					LA.changeItems(mBrowserFavsTable.getItemsArray(null, root));
					path.add(item);
					updatePath();
				} else {
					setResult(Activity.RESULT_OK, item.saveToIntent(new Intent()));
					finish();
				}
			}
		});
		updatePath();
		
		registerForContextMenu(LV);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final BrowserFavs selItem = LA.getItem(((AdapterView.AdapterContextMenuInfo)menuInfo).position);
		
		menu.add(R.string.label_edit).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				BrowserFavsFormDialog d = new BrowserFavsFormDialog();
				d.setEditedItem(selItem);
				d.setOnBrowserFavsFormDialogDismiss(new OnBrowserFavsFormDialogDismiss() {
					@Override
					public void onBrowserFavsFormDialogDismiss() {
						LA.changeItems(mBrowserFavsTable.getItemsArray(null, root));
						LV.invalidateViews();
					}
				});
				d.show(getSupportFragmentManager(), "edit");
				return false;
			}
		});
		menu.add(R.string.label_delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				mBrowserFavsTable.deleteWithSubchilds(selItem);
				LA.changeItems(mBrowserFavsTable.getItemsArray(null, root));
				LV.invalidateViews();
				return false;
			}
		});
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		if (path.size() == 0) {
			super.onBackPressed();
		} else {
			path.remove(path.size()-1);
			root = path.size() > 0 ? path.get(path.size()-1) : fakeRoot;
			LA.changeItems(mBrowserFavsTable.getItemsArray(null, root));
			LV.invalidateViews();
			updatePath();
		}
	}
	
	private void updatePath() {
		if (path.size() == 0) {
			textPath.setText(getResources().getString(R.string.label_root_directory));
		} else {
			String p = getResources().getString(R.string.label_root_directory);
			for(BrowserFavs f : path) {
				p += " &#187; " + f.browser_favs_label;
			}
			textPath.setText(Html.fromHtml(p));
		}
	}
	
	private class FavsAdapter extends BaseAdapter {
		
		private ArrayList<BrowserFavs> items;
		private LayoutInflater la;
		
		public FavsAdapter(ArrayList<BrowserFavs> it, LayoutInflater l) {
			items = it;
			la = l;
			listOverlayEmpty.setVisibility(getCount() > 0 ? View.GONE : View.VISIBLE);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public BrowserFavs getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = la.inflate(R.layout.item_browser_favs_view, parent, false);
			
			BrowserFavs item = getItem(position);
			boolean isFolder = item.browser_favs_type != null && item.browser_favs_type == BrowserFavs.FavsType.FOLDER.getValue();
			
			TextView view = (TextView) convertView;
			view.setText(isFolder ? 
				Html.fromHtml("<b>" + item.browser_favs_label + "</b>") : 
					Html.fromHtml("<b>" + item.browser_favs_label + "</b><br><small>" + item.browser_favs_url + "</small>"));
			view.setCompoundDrawablesWithIntrinsicBounds(isFolder ? R.drawable.ic_action_folder : R.drawable.star, 0, 0, 0);
			
			return convertView;
		}
		
		public void changeItems(ArrayList<BrowserFavs> it) {
			items.clear();
			items.addAll(it);
			this.notifyDataSetChanged();
			listOverlayEmpty.setVisibility(getCount() > 0 ? View.GONE : View.VISIBLE);
		}
		
	}

}
