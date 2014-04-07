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
package net.mjrz.fm.actions;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.entity.beans.FutureTransaction;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.hibernate.Session;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddTransactionAction  extends TransactionValidation{
	FManEntityManager em = null;

	public AddTransactionAction() {
		em = new FManEntityManager();
	}

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		em = new FManEntityManager();
		try {
			User u = req.getUser();
			Transaction tx = (Transaction) req.getProperty("TRANSACTION");
			Date today = new Date();
			boolean isUpdate = (Boolean) req.getProperty("UPDATETX");
			FutureTransaction st = (FutureTransaction) req
					.getProperty("SCHEDULEDTRANSACTION");

			ActionResponse resp = null;
			if (tx.getTxDate().after(today)) {
				tx.setTxStatus(AccountTypes.TX_STATUS_PENDING);
				resp = addTransaction(u, tx, isUpdate, st);
			}
			else {
				tx.setTxStatus(AccountTypes.TX_STATUS_COMPLETED);
				resp = addTransaction(u, tx, isUpdate, st);
			}
			return resp;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public boolean validate() {
		return false;
	}

	public void validate(Session s, Transaction t, Account from, Account to,
			ActionResponse resp) throws Exception {
		String fitid = t.getFitid();
		if (fitid != null && fitid.trim().length() > 0) {
			if (em.fitIdExists(s, t.getInitiatorId(), t.getFromAccountId(),
					t.getToAccountId(), t.getFitid())) {
				resp.setErrorCode(ActionResponse.TX_EXISTS_ERROR);
				return;
			}
		}
		/* Basic error checking... */
		if (t.getTxAmount().doubleValue() < 0) {
			resp.setErrorCode(ActionResponse.INVALID_TX);
			resp.setErrorMessage("Transaction amount must be greater than zero");
			return;
		}
		if (from.getAccountId() == to.getAccountId()) {
			resp.setErrorCode(ActionResponse.INVALID_TX);
			resp.setErrorMessage("To and from accounts are same");
			return;
		}
		if (from.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
			resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
			resp.setErrorMessage("Account is locked [" + from.getAccountName()
					+ "]");
			return;
		}
		if (to.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
			resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
			resp.setErrorMessage("Account is locked [" + to.getAccountName()
					+ "]");
			return;
		}
		// if(from.getAccountType() == AccountTypes.ACCT_TYPE_EXPENSE) {
		// resp.setErrorCode(ActionResponse.INVALID_FROM_ACCOUNT);
		// }
		// if(to.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
		// resp.setErrorCode(ActionResponse.INVALID_TO_ACCOUNT);
		// return;
		// }
		// if(t.getTxAmount() > from.getCurrentBalance() &&
		// from.getAccountType() != AccountTypes.ACCT_TYPE_LIABILITY) {
		// resp.setErrorCode(ActionResponse.INSUFFICIENT_BALANCE);
		// return;
		// }
		// if(to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
		// if(t.getTxAmount() > to.getCurrentBalance()) {
		// resp.setErrorCode(ActionResponse.LIAB_ACCT_EXCEEDED_BALANCE);
		// }
		// return;
		// }
		// if(from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME &&
		// to.getAccountType() != AccountTypes.ACCT_TYPE_CASH) {
		// resp.setErrorCode(ActionResponse.INVALID_TO_ACCOUNT);
		// return;
		// }
	}

	private ActionResponse addTransaction(User u, Transaction t,
			boolean isUpdate, FutureTransaction st) throws Exception {
		Session s = null;
		ActionResponse resp = new ActionResponse();
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			u = (User) s.load(User.class, u.getUid());

			if (isUpdate) {
				Transaction oldTx = (Transaction) s.load(Transaction.class,
						t.getTxId());
				Account from = em.getAccount(s, u.getUid(),
						oldTx.getFromAccountId());
				Account to = em.getAccount(s, u.getUid(),
						oldTx.getToAccountId());

				if (!deleteTransaction(s, oldTx, from, to)) {
					resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
					resp.setErrorMessage("Unable to delete transaction");
					return resp;
				}
				else {
					/* Remove the old tx from temp table */
					em.deleteTT(s, oldTx.getTxId());
				}
			}
			Account from = em.getAccount(s, u.getUid(), t.getFromAccountId());
			Account to = em.getAccount(s, u.getUid(), t.getToAccountId());

			long id = IDGenerator.getInstance().generateId(s);
			t.setTxId(id);

			/* Populate the temp table for UI updates and paging */
			TT tt = new TT();

			Converter bdConverter = new BigDecimalConverter(
					new BigDecimal(0.0d));
			ConvertUtils.register(bdConverter, BigDecimal.class);
			BeanUtils.copyProperties(tt, t);

			tt.setFromName(from.getAccountName());
			tt.setToName(to.getAccountName());

			validate(s, t, from, to, resp);
			if (resp.getErrorCode() != ActionResponse.NOERROR) {
				return resp;
			}

			/* Assumed to be clean from here on */

			if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
				s.save(t);
				s.save(tt); // save the temp object for UI updates and paging
				resp.setErrorCode(ActionResponse.NOERROR);
			}
			else {
				if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
					BigDecimal curr = from.getCurrentBalance();
					from.setCurrentBalance(curr.add(t.getTxAmount()));
				}
				else if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
					BigDecimal curr = from.getCurrentBalance();
					from.setCurrentBalance(curr.add(t.getTxAmount()));
				}
				else {
					if (from.getAccountType() != AccountTypes.ACCT_TYPE_EXPENSE) {
						BigDecimal curr = from.getCurrentBalance();
						from.setCurrentBalance(curr.subtract(t.getTxAmount()));
					}
				}

				if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
					BigDecimal curr = to.getCurrentBalance();
					to.setCurrentBalance(curr.subtract(t.getTxAmount()));
				}
				else {
					BigDecimal curr = to.getCurrentBalance();
					to.setCurrentBalance(curr.add(t.getTxAmount()));
				}

				/* Finally set ending balance for from and to accounts */
				t.setFromAccountEndingBal(from.getCurrentBalance());
				t.setToAccountEndingBal(to.getCurrentBalance());
				tt.setFromAccountEndingBal(from.getCurrentBalance());
				tt.setToAccountEndingBal(to.getCurrentBalance());

				s.save(from);
				s.save(to);
				s.save(t);
				s.save(tt); // save the temp object for UI updates and paging

				// System.out.println("Added tx: " + t.getTxAmount() + ":" +
				// from.getAccountName() + ":" + from.getCurrentBalance());
				/* Add scheduled transaction if applicable */
				addFutureTransaction(s, t, st);

				resp.setErrorCode(ActionResponse.NOERROR);

				/* Add to alerts cache if applicable */
				AlertsEntityManager aem = new AlertsEntityManager();
				checkAlert(s, from, aem);
				checkAlert(s, to, aem);
			}
			return resp;
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage(e.getMessage());
			throw e;
		}
		finally {
			if (s != null) {
				if (resp.getErrorCode() == ActionResponse.NOERROR) {
					s.getTransaction().commit();
				}
				else {
					s.getTransaction().rollback();
				}
				HibernateUtils.closeSession();
			}
		}
	}

	private void addFutureTransaction(Session s, Transaction t,
			FutureTransaction st) throws Exception {
		if (st == null)
			return;

		long id = IDGenerator.getInstance().generateId(s);
		st.setId(id);
		st.setUid(t.getInitiatorId());
		st.setTransactionId(t.getTxId());
		Date next = getNextRunDate(st);
		st.setNextRunDate(next);
		s.save(st);
	}

	private Date getNextRunDate(FutureTransaction st) {
		int freq = st.getFrequency();
		String unit = st.getFrequencyUnit();

		GregorianCalendar gc = new GregorianCalendar();

		if (unit.equals(FutureTransaction.FREQUENCY_STRINGS[0])) { // daily
			gc.add(Calendar.DATE, freq);
		}
		if (unit.equals(FutureTransaction.FREQUENCY_STRINGS[1])) { // weekly
			gc.add(Calendar.DATE, 7 * freq);
		}
		if (unit.equals(FutureTransaction.FREQUENCY_STRINGS[2])) { // monthly
			gc.add(Calendar.MONTH, freq);
		}

		// System.out.println("Next run time: " + gc.getTime());
		return gc.getTime();
	}

	private boolean deleteTransaction(Session s, Transaction currTx,
			Account from, Account to) throws Exception {
		try {
			FManEntityManager em = new FManEntityManager();
			Transaction t = (Transaction) s.load(Transaction.class,
					currTx.getTxId());
			long id = t.getTxId();
			if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
				int rc = em.deleteTransaction(s, id);
				return rc == 1;
			}

			BigDecimal fbal = from.getCurrentBalance();
			BigDecimal tbal = to.getCurrentBalance();
			if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
				from.setCurrentBalance(fbal.subtract(t.getTxAmount()));
			}
			else {
				from.setCurrentBalance(fbal.add(t.getTxAmount()));
			}
			if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
				to.setCurrentBalance(tbal.add(t.getTxAmount()));
			}
			else {
				to.setCurrentBalance(tbal.subtract(t.getTxAmount()));
			}

			int rc = em.deleteTransaction(s, t.getTxId());
			if (rc == 1) {
				s.save(from);
				s.save(to);
				return true;
			}
			return false;
		}
		catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean checkAlert(Session s, Account a, AlertsEntityManager em) {
		try {
			List alerts = em.getAlerts(s, a.getAccountId());
			if (alerts == null || alerts.size() == 0)
				return false;

			Alert alert = (Alert) alerts.get(0);
			AlertsEntityManager.checkAlert(a, alert);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
