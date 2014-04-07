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

import java.util.List;

import net.mjrz.fm.entity.beans.CurrencyMonitor;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.entity.utils.IDGenerator;

import org.hibernate.Query;
import org.hibernate.Session;

public class CurrencyEntityManager {

	public void addCurrencyMonitor(CurrencyMonitor u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
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

	public boolean currencyMonitorExists(CurrencyMonitor u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select count(*) from CurrencyMonitor R where R.country=?";
			Query q = s.createQuery(query);
			q.setString(0, u.getCountry());
			int rows = (Integer) q.uniqueResult();
			s.getTransaction().commit();
			return rows != 0;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public void updateCurrencyMonitor(CurrencyMonitor c) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			if (c.getId() == 0)
				throw new NullPointerException();

			s.update(c);
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

	public void deleteCurrencyMonitor(CurrencyMonitor u) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			s.delete(u);
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

	public int deleteCurrencyMonitor(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "delete from CurrencyMonitor R where R.id=?";
			Query q = s.createQuery(query);
			q.setLong(0, id);
			int rows = q.executeUpdate();
			s.getTransaction().commit();
			return rows;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	public CurrencyMonitor getMonitoredCurrency(long id) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from CurrencyMonitor R where R.id=?";

			Query q = s.createQuery(query);
			q.setLong(0, id);
			return (CurrencyMonitor) q.uniqueResult();
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
	public List getMonitoredCurrencies(long pid) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();
			String query = "select R from CurrencyMonitor R where R.ownerid=? order by R.code";

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
}
