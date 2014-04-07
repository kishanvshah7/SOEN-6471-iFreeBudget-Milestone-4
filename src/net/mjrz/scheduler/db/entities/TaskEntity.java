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

public class TaskEntity {
	long id;
	String name;
	Date startTime;
	Date endTime;
	Set<TaskSchedule> schedules;
	long businessObjectId;
	String taskType;

	public Set<TaskSchedule> getSchedules() {
		return schedules;
	}
	public void setSchedules(Set<TaskSchedule> schedules) {
		this.schedules = schedules;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public long getBusinessObjectId() {
		return businessObjectId;
	}
	public void setBusinessObjectId(long businessObjectId) {
		this.businessObjectId = businessObjectId;
	}
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String type) {
		this.taskType = type;
	}
}
