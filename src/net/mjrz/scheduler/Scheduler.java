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
package net.mjrz.scheduler;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.mjrz.scheduler.task.BasicTask;
import net.mjrz.scheduler.task.Task;

import org.apache.log4j.Logger;

/**
 * Scheduler executes tasks submitted to it for future execution in a 
 * background thread. Tasks submitted for execution can be cancelled
 * anytime before execution or during execution.
 * 
 * @author mjrz
 *
 */
public class Scheduler extends Observable implements Runnable {
	private LinkedBlockingQueue<Task> taskQueue = null;
	private ScheduledThreadPoolExecutor executor = null;
	private WeakHashMap<Task, ScheduledFuture<?>> tasks;

	private static final long ONE_SECOND = 1000L;

	private static Scheduler instance = null;

	private static Logger logger = Logger.getLogger(Scheduler.class.getName());

	public synchronized static void initialize() {
		if (instance == null) {
			instance = new Scheduler();
		}
		new Thread(instance).start();
		logger.info("Scheduler started");
	}

	private Scheduler() {
		taskQueue = new LinkedBlockingQueue<Task>(16);
		executor = new ScheduledThreadPoolExecutor(5);
		tasks = new WeakHashMap<Task, ScheduledFuture<?>>();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Task t = taskQueue.take();
				long delay = t.getSchedule().getDelay();
				if (delay > ONE_SECOND) {
					ScheduledFuture<?> value = executor.schedule(t, delay, TimeUnit.MILLISECONDS);
					tasks.put(t, value);
					logger.info("Task scheduled..." + t.toString() + ","
							+ t.getSchedule());
				}
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
	}

	private void addTask(Task task) {
		taskQueue.offer(task);
	}
	
	private void removeTask(String name) {
		Task tmp = new BasicTask(name);
		removeTask(tmp);
	}

	private void removeTask(Task task) {
		Set<Entry<Task, ScheduledFuture<?>>> set = tasks.entrySet();
		for(Entry<Task, ScheduledFuture<?>> e : set) {
			if(e.getKey().getName().equals(task.getName())) {
				logger.info("Got future.. " + e.getValue());
				e.getValue().cancel(true);
				break;
			}
		}
		executor.purge();
	}

	// Static methods to access the scheduler.
	
	/**
	 * This method is called when a task execution completes. If the task is
	 * repeating, it will be rescheduled according to the schedule. Scheduler
	 * also notifies any registered observers about the task completion.
	 * 
	 * @param task
	 *            <code>Task</code> that needs to be rescheduled or cleaned up.
	 */
	public static void taskDone(Task task) {
		if (instance == null) {
			return;
		}
		instance.removeTask(task);
		if (task.getSchedule().isRepeating()) {
			try {
				Task newtask = (Task) ((BasicTask) task).clone();
				newtask.getSchedule().setStartTime(new Date());
				scheduleTask(newtask);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		instance.setChanged();
		instance.notifyObservers(task);
		task = null;
	}

	/**
	 * Schedules a task for future execution based on the schedule of the task.
	 * 
	 * @param task
	 *            <code>Task</code> that is to be scheduled.
	 * @see Schedule         
	 */
	public static void scheduleTask(Task task) {
		if (instance == null)
			return;
		instance.addTask(task);
	}
	
	/**
	 * Cancels a task scheduled for execution
	 * 
	 * @param task
	 *            to cancel
	 */
	public static void cancleTask(Task task) {
		if (instance == null)
			return;
	}
	
	/**
	 * Cancels a task with specified name
	 * 
	 * @param name
	 *            of the task to cancel
	 */
	public static void cancelTask(String name) {
		if(instance == null) {
			return;
		}
		instance.removeTask(name);
	}
	
	/**
	 * Register an observer to be notified with task updates.
	 * 
	 * @param o
	 *            observer to register
	 */
	public static void register(Observer o) {
		if(instance == null)
			return;
		instance.addObserver(o);
	}
	
	/**
	 * De register an observer from the scheduler
	 * 
	 * @param o
	 *            observer to deregister
	 */
	public static void deRegister(Observer o) {
		if(instance == null)
			return;
		instance.deleteObserver(o);
	}
}
