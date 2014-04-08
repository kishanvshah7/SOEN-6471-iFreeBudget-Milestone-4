/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Copyright   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package net.mjrz.fm.entity;


import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.beans.Account;
import org.hibernate.Session;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;
import net.mjrz.fm.entity.beans.AccountCategory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import net.mjrz.fm.utils.crypto.CHelper;
import org.hibernate.Query;

public class FManAccountEntityManager {
	public void addAccount(User u, Account a) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			long id = IDGenerator.getInstance().generateId(s);
			a.setAccountId(id);
			s.save(a);
			s.getTransaction().commit();
		} catch (Exception e) {
			throw e;
		} finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public Long getNextAccountCategoryId() throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			Long id = IDGenerator.getInstance().generateId(s);
			s.getTransaction().commit();
			return id;
		} catch (Exception e) {
			throw e;
		} finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public boolean addAccountCategory(AccountCategory c) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			if (c.getCategoryId() == null) {
				long id = IDGenerator.getInstance().generateId(s);
				c.setCategoryId(id);
			}
			java.io.Serializable ret = s.save(c);
			s.getTransaction().commit();
			return ret != null;
		} catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		} finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void updateAccountCategory(AccountCategory c) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			if (c.getCategoryId() == null)
				throw new NullPointerException();
			s.update(c);
			s.getTransaction().commit();
		} catch (Exception e) {
			throw e;
		} finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}
        
        public Account getAccount(Session s, long uid, long aid) throws Exception {
		try {
			String query = "select R from Account R where R.accountId=? and R.ownerId=?";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			q.setLong(1, uid);
			Account a = (Account) q.uniqueResult();
			return a;
		}
		catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Account> getAccount(long uid, int acctType, String acctName,
			String acctNumber) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			String query = "select R from Account R where R.ownerId=? and "
					+ "R.accountType=? and (R.accountName=? or R.accountNumber=?)";

			if (acctNumber == null || acctNumber.trim().length() == 0) {
				query = "select R from Account R where R.ownerId=? and "
						+ "R.accountType=? and R.accountName=?";
			}

			Query q = s.createQuery(query);

			q.setLong(0, uid);
			q.setInteger(1, acctType);
			q.setParameter(2, acctName);

			if (acctNumber != null && acctNumber.trim().length() > 0) {
				q.setParameter(3, acctNumber);
			}

			List<Account> a = q.list();
			s.getTransaction().commit();
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

	/**
	 * Note: This and the getAccountFromNumber set the accountName attribute as
	 * encrypted string because these will be called from the XMLProcessor for
	 * importing OFX data. This is a special case, there is no need to encrypt
	 * data since it will be done automatically
	 */
	public Account getAccount(long uid, String acctName) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Account R where R.ownerId=? and R.accountName=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setString(1, CHelper.encrypt(acctName));
			Account a = (Account) q.uniqueResult();
			s.getTransaction().commit();
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

	/**
	 * Note: This and the getAccountFromNumber set the accountName attribute as
	 * encrypted string because these will be called from the XMLProcessor for
	 * importing OFX data. This is a special case, there is no need to encrypt
	 * data since it will be done automatically
	 */
	public Account getAccountFromNumber(long uid, String acctNum)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Account R where R.ownerId=? and R.accountNumber=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setString(1, CHelper.encrypt(acctNum));
			Account a = (Account) q.uniqueResult();
			s.getTransaction().commit();
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

	public Account getAccountFromName(long uid, int acctType, String acctName)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Account R where R.ownerId=? and R.accountType=? and R.accountName=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setInteger(1, acctType);
			q.setString(2, acctName);
			Account a = (Account) q.uniqueResult();
			s.getTransaction().commit();
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

	public Account getAccount(long uid, long aid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from Account R where R.accountId=? and R.ownerId=?";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			q.setLong(1, uid);
			Account a = (Account) q.uniqueResult();
			s.getTransaction().commit();
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

	public List getAccountNames(User u, int accountType) throws Exception {
		Session s = null;
		try {
			String query = "select A.accountName from Account A where A.ownerId=? and A.accountType=?";
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			Query q = s.createQuery(query);
			q.setLong(0, u.getUid());
			q.setInteger(1, accountType);
			List ret = q.list();
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

	public List getAccountNames(User u) throws Exception {
		return getAccountNames(u.getUid());
	}

	public List getAccountNames(long uid) throws Exception {
		Session s = null;
		try {
			String query = "select A.accountName from Account A where A.ownerId=?";
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			List ret = q.list();
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

	public List getAccountsForUser(long uid) throws Exception {
		Session s = null;
		try {
			String query = "select R from Account R where R.ownerId=? order by R.accountType, R.accountName";
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
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

	public List getAccountsForUser(User u) throws Exception {
		return getAccountsForUser(u.getUid());
	}

	public List getAccountsForUser(User u, int acctType) throws Exception {
		return getAccountsForUser(u.getUid(), acctType);
	}

	public List getAccountsForUser(long uid, int acctType) throws Exception {
		Session s = null;
		try {
			String query = "select R from Account R where R.ownerId=? and R.accountType=?";
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setInteger(1, acctType);
			List<?> ret = q.list();
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

	public int deleteAccount(long uid, long accountid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();

			String query = "delete from Account R where R.accountId=?";
			s.beginTransaction();
                        FManEntityManager fm=new FManEntityManager();
			int r = fm.deleteTransactions(s, uid, accountid);
			Query q = s.createQuery(query);
			q.setLong(0, accountid);
			r = q.executeUpdate();
			s.getTransaction().commit();
			return r;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
	}
        
        public List getAccountCategories(long uid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from AccountCategory R where R.uid=? order by R.categoryId";

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

	public List getChildrenAccountCategories(long uid, long catid)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from AccountCategory R where R.uid=? and R.parentCategoryId=? order by R.categoryId";

			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setLong(1, catid);
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
        
        public boolean deleteAccountCategory(AccountCategory c) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			boolean result = true;

			String query = "delete from AccountCategory R where R.categoryId=? and R.uid=?";
			Query q = s.createQuery(query);
			q.setLong(0, c.getCategoryId());
			q.setLong(1, c.getUid());
			int r = q.executeUpdate();
			if (r != 1) {
				s.getTransaction().rollback();
			}
			else {
				s.getTransaction().commit();
			}
			return result;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public boolean deleteAccountCategory(ArrayList<AccountCategory> list)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			boolean result = true;

			String query = "delete from AccountCategory R where R.categoryId=? and R.uid=?";
			Query q = s.createQuery(query);
			for (AccountCategory c : list) {

				/*
				 * First move all accounts with this categoryid to parents
				 * categoryid
				 */
                          //  FManEntityManager em=new  FManEntityManager();
				int u = updateAccountForCategory(s, c.getCategoryId(),
						c.getParentCategoryId());
				if (u == 0)
					continue;

				q.setLong(0, c.getCategoryId());
				q.setLong(1, c.getUid());
				int r = q.executeUpdate();
				if (r != 1) {
					result = false;
					break;
				}
			}
			if (result)
				s.getTransaction().commit();
			else {
				s.getTransaction().rollback();
			}
			return result;
		}
		catch (Exception e) {
			s.getTransaction().rollback();
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	

	public int updateAccountForCategory(Long accountId, Long newcategory)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			if (accountId == null || newcategory == null)
				return -1;

			String query = "update Account R set R.categoryId=? where R.accountId=?";
			Query q = s.createQuery(query);
			q.setLong(0, newcategory);
			q.setLong(1, accountId);
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

	public void updateAccountNumber(Long accountid, String anumber)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			Account a = (Account) s
					.load(Account.class, Long.valueOf(accountid));
			a.setAccountNumber(anumber);
			// String query =
			// "update Account R set R.accountNumber=? where R.accountId=?";
			// Query q = s.createQuery(query);
			// q.setString(0, anumber);
			// q.setLong(1, accountid);
			// q.executeUpdate();
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
         public int updateAccountForCategory(Session s, Long oldcategory,
			Long newcategory) throws Exception {
		try {
			if (oldcategory == null || newcategory == null)
				return -1;

			String query = "update Account R set R.categoryId=? where R.categoryId=?";
			Query q = s.createQuery(query);
			q.setLong(0, newcategory);
			q.setLong(1, oldcategory);
			int r = q.executeUpdate();
			return r;
		}
		catch (Exception e) {
			throw e;
		}
	}
	public static boolean isCategoryPopulated(long catid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.accountId from Account R where R.categoryId=?";
			Query q = s.createQuery(query);
			q.setLong(0, catid);
			List l = q.list();
			s.getTransaction().commit();

			if (l == null)
				return false;

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

	public static String getAccountNameFromId(long aid, boolean closeSession)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.accountName from Account R where R.accountId=?";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			String aname = (String) q.uniqueResult();
			return aname;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (closeSession)
				if (s != null)
					HibernateUtils.closeSession();
		}
	}

	public static String getAccountName(long aid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.accountName from Account R where R.accountId=?";
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
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from AccountCategory R where R.parentCategoryId=?";
			Query q = s.createQuery(query);
			q.setLong(0, -1);
			List<AccountCategory> ret = (List<AccountCategory>) q.list();
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

	public static AccountCategory getRootCategory(String type) throws Exception {
		List<AccountCategory> l = getRootCategoies();
		if (l == null || l.size() == 0) {
			return null;
		}
		for (AccountCategory c : l) {
			if (c.getCategoryName().equals(type)) {
				return c;
			}
		}
		return null;
	}

	public static String getAccountNumber(long aid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.accountNumber from Account R where R.accountId=?";
			Query q = s.createQuery(query);
			q.setLong(0, aid);
			String anum = (String) q.uniqueResult();
			s.getTransaction().commit();
			return anum;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public static boolean accountExists(long uid, int acctType, String acctName)
			throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R.accountId from Account R where R.ownerId=? and R.accountType=? and R.accountName=?";
			Query q = s.createQuery(query);
			q.setLong(0, uid);
			q.setInteger(1, acctType);
			q.setString(2, acctName);
			Long a = (Long) q.uniqueResult();
			s.getTransaction().commit();
			return a != null;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	
        
}