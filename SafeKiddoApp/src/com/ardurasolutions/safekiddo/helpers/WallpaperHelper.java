package com.ardurasolutions.safekiddo.helpers;

import java.io.IOException;
import java.io.InputStream;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.hv.console.Console;

public class WallpaperHelper {
	
	public static String[] getWallpapers(Context ctx) {
		try {
			return ctx.getAssets().list("w");
		} catch (IOException e) {
			return new String[]{};
		}
	}
	
	public static InputStream getWallpaperStream(Context ctx, String name) throws IOException {
		return ctx.getAssets().open("w/" + name);
	}
	
	public static boolean setWallpaper(Context ctx, String wName) {
		WallpaperManager wpm = WallpaperManager.getInstance(ctx);
		try {
			int size = ctx.getResources().getDisplayMetrics().heightPixels;
			Bitmap b = BitmapFactory.decodeStream(WallpaperHelper.getWallpaperStream(ctx, wName));
			Bitmap r = Bitmap.createScaledBitmap(b, size, size, true);
			
			wpm.setBitmap(r);
			return true;
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("WallpaperHelper::setWallpaper[IO]", e);
			return false;
		}
	}
	
	public static boolean setDefaultWallpaper(Context ctx) {
		String bmpName = getWallpapers(ctx)[0];
		return setWallpaper(ctx, bmpName);
	}

}
