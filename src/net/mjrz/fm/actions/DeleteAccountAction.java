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

import java.util.Iterator;
import java.util.List;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.BudgetedAccount;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class DeleteAccountAction {
	private static Logger logger = Logger.getLogger(DeleteAccountAction.class
			.getName());

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			FManEntityManager em = new FManEntityManager();

			User u = req.getUser();

			long acctId = (Long) req.getProperty("ACCOUNTID");

			if (!canDelete(em, acctId)) {
				resp.setErrorCode(ActionResponse.ACCOUNT_DELETE_ERROR);
				resp.setErrorMessage("Account has transactions, "
						+ "please delete them first.");
				return resp;
			}

			int r = em.deleteAccount(u.getUid(), acctId);
			if (r > 0) {
				deleteAccountFromBudgets(em, acctId);
				resp.setErrorCode(ActionResponse.NOERROR);
				resp.addResult("NUMROWS", r);
			}
			else {
				resp.setErrorCode(ActionResponse.ACCOUNT_DELETE_ERROR);
				resp.setErrorMessage("Unable to delete account");
				return resp;
			}
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}

	private boolean canDelete(FManEntityManager em, long accountId)
			throws Exception {

		long count = em.getTransactionsCount(SessionManager.getSessionUserId(),
				accountId);

		logger.error("Account to be deleted has transactions. Not deleting");
		return count == 0;
	}

	private void deleteAccountFromBudgets(FManEntityManager em, long accountId) {
		try {
			List budgets = em.getBudgets(SessionManager.getSessionUserId());
			if (budgets != null) {
				for (Object o : budgets) {
					Budget b = (Budget) o;
					Iterator<BudgetedAccount> it = b.getAccounts().iterator();
					boolean isDirty = false;
					while (it.hasNext()) {
						BudgetedAccount a = it.next();
						if (a.getAccountId() == accountId) {
							it.remove();
							isDirty = true;
						}
					}
					if (isDirty) {
						em.updateBudget(b);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}
}
