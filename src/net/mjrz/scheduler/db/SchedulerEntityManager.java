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
package net.mjrz.scheduler.db;

import java.util.*;

import net.mjrz.scheduler.db.entities.ScheduleConstraint;
import net.mjrz.scheduler.db.entities.TaskEntity;
import net.mjrz.scheduler.db.entities.TaskSchedule;
import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Task;
import net.mjrz.scheduler.task.constraints.Constraint;
import net.mjrz.scheduler.task.constraints.DayConstraint;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.util.SerializationHelper;

public class SchedulerEntityManager {

	public TaskEntity addTask(Session s, Task task, long refId)
			throws Exception {
		TaskEntity t = new TaskEntity();

		t.setName(task.getName());
		t.setStartTime(task.getSchedule().getStartTime());
		t.setEndTime(task.getSchedule().getEndTime());
		t.setTaskType(task.getClass().getName());
		t.setBusinessObjectId(refId);

		Set<TaskSchedule> set = new HashSet<TaskSchedule>();

		TaskSchedule ts = new TaskSchedule();
		ts.setRepeatType(task.getSchedule().getRepeatType().getType());
		ts.setStep(task.getSchedule().getStep());
		ts.setNextRunTime(task.getSchedule().getNextRunTime());
		ts.setLastRunTime(null);

		Constraint c = task.getSchedule().getConstraint();
		if (c != null) {
			Set<ScheduleConstraint> constr = new HashSet<ScheduleConstraint>();
			ScheduleConstraint sc = new ScheduleConstraint();
			sc.setConstraintType(c.getType().getScheduleConstraint());
			sc.setConstraint(SerializationHelper.serialize(c));
			constr.add(sc);
			ts.setConstraints(constr);
		}
		set.add(ts);

		t.setSchedules(set);

		s.save(t);

		return t;
	}

	public void updateTask(Session s, String name, Date nextRunTime,
			Date lastRunTime) throws Exception {
		TaskEntity t = (TaskEntity) s.createQuery(
				"select T from TaskEntity T where T.name=?").setString(0, name)
				.uniqueResult();
		if (t != null) {
			Set<TaskSchedule> set = t.getSchedules();
			for (TaskSchedule ts : set) {
				ts.setNextRunTime(nextRunTime);
				ts.setLastRunTime(lastRunTime);

			}
			s.save(t);
		}
	}

	public TaskEntity getTask(Session s, String name) throws Exception {
		TaskEntity t = (TaskEntity) s.createQuery(
				"select T from TaskEntity T where T.name=?").setString(0, name)
				.uniqueResult();
		return t;
	}

	public boolean taskExists(Session s, String name) throws Exception {
		Integer i = (Integer) s.createQuery(
				"select count(T) from TaskEntity T where T.name=?").setString(
				0, name).uniqueResult();
		if (i == null)
			return false;

		return i.intValue() != 0;
	}

	public void deleteTask(Session s, TaskEntity t) throws Exception {
		Set<TaskSchedule> sch = t.getSchedules();
		if (sch != null) {
			for (TaskSchedule ts : sch) {
				int numUpdates = deleteSchedule(s, ts);
				Logger.getLogger(getClass()).info(
						"Deleted schedule " + ts.getId() + "," + numUpdates);
			}
		}
		Query q = s.createQuery("delete from TaskEntity R where R.id=?");
		q.setLong(0, t.getId());
		int ret = q.executeUpdate();
		Logger.getLogger(getClass()).info(
				"Deleted task " + t.getName() + "," + ret);
	}

	private int deleteSchedule(Session s, TaskSchedule ts) throws Exception {
		Set<ScheduleConstraint> constraints = ts.getConstraints();
		if (constraints != null) {
			for (ScheduleConstraint sc : constraints) {
				int numUpdates = deleteConstraint(s, sc);
				Logger.getLogger(getClass()).info(
						"Deleted constraint for task " + ts.getId() + ","
								+ numUpdates);
			}
		}
		Query q = s.createQuery("delete from TaskSchedule R where R.id=?");
		q.setLong(0, ts.getId());
		int ret = q.executeUpdate();
		return ret;
	}

	private int deleteConstraint(Session s, ScheduleConstraint sc)
			throws Exception {
		Query q = s
				.createQuery("delete from ScheduleConstraint R where R.id=?");
		q.setLong(0, sc.getId());
		int ret = q.executeUpdate();
		return ret;
	}

	@SuppressWarnings("unchecked")
	public List<TaskEntity> getTasks(Session s) {
		return s.createQuery("select T from TaskEntity T").list();
	}
}
