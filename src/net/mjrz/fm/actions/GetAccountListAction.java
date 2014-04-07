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

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetAccountListAction {
	public GetAccountListAction() {

	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		try {
			User user = request.getUser();
			if (request.propertyExists("ACCOUNT_TYPE")) {
				String listType = (String) request.getProperty("LISTTYPE");
				int accountType = (Integer) request.getProperty("ACCOUNT_TYPE");
				if (listType != null && listType.equals("ACCOUNTS")) {
					return getAccountList(user, accountType);
				}
				else {
					FManEntityManager em = new FManEntityManager();
					List ret = em.getAccountNames(user, accountType);
					ActionResponse resp = new ActionResponse();
					resp.setErrorCode(ActionResponse.NOERROR);
					resp.setResultList(ret);
					return resp;
				}
			}
			return null;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public ActionResponse getAccountList(User user) throws Exception {
		try {
			FManEntityManager em = new FManEntityManager();
			List ret = em.getAccountsForUser(user);
			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("RESULTSET", ret);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public ActionResponse getAccountList(long userId) throws Exception {
		try {
			FManEntityManager em = new FManEntityManager();
			List ret = em.getAccountsForUser(userId);
			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("RESULTSET", ret);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public ActionResponse getAccountList(User user, int accountType)
			throws Exception {
		try {
			FManEntityManager em = new FManEntityManager();
			List ret = em.getAccountsForUser(user, accountType);
			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("RESULTSET", ret);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}

	// public ActionResponse executeAction(User user) throws Exception {
	// try {
	// FManEntityManager em = new FManEntityManager();
	// List<String> ret = em.getAccountNames(user);
	// ActionResponse resp = new ActionResponse();
	// resp.setErrorCode(ActionResponse.NOERROR);
	// resp.setResultList(ret);
	// return resp;
	// }
	// catch(Exception e) {
	// throw e;
	// }
	// }

	public boolean validate() {
		return false;
	}
}
