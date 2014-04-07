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

import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Alert;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.AlertsCache;

public class AddAlertAction {

	public AddAlertAction() {
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			AlertsEntityManager em = new AlertsEntityManager();
			Alert entry = (Alert) request.getProperty("ENTRY");

			Account a = new FManEntityManager().getAccount(
					SessionManager.getSessionUserId(), entry.getAccountId());

			// AlertsEntityManager.checkAlert(a, entry);

			em.addAlert(entry);
			resp.addResult("ENTRY", entry);

			AlertsCache.getInstance().addToCache(entry);
			AlertsEntityManager.checkAlert(a, entry);
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Unable to add alert, reason = "
					+ e.getMessage());
		}
		return resp;
	}
}
