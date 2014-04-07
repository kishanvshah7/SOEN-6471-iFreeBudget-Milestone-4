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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.search.newfilter.Filter;
import net.mjrz.fm.search.newfilter.NewFilterUtils;
import net.mjrz.fm.search.newfilter.Order;

import org.hibernate.Query;
import org.hibernate.Session;

public class GetBalanceHistoryAction {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			Account account = (Account) request.getProperty("ACCOUNT");
			Integer index = (Integer) request.getProperty("INDEX");
			Integer pageSize = (Integer) request.getProperty("PAGE_SIZE");

			ActionResponse resp = new ActionResponse();

			Filter f = NewFilterUtils.getAccountFilter(account);
			f.addOrder(new Order("Date", Order.ASC));
			f.addOrder(new Order("Amount", Order.DESC));

			Query q = buildQueryFromFilter(s, f, index, pageSize);
			List txList = q.list();
			ArrayList<String> dates = new ArrayList<String>();
			ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();

			getResponseObjects(account, txList, dates, values);

			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("DATES", dates);
			resp.addResult("VALUES", values);
			resp.addResult("TXLIST", txList);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null)
				HibernateUtils.closeSession();
		}
	}

	private void getResponseObjects(Account account, List<TT> txList,
			ArrayList<String> dates, ArrayList<BigDecimal> values) {
		long acctId = account.getAccountId();
		int sz = txList.size();
		for (int i = 0; i < sz; i++) {
			TT t = (TT) txList.get(i);
			if (t.getFromAccountId() == acctId) {
				values.add(t.getFromAccountEndingBal());
			}
			else {
				values.add(t.getToAccountEndingBal());
			}
			dates.add(sdf.format(t.getTxDate()));
		}
	}

	private Query buildQueryFromFilter(Session s, Filter f, Integer index,
			Integer pageSize) throws Exception {
		if (index == null || pageSize == null) {
			return f.getQueryObject(s, false).setFirstResult(0)
					.setMaxResults(10);
		}
		Query q = f.getQueryObject(s, false).setFirstResult(index)
				.setMaxResults(pageSize);
		return q;
	}
}
