package com.ardurasolutions.safekiddo.helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTime {
	
	public static final String FORMAT_FULL = "yyyy-MM-dd HH:mm:ss"; 
	public static final String FORMAT_STANDARD = "yyyy-MM-dd HH:mm"; 
	public static final String FORMAT_YMD = "yyyy-MM-dd"; 
	public static final String FORMAT_HM = "HH:mm"; 
	public static final String FORMAT_HMS = "HH:mm:ss"; 
	public static final String FORMAT_MY = "MMM yyyy"; 
	public static final String FORMAT_Y = "yyyy"; 
	
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;

	public static String getTimeAgo(long time) {
		// TODO: use DateUtils methods instead
		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = nowLong();
		if (time > now || time <= 0) {
			return null;
		}

		final long diff = now - time;
		if (diff < MINUTE) {
			return "przed chwilą";
		} else if (diff < 2 * MINUTE) {
			return "ponad minutę temu";
		} else if (diff < 60 * MINUTE) {
			return diff / MINUTE + " minut temu";
		} else if (diff < 120 * MINUTE) {
			return "ponad godzinę temu";
		} else if (diff < 24 * HOUR) {
			return diff / HOUR + " godzin temu";
		} else if (diff < 48 * HOUR) {
			return "wczoraj";
		} else {
			return diff / DAY + " dni temu";
		}
	}
	
	/**
	 * get unix timestamp from string
	 * @param date - in format<b>YYYY-mm-dd</b> or <b>YYYY-mm-dd H:i</b> or <b>YYYY-mm-dd H:i:s</b>
	 * @return
	 */
	public static Integer timestampFromString(String dateString) {
		return Double.valueOf(fromString(dateString).getTimeInMillis() / 1000D).intValue();
	}
	
	public static Calendar fromString(String dateString) {
		String[] dateTime = dateString.split(" ");
		String[] date = dateTime[0].split("-");
		String[] time = dateTime.length == 2 ? dateTime[1].split(":") : null;
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.valueOf(date[0]));
		cal.set(Calendar.MONTH, Integer.valueOf(date[1])-1); // TODO : -1 bo miesiące sa o 0 (0 styczeń, 11 grudzień)
		cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date[2]));
		if (time != null) {
			cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time[0]));
			cal.set(Calendar.MINUTE, Integer.valueOf(time[1]));
			cal.set(Calendar.SECOND, time.length == 3 ? Integer.valueOf(time[2]) : 0);
		} else {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		
		return cal;
	}
	
	/**
	 * is year, month and day is equal
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static boolean isToday(Calendar c1, Calendar c2) {
		return 
			c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && 
			c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
			c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
	} 
	
	public static boolean isBefore(Calendar c1, Calendar c2) {
		if (c1.get(Calendar.YEAR) > c2.get(Calendar.YEAR)) return true;
		if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) > c2.get(Calendar.MONTH)) return true;
		if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) && c1.get(Calendar.DAY_OF_MONTH) > c2.get(Calendar.DAY_OF_MONTH)) return true;
		return false;
	} 

	public static Integer getUnixTimestamp() {
		return Long.valueOf(Calendar.getInstance().getTimeInMillis() / 1000).intValue();
	}
	
	public static String now() { 
		return new SimpleDateFormat(
			FORMAT_FULL, 
			Locale.getDefault()
		).format(Calendar.getInstance().getTime()); 
	}
	
	public static Integer nowTimestamp() {
		return Long.valueOf(Calendar.getInstance().getTimeInMillis() / 1000).intValue();
	}
	
	public static long nowLong() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	public static String format(Long timestamp){ 
		return format(timestamp, FORMAT_STANDARD); 
	}
	
	public static String format(Long timestamp, String format) { 
		return new SimpleDateFormat(format, Locale.getDefault()).format(timestamp); 
	}
	
	public static String formatUnixTimestamp(Long timestamp){ 
		if (timestamp == null)
			return null;
		else
			return format(timestamp * 1000, FORMAT_STANDARD); 
	}
	
	public static String formatUnixTimestamp(Integer timestamp){
		if (timestamp == null)
			return null;
		else
			return format(Long.valueOf(timestamp * 1000L), FORMAT_STANDARD); 
	}
	
	public static String formatUnixTimestampYMD(Integer timestamp){ 
		if (timestamp == null)
			return null;
		else
			return format(Long.valueOf(timestamp * 1000L), FORMAT_YMD); 
	}
	
	public static String formatUnixTimestampHM(Integer timestamp){ 
		if (timestamp == null)
			return null;
		else
			return format(Long.valueOf(timestamp * 1000L), FORMAT_HM); 
	}
	
	public static String formatFull(Long timestamp){ 
		return format(timestamp, FORMAT_FULL); 
	}
	
	public static String formatYMD(Long timestamp){ 
		return format(timestamp, FORMAT_YMD); 
	} 

	public static String formatHM(Long timestamp){ 
		return format(timestamp, FORMAT_HM); 
	} 

	public static String formatHMS(Long timestamp){ 
		return format(timestamp, FORMAT_HMS); 
	} 

}
