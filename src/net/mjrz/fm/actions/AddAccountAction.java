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
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.FManEntity;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddAccountAction {
	public AddAccountAction() {

	}

	public ActionResponse executeAction(User user, FManEntity entity)
			throws Exception {
		try {
			Account account = (Account) entity;
			account.setOwnerId(user.getUid());
			FManEntityManager em = new FManEntityManager();
			ActionResponse resp = new ActionResponse();
			if (validate(em, user, account, resp)) {
				em.addAccount(user, account);
				resp.addResult("ACCOUNT", account);
				resp.setErrorCode(ActionResponse.NOERROR);
			}
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}

	public boolean validate(FManEntityManager em, User u, Account a,
			ActionResponse resp) throws Exception {
		if (a.getAccountName() == null || a.getAccountName().length() > 30) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Invalid account name. Maximum 30 characters");
			return false;
		}
		List<Account> aList = em.getAccount(u.getUid(), a.getAccountType(),
				a.getAccountName(), a.getAccountNumber());
		if (aList != null && aList.size() > 0) {
			for (Account chk : aList) {
				if (chk.getCategoryId().equals(a.getCategoryId())) {
					resp.setErrorCode(ActionResponse.ACCOUNT_EXISTS_ADD_FAIL);
					resp.setErrorMessage("Account with same name or number "
							+ "already exists in the category");
					return false;
				}
			}
		}
		return true;
	}
}
