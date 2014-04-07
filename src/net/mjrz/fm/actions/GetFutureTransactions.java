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

public class GetFutureTransactions {

	public GetFutureTransactions() {
	}

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		User user = req.getUser();
		long uid = user.getUid();
		ActionResponse resp = new ActionResponse();
		FManEntityManager em = new FManEntityManager();

		List list = em.listFutureTransactions(uid);
		if (list != null) {
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("FTLIST", list);
		}
		else {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Unable to retrieve scheduled transactions");
		}
		return resp;
	}
}
