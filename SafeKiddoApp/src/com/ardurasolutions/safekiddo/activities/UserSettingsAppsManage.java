package com.ardurasolutions.safekiddo.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.AppsHelper;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.proto.view.CheckBoxHv;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.ardurasolutions.safekiddo.sql.tables.skeletons.AllApps;
import com.hv.styleddialogs.TextDialog;
import com.hv.styleddialogs.proto.BasicDialog.OnDialogReady;

public class UserSettingsAppsManage extends ActionBarActivity {
	
	public class AppItem {
		private String pkg, label, className;
		private Drawable icon;
		private boolean checked = false;
		private Long localId;
		private boolean warnInUnlock = false;
		
		public Long getLocalId() {
			return localId;
		}
		public void setLocalId(Long id) {
			localId = id;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getPkg() {
			return pkg;
		}
		public void setPkg(String pkg) {
			this.pkg = pkg;
		}
		public Drawable getIcon() {
			return icon;
		}
		public void setIcon(Drawable d) {
			this.icon = d;
		}
		public void setIcon(ResolveInfo ri, PackageManager pm) {
			Drawable iconRes = ri.activityInfo.loadIcon(pm);
			int iconSize = getResources().getDimensionPixelSize(R.dimen.user_settings_icon_size);
			Bitmap bitmap = ((BitmapDrawable)iconRes).getBitmap();
			if (bitmap.getWidth() != iconSize && bitmap.getHeight()!= iconSize){
				bitmap = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
				iconRes = new BitmapDrawable(getResources(), bitmap);
			}
			this.icon = iconRes;
		}
		public String getClassName() {
			return className;
		}
		public void setClassName(String className) {
			this.className = className;
		}
		public boolean isChecked() {
			return checked;
		}
		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		public boolean isWarnInUnlock() {
			return warnInUnlock;
		}
		public void setWarnInUnlock(boolean warnInUnlock) {
			this.warnInUnlock = warnInUnlock;
		}
	}
	
	private ListView LV;
	private AppListAdapter LA;
	private ArrayList<AllApps> allAppsList;
	private AllAppsTable mAllAppsTable;
	private View progress;
	private ArrayList<AppItem> items;
	private boolean finishPrevActivity = true;
	private List<String> browsers;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_settings_apps_manage);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		LV = (ListView) findViewById(R.id.list);
		progress = findViewById(R.id.progressBar1);
		
		playStartLoadAnimation();
		
		mAllAppsTable = LocalSQL.getInstance(UserSettingsAppsManage.this).getTable(AllAppsTable.class);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				browsers = AppsHelper.getInstalledBrowsersPackages(UserSettingsAppsManage.this);
				mAllAppsTable.refreshList();
				updateItems();
				
				LA = new AppListAdapter(items);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LV.setAdapter(LA);
						playFinishLoadAnimation();
					}
				});
			}
		}).start();
	}
	
	private class AppListAdapter extends BaseAdapter {
		
		private ArrayList<AppItem> allItems;
		
		public AppListAdapter(ArrayList<AppItem> ai) {
			allItems = ai;
		}

		@Override
		public int getCount() {
			return allItems.size();
		}

		@Override
		public AppItem getItem(int position) {
			return allItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppItem item = getItem(position);
			
			ListViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(UserSettingsAppsManage.this).inflate(R.layout.item_app_mng, parent, false);
				holder = new ListViewHolder((CheckBoxHv) convertView.findViewById(R.id.appLabel));
				convertView.setTag(holder);
			} else {
				holder = (ListViewHolder) convertView.getTag();
			}
			
			CheckBoxHv cb = holder.getCheckBox();
			
			cb.setTag(item);
			cb.setText(item.getLabel());
			cb.setChecked(!item.isChecked());
			cb.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, getResources().getDrawable(R.drawable.checkbox_button2), null);
			cb.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(final View v) {
					final AppItem item = (AppItem) v.getTag();
					final boolean isChecked = ((CheckBoxHv) v).isChecked();
					
					if (item.isWarnInUnlock() && isChecked) {
						TextDialog d = new TextDialog();
						d.setTitle(getResources().getString(R.string.label_warning));
						d.setText(getResources().getString(R.string.label_unlock_browser_msg));
						d.setNegativeButton(R.string.label_cancel, null);
						d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								item.setChecked(!isChecked);
								new Thread(new Runnable() {
									@Override
									public void run() {
										mAllAppsTable.updateEntryBlockedFlag(item.getLocalId(), !isChecked);
										updateItems();
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												LA.notifyDataSetChanged();
											}
										});
									}
								}).start();
								dialog.dismiss();
							}
						});
						d.setOnDialogReady(new OnDialogReady() {
							@Override
							public void onDialogReady(Dialog d) {
								d.setOnDismissListener(new OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialog) {
										LA.notifyDataSetChanged();
									}
								});
							}
						});
						d.show(getSupportFragmentManager(), "warn");
					} else {
						item.setChecked(!isChecked);
						new Thread(new Runnable() {
							@Override
							public void run() {
								mAllAppsTable.updateEntryBlockedFlag(item.getLocalId(), !isChecked);
								updateItems();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										LA.notifyDataSetChanged();
									}
								});
							}
						}).start();
					}
				}
			});
			
			return convertView;
		}
		
		private class ListViewHolder {
			private CheckBoxHv mCheckBox;
			public ListViewHolder(CheckBoxHv cb) {
				mCheckBox = cb;
			}
			
			public CheckBoxHv getCheckBox() {
				return mCheckBox;
			}
		}
		
	}
	
	
	
	private void updateItems() {
		allAppsList = mAllAppsTable.getAllWithIcons(null, false);
		if (items == null) 
			items = new ArrayList<AppItem>(); 
		else 
			items.clear();
		
		for(AllApps app : allAppsList) {
			if (app.all_apps_package.equals(getPackageName())) continue;
			AppItem item = new AppItem();
			
			item.setLocalId(app._id);
			item.setLabel(app.all_apps_label);
			item.setPkg(app.all_apps_package);
			item.setIcon(app.all_apps_icon);
			item.setClassName(app.all_apps_class);
			item.setChecked(app.isBlocked());
			item.setWarnInUnlock(browsers.contains(app.all_apps_package));
			
			items.add(item);
		}
	}
	
	private void playFinishLoadAnimation() {
		Animation a = AnimationUtils.loadAnimation(UserSettingsAppsManage.this, android.R.anim.fade_out);
		a.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation) {
				progress.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) { }
		});
		progress.startAnimation(a);
	}
	
	private void playStartLoadAnimation() {
		progress.setVisibility(View.VISIBLE);
		Animation a = AnimationUtils.loadAnimation(UserSettingsAppsManage.this, android.R.anim.fade_in);
		progress.startAnimation(a);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.label_enable_all).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				TextDialog d = new TextDialog();
				d.setTitle(getResources().getString(R.string.dialog_enableall_title));
				d.setText(getResources().getString(R.string.dialog_enableall_msg));
				d.setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						progress.clearAnimation();
						playStartLoadAnimation();
						new Thread(new Runnable() {
							@Override
							public void run() {
								mAllAppsTable.updateBlockeFlagOnAll(false);
								updateItems();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										LA.notifyDataSetChanged();
										playFinishLoadAnimation();
									}
								});
							}
						}).start();
					}
				});
				d.setNegativeButton(R.string.label_cancel, null);
				d.show(getSupportFragmentManager(), "d0");
				return false;
			}
		});
		menu.add(R.string.label_disable_all).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				progress.clearAnimation();
				playStartLoadAnimation();
				new Thread(new Runnable() {
					@Override
					public void run() {
						mAllAppsTable.updateBlockeFlagOnAll(true);
						updateItems();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								LA.notifyDataSetChanged();
								playFinishLoadAnimation();
							}
						});
					}
				}).start();
				return false;
			}
		});
		menu.add(R.string.label_disable_browsers).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				progress.clearAnimation();
				playStartLoadAnimation();
				new Thread(new Runnable() {
					@Override
					public void run() {
						mAllAppsTable.blockApps(AppsHelper.getInstalledBrowsers(UserSettingsAppsManage.this));
						updateItems();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								LA.notifyDataSetChanged();
								playFinishLoadAnimation();
							}
						});
					}
				}).start();
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onStop() {
		sendBroadcast(new Intent().setAction(Constants.BRODCAST_BLOCKED_APPS));
		super.onStop();
		if (finishPrevActivity && UserSettings.get() != null)
			UserSettings.get().selfFinish();
		finish();
	}
	
	@Override
	public void onBackPressed() {
		finishPrevActivity = false;
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finishPrevActivity = false;
				setResult(Activity.RESULT_OK);
				finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
