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

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public final class BuildCalendar {
	private int[][] calendarMap;
	GregorianCalendar calendar;

	public BuildCalendar() {
		calendarMap = new int[6][7];
		initializeCalendar();
	}

	private void initializeCalendar() {
		for (int i = 0; i < CalendarConstants.MAXWEEKS; i++) {
			for (int j = 0; j < CalendarConstants.MAXDAYS; j++) {
				calendarMap[i][j] = 0;
			}
		}
	}

	public void setCalendar(int year, int month, int date) {
		initializeCalendar();
		GregorianCalendar cal = new GregorianCalendar(year, month, date);
		int index = 0;

		int tmonth = cal.get(Calendar.MONTH);
		if (tmonth == 1)
			index = 28;
		if (tmonth == 1 && cal.isLeapYear(year)) {
			index = 29;
		}
		if (tmonth == 0 || tmonth == 2 || tmonth == 4 || tmonth == 6
				|| tmonth == 7 || tmonth == 9 || tmonth == 11)
			index = 31;
		if (tmonth == 3 || tmonth == 5 || tmonth == 8 || tmonth == 10)
			index = 30;

		for (int i = 1; i <= index; i++) {
			GregorianCalendar tmp = new GregorianCalendar(year, month, i);
			int yloc = tmp.get(Calendar.WEEK_OF_MONTH);
			int xloc = tmp.get(Calendar.DAY_OF_WEEK);
			yloc -= 1;
			xloc -= 1;
			calendarMap[yloc][xloc] = i;
		}
		calendar = cal;
	}

	public GregorianCalendar getCalendar() {
		return calendar;
	}

	public int[][] getCalendarArray() {
		return this.calendarMap;
	}

	public GregorianCalendar getNextMonth() {
		GregorianCalendar next = new GregorianCalendar();
		next.setTime(calendar.getTime());
		next.add(Calendar.MONTH, 1);
		return next;
	}

	public GregorianCalendar getPreviousMonth() {
		GregorianCalendar prev = new GregorianCalendar();
		prev.setTime(calendar.getTime());
		prev.add(Calendar.MONTH, -1);
		return prev;
	}
}
