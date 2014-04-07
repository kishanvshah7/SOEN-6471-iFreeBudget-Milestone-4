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
package net.mjrz.scheduler.task;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.mjrz.scheduler.Scheduler;

/**
 * Basic implementation of the <code>Task</code> interface.
 * @author mjrz
 *
 */
public class BasicTask extends AbstractTask {
	protected Schedule schedule;
	protected boolean done = false;
	protected boolean cancelled = false;
	protected Object result;
	protected int runCount;
	
	public BasicTask(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	@Override
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Override
	public final void run() {
		Date lastRunTime = new Date();
		schedule.setLastRunTime(lastRunTime);
		System.out.println("Ring..." + name + " @ " + lastRunTime);
		executeTask();
		Scheduler.taskDone(this);
	}
		
	@Override
	public void executeTask() {
		result = "Done";
		done = true;
		cancelled = true;
		runCount++;		
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		done = true;
		cancelled = true;
		return cancelled;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return result;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		return result;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	@Override
	public String toString() {
		return new StringBuilder(name).append("(").append(runCount).append(") ").toString();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Schedule sch = (Schedule) ( (BasicSchedule) schedule).clone();
		Task ret = (Task) super.clone();
		ret.setSchedule(sch);
		return ret;
	}
}
