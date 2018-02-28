package com.ardurasolutions.safekiddo.dev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.SKAdmin;
import com.ardurasolutions.safekiddo.helpers.CommonUtils;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.helpers.FilesUtils;
import com.ardurasolutions.safekiddo.helpers.HeartBeatHelper;
import com.ardurasolutions.safekiddo.helpers.Toaster;
import com.ardurasolutions.safekiddo.helpers.UserHelper;
import com.ardurasolutions.safekiddo.launcher.proto.DesktopHelper;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.hv.console.Console;

public class DevActivity extends ActionBarActivity {
	
	private Spinner spinner1;
	private Config prefs;
	private EditText editText1;
	
	private class SKServerInfo {
		private String name, address;
		
		public SKServerInfo(String n, String a) {
			setAddress(a);
			setName(n);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}
	
	private class SpinBaseAdapter extends BaseAdapter {
		
		private ArrayList<SKServerInfo> items;
		
		public SpinBaseAdapter(ArrayList<SKServerInfo> i) {
			items = i;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(DevActivity.this).inflate(R.layout.dev_item_server, spinner1, false);
			SKServerInfo item = items.get(position);
			
			((TextView) convertView.findViewById(R.id.dev_text1)).setText(item.getName());
			((TextView) convertView.findViewById(R.id.dev_text2)).setText(item.getAddress());
			
			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(DevActivity.this).inflate(R.layout.dev_item_server, spinner1, false);
			SKServerInfo item = items.get(position);
			
			((TextView) convertView.findViewById(R.id.dev_text1)).setText(item.getName());
			((TextView) convertView.findViewById(R.id.dev_text2)).setText(item.getAddress());
			
			return convertView;
		}
		
	}
	
	private ArrayList<SKServerInfo> items;
	private CheckBox checkBoxWVDebug;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dev);
		
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		editText1 = (EditText) findViewById(R.id.editText1);
		checkBoxWVDebug = (CheckBox) findViewById(R.id.checkBoxWVDebug);
		prefs = Config.getInstance(this);
		
		checkBoxWVDebug.setChecked(prefs.load(Config.KeyNames.DEV_BROWSER_CONTENT_DEBUG, false));
		checkBoxWVDebug.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.save(Config.KeyNames.DEV_BROWSER_CONTENT_DEBUG, isChecked);
			}
		});
		
		items = new ArrayList<SKServerInfo>();
		items.add(new SKServerInfo("Use build in", Constants.getBaseUrl()));
		items.add(new SKServerInfo("HIVEDI Local machine 1", "http://192.168.3.155/a/"));
		items.add(new SKServerInfo("HIVEDI Local machine 2 (MAC)", "http://192.168.3.152:8888/api/v1/"));
		items.add(new SKServerInfo("CUSTOM", "Enter below"));
		
		spinner1.setAdapter(new SpinBaseAdapter(items));
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				editText1.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
		
		String server = prefs.load(Config.KeyNames.DEV_SERVER, "");
		if (!server.equals("")) {
			int i=0; boolean wasFound = false;
			for(SKServerInfo si : items) {
				if (si.getAddress().equals(server)) {
					spinner1.setSelection(i);
					wasFound = true;
					break;
				}
				i++;
			}
			if (!wasFound) {
				spinner1.setSelection(3);
				editText1.setVisibility(View.VISIBLE);
				editText1.setText(server);
			}
		}
		
		((TextView) findViewById(R.id.textView2)).setText(Constants.APP_MODE.toString());
		((TextView) findViewById(R.id.textView3)).setText("127.0.0.1:" + Config.getInstance(this).load(Config.KeyNames.LOCAL_PROXY_PORT, Constants.LOCAL_PROXY_PORT));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Fast uninstall").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
				ComponentName mDeviceAdmin = new ComponentName(DevActivity.this, SKAdmin.class);
				mDPM.removeActiveAdmin(mDeviceAdmin);
				
				Uri packageURI = Uri.parse("package:" + getPackageName());
				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
				startActivity(uninstallIntent);
				
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	public void handleSaveServer(View v) {
		switch(spinner1.getSelectedItemPosition()) {
			default:
			case 0: prefs.save(Config.KeyNames.DEV_SERVER, ""); break;
			case 1: prefs.save(Config.KeyNames.DEV_SERVER, items.get(1).getAddress()); break;
			case 2: prefs.save(Config.KeyNames.DEV_SERVER, items.get(2).getAddress()); break;
			case 3: prefs.save(Config.KeyNames.DEV_SERVER, editText1.getText().toString()); break;
		}
		Toaster.showMsg(this, "Server info saved");
	}
	
	@SuppressWarnings("resource")
	public void handleCopyDb(View v) {
		try {
			File sd = Environment.getExternalStorageDirectory(); 
			File data = Environment.getDataDirectory(); 

			if (sd.canWrite()) { 
				
				String currentDBPath = "/data/" + getPackageName() + "/databases/" + LocalSQL.DB_NAME; 
				String backupDBPath = "safekiddo.sqlite"; 
				File currentDB = new File(data, currentDBPath); 
				File backupDB = new File(sd, backupDBPath); 

				if (currentDB.exists()) { 
					FileChannel src = new FileInputStream(currentDB).getChannel(); 
					FileChannel dst = new FileOutputStream(backupDB).getChannel(); 
					dst.transferFrom(src, 0, src.size()); 
					dst.close(); 
					src.close(); 
				} 
				Toast.makeText(this, "Save on SD Card: " + backupDBPath, Toast.LENGTH_LONG).show(); 
				FilesUtils.refreshFileOnMTP(DevActivity.this, backupDB);
			} else
				Toast.makeText(this, "Error! (save)", Toast.LENGTH_LONG).show(); 
		} catch (Exception e) { 
			e.printStackTrace();
			Toast.makeText(this, "Error (unknown exception)!", Toast.LENGTH_LONG).show(); 
		} 
	}
	
	public void handleRemoveDesktopConfig(View v) {
		DesktopHelper.removeAllConfigs(this);
		Toast.makeText(this, "All config deleted", Toast.LENGTH_LONG).show(); 
	}
	
	public void handleRemoveCurrentChild(View v) {
		UserHelper.saveCurrentChildProfile(this,  null);
		Toast.makeText(this, "Current child set to NULL", Toast.LENGTH_LONG).show(); 
	}
	
	public void handleRemoveSession(View v) {
		Config.getInstance(this).save(Config.KeyNames.SESSION_ID, "");
		Toast.makeText(this, "Session set to NULL", Toast.LENGTH_LONG).show(); 
	}
	
	public void handleLogoutFlag(View v) {
		Config.getInstance(this).save(Config.KeyNames.IS_USER_LOGOUT_BY_SERVER, true);
		Toast.makeText(this, "LOGOUT = TRUE", Toast.LENGTH_LONG).show(); 
	}
	
	public void handleCheckProxy(View v) {
		CommonUtils.checkLocalProxy(this, new CommonUtils.ProxyCheckCallback() {
			@Override
			public void onProxyCheckFinish(final boolean result) {
				Console.logd("ProxyCheckFinish result=" + result);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toaster.showMsg(DevActivity.this, "PROXY IS WORKING: " + result);
					}
				});
			}
		});
	}
	
	public void handleRunHeartBeat(View v) {
		HeartBeatHelper.runNow(this);
	}

}
