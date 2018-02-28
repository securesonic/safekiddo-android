package com.ardurasolutions.safekiddo.receviers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.sql.LocalSQL;
import com.ardurasolutions.safekiddo.sql.tables.AllAppsTable;
import com.hv.console.Console;

public class PackageInstall extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		Console.logd("ACTION: " + intent.getAction() + ", DATA: " + intent.getData());
		
		final AllAppsTable mAllAppsTable = LocalSQL.getInstance(context).getTable(AllAppsTable.class);
		final String pkg = intent.getData() != null ? intent.getData().toString() : null;
		
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			if (pkg != null) {
				mAllAppsTable.refreshList();
				context.sendBroadcast(new Intent().setAction(Constants.BRODCAST_BLOCKED_APPS));
			}
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
			if (pkg != null) {
				mAllAppsTable.deleteByPkg(pkg, mAllAppsTable.getTransactionConn());
				context.sendBroadcast(new Intent().setAction(Constants.BRODCAST_BLOCKED_APPS));
			}
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED)) {
			mAllAppsTable.refreshList(true);
			context.sendBroadcast(new Intent().setAction(Constants.BRODCAST_BLOCKED_APPS));
		}
	}

}
