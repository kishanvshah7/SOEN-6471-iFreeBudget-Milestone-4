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
package net.mjrz.fm.services;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddTransactionAction;
import net.mjrz.fm.actions.GetNetWorthAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.TaskEntityManager;
import net.mjrz.fm.entity.beans.NetWorthHistory;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.utils.NotificationHandler;
import net.mjrz.fm.ui.utils.notifications.types.MissedTxNotification;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.schedule.ScheduledTx;
import net.mjrz.scheduler.Scheduler;
import net.mjrz.scheduler.db.entities.ScheduleConstraint;
import net.mjrz.scheduler.db.entities.TaskEntity;
import net.mjrz.scheduler.db.entities.TaskSchedule;
import net.mjrz.scheduler.task.BasicSchedule;
import net.mjrz.scheduler.task.MonthSchedule;
import net.mjrz.scheduler.task.MonthScheduleDayBased;
import net.mjrz.scheduler.task.Schedule;
import net.mjrz.scheduler.task.Schedule.RepeatType;
import net.mjrz.scheduler.task.Task;
import net.mjrz.scheduler.task.WeekSchedule;
import net.mjrz.scheduler.task.constraints.Constraint;
import net.mjrz.scheduler.task.constraints.MonthConstraint;
import net.mjrz.scheduler.task.constraints.MonthConstraintDayBased;

import org.apache.log4j.Logger;
import org.hibernate.util.SerializationHelper;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class SchedulerService extends TimerTask {

	private User user;

	private Timer timer = null;

	long DELAY = 24 * 60 * 60 * 1000; // Num msecs in a day;

	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");

	private static Logger logger = Logger.getLogger(SchedulerService.class
			.getName());

	public static void start(User user) throws Exception {
		new SchedulerService(user);
	}

	@SuppressWarnings("unused")
	private SchedulerService() {

	}

	public SchedulerService(User user) throws Exception {
		logger.info("Scheduler service started");

		this.user = user;
		executePendingRequests();
		executeFutureTransactions();
		updateNetWorth();
		timer = new Timer();
		timer.scheduleAtFixedRate(this, getNextRunTime(), DELAY);
	}

	public void run() {
		try {
			executePendingRequests();
			executeFutureTransactions();
			updateNetWorth();
		}
		catch (Exception e) {
			try {
				throw e;
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void executePendingRequests() throws Exception {
		try {
			FManEntityManager em = new FManEntityManager();

			Calendar cal = new GregorianCalendar();
			String sdt = sdf.format(cal.getTime());
			AddTransactionAction action = new AddTransactionAction();
			List l = em.getTransactions(user, sdt.toString(),
					AccountTypes.TX_STATUS_PENDING);

			if (l != null)
				for (int i = 0; i < l.size(); i++) {
					Transaction t = (Transaction) l.get(i);
					ActionRequest req = new ActionRequest();
					req.setActionName("addTransaction");
					req.setUser(user);
					req.setProperty("TRANSACTION", t);
					req.setProperty("UPDATETX", Boolean.valueOf(true));

					ActionResponse result = action.executeAction(req);
					if (result.getErrorCode() == ActionResponse.NOERROR) {
						// System.out.println("Successfully updated:" + i);
					}
					else {
						// System.out.println("Failed to update:" + i);
					}
				}
		}
		catch (Exception e) {
			e.printStackTrace();
			timer.cancel();
			throw e;
		}
	}

	private Schedule rebuildSchedule(Date next, Date end, TaskSchedule ts)
			throws Exception {
		Schedule.RepeatType type = getTypeFromInt(ts.getRepeatType());
		ScheduleConstraint constr = getConstraint(ts);

		if (type == RepeatType.WEEK) {
			if (constr != null) {
				Schedule sch = new WeekSchedule(next, end);
				byte[] bb = constr.getConstraint();
				Constraint s = (Constraint) SerializationHelper.deserialize(bb);
				sch.setRepeatType(type, ts.getStep());
				sch.setConstraint(s);
				return sch;
			}
		}
		else if (type == RepeatType.MONTH) {
			if (constr != null) {
				Schedule sch = null;
				byte[] bb = constr.getConstraint();
				Constraint s = (Constraint) SerializationHelper.deserialize(bb);
				if (s instanceof MonthConstraint) {
					sch = new MonthSchedule(next, end);
				}
				else if (s instanceof MonthConstraintDayBased) {
					sch = new MonthScheduleDayBased(next, end);
				}
				sch.setRepeatType(type, ts.getStep());
				sch.setConstraint(s);
				return sch;
			}
		}
		else {
			Schedule sch = new BasicSchedule(next, end);
			sch.setRepeatType(type, ts.getStep());
			return sch;
		}
		return null;
	}

	private ScheduleConstraint getConstraint(TaskSchedule ts) {
		Set<ScheduleConstraint> cset = ts.getConstraints();
		if (cset != null && cset.size() > 0) {
			ScheduleConstraint ret = null;
			for (ScheduleConstraint sc : cset) {
				ret = sc;
			}
			return ret;
		}
		return null;
	}

	private void executeFutureTransactions() throws Exception {
		TaskEntityManager em = new TaskEntityManager();
		List<TaskEntity> list = em.getTasks();
		if (list == null)
			return;

		Date now = new Date();
		HashMap<String, List<Date>> missedTx = new HashMap<String, List<Date>>();
		for (TaskEntity e : list) {
			Set<TaskSchedule> set = e.getSchedules();
			for (TaskSchedule ts : set) {
				Date next = ts.getNextRunTime();
				try {
					Schedule sch = rebuildSchedule(next, e.getEndTime(), ts);
					// System.out.println(e.getName() + ":" + sch + ":" +
					// sch.getClass().getName());

					if (next.after(now)) {
						Task t = new ScheduledTx(e.getName(),
								e.getBusinessObjectId());
						t.setSchedule(sch);
						Scheduler.scheduleTask(t);
					}
					else {
						List<Date> missed = null;
						if (ts.getLastRunTime() != null) {
							missed = sch.getRunTimesBetween(
									ts.getLastRunTime(), now);
						}
						else {
							missed = sch.getRunTimesBetween(e.getStartTime(),
									now);
						}

						if (missed != null) {
							missedTx.put(e.getName(), missed);
						}

						if (missedTx.size() > 0) {
							scheduleMissedTask(e, ts, missed);
						}
					}
				}
				catch (Exception ex) {
					logger.error("Error creating task: " + e.getName());
					logger.error(MiscUtils.stackTrace2String(ex));
					continue;
				}
			}
		}
		/* send notification */
		if (missedTx.size() > 0) {
			MissedTxNotification n = new MissedTxNotification(missedTx);
			NotificationHandler.addToQueue(n);
		}
	}

	private void scheduleMissedTask(TaskEntity e, TaskSchedule ts,
			List<Date> missed) throws Exception {
		if (missed == null || missed.size() == 0) {
			return;
		}
		long id = e.getBusinessObjectId();
		Task t = new ScheduledTx(e.getName(), id);
		Date d = missed.get(missed.size() - 1);
		Schedule s = new BasicSchedule(d, e.getEndTime());
		s.setRepeatType(getTypeFromInt(ts.getRepeatType()), ts.getStep());
		t.setSchedule(s);
		Scheduler.scheduleTask(t);
	}

	private void updateNetWorth() {
		try {
			ActionRequest req = new ActionRequest();
			req.setActionName("getNetWorth");
			req.setUser(user);
			GetNetWorthAction action = new GetNetWorthAction();
			ActionResponse resp = action.executeAction(req);
			if (!resp.hasErrors()) {
				BigDecimal nw = (BigDecimal) resp.getResult("NET_VALUE");
				BigDecimal av = (BigDecimal) resp.getResult("ASSET_VALUE");
				BigDecimal lv = (BigDecimal) resp.getResult("LIAB_VALUE");

				GregorianCalendar curr = new GregorianCalendar();
				int yr = curr.get(Calendar.YEAR);
				int mo = curr.get(Calendar.MONTH);
				int dt = curr.get(Calendar.DATE);

				GregorianCalendar gc = new GregorianCalendar(yr, mo, dt);

				NetWorthHistory hist = new NetWorthHistory();
				hist.setAssets(av);
				hist.setLiabs(lv);
				hist.setNetWorth(nw);
				hist.setDate(gc.getTime());
				hist.setUid(user.getUid());

				FManEntityManager em = new FManEntityManager();
				em.addNetWorthHistory(hist);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			timer.cancel();
		}
	}

	private Date getNextRunTime() {
		Calendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DATE, 1);
		Calendar result = new GregorianCalendar(tomorrow.get(Calendar.YEAR),
				tomorrow.get(Calendar.MONTH), tomorrow.get(Calendar.DATE), 0, 0);

		logger.info("Next run time: " + result.getTime());
		return result.getTime();
	}

	private Schedule.RepeatType getTypeFromInt(int type) {
		switch (type) {
		case 1:
			return Schedule.RepeatType.SECOND;
		case 2:
			return Schedule.RepeatType.MINUTE;
		case 3:
			return Schedule.RepeatType.HOUR;
		case 4:
			return Schedule.RepeatType.DATE;
		case 5:
			return Schedule.RepeatType.WEEK;
		case 6:
			return Schedule.RepeatType.MONTH;
		case 7:
			return Schedule.RepeatType.YEAR;
		case 8:
			return Schedule.RepeatType.DAYOFWEEK;
		case 9:
			return Schedule.RepeatType.DAYOFMONTH;
		}
		return Schedule.RepeatType.NONE;
	}
}
