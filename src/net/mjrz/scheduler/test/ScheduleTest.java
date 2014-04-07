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
package net.mjrz.scheduler.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Iterator;

import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.task.BasicSchedule;
import net.mjrz.scheduler.task.BasicTask;
import net.mjrz.scheduler.task.MonthSchedule;
import net.mjrz.scheduler.task.MonthScheduleDayBased;
import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Task;
import net.mjrz.scheduler.task.WeekSchedule;
import net.mjrz.scheduler.task.constraints.Constraint;
import net.mjrz.scheduler.task.constraints.MonthConstraint;
import net.mjrz.scheduler.task.constraints.MonthConstraintDayBased;
import net.mjrz.scheduler.task.constraints.WeekConstraint;

import static net.mjrz.scheduler.task.Schedule.*;

public class ScheduleTest {
	public static void main(String[] args) throws Exception {
		System.out.println("Test daily");
		testDaily();
		System.out.println("_____________________");
		System.out.println("Test weekly");
		testWeekly();
		System.out.println("_____________________");
		System.out.println("Test monthly day based");
		testMonthlyDayBased();
		System.out.println("_____________________");
		System.out.println("Test monthly");
		testMonthly();
		System.out.println("_____________________");

	}
	
	private static void testDaily() throws Exception {
		Date st = new Date();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 3);
		
		Date en = new Date(c.getTimeInMillis());
		BasicSchedule s = new BasicSchedule(st, en);	
		s.setRepeatType(RepeatType.DATE, 2);
				
		List<Date> rts = s.getRunTimesBetween(s.getStartTime(), en);
		System.out.println(s);
		for(Date d : rts) {
			System.out.println(d);			
		}		
	}
	
	private static void testWeekly() throws Exception {
		Date st = new Date();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 5);
		
		Date en = new Date(c.getTimeInMillis());
		WeekSchedule s = new WeekSchedule(st, en);		
		WeekConstraint co = new WeekConstraint();
		co.addDay(DayOfWeek.Monday);
		co.addDay(DayOfWeek.Tuesday);

		s.setRepeatType(RepeatType.WEEK, 2);
		s.setConstraint(co);
				
		List<Date> rts = s.getRunTimesBetween(s.getStartTime(), en);
		System.out.println(s);
		for(Date d : rts) {
			System.out.println(d);			
		}		
	}

	private static void testMonthly() throws Exception {
		Date st = new Date();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 11);
		
		Date en = new Date(c.getTimeInMillis());
		MonthSchedule s = new MonthSchedule(st, en);		
		Constraint co = new MonthConstraint(1);

		s.setRepeatType(RepeatType.MONTH, 1);
		s.setConstraint(co);
		
		List<Date> rts = s.getRunTimesBetween(s.getStartTime(), en);
		System.out.println(s);
		for(Date d : rts) {
			System.out.println(d);			
		}		
	}
	
	private static void testMonthlyDayBased() throws Exception {
		Date st = new Date();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 11);
		
		Date en = new Date(c.getTimeInMillis());
		MonthScheduleDayBased s = new MonthScheduleDayBased(st, en);		
		Constraint co = new MonthConstraintDayBased(WeekOfMonth.First, DayOfWeek.Friday);

		s.setRepeatType(RepeatType.MONTH, 2);
		s.setConstraint(co);
		
		List<Date> rts = s.getRunTimesBetween(s.getStartTime(), en);
		System.out.println(s);
		for(Date d : rts) {
			System.out.println(d);			
		}		
	}
}
