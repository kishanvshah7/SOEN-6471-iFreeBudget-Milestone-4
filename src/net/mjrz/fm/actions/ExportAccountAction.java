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
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;

public abstract class ExportAccountAction {
	public ActionResponse executeAction(ActionRequest req) throws Exception {
		ActionResponse response = new ActionResponse();
		FManEntityManager em = new FManEntityManager();
		Account acct = (Account) req.getProperty("ACCOUNT");
		User u = req.getUser();
		String dest = (String) req.getProperty("DESTFILE");
		if (acct == null || u == null || acct.getAccountNumber() == null) {
			response.setErrorCode(ActionResponse.INVALID_FROM_ACCOUNT);
			response.setErrorMessage("Invalid account");
		}
		try {
			List<Transaction> txList = em.getTransactionsForAccount(u.getUid(),
					acct.getAccountId());
			writeFile(u.getUid(), acct, dest, txList);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	abstract void writeFile(long userId, Account exportAccount,
			String destFile, List<Transaction> txList) throws Exception;
}
