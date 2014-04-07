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
package net.mjrz.fm.entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.AlertsCache;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class AlertsEntityManager {
	private static Logger logger = Logger.getLogger(AlertsEntityManager.class
			.getName());

	public long addAlert(Alert u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = u.getId();
			if (id == 0) {
				id = IDGenerator.getInstance().generateId(s);
				u.setId(id);
				s.save(u);
			}
			else {
				s.update(u);
			}
			s.getTransaction().commit();

			return id;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void saveAlert(Alert alert) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.update(alert);
			s.getTransaction().commit();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void updateAlert(Session s, Alert alert) throws Exception {
		try {
			s.save(alert);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public List getAlerts(Session s, long accountId) throws Exception {
		try {
			String query = "select R from Alert R where R.accountId=? order by R.id";

			Query q = s.createQuery(query);
			q.setLong(0, accountId);
			return q.list();
		}
		catch (Exception e) {
			throw e;
		}
	}

	public List getAlerts(long accountId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Alert R where R.accountId=? order by R.id";

			Query q = s.createQuery(query);
			q.setLong(0, accountId);
			return q.list();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Alert> getAllAlerts() throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Alert R";

			Query q = s.createQuery(query);
			return q.list();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Alert getAlert(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Alert R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			return (Alert) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public int deleteAlert(long accountid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();

			String query = "delete from Alert R where R.accountId=?";
			s.beginTransaction();
			Query q = s.createQuery(query);
			q.setLong(0, accountid);
			int r = q.executeUpdate();
			s.getTransaction().commit();
			return r;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
	}

	public static final boolean hasAlert(long accountId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select count(*) from Alert R where R.accountId=?";

			Query q = s.createQuery(query);
			q.setLong(0, accountId);
			Long count = (Long) q.uniqueResult();
			return count > 0;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	public static final boolean accountAlertRaised(long accountId)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Alert R where R.accountId=?";

			Query q = s.createQuery(query);
			q.setLong(0, accountId);
			List alerts = (List) q.list();

			if (alerts == null || alerts.size() == 0)
				return false;

			Account a = (Account) s
					.load(Account.class, Long.valueOf(accountId));

			return checkAlert(s, a, alerts);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean checkAlert(Session s, Account a, List alerts) {
		try {
			if (alerts == null || alerts.size() == 0)
				return false;

			Alert alert = (Alert) alerts.get(0);

			if (alert.getConditional().equals(Alert.EXCEEDS)) {
				BigDecimal bal = a.getCurrentBalance();
				BigDecimal alertBal = alert.getAmount();
				if (bal == null || alertBal == null)
					return false;
				return bal.compareTo(alertBal) >= 0;
			}
			if (alert.getConditional().equals(Alert.FALLS_BELOW)) {
				BigDecimal bal = a.getCurrentBalance();
				BigDecimal alertBal = alert.getAmount();
				if (bal == null || alertBal == null)
					return false;
				return bal.compareTo(alertBal) <= 0;
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		return false;
	}

	/* Utility methods */
	public static void updateAllAlerts() {
		try {
			FManEntityManager em = new FManEntityManager();
			AlertsEntityManager aem = new AlertsEntityManager();

			List alerts = aem.getAllAlerts();
			if (alerts == null)
				return;

			for (Object o : alerts) {
				Alert a = (Alert) o;
				if (a.getAmount() == null) {
					aem.deleteAlert(a.getAccountId());
					continue;
				}
				Account acct = em.getAccount(SessionManager.getSessionUserId(),
						a.getAccountId());
				AlertsEntityManager.checkAlert(acct, a);
			}
		}
		catch (Exception e) {
			Logger log = Logger.getLogger(AlertsEntityManager.class.getName());
			log.error(e);
		}
	}

	private static void updateAlertCache(long accountId, int status) {
		try {
			AlertsCache.getInstance().setAlert(accountId, status);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	@SuppressWarnings("unchecked")
	public static void checkAlert(Account account, Alert alert) {
		if (account.getAccountType() == net.mjrz.fm.constants.AccountTypes.ACCT_TYPE_EXPENSE) {
			checkExpenseAlert(account, alert);
			return;
		}
		try {
			if (alert.getConditional().equals(Alert.EXCEEDS)) {
				BigDecimal bal = account.getCurrentBalance();
				BigDecimal alertBal = alert.getAmount();
				if (bal == null || alertBal == null)
					return;

				if (bal.compareTo(alertBal) >= 0) {
					alert.setStatus(Alert.ALERT_RAISED);
					updateAlertCache(account.getAccountId(), Alert.ALERT_RAISED);
				}
				else {
					alert.setStatus(Alert.ALERT_CLEARED);
					updateAlertCache(account.getAccountId(),
							Alert.ALERT_CLEARED);
				}
			}
			if (alert.getConditional().equals(Alert.FALLS_BELOW)) {
				BigDecimal bal = account.getCurrentBalance();
				BigDecimal alertBal = alert.getAmount();
				if (bal == null || alertBal == null)
					return;

				if (bal.compareTo(alertBal) <= 0) {
					alert.setStatus(Alert.ALERT_RAISED);
					updateAlertCache(account.getAccountId(), Alert.ALERT_RAISED);
				}
				else {
					alert.setStatus(Alert.ALERT_CLEARED);
					updateAlertCache(account.getAccountId(),
							Alert.ALERT_CLEARED);
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	@SuppressWarnings("unchecked")
	private static void checkExpenseAlert(Account a, Alert alert) {
		if (a.getAccountType() != net.mjrz.fm.constants.AccountTypes.ACCT_TYPE_EXPENSE) {
			return;
		}
		try {
			String[] range = getDateRange(alert.getRange());
			List txList = new FManEntityManager().getTransactionsTo(
					SessionManager.getSessionUserId(), a.getAccountId(),
					range[0], range[1]);
			BigDecimal total = new BigDecimal(0d);
			if (txList != null) {
				if (txList.size() == 0) {
					alert.setStatus(Alert.ALERT_CLEARED);
					updateAlertCache(a.getAccountId(), Alert.ALERT_CLEARED);
					return;
				}

				for (Object o : txList) {
					Transaction t = (Transaction) o;
					total = total.add(t.getTxAmount());
				}
				if (alert.getConditional().equals(Alert.FALLS_BELOW)) {
					if (total.compareTo(alert.getAmount()) <= 0) {
						alert.setStatus(Alert.ALERT_RAISED);
						updateAlertCache(a.getAccountId(), Alert.ALERT_RAISED);
					}
					else {
						alert.setStatus(Alert.ALERT_CLEARED);
						updateAlertCache(a.getAccountId(), Alert.ALERT_CLEARED);
					}
				}
				if (alert.getConditional().equals(Alert.EXCEEDS)) {
					if (total.compareTo(alert.getAmount()) >= 0) {
						alert.setStatus(Alert.ALERT_RAISED);
						updateAlertCache(a.getAccountId(), Alert.ALERT_RAISED);
					}
					else {
						alert.setStatus(Alert.ALERT_CLEARED);
						updateAlertCache(a.getAccountId(), Alert.ALERT_CLEARED);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public static String[] getDateRange(String rangeStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
		
		String[] ret = new String[2];
		Calendar start = new GregorianCalendar();
		start.set(Calendar.HOUR_OF_DAY, 0);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);

		Calendar end = new GregorianCalendar();
		end.set(Calendar.HOUR_OF_DAY, 23);
		end.set(Calendar.MINUTE, 59);
		end.set(Calendar.SECOND, 59);

		if (rangeStr.equals(Alert.RANGE[0])) { // this week
			int dayOfWeek = start.get(Calendar.DAY_OF_WEEK) - 1;
			start.add(Calendar.DATE, -1 * dayOfWeek);

			dayOfWeek = end.get(Calendar.DAY_OF_WEEK);
			end.add(Calendar.DATE, (7 - dayOfWeek));
		}
		if (rangeStr.equals(Alert.RANGE[1])) { // this month
			int dayOfMonth = start.get(Calendar.DATE) - 1;
			start.add(Calendar.DATE, -1 * dayOfMonth);

			dayOfMonth = end.get(Calendar.DATE);
			end.add(Calendar.MONTH, 1);
			end.set(Calendar.DATE, 1);
			end.add(Calendar.DATE, -1);

			ret[0] = sdf.format(start.getTime());
			ret[1] = sdf.format(end.getTime());
		}
		if (rangeStr.equals(Alert.RANGE[2])) { // today
		}
		ret[0] = sdf.format(start.getTime());
		ret[1] = sdf.format(end.getTime());
		return ret;
	}
}
