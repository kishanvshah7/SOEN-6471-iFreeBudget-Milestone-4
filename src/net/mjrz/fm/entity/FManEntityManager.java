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
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.AttachmentRef;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.Contact;
import net.mjrz.fm.entity.beans.FManEntity;
import net.mjrz.fm.entity.beans.Favourites;
import net.mjrz.fm.entity.beans.NetWorthHistory;
import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.entity.beans.Portfolio;
import net.mjrz.fm.entity.beans.PortfolioEntry;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.TxDecorator;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.crypto.CHelper;

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
public class FManEntityManager {
	private FManAccountEntityManager fManAccountEntityManager = new FManAccountEntityManager();
	private static Logger logger = Logger.getLogger(FManEntityManager.class
			.getName());

	public void cleanTT() throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			Query deleteQuery = s.createQuery("delete from TT tt");
			int del = deleteQuery.executeUpdate();
			logger.info("Clean up temp tables. Deleted " + del + " rows");
			s.getTransaction().commit();
		}
		catch (Exception e) {
			throw e;
		}
	}

	public int deleteTT(Session s, long txId) throws Exception {
		try {
			try {
				String query = "delete from TT T where T.txId=?";
				Query q = s.createQuery(query);
				q.setLong(0, txId);
				int r = q.executeUpdate();
				return r;
			}
			catch (Exception e) {
				throw e;
			}
		}
		catch (Exception e) {
			throw e;
		}
	}

	public void initializeTT(User u) throws Exception {
		Session s = null;
		try {
			long start = System.currentTimeMillis();
			cleanTT();
			List l = getTransactions(u);
			if (l.size() == 0)
				return;

			List<TT> children = new ArrayList<TT>();

			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			int sz = l.size();
			for (int i = 0; i < sz; i++) {
				Transaction t = (Transaction) l.get(i);
				Account from = this.getAccount(s, u.getUid(),
						t.getFromAccountId());
				Account to = this.getAccount(s, u.getUid(), t.getToAccountId());
				TT tt = new TT();

				Converter bdConverter = new BigDecimalConverter(new BigDecimal(
						0.0d));
				ConvertUtils.register(bdConverter, BigDecimal.class);
				BeanUtils.copyProperties(tt, t);

				tt.setFromName(from.getAccountName());
				tt.setToName(to.getAccountName());

				if (tt.getParentTxId() != null
						&& tt.getParentTxId().longValue() != 0) {
					children.add(tt);
				}

				s.save(tt);
				if (i % 20 == 0) {
					s.flush();
					s.clear();
				}
			}
			updateParentInfo(s, children);

			String query2 = "select count(*) from TT";
			Query q2 = s.createQuery(query2);
			Long val = Long.valueOf(q2.uniqueResult().toString());
			s.getTransaction().commit();
			long stop = System.currentTimeMillis();
			logger.info("Temp tables initialized in " + (stop - start)
					+ " msecs. Loaded " + val + " rows out of " + sz);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	private void updateParentInfo(Session s, List<TT> children)
			throws Exception {
		if (children == null || children.size() == 0)
			return;

		String query = "update TT R set R.isParent=" + TT.IsParent.YES.getVal()
				+ " where R.txId in(<inlist>)";
		int sz = children.size();
		StringBuilder inlist = new StringBuilder();
		for (int i = 0; i < sz; i++) {
			TT t = children.get(i);
			long parentId = t.getParentTxId();
			inlist.append(String.valueOf(parentId));
			if (i < sz - 1) {
				inlist.append(",");
			}
		}
		query = query.replaceAll("<inlist>", inlist.toString());
		logger.info(query);
		Query q = s.createQuery(query);
		int upd = q.executeUpdate();
		logger.info("Updated parent info for " + upd + " transactions");
	}

	public static boolean userExists(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			String query = "select uid from User R where R.uid=?";

			Query q = s.createQuery(query);
			q.setLong(0, uid);

			Long l = (Long) q.uniqueResult();

			s.getTransaction().commit();

			return l != null;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addUser(User u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.save(u);
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

	public void addTransaction(ActionResponse resp, Transaction t)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			t.setTxId(id);
			s.save(t);
			s.getTransaction().commit();
			resp.setErrorCode(ActionResponse.NOERROR);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addAccount(User u, Account a) throws Exception {
		fManAccountEntityManager.addAccount(u, a);
	}

	public void addNetWorthHistory(NetWorthHistory u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			String query = "delete from NetWorthHistory R where R.uid=? and R.date=?";

			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
			q.setDate(1, u.getDate());

			q.executeUpdate();

			long id = IDGenerator.getInstance().generateId(s);
			u.setId(id);
			s.save(u);

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

	public User getUser(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from User R where R.uid=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			User u = (User) q.uniqueResult();
			s.getTransaction().commit();
			return u;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public User getUser(String uname) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from User R where R.userName=?";
			Query q = s.createQuery(query);
			q.setString(0, uname);
			User u = (User) q.uniqueResult();
			s.getTransaction().commit();
			return u;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Account getAccount(Session s, long uid, long aid) throws Exception {
	return fManAccountEntityManager.getAccount(s,uid,aid);
          
	}

	@SuppressWarnings("unchecked")
	public List<Account> getAccount(long uid, int acctType, String acctName,
			String acctNumber) throws Exception {
        return fManAccountEntityManager.getAccount(uid,acctType,acctName,acctNumber);
		
	}

	/**
	 * Note: This and the getAccountFromNumber set the accountName attribute as
	 * encrypted string because these will be called from the XMLProcessor for
	 * importing OFX data. This is a special case, there is no need to encrypt
	 * data since it will be done automatically
	 */
	public Account getAccount(long uid, String acctName) throws Exception {
             return fManAccountEntityManager.getAccount(uid,acctName);
		
	}

	/**
	 * Note: This and the getAccountFromNumber set the accountName attribute as
	 * encrypted string because these will be called from the XMLProcessor for
	 * importing OFX data. This is a special case, there is no need to encrypt
	 * data since it will be done automatically
	 */
	public Account getAccountFromNumber(long uid, String acctNum)
			throws Exception {
             return fManAccountEntityManager.getAccountFromNumber(uid,acctNum);
		
	}

	public Account getAccountFromName(long uid, int acctType, String acctName)
			throws Exception {
            return fManAccountEntityManager.getAccountFromName(uid,acctType,acctName);
		
	}

	public Account getAccount(long uid, long aid) throws Exception {
		 return fManAccountEntityManager.getAccount(uid,aid);
	}

	public List getAccountNames(User u, int accountType) throws Exception {
            return fManAccountEntityManager.getAccountNames(u,accountType);
		
	}

	public List getAccountNames(User u) throws Exception {
		return fManAccountEntityManager.getAccountNames(u);
	}

	public List getAccountNames(long uid) throws Exception {
            return fManAccountEntityManager.getAccountNames(uid);
		
	}

	public List getAccountsForUser(long uid) throws Exception {
		return fManAccountEntityManager.getAccountsForUser(uid);
	}

	public List getAccountsForUser(User u) throws Exception {
		return fManAccountEntityManager.getAccountsForUser(u);
	}

	public List getAccountsForUser(User u, int acctType) throws Exception {
		return fManAccountEntityManager.getAccountsForUser(u,acctType);
	}

	public List getAccountsForUser(long uid, int acctType) throws Exception {
		return fManAccountEntityManager.getAccountsForUser(uid,acctType);
	}

	public int deleteAccount(long uid, long accountid) throws Exception {
		return fManAccountEntityManager.deleteAccount(uid,accountid);
	}

	/*
	 * Not commiting data at this point because this method will be called as
	 * part of AddTransaction action
	 */
	public int deleteTransactions(Session s, long uid, long acctid)
			throws Exception {
		try {
			String query = "delete from Transaction T where T.initiatorId=? and T.fromAccountId=? or T.toAccountId=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, acctid);
			q.setLong(2, acctid);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public int updateChildrenTx(Session s, long oldTxId, long newTxId)
			throws Exception {
		try {
			String query = "update Transaction R set R.parentTxId=? where R.parentTxId=?";
			Query q = s.createQuery(query);
			q.setLong(0, newTxId);
			q.setLong(1, oldTxId);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public int updateChildrenTT(Session s, long oldTxId, long newTxId)
			throws Exception {
		try {
			String query = "update TT R set R.parentTxId=? where R.parentTxId=?";
			Query q = s.createQuery(query);
			q.setLong(0, newTxId);
			q.setLong(1, oldTxId);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public Integer getNumChildren(Session s, Long txId) throws Exception {
		try {
			String query = "select count(R.txId) from Transaction R where R.parentTxId=?";
			Query q = s.createQuery(query);
			q.setLong(0, txId);
			Integer count = (Integer) q.uniqueResult();
			return count;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public int deleteTransaction(Session s, long txId) throws Exception {
		try {
			String query = "delete from Transaction T where T.txId=?";
			Query q = s.createQuery(query);
			q.setLong(0, txId);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public List getTransactions(User u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? order by R.txDate desc";
			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
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

	public long getTransactionsCount(long uid, long accountId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select count(R.txId) from Transaction R "
					+ "where R.initiatorId=? and (R.fromAccountId=? or R.toAccountId=?)";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, accountId);
			q.setLong(2, accountId);
			long count = (Long) q.uniqueResult();
			// System.out.println("Count = " + count);
			return count;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List getNetWorthHistory(User u, String from, String to)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			String query = "select R from NetWorthHistory R where R.uid=? and R.date >= ? and R.date <= ? order by R.date";

			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
			q.setString(1, from);
			q.setString(2, to);
			List l = q.list();

			for (int i = 0; i < l.size(); i++) {
				NetWorthHistory hist = (NetWorthHistory) l.get(i);
			}
			return l;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List getTransactions(User u, String dt, int status) throws Exception {
		Session s = null;
		try {

			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and R.txDate <= ? and R.txStatus=? order by R.txDate desc";
			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
			q.setString(1, dt);
			q.setInteger(2, status);
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

	// Do not close session here. This method will be called in middle of a
	// transaction
	public List getTransactionsTo(long uid, long accountId, String fromDate,
			String toDate) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and R.txDate >= ? and R.txDate <= ?"
					+ "and R.toAccountId=? order by R.txDate desc";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setString(1, fromDate);
			q.setString(2, toDate);

			q.setLong(3, accountId);
			return q.list();
		}
		catch (Exception e) {
			throw e;
		}
	}

	public List getTransactions(User u, String fromDate, String toDate,
			int status) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and R.txDate >= ? and R.txDate <= ?"
					+ "and R.txStatus=? order by R.txDate desc";

			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());

			q.setString(1, fromDate);
			q.setString(2, toDate);

			q.setInteger(3, status);
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
	public List<Transaction> getTransactionsForAccount(long userId,
			long accountId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and "
					+ "(R.fromAccountId=? or R.toAccountId=?) order by R.txDate desc";

			Query q = s.createQuery(query);
			q.setLong(0, userId);
			q.setLong(1, accountId);
			q.setLong(2, accountId);

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

	public void updateTransactionAmount(long txId, BigDecimal val)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			Transaction t = (Transaction) s.load(Transaction.class,
					Long.valueOf(txId));
			TT tt = (TT) s.load(TT.class, Long.valueOf(txId));

			t.setTxAmount(val);
			tt.setTxAmount(val);

			s.update(t);
			s.update(tt);

			s.getTransaction().commit();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Transaction getTransaction(User u, long txId) throws Exception {
		return getTransaction(u.getUid(), txId);
	}

	public Transaction getTransaction(long uid, long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and R.txId=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, txId);
			return (Transaction) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List<Transaction> getChildTransactions(User u, long txId)
			throws Exception {
		return getChildTransactions(u.getUid(), txId);
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getChildTransactions(long uid, long txId)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Transaction R where R.initiatorId=? and R.parentTxId=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, txId);
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
	public List<TT> getChildTT(long uid, long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from TT R where R.initiatorId=? and R.parentTxId=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, txId);
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

	public boolean fitIdExists(Session s, long userid, long fromAcctId,
			long toAcctId, String fitid) throws Exception {
		try {
			// String query =
			// "select R.txId from Transaction R where R.initiatorId=? " +
			// "and (R.fromAccountId=? or R.toAccountId=?) and R.fitid=?";

			String query = "select R.txId from Transaction R where R.initiatorId=? "
					+ "and R.fromAccountId=? and R.toAccountId=? and R.fitid=?";

			Query q = s.createQuery(query);
			q.setLong(0, userid);
			q.setLong(1, fromAcctId);
			q.setLong(2, toAcctId);
			q.setString(3, fitid);

			Long txid = (Long) q.uniqueResult();
			// if(txid == null) {
			// System.out.println("new");
			// }
			// System.out.println(txid);
			return txid != null;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public Transaction getTransaction(Session s, User u, long txId)
			throws Exception {
		try {
			String query = "select R from Transaction R where R.initiatorId=? and R.txId=?";
			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
			q.setLong(1, txId);
			return (Transaction) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
	}

	public List getAccountCategories(long uid) throws Exception {
            return fManAccountEntityManager.getAccountCategories(uid);
		
	}

	public List getChildrenAccountCategories(long uid, long catid)
			throws Exception {
             return fManAccountEntityManager.getChildrenAccountCategories(uid,catid);
		
	}

	public Long getNextAccountCategoryId() throws Exception {
		return fManAccountEntityManager.getNextAccountCategoryId();
	}

	public boolean addAccountCategory(AccountCategory c) throws Exception {
		return fManAccountEntityManager.addAccountCategory(c);
	}

	public void updateAccountCategory(AccountCategory c) throws Exception {
		fManAccountEntityManager.updateAccountCategory(c);
	}

	public boolean deleteAccountCategory(AccountCategory c) throws Exception {
		return fManAccountEntityManager.deleteAccountCategory(c);
	}

	public boolean deleteAccountCategory(ArrayList<AccountCategory> list)
			throws Exception {
		return fManAccountEntityManager.deleteAccountCategory(list);
	}

	

	public int updateAccountForCategory(Long accountId, Long newcategory)
			throws Exception {
		return fManAccountEntityManager.updateAccountForCategory(accountId,newcategory);
	}

	public void updateAccountNumber(Long accountid, String anumber)
			throws Exception {
		fManAccountEntityManager.updateAccountNumber(accountid, anumber);
	}

	public static boolean isCategoryPopulated(long catid) throws Exception {
		return FManAccountEntityManager.isCategoryPopulated(catid);
	}

	public static String getAccountNameFromId(long aid, boolean closeSession)
			throws Exception {
		return FManAccountEntityManager.getAccountNameFromId(aid,closeSession);
	}

	public static String getAccountName(long aid) throws Exception {
		return FManAccountEntityManager.getAccountName(aid);
	}

	public static String getCategoryName(long aid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.categoryName from AccountCategory R where R.categoryId=(select A.categoryId from Account A where A.accountId=?)";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			String aname = (String) q.uniqueResult();
			s.getTransaction().commit();
			return aname;
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
	public static List<AccountCategory> getRootCategoies() throws Exception {
		return FManAccountEntityManager.getRootCategoies();
	}
        public int updateAccountForCategory(Session s, Long oldcategory,
			Long newcategory) throws Exception {
		return fManAccountEntityManager.updateAccountForCategory(s,oldcategory,newcategory);
	}
	public static AccountCategory getRootCategory(String type) throws Exception {
		return FManAccountEntityManager.getRootCategory(type);
	}

	public static String getAccountNumber(long aid) throws Exception {
		return FManAccountEntityManager.getAccountNumber(aid);
	}

	public static boolean accountExists(long uid, int acctType, String acctName)
			throws Exception {
            return FManAccountEntityManager.accountExists(uid,acctType,acctName);
		
	}

	public static double getCurrentBalance(long aid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.currentBalance from Account R where R.accountId=?";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			double aname = (Double) q.uniqueResult();
			s.getTransaction().commit();
			return aname;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addContact(Contact u, boolean existing) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			if (existing) {
				deleteContact(s, u.getId());
			}
			long id = IDGenerator.getInstance().generateId(s);
			u.setId(id);
			s.save(u);
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

	public int deleteContact(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			int r = deleteContact(s, id);
			s.getTransaction().commit();
			return r;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
	}

	private int deleteContact(Session s, long id) throws Exception {
		try {
			String query = "delete from Contact R where R.id=?";
			Query q = s.createQuery(query);
			q.setLong(0, id);
			return q.executeUpdate();
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
	}

	public List getContacts(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Contact R where R.userId=? order by R.fullName";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
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

	public Contact getContact(long cid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Contact R where R.id=? order by R.fullName";

			Query q = s.createQuery(query);
			q.setLong(0, cid);
			return (Contact) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addONLB(ONLBDetails u, boolean existing) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			if (existing) {
				deleteOnlb(s, u.getId());
			}
			long id = IDGenerator.getInstance().generateId(s);
			u.setId(id);
			s.save(u);
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

	private int deleteOnlb(Session s, long id) throws Exception {
		try {
			String query = "delete from ONLBDetails R where R.id=?";
			Query q = s.createQuery(query);
			q.setLong(0, id);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
	}

	public ONLBDetails getONLBFromAccountId(long acctid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from ONLBDetails R where R.accountId=?";

			Query q = s.createQuery(query);
			q.setLong(0, acctid);

			ONLBDetails a = (ONLBDetails) q.uniqueResult();
			return a;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public ONLBDetails getONLB(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from ONLBDetails R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			return (ONLBDetails) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List getPortfolio(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Portfolio R where R.uid=?";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
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

	public long addPortfolio(Portfolio u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			u.setId(id);
			s.save(u);
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

	public long addPortfolioEntry(PortfolioEntry u) throws Exception {
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

	public int deletePortfolioEntry(PortfolioEntry u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();

			String query = "delete from PortfolioEntry R where R.id=?";
			s.beginTransaction();
			Query q = s.createQuery(query);
			q.setLong(0, u.getId());
			int r = q.executeUpdate();
			s.getTransaction().commit();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List getPortfolioEntries(long pid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from PortfolioEntry R where R.portfolioId=? order by R.id";

			Query q = s.createQuery(query);
			q.setLong(0, pid);
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

	public PortfolioEntry getPortfolioEntry(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from PortfolioEntry R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			return (PortfolioEntry) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Long getPortfolioEntryExists(long pid, String name) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.id from PortfolioEntry R where R.portfolioId=? and R.name=?";

			Query q = s.createQuery(query);
			q.setLong(0, pid);
			q.setString(1, name);

			s.getTransaction().commit();
			return (Long) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public PortfolioEntry getPortfolioEntry(long pid, String name)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from PortfolioEntry R where R.portfolioId=? and R.name=?";

			Query q = s.createQuery(query);
			q.setLong(0, pid);
			q.setString(1, name);
			return (PortfolioEntry) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public int deleteFutureTransaction(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "delete from FutureTransaction R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			int ret = q.executeUpdate();
			s.getTransaction().commit();
			return ret;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List getFutureTransaction(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from FutureTransaction R where R.uid=? and R.nextRunDate = ? order by R.id";

			GregorianCalendar today = new GregorianCalendar();
			// String todayStr = DateUtils.calendarToString(today,
			// DateUtils.FORMAT_YYYYMMDD);

			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setDate(1, today.getTime());
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

	public List listFutureTransactions(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select F.id, A.accountName as from, "
					+ "B.accountName as to, T.txAmount, F.nextRunDate, "
					+ "F.endDate  from FutureTransaction F,  "
					+ "Account A, Account B, Transaction T  "
					+ "where F.uid=? and F.transactionId=T.txId and "
					+ "T.fromAccountId = A.accountId and T.toAccountId=B.accountId";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
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

	public void addFavourite(Favourites fav) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			fav.setId(id);
			s.save(fav);
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

	public int deleteFavourites(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "delete from Favourites R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			int ret = q.executeUpdate();
			s.getTransaction().commit();
			return ret;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public List<Favourites> getFavourites(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Favourites R where R.uid=? order by R.name";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
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

	public TxDecorator getTxDecorator(long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from TxDecorator R where R.txId=?";

			Query q = s.createQuery(query);
			q.setLong(0, txId);
			return (TxDecorator) q.uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public boolean isParent(long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select count(R) from TT R where R.parentTxId=?";

			Query q = s.createQuery(query);
			q.setLong(0, txId);
			List l = q.list();
			return l.size() != 0;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addTxDecorator(TxDecorator dec) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.saveOrUpdate(dec);
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

	public int deleteTxDecorator(long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "delete from TxDecorator R where R.txId=?";

			Query q = s.createQuery(query);
			q.setLong(0, txId);

			int r = q.executeUpdate();

			s.getTransaction().commit();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void addBudget(Budget b) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			b.setId(id);
			s.save(b);
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

	public void updateBudget(Budget b) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.update(b);
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

	public void deleteBudget(Budget b) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.delete(b);
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

	public List<Budget> getBudgets(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Budget R where R.uid=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			List<Budget> l = Collections.checkedList(q.list(), Budget.class);
			s.getTransaction().commit();
			return l;
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
	public static List<AttachmentRef> getAttachmentId(long txId)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from AttachmentRef R where R.key=?";
			Query q = s.createQuery(query);
			q.setLong(0, txId);
			List<AttachmentRef> ret = q.list();
			// System.out.println("Num attachments for " + txId + " = " +
			// ret.size());
			s.getTransaction().commit();
			return ret;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public static TT getTT(long txId) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			String query = "select R from TT R where R.txId=?";
			Query q = s.createQuery(query);
			q.setLong(0, txId);
			TT tt = (TT) q.uniqueResult();
			s.getTransaction().commit();
			return tt;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	/* General utility methods */
	public void addObject(String tableName, FManEntity entity) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			entity.setPK(id);
			s.save(entity);
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

	public void updateObject(String tableName, FManEntity entity)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.update(entity);
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

	public void saveOrUpdateObject(String tableName, FManEntity entity)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.saveOrUpdate(entity);
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

	public int deleteObject(String name, String filter) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String q = "delete from " + name + " P ";
			if (filter != null) {
				q = q + " where P." + filter;
			}
			Query query = s.createQuery(q);
			int ret = query.executeUpdate();
			s.getTransaction().commit();
			return ret;
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
	public List<Object> getObjects(String name, String filter) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String q = "select P from " + name + " P ";
			if (filter != null) {
				q = q + " where P." + filter;
			}
			return s.createQuery(q).list();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Object getObject(String name, String filter) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String q = "select P from " + name + " P ";
			if (filter != null) {
				q = q + " where P." + filter;
			}
			return s.createQuery(q).uniqueResult();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}
	/**/
}
