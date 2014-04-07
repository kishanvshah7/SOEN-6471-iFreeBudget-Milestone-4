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
import net.mjrz.fm.utils.AlertsCache;

public class RemoveAlertAction {
	public RemoveAlertAction() {
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			AlertsEntityManager em = new AlertsEntityManager();
			Long accountId = (Long) request.getProperty("ACCOUNTID");

			long c = em.deleteAlert(accountId);
			resp.addResult("DELTECOUNT", c);
			AlertsCache.getInstance().removeAlert(accountId);
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Unable to remove alert, reason = "
					+ e.getMessage());
		}
		return resp;
	}
}
