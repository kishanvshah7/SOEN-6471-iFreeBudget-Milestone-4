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
package net.mjrz.fm.entity.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.mjrz.fm.entity.beans.UniqueID;

import org.hibernate.Session;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public final class IDGenerator {
	private ConcurrentLinkedQueue<Long> queue = null;

	private static final int CACHE_SZ = 10;
	private static final int START = 100;
	private static final String DEFAULT_TB = "global_tb";

	private static final IDGenerator instance = new IDGenerator();

	public static IDGenerator getInstance() {
		return instance;
	}

	private IDGenerator() {
		queue = new ConcurrentLinkedQueue<Long>();
	}

	private synchronized Long getNextUid(String tableName) {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().openSession();
			s.beginTransaction();

			String query = "select R from UniqueID R where R.tableName='<table_name>'";
			query = query.replaceAll("<table_name>", tableName);

			UniqueID uniqId = (UniqueID) s.createQuery(query).uniqueResult();
			if (uniqId == null) {
				uniqId = new UniqueID();
				uniqId.setUid(Long.valueOf(START));
			}
			long ret = uniqId.getUid();

			Long newId = uniqId.getUid() + CACHE_SZ;

			// System.out.println("Current = " + ret + " updated to " + newId);

			uniqId.setUid(newId);
			uniqId.setTimestamp(System.currentTimeMillis());
			uniqId.setTableName(tableName);

			s.saveOrUpdate(uniqId);
			s.flush();
			s.getTransaction().commit();
			return ret;
		}
		finally {
			if (s != null) {
				s.disconnect();
				s.close();
			}
		}
	}

	public synchronized long generateId(Session s) throws Exception {
		try {
			if (queue.size() == 0) {
				Long next = getNextUid(DEFAULT_TB);
				for (int i = 0; i < CACHE_SZ; i++) {
					queue.offer(next);
					next++;
				}
			}
			Long val = queue.poll();
			return val;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
