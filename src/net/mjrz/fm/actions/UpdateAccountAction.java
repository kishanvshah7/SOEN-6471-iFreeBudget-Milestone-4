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

import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;

import org.hibernate.Session;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class UpdateAccountAction {
	public UpdateAccountAction() {

	}

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		Session s = null;
		try {
			User user = req.getUser();
			ActionResponse resp = new ActionResponse();

			Account account = (Account) req.getProperty("ACCOUNT");
			Boolean validate = (Boolean) req.getProperty("VALIDATENAME");

			if (validate != null) {
				if (validate
						&& FManEntityManager.accountExists(user.getUid(),
								account.getAccountType(),
								account.getAccountName()) == false) {
					if (account.getAccountName().length() < 30) {
						s = HibernateUtils.getSessionFactory()
								.getCurrentSession();
						s.beginTransaction();
						s.update(account);
						checkAlert(s, account, new AlertsEntityManager());
						s.getTransaction().commit();
						resp.setErrorCode(ActionResponse.NOERROR);
					}
					else {
						resp.setErrorCode(ActionResponse.GENERAL_ERROR);
						resp.setErrorMessage("Invalid account name. Maximum 30 characters");
					}
				}
				else {
					resp.setErrorCode(ActionResponse.ACCOUNT_EXISTS_ADD_FAIL);
					resp.setErrorMessage("Account with same name already exists in the category");
				}
			}
			else {
				s = HibernateUtils.getSessionFactory().getCurrentSession();
				s.beginTransaction();
				s.update(account);
				checkAlert(s, account, new AlertsEntityManager());
				s.getTransaction().commit();
				resp.setErrorCode(ActionResponse.NOERROR);
			}
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (s != null) {
				HibernateUtils.closeSession();
			}
		}
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

	public boolean validate() throws Exception {
		return false;
	}
}
