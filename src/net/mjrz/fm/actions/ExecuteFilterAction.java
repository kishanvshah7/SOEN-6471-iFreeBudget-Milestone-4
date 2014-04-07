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

import java.util.List;

import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.search.newfilter.Filter;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ExecuteFilterAction {
	public ExecuteFilterAction() {

	}

	private void getCount(Session s, Filter filter, ActionResponse resp)
			throws Exception {
		Query q = filter.getQueryObject(s, true);
		Long count = Long.valueOf(q.uniqueResult().toString());
		resp.addResult("COUNT", count);
	}

	@SuppressWarnings("unchecked")
	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		Session s = null;
		try {
			s = HibernateUtils.getSessionFactory().getCurrentSession();
			s.beginTransaction();

			User u = request.getUser();

			Filter filter = (Filter) request.getProperty("FILTER");
			Long curr = (Long) request.getProperty("CURRIDX");
			Long size = (Long) request.getProperty("PAGE_SIZE");
			Boolean count = (Boolean) request.getProperty("COUNT");
			if (count != null && count) {
				getCount(s, filter, resp);
				return resp;

			}

			Query q = buildQueryFromFilter(s, filter, curr, size);
			List txList = q.list();
			resp.setErrorCode(ActionResponse.NOERROR);
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

	private Query buildQueryFromFilter(Session s, Filter f, Long currentIdx,
			Long size) throws Exception {
		if (currentIdx == null || size == null)
			return f.getQueryObject(s, false);

		Query q = f.getQueryObject(s, false)
				.setFirstResult(currentIdx.intValue())
				.setMaxResults(size.intValue());
		return q;
	}
}
