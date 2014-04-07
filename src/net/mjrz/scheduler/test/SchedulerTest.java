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

import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.task.BasicSchedule;
import net.mjrz.scheduler.task.BasicTask;
import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Task;

public class SchedulerTest {
	public static void main(String[] args) throws Exception {

		// Initialize the scheduler
		Scheduler.initialize();
		
		// Create a task
		Task task = new BasicTask("Test task");
		
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		end.add(Calendar.HOUR, 2);
		
		// Create a schedule for the task
		Schedule schedule = new BasicSchedule(start.getTime(), end.getTime());
		
		// Set recurrance - Repeat every 2 minutes
		schedule.setRepeatType(Schedule.RepeatType.MINUTE, 1);
		
		task.setSchedule(schedule);
		
		// Submit the task 
		Scheduler.scheduleTask(task);
	}
}
