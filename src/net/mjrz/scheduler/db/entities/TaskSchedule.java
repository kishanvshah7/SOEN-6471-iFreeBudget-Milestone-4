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
package net.mjrz.scheduler.db.entities;

import java.util.Date;
import java.util.Set;

import net.mjrz.scheduler.task.Schedule;

public class TaskSchedule {
	long id;
	Date nextRunTime;
	Date lastRunTime;
	int repeatType;
	int step;
	private Set<ScheduleConstraint> constraints;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Date getNextRunTime() {
		return nextRunTime;
	}
	public void setNextRunTime(Date nextRunTime) {
		this.nextRunTime = nextRunTime;
	}
	public Date getLastRunTime() {
		return lastRunTime;
	}
	public void setLastRunTime(Date lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	public int getRepeatType() {
		return repeatType;
	}
	public void setRepeatType(int repeatType) {
		this.repeatType = repeatType;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public Set<ScheduleConstraint> getConstraints() {
		return constraints;
	}
	public void setConstraints(Set<ScheduleConstraint> constraints) {
		this.constraints = constraints;
	}
}
