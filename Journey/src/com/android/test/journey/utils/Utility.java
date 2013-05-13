package com.android.test.journey.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
	public static long getDateTimeMilis(long miliseconds) {
		Date nowDate = new Date();
		nowDate.setTime(miliseconds);
		nowDate.setHours(0);
		nowDate.setMinutes(0);
		nowDate.setSeconds(0);
		
		return nowDate.getTime() / Constant.SECOND_MILISECONDS * Constant.SECOND_MILISECONDS;
	}
	
	public static String getDateString(long mTime, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat (format);
		String dateString;
		
		// dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateString = dateFormat.format(new Date(mTime));
		
		return dateString;
	}
	
	public static Date getDate(String dateStr, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = null;
		
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}
}
