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
public class GetNetWorthHistoryAction {
	public static final int HISTORY_10DAYS = 1;
	public static final int HISTORY_10WEEKS = 2;
	public static final int HISTORY_10MONTHS = 3;

	public GetNetWorthHistoryAction() {

	}

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		try {
			User u = req.getUser();

			String fromDt = (String) req.getProperty("FROM_DATE");
			String toDt = (String) req.getProperty("TO_DATE");

			FManEntityManager em = new FManEntityManager();
			List hist = em.getNetWorthHistory(u, fromDt, toDt);

			// System.out.println("NetWorth history: " + hist.size());

			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.setResultList(hist);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
