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

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.ONLBDetails;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddONLBDetailsAction {
	public AddONLBDetailsAction() {

	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			ONLBDetails c = (ONLBDetails) request.getProperty("ONLB");

			boolean b = false;
			FManEntityManager em = new FManEntityManager();
			ONLBDetails tmp = em.getONLBFromAccountId(c.getAccountId());
			if (tmp != null) {
				b = true;
				c.setId(tmp.getId());
			}
			else
				b = false;

			em.addONLB(c, b);
			resp.setErrorCode(ActionResponse.NOERROR);
			return resp;
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage(e.getMessage());
			throw e;
		}
	}
}
