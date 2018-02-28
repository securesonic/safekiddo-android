package com.ardurasolutions.safekiddo.browser.proto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class SerialBitmap implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5267697448507974877L;
	private static final int NO_IMAGE = -1;
	
	private Bitmap bitmap;
	
	public SerialBitmap(Bitmap b) {
		bitmap = b;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}
	
	
	// TODO zoptymalizować
	// Converts the Bitmap into a byte array for serialization
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		if (bitmap != null) {
//			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//			bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteStream);
//			byte bitmapBytes[] = byteStream.toByteArray();
//			out.write(bitmapBytes, 0, bitmapBytes.length);
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			final byte[] imageByteArray = stream.toByteArray();
			out.writeInt(imageByteArray.length);
			out.write(imageByteArray);
		} else {
			out.writeInt(NO_IMAGE);
		}
	}
	
	// Deserializes a byte array representing the Bitmap and decodes it
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		final int length = in.readInt();
		
		if (length != NO_IMAGE) {
			final byte[] imageByteArray = new byte[length];
	        in.readFully(imageByteArray);
	        bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, length);
//			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//			int b;
//			while((b = in.read()) != -1) 
//				byteStream.write(b);
//			byte bitmapBytes[] = byteStream.toByteArray();
//			bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
		}
	}

}
