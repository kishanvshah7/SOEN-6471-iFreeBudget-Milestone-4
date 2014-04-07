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
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.entity.beans.AttachmentRef;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class DeleteTransactionAction {
	private static Logger logger = Logger
			.getLogger(DeleteTransactionAction.class.getName());

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		User u = req.getUser();
		long txId = (Long) req.getProperty("TXID");

		ActionResponse resp = new ActionResponse();

		List<Transaction> children = new FManEntityManager()
				.getChildTransactions(u, txId);

		/* Begin tx */
		Session s = HibernateUtils.getSessionFactory().getCurrentSession();
		s.beginTransaction();

		try {
			/* First delte children if available */
			if (children != null && children.size() > 0) {
				for (Transaction t : children) {
					int delCount = deleteTransaction(s, u, t);
					if (delCount != 1) {
						resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
						resp.setErrorMessage("Unable to delete child tx for id "
								+ txId);
						break;
					}
				}
			}
			/* Finall delete parent */
			int delCount = deleteTransaction(s, u, txId);
			if (delCount != 1) {
				resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
				resp.setErrorMessage("Unable to delete child tx for id " + txId);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
			resp.setErrorMessage(e.getMessage());
			throw e;
		}
		finally {
			if (resp.getErrorCode() == ActionResponse.NOERROR) {
				s.getTransaction().commit();
				resp.setErrorCode(ActionResponse.NOERROR);
			}
			else {
				s.getTransaction().rollback();
				resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
				resp.setErrorMessage("Unable to delete transaction");
			}
			if (s != null)
				HibernateUtils.closeSession();
		}
		return resp;
	}

	public boolean validate() {
		return false;
	}

	private void updateFromAcct(Account from, Transaction t) {
		if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
			BigDecimal curr = from.getCurrentBalance();
			from.setCurrentBalance(curr.subtract(t.getTxAmount()));
			return;
		}
		if (from.getAccountType() == AccountTypes.ACCT_TYPE_CASH) {
			BigDecimal curr = from.getCurrentBalance();
			from.setCurrentBalance(curr.add(t.getTxAmount()));
			return;
		}
		if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
			BigDecimal curr = from.getCurrentBalance();
			from.setCurrentBalance(curr.subtract(t.getTxAmount()));
			return;
		}
		if (from.getAccountType() != AccountTypes.ACCT_TYPE_EXPENSE) {
			BigDecimal curr = from.getCurrentBalance();
			from.setCurrentBalance(curr.add(t.getTxAmount()));
			return;
		}
	}

	private void updateToAcct(Account to, Transaction t) {
		if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
			BigDecimal curr = to.getCurrentBalance();
			to.setCurrentBalance(curr.add(t.getTxAmount()));
		}
		else {
			BigDecimal curr = to.getCurrentBalance();
			to.setCurrentBalance(curr.subtract(t.getTxAmount()));
		}
	}

	private int deleteTransaction(Session s, User u, long txId)
			throws Exception {
		Transaction t = new FManEntityManager().getTransaction(s, u, txId);
		return deleteTransaction(s, u, t);
	}

	private int deleteTransaction(Session s, User u, Transaction t)
			throws Exception {
		try {
			ActionResponse resp = new ActionResponse();

			u = (User) s.load(User.class, u.getUid());
			FManEntityManager em = new FManEntityManager();

			TT tt = new TT();
			Converter bdConverter = new BigDecimalConverter(
					new BigDecimal(0.0d));
			ConvertUtils.register(bdConverter, BigDecimal.class);
			BeanUtils.copyProperties(tt, t);

			long id = t.getTxId();
			if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
				em.deleteTransaction(s, id);
				em.deleteTT(s, tt.getTxId());
				s.getTransaction().commit();
				resp.setErrorCode(ActionResponse.NOERROR);
				return -1;
			}

			Account from = em.getAccount(s, u.getUid(), t.getFromAccountId());
			Account to = em.getAccount(s, u.getUid(), t.getToAccountId());

			updateFromAcct(from, t);
			updateToAcct(to, t);

			deleteAttachments(s, t.getTxId());

			int rc = em.deleteTransaction(s, t.getTxId());

			if (rc == 1) {
				s.save(from);
				s.save(to);

				em.deleteTT(s, tt.getTxId());

				/* Add to alerts cache if applicable */
				AlertsEntityManager aem = new AlertsEntityManager();
				checkAlert(s, from, aem);
				checkAlert(s, to, aem);
			}
			return rc;
		}
		catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private void deleteAttachments(Session s, long txId) throws Exception {
		List<AttachmentRef> atts = s
				.createQuery("from AttachmentRef R " + "where R.key=?")
				.setLong(0, txId).list();

		if (atts != null && atts.size() > 0) {
			for (AttachmentRef ref : atts) {
				if (!deleteAttachment(s, ref)) {
					throw new RuntimeException(
							"Unable to delete attachments for tx: " + txId);
				}
				s.delete(ref);
			}
			logger.debug("Deleted " + atts.size() + " attachments.");
		}
	}

	private boolean deleteAttachment(Session s, AttachmentRef ref)
			throws Exception {
		Query q = s.createQuery("delete from Attachment R where R.id=?");
		q.setLong(0, ref.getAttachmentId());
		int r = q.executeUpdate();
		logger.debug("Num deleted attachments : " + r);
		return r > 0;
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
