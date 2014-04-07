/*******************************************************************************
 * Copyright  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.mjrz.fm.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class DateUtils {
	public static final String FORMAT_YYYYMMDD = "yyyy-MM-dd";
	public static final String FORMAT_YYYYMMDD_SLASHES = "yyyy/MM/dd";
	public static final String GENERIC_DISPLAY_FORMAT = "E, dd MMM yyyy";
	public static final String TIME_DISPLAY_FORMAT = "HH mm ss";
	public static final int THIS_WEEK = 1;
	public static final int THIS_MONTH = 2;

	/**
	 * 
	 * @param dt
	 * @return
	 */
	public static final String getGenericDisplayFormat(String dt) {
		Calendar cal = stringToCalendar(dt, "-");
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				DateUtils.GENERIC_DISPLAY_FORMAT);
		return (sdf.format(cal.getTime()));
	}

	/**
	 * 
	 * @param dt
	 * @param tzString
	 * @return
	 */
	public static final String getGenericDisplayFormat(String dt,
			String tzString) {
		Calendar cal = stringToCalendar(dt, "-", tzString);
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				DateUtils.GENERIC_DISPLAY_FORMAT);
		return (sdf.format(cal.getTime()));
	}

	public static final String formatDate(Date dt, String format) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dt);

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getDefault());
		return (sdf.format(cal.getTime()));
	}

	public static final String getCurrentDate(String format) {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getDefault());
		return (sdf.format(cal.getTime()));
	}

	public static final String dateToString(Date dt, String dateformat) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dt);

		StringBuffer ret = new StringBuffer();
		String separator = String.valueOf("");
		if (dateformat.equals(DateUtils.FORMAT_YYYYMMDD)) {
			separator = "-";
		}
		if (dateformat.equals(DateUtils.FORMAT_YYYYMMDD_SLASHES)) {
			separator = "/";
		}
		ret.append(cal.get(Calendar.YEAR));
		ret.append(separator);
		ret.append(cal.get(Calendar.MONTH) + 1);
		ret.append(separator);
		ret.append(cal.get(Calendar.DATE));

		return ret.toString();
	}

	public static final String dateToString(Date dt, String tzString,
			String dateformat) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(dt);
		cal.setTimeZone(TimeZone.getTimeZone(tzString));

		StringBuffer ret = new StringBuffer();
		String separator = String.valueOf("");
		if (dateformat.equals(DateUtils.FORMAT_YYYYMMDD)) {
			separator = "-";
		}
		if (dateformat.equals(DateUtils.FORMAT_YYYYMMDD_SLASHES)) {
			separator = "/";
		}
		ret.append(cal.get(Calendar.YEAR));
		ret.append(separator);
		ret.append(cal.get(Calendar.MONTH) + 1);
		ret.append(separator);
		ret.append(cal.get(Calendar.DATE));

		return ret.toString();
	}

	public static final String getTimeFromDate(Date dt) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(dt);

		StringBuffer ret = new StringBuffer();
		ret.append(cal.get(Calendar.HOUR));
		ret.append(":");
		ret.append(cal.get(Calendar.MINUTE));

		return ret.toString();
	}

	public static final String getTimeFromDate(Date dt, String tzString) {
		try {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(dt);
			gc.setTimeZone(TimeZone.getTimeZone(tzString));
			StringBuffer ret = new StringBuffer();
			ret.append(gc.get(Calendar.HOUR));
			ret.append(":");
			ret.append(gc.get(Calendar.MINUTE));
			ret.append(" ");
			if (gc.get(Calendar.AM_PM) == 0) {
				ret.append("AM");
			}
			else {
				ret.append("PM");
			}
			return ret.toString();
		}
		catch (Exception e) {
			return "";
		}
	}

	public static final String getDateTimeFromDate(Date dt, String tzString) {
		try {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(dt);
			gc.setTimeZone(TimeZone.getTimeZone(tzString));
			StringBuffer ret = new StringBuffer();
			ret.append(gc.get(Calendar.YEAR));
			ret.append("-");
			ret.append(gc.get(Calendar.MONTH) - 1);
			ret.append("-");
			ret.append(gc.get(Calendar.DATE));
			ret.append(" ");
			ret.append(gc.get(Calendar.HOUR));
			ret.append(":");
			ret.append(gc.get(Calendar.MINUTE));
			ret.append(" ");
			if (gc.get(Calendar.AM_PM) == 0) {
				ret.append("AM");
			}
			else {
				ret.append("PM");
			}
			return ret.toString();
		}
		catch (Exception e) {
			return "";
		}
	}

	public static final String calendarToString(Calendar cal, String dateformat) {
		StringBuffer ret = new StringBuffer();
		if (dateformat.equals(FORMAT_YYYYMMDD)) {
			ret.append(cal.get(Calendar.YEAR));
			ret.append("-");

			String month = null;
			int mo = cal.get(Calendar.MONTH) + 1; /*
												 * Calendar month is zero
												 * indexed, string months are
												 * not
												 */
			if (mo < 10) {
				month = "0" + mo;
			}
			else {
				month = "" + mo;
			}
			ret.append(month);

			ret.append("-");

			String date = null;
			int dt = cal.get(Calendar.DATE);
			if (dt < 10) {
				date = "0" + dt;
			}
			else {
				date = "" + dt;
			}
			ret.append(date);
		}

		return ret.toString();
	}

	public static final Calendar stringToCalendar(String date, String delim) {
		ArrayList<String> split = MiscUtils.splitString(date, delim);
		int yr = Integer.parseInt(split.get(0));
		int mo = Integer.parseInt(split.get(1)) - 1; /*
													 * Calendar month is zero
													 * indexed, string months
													 * are not
													 */
		int dt = Integer.parseInt(split.get(2));

		GregorianCalendar gc = new GregorianCalendar(yr, mo, dt);
		return gc;
	}

	public static final Calendar stringToCalendar(String date, String delim,
			String tzString) {
		ArrayList<String> split = MiscUtils.splitString(date, delim);
		int yr = Integer.parseInt(split.get(0));
		int mo = Integer.parseInt(split.get(1)) - 1; /*
													 * Calendar month is zero
													 * indexed, string months
													 * are not
													 */
		int dt = Integer.parseInt(split.get(2));

		GregorianCalendar gc = new GregorianCalendar(yr, mo, dt);
		gc.setTimeZone(TimeZone.getTimeZone(tzString));
		return gc;
	}

	public static final GregorianCalendar getCurrentCalendar(
			String utimezonestring) {
		try {
			GregorianCalendar gc = new GregorianCalendar();

			gc.setTimeZone(TimeZone.getTimeZone(utimezonestring));
			return gc;
		}
		catch (Exception e) {
			// If exception, return server TimeStamp
			return new GregorianCalendar();
		}
	}

	public static String[] getDateRange(int cmd) {
		GregorianCalendar to = new GregorianCalendar();
		to.set(Calendar.HOUR_OF_DAY, 23);
		to.set(Calendar.MINUTE, 59);
		to.set(Calendar.SECOND, 59);
		
		GregorianCalendar from = new GregorianCalendar();
		from.set(Calendar.HOUR_OF_DAY, 0);
		from.set(Calendar.MINUTE, 0);
		from.set(Calendar.SECOND, 0);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		String ret[] = new String[2];
		ret[1] = sdf.format(to.getTime());

		if (cmd == THIS_WEEK) {
			int limit = 7;
			int count = 0;
			while (true) {
				if (from.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					break;
				}
				if (++count >= limit) {
					break;
				}
				from.add(Calendar.DATE, -1);
			}
			ret[0] = sdf.format(from.getTime());

		}
		if (cmd == THIS_MONTH) {
			int limit = 31;
			int count = 0;
			while (true) {
				if (from.get(Calendar.DAY_OF_MONTH) == 1) {
					break;
				}
				if (++count >= limit) {
					break;
				}
				from.add(Calendar.DATE, -1);
			}
			ret[0] = sdf.format(from.getTime());
		}
		ret[0] = sdf.format(from.getTime());
		return ret;
	}

	public static final String getDayString(int day) {
		switch (day) {
		case Calendar.SUNDAY:
			return "SUNDAY";
		case Calendar.MONDAY:
			return "MONDAY";
		case Calendar.TUESDAY:
			return "TUESDAY";
		case Calendar.WEDNESDAY:
			return "WEDNESDAY";
		case Calendar.THURSDAY:
			return "THURSDAY";
		case Calendar.FRIDAY:
			return "FRIDAY";
		case Calendar.SATURDAY:
			return "SATURDAY";
		}
		return "";
	}

	public static void main(String args[]) {
		String[] range = getDateRange(THIS_WEEK);
		System.out.println(range[0] + "-" + range[1]);
	}
}
