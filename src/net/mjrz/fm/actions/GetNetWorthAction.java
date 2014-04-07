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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetNetWorthAction {
	public GetNetWorthAction() {

	}

	public ActionResponse executeAction(ActionRequest req) throws Exception {
		try {
			User u = req.getUser();
			FManEntityManager em = new FManEntityManager();
			List accounts = em.getAccountsForUser(u);

			BigDecimal liabs = new BigDecimal(0.0d);
			BigDecimal assets = new BigDecimal(0.0d);
			BigDecimal total = new BigDecimal(0.0d);

			Iterator it = accounts.iterator();
			while (it.hasNext()) {
				Account a = (Account) it.next();
				if (a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
					liabs = liabs.add(a.getCurrentBalance());
				}
				if (a.getAccountType() == AccountTypes.ACCT_TYPE_CASH) {
					assets = assets.add(a.getCurrentBalance());
				}
			}
			total = assets.subtract(liabs);
			ActionResponse resp = new ActionResponse();
			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("ASSET_VALUE", assets);
			resp.addResult("LIAB_VALUE", liabs);
			resp.addResult("NET_VALUE", total);
			return resp;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
