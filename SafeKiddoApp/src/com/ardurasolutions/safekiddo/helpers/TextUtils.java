package com.ardurasolutions.safekiddo.helpers;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import android.graphics.Typeface;
import android.location.Location;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.hv.console.Console;

public class TextUtils {
	
	public static String join(Object[] array, String separator) {
		return join(array, separator, 0, array.length);
	}
	
	public static String join(ArrayList<String> array, String separator) {
		return join((String [])array.toArray(new String[array.size()]), separator, 0, array.size());
	}
	
	public static String join(ArrayList<String> array, String separator, int startIndex, int endIndex) {
		return join(
			(String [])array.toArray(new String[array.size()]),
			separator, startIndex, endIndex
		);
	}
	
	public static String join(Object[] array, String separator, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		int noOfItems = endIndex - startIndex;
		if (noOfItems <= 0) {
		    return null;
		}
		
		StringBuilder buf = new StringBuilder(noOfItems * 16);
		buf.append(array[startIndex]);
		
		if (endIndex - startIndex > 1) {
			for (int i = startIndex + 1; i < endIndex; i++) {
				buf.append(separator);
				if (array[i] != null) {
					buf.append(array[i]);
				}
			}
		}
		return buf.toString();
    }
	
	public static String urlTrailingSlash(String url) {
		if (url == null) return null;
		String s = new String(url);
		if (s.endsWith("/")) {
		    s = s.substring(0, s.length() - 1);
		}
		return s;
	}
	
	public static String arrayToString(ArrayList<?> list) {
		StringBuilder sb = new StringBuilder();
		
		if (list != null && list.size() > 0) {
			for(Object item : list)
				sb.append(item.toString()).append(",");
		}
		
		return sb.toString();
	}
	
	public static String arrayToString(long[] list) {
		StringBuilder sb = new StringBuilder();
		
		if (list != null && list.length > 0) {
			for(long item : list)
				sb.append(Long.toString(item)).append(",");
			
			return sb.toString().substring(0, sb.length()-1);
		} else 
			return "";
	}
	
	private static String[] plchars            = new String[]{"ą","ś","ć","ź","ż","ó","ł","ń","ę","Ą","Ś","Ć","Ź","Ż","Ó","Ł","Ń","Ę"};
	private static String[] plcharsReplacments = new String[]{"a","s","c","z","z","o","l","n","e","A","S","C","Z","Z","O","L","N","E"};

	public static String normalizePlChars(String val) {
		String res = val;
		for(int i=0; i<plchars.length; i++)
			res = res.replaceAll(plchars[i], plcharsReplacments[i]);
		return res;
	}
	
	public static String capitalizeFully(String str, char... delimiters) {
		int delimLen = delimiters == null ? -1 : delimiters.length;
		if (isEmpty(str) || delimLen == 0) {
			return str;
		}
		str = str.toLowerCase(Locale.getDefault());
		return capitalize(str, delimiters);
	}
	
	public static String capitalizeFully(String str) {
		return capitalizeFully(str, null);
	}
	
	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}
	
	private static boolean isDelimiter(char ch, char[] delimiters) {
		if (delimiters == null) {
			return Character.isWhitespace(ch);
		}
		for (char delimiter : delimiters) {
			if (ch == delimiter) {
				return true;
			}
		}
		return false;
    }
	
	public static String capitalize(String str) {
		return capitalize(str, null);
	}
	
	public static String capitalize(String str, char... delimiters) {
		int delimLen = delimiters == null ? -1 : delimiters.length;
		if (isEmpty(str) || delimLen == 0)
			return str;
		char[] buffer = str.toCharArray();
		boolean capitalizeNext = true;
		for (int i = 0; i < buffer.length; i++) {
			char ch = buffer[i];
			if (isDelimiter(ch, delimiters)) {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer[i] = Character.toTitleCase(ch);
				capitalizeNext = false;
			}
		}
		return new String(buffer);
	}
	
	public static String removeDoubleSpaces(String in){
		return in.replaceAll("\\s+", " ");
	}
	
	public static String trimTrailingZeros(String number) {
		if(!number.contains("."))
			return number;
		return number.replaceAll("\\.?0*$", "");
	}
	
	public static String md5(final String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			// e.printStackTrace();
		}
		return "";
	}
	
	public static synchronized String formatPrice(String price) {
		try {
			Double d = Double.valueOf(price);
			return formatPrice(d);
		} catch (Exception e) {
			return "0,00";
		}
	}

	public static synchronized String formatPrice(Double price) {
		if (price == 0.0) {
			return "0,00";
		}
		try {
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator(',');
			otherSymbols.setGroupingSeparator(' '); 
			DecimalFormat df = new DecimalFormat("#,##0.00", otherSymbols);
			return df.format(price);
		} catch (Exception e) {
			return "0,00";
		}
	}
	
	public static synchronized String formatCount(Double price) {
		if (price == 0.0) {
			return "0.0";
		}
		try {
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator('.');
			otherSymbols.setGroupingSeparator(' '); 
			DecimalFormat df = new DecimalFormat("#,##0.0", otherSymbols);
			return trimTrailingZeros(df.format(price));
		} catch (Exception e) {
			return "0.0";
		}
	}
	
	public static synchronized String formatKilometers(Double price) {
		if (price == 0.0) {
			return "0";
		}
		try {
			DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
			otherSymbols.setDecimalSeparator(',');
			otherSymbols.setGroupingSeparator(' '); 
			DecimalFormat df = new DecimalFormat("###,###,###", otherSymbols);
			return df.format(price);
		} catch (Exception e) {
			return "0";
		}
	}
	
	public static Double doubleFromString(String s) {
		if (s == null) return null;
		if (s.trim().length() == 0) return null;
		String v = s.replaceAll(",", ".");
		try {
			return Double.valueOf(v);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static Integer intFromString(String s) {
		if (s == null) return null;
		if (s.trim().length() == 0) return null;
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public static void spannableText(TextView txt, String part1, String part2, final Typeface face1, final Typeface face2) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(part1 + part2);
		
		if (part1.length() > 0)
		ssb.setSpan(new TypefaceSpan("") {
			@Override
			public void updateMeasureState(TextPaint paint) {
				paint.setTypeface(face1);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setTypeface(face1);
			}
		}, 0, part1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

		if (part2.length() > 0)
		ssb.setSpan(new TypefaceSpan("") {
			@Override
			public void updateMeasureState(TextPaint paint) {
				paint.setTypeface(face2);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setTypeface(face2);
			}
		}, part1.length(), part1.length() + part2.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		
		txt.setText(ssb, BufferType.SPANNABLE);
	}
	
	public static void spannableText(TextView txt, String part1, String part2, String part3, final Typeface face1, final Typeface face2, final Typeface face3) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(part1 + part2 + part3);
		
		ssb.setSpan(new TypefaceSpan("") {
			@Override
			public void updateMeasureState(TextPaint paint) {
				paint.setTypeface(face1);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setTypeface(face1);
			}
		}, 0, part1.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		
		ssb.setSpan(new TypefaceSpan("") {
			@Override
			public void updateMeasureState(TextPaint paint) {
				paint.setTypeface(face2);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setTypeface(face2);
			}
		}, part1.length(), part1.length() + part2.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		
		ssb.setSpan(new TypefaceSpan("") {
			@Override
			public void updateMeasureState(TextPaint paint) {
				paint.setTypeface(face3);
			}
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setTypeface(face3);
			}
		}, part1.length() + part2.length(), part1.length() + part2.length() + part3.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		
		txt.setText(ssb, BufferType.SPANNABLE);
	}
	
	public static void spnnableTextSize(TextView txt, String part1, String part2, int size1, int size2) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(part1 + part2);
		
		ssb.setSpan(new AbsoluteSizeSpan(size1, false), 0, part1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new AbsoluteSizeSpan(size2, false), part1.length(), part1.length() + part2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		txt.setText(ssb, BufferType.SPANNABLE);
	}
	
	/**
	 * must have valid url with scheme etc: http://192.168.1.1, https://192....
	 * @param url
	 */
	public static boolean isValidIpAdress(String url) {
		final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
		final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
		try {
			URI uri = new URI(url);
			return addressPattern.matcher(uri.getHost()).matches();
		} catch (URISyntaxException e) {
			if (Console.isEnabled())
				Console.loge("TextUtils :: isValidIpAdress[URISyntaxException]", e);
		} catch (Exception e) {
			if (Console.isEnabled())
				Console.loge("TextUtils :: isValidIpAdress", e);
		}
		return false;
	}
	
	public static String getUrlDomain(String url) {
		String res = url;
		
		try {
			URI uri = new URI(url);
			res = uri.getHost();
		} catch (URISyntaxException e) { 
		} catch (Exception e) { }
		
		return res;
	}
	
	public static String locationToString(Location loc) {
		if (loc != null) {
			String res = 
			"{" +
			
			"lat: " + loc.getLatitude() + ", " +
			"lng: " + loc.getLongitude() + ", " +
			"acc: " + loc.getAccuracy() + ", " +
			"speed: " + loc.getSpeed() + ", " +
			"provider: " + loc.getProvider() + ", " +
			"time: " + loc.getTime() + " (" + DateTime.formatFull(loc.getTime()) + ")" +
			
			"}";
			return res;
		} else
			return "NULL";
	}
	
	private static final String PAGE_BLANK = "about:blank";
	private static final String PAGE_DATA = "data:text/html";
	
	/**
	 * zwraca "ładny" adres url<br>
	 * zamienia hosta na małe literki, usuwa / jeżeli adres nie ma ścieżki lub hash-a
	 * @param url
	 * @return
	 */
	public static String parseToNiceUrl(String url) {
		if (url == null) return null;
		if (url.trim().toLowerCase(Locale.getDefault()).equals(PAGE_BLANK)) return url.trim();
		if (url.trim().toLowerCase(Locale.getDefault()).startsWith(PAGE_DATA)) return url.trim();
		if (url.trim().startsWith("//")) return url.trim();
		
		/*
		 * zamianie nieprawidłowe duże litery w nazie urla na male
		 * np: Http://Onet.pl/StronaWWW -> http://onet.pl/StronaWWW
		 */
		String u = url.trim();
		try {
			u = NormalizeURL.normalize(url);
		} catch (MalformedURLException e) {
			if (Console.isEnabled())
				Console.loge("TextUtils :: parseToNiceUrl", e);
		}
		
		/*
		 * usuwanie z adresu url "/" na końcu jezeli w adresie jest tylko domena (np. http://kenumir.pl/ => http://kenumir.pl)
		 */
		try {
			URI uri = new URI(u);
			
			boolean slashToRemove = false;
			
			String path = uri.getPath() != null ? uri.getPath().trim() : null;
			if (path != null && path.length() >= 0 && path.length() <=1)  {
				
				slashToRemove = uri.getQuery() == null || (uri.getQuery() != null && uri.getQuery().trim().length() == 0);
				if (slashToRemove)
					slashToRemove = uri.getFragment() == null || (uri.getFragment() != null && uri.getFragment().trim().length() == 0);
				if (slashToRemove)
					slashToRemove = u.substring(u.length() - 1, u.length()).equals("/");
			}
			
			if (slashToRemove)
				u = u.substring(0, u.length() - 1);
			
		} catch (URISyntaxException e) { 
		} catch (Exception e) { }
		
		return u;
	}
}
