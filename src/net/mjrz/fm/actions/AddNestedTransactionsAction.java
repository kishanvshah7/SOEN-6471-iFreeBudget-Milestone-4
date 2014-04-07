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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.entity.beans.Attachment;
import net.mjrz.fm.entity.beans.AttachmentRef;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;
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
public class AddNestedTransactionsAction {
	private static Logger logger = Logger
			.getLogger(AddNestedTransactionsAction.class.getName());
	FManEntityManager em = null;

	public AddNestedTransactionsAction() {
		em = new FManEntityManager();
	}

	@SuppressWarnings("unchecked")
	public ActionResponse executeAction(ActionRequest req) throws Exception {
		em = new FManEntityManager();
		try {
			User u = req.getUser();
			ArrayList<Transaction> txList = (ArrayList<Transaction>) req
					.getProperty("TXLIST");

			Date today = new Date();

			boolean isUpdate = (Boolean) req.getProperty("UPDATETX");

			List<String> attachments = (List<String>) req
					.getProperty("ATTACHMENTS");

			if (isUpdate && txList.size() != 1) {
				ActionResponse errResp = new ActionResponse();
				errResp.setErrorCode(ActionResponse.INVALID_TX);
				errResp.setErrorMessage("Invalid tx to update");
				return errResp;
			}

			ActionResponse resp = null;
			for (Transaction tx : txList) {
				if (tx.getTxDate().after(today)) {
					tx.setTxStatus(AccountTypes.TX_STATUS_PENDING);
				}
				else {
					tx.setTxStatus(AccountTypes.TX_STATUS_COMPLETED);
				}
			}
			resp = addTransactions(u, txList, isUpdate, attachments);
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
	}

	private ActionResponse addTransactions(User u,
			ArrayList<Transaction> txList, boolean isUpdate,
			List<String> attachments) throws Exception {

		Session s = null;
		ActionResponse resp = new ActionResponse();
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			u = (User) s.load(User.class, u.getUid());

			int sz = txList.size();

			for (int i = 0; i < sz; i++) {
				Transaction t = txList.get(i);
				if (i > 0) {
					Transaction parent = txList.get(0);
					t.setParentTxId(parent.getTxId());
				}
				addTransaction(s, u, t, resp, isUpdate);

				if (resp.getErrorCode() != ActionResponse.NOERROR) {
					break;
				}
			}

			/* Add attachment to the first tx in the list */
			if (sz > 0 && attachments != null) {
				Long txId = txList.get(0).getTxId();
				for (String attachmentPath : attachments) {
					addAttachment(s, attachmentPath, txId);
				}
			}
			return resp;
		}
		catch (Exception e) {
			e.printStackTrace();
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage(e.getMessage());
			throw e;
		}
		finally {
			if (s != null) {
				if (resp.getErrorCode() == ActionResponse.NOERROR) {
					s.getTransaction().commit();
					if (txList.size() > 0) {
						Long sourceAcctId = txList.get(0).getFromAccountId();
						resp.addResult("SOURCEACCOUNTID", sourceAcctId);
					}
				}
				else {
					s.getTransaction().rollback();
				}
				HibernateUtils.closeSession();
			}
		}
	}

	private void addTransaction(Session s, User u, Transaction t,
			ActionResponse resp, boolean isUpdate) throws Exception {
		/*
		 * Hack.. Even if the tx is an update, the tx can have an null(0) txId.
		 * This happens when, a tx is edited and a new to account is added as a
		 * split transaction.
		 */
		long oldTxId = t.getTxId();
		if (isUpdate && oldTxId != 0) {
			Transaction oldTx = (Transaction) s.load(Transaction.class,
					t.getTxId());
			Account from = em.getAccount(s, u.getUid(),
					oldTx.getFromAccountId());
			Account to = em.getAccount(s, u.getUid(), oldTx.getToAccountId());

			if (!deleteTransaction(s, oldTx, from, to)) {
				resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
				resp.setErrorMessage("Unable to delete transaction");
				return;
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

		validate(s, t, from, to, resp);
		if (resp.getErrorCode() != ActionResponse.NOERROR) {
			return;
		}

		/* Populate the temp table for UI updates and paging */
		TT tt = new TT();

		Converter bdConverter = new BigDecimalConverter(new BigDecimal(0.0d));
		ConvertUtils.register(bdConverter, BigDecimal.class);
		BeanUtils.copyProperties(tt, t);

		tt.setFromName(from.getAccountName());
		tt.setToName(to.getAccountName());

		/* update parent txid for all child transactions */
		if (isUpdate) {
			if (!updateChildren(s, oldTxId, id)) {
				resp.setErrorCode(ActionResponse.INVALID_TX);
				resp.setErrorMessage("Unable to update children info for nested tx");
				return;
			}
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

			resp.setErrorCode(ActionResponse.NOERROR);

			/* Add to alerts cache if applicable */
			AlertsEntityManager aem = new AlertsEntityManager();
			checkAlert(s, from, aem);
			checkAlert(s, to, aem);
		}
	}

	private boolean updateChildren(Session s, long oldTxId, long newTxId)
			throws Exception {
		int countT = em.updateChildrenTx(s, oldTxId, newTxId);
		int countTT = em.updateChildrenTT(s, oldTxId, newTxId);

		logger.debug("Updated children info. num updates = " + countT + ","
				+ countTT);
		return countT == countTT;
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

			if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY
					|| from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
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

			deleteAttachments(s, t.getTxId());

			int rc = em.deleteTransaction(s, t.getTxId());
			if (rc == 1) {
				logger.info("Deleted tx for update count = " + rc);
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

	private void addAttachment(Session s, String filePath, Long txId)
			throws Exception {
		try {
			String mimeType = getMimeType(filePath);
			if (mimeType == null)
				return;

			File f = new File(filePath);
			FileInputStream fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);
			Long len = f.length();
			byte[] data = new byte[len.intValue()];
			byte c;
			int i = 0;
			while (true) {
				try {
					c = dis.readByte();
					data[i] = c;
					i++;
				}
				catch (EOFException eof) {
					break;
				}
			}
			Attachment at = new Attachment();
			at.setId(IDGenerator.getInstance().generateId(s));
			at.setAttachment(data);

			AttachmentRef atRef = new AttachmentRef();
			atRef.setId(IDGenerator.getInstance().generateId(s));
			atRef.setKey(txId);
			atRef.setSource("Transaction");
			atRef.setMimeType(mimeType);
			atRef.setFileName(f.getName());
			atRef.setAttachmentId(at.getId());

			s.save(atRef);
			s.save(at);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
	}

	private String getMimeType(String filePath) {
		int pos = filePath.indexOf('.');
		if (pos >= 0 && pos + 1 < filePath.length()) {
			String ext = filePath.substring(pos + 1);
			if (ext.equalsIgnoreCase("pdf")) {
				return "application/pdf";
			}
			else if (ext.equalsIgnoreCase("jpg")
					|| ext.equalsIgnoreCase("jpeg")
					|| ext.equalsIgnoreCase("jpe")) {
				return "image/jpeg";
			}
			else if (ext.equalsIgnoreCase("bmp")) {
				return "image/bmp";
			}
			return null;
		}
		return null;
	}
}
