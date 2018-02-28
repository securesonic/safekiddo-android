package com.ardurasolutions.safekiddo.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ardurasolutions.safekiddo.extra.CommonCodecs.DigestUtils;
import com.ardurasolutions.safekiddo.extra.CommonCodecs.Hex;
import com.hv.console.Console;

public class FilesUtils {
	
	public interface OnScanFile {
		/**
		 * if must be stopen return true
		 * @param f
		 * @return
		 */
		public boolean onFile(File f);
	}
	
	public static void scanDir(File dir, OnScanFile listener) {
		if (dir != null) {
			Boolean doStop = false;
			File[] files  = dir.listFiles();
			if (files != null) {
				for(int i=0; i < files.length; i++) {
					if (doStop) break;
					File file = files[i];
					if (file.isDirectory() && !doStop)
						scanDir(file, listener);
					else {
						if (listener != null)
							doStop = listener.onFile(file);
						if (doStop) break;
					}
				}
			}
		}
	}
	
	private static final String[] imageExtList = new String[]{".png", ".jpg", ".jpeg"};
	
	public static String[] getImageFilesList(File dir) {
		ArrayList<String> fileList = new ArrayList<String>();
		if (dir != null && dir.exists() && dir.isDirectory()) {
			for(File f : dir.listFiles()) {
				if (!f.exists()) continue;
				if (!f.isFile()) continue;
				String fullFilePath = f.getAbsolutePath();
				Boolean isImage = false;
				for(String ext : imageExtList) {
					if (fullFilePath.toLowerCase(Locale.getDefault()).endsWith(ext)) {
						isImage = true;
						break;
					}
				}
				if (!isImage) continue;
				fileList.add(fullFilePath);
			} 
		}
		return fileList.size() > 0 ? (String [])fileList.toArray(new String[fileList.size()]) : null;
	}
	
	/**
	 * najszybsze MD5 pliku jakie udało się znaleźć
	 */
	public static String md5file(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			return new String(Hex.encodeHex(DigestUtils.md5(fis)));
		} catch (IOException e) {
			if (Console.isEnabled())
				Console.loge("md5file", e);
		}
		return "";
	}
	
	public static String extractRelativePath(File root, File f) {
		String rp = root.getAbsolutePath();
		String fp = f.getAbsolutePath();
		return fp.length() > rp.length() && fp.startsWith(rp) ? fp.substring(rp.length(), fp.length()) : fp;
	}
	
	public static String extractFilePath(String fullFilePath) {
		return fullFilePath.substring(0, fullFilePath.lastIndexOf(File.separator)) + "/";
	}
	
	public static String extractFileName(String fullFileName){
		return fullFileName.substring(fullFileName.lastIndexOf("/")+1, fullFileName.length() );
	}
	
	public static void refreshFileOnMTP(Context ctx, File f) {
		ctx.sendBroadcast(new Intent(
			Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
			Uri.parse("file://" + f.getAbsolutePath() + "/")
		));
	}

}
