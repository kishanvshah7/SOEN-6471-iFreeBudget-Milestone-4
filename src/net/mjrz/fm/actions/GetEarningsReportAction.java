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

/*******************************************************************************
 * Copyright 2008 Mjrz.net
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetEarningsReportAction {
	public GetEarningsReportAction() {

	}

	@SuppressWarnings("unchecked")
	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			FManEntityManager em = new FManEntityManager();
			String[] range = null;
			User u = request.getUser();
			range = (String[]) request.getProperty("DATERANGE");

			// range = getDateRange(cmd);

			List txList = em.getTransactions(u, range[0], range[1],
					AccountTypes.TX_STATUS_COMPLETED);
			HashMap<Long, ArrayList<Transaction>> map = new HashMap<Long, ArrayList<Transaction>>();

			BigDecimal totalSpending = new BigDecimal("0.0");
			BigDecimal totalIncome = new BigDecimal("0.0");

			for (int i = 0; i < txList.size(); i++) {
				Transaction t = (Transaction) txList.get(i);
				Account from = em.getAccount(u.getUid(), t.getFromAccountId());
				Account to = em.getAccount(u.getUid(), t.getToAccountId());

				if (to.getAccountType() == AccountTypes.ACCT_TYPE_EXPENSE) {
					totalSpending = totalSpending.add(t.getTxAmount());
				}
				if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
					totalIncome = totalIncome.add(t.getTxAmount());
					if (map.containsKey(t.getFromAccountId())) {
						map.get(t.getFromAccountId()).add(t);
					}
					else {
						ArrayList<Transaction> list = new ArrayList<Transaction>();
						list.add(t);
						map.put(t.getFromAccountId(), list);
					}
				}
			}

			BigDecimal amtSaved = totalIncome.subtract(totalSpending);

			BigDecimal mult = totalSpending.multiply(new BigDecimal("100"));
			Double pctSaved = 0d;
			if (totalIncome.intValue() != 0)
				pctSaved = mult.doubleValue() / totalIncome.doubleValue();
			else
				pctSaved = 100d;

			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("TOTALINCOME", totalIncome);
			resp.addResult("TOTALSPENDING", totalSpending);
			resp.addResult("SAVINGS", amtSaved);
			resp.addResult("PCTSAVINGS", pctSaved);
			resp.addResult("RANGE", range);

			resp.addResult("EXPENSELIST", map);
		}
		catch (Exception e) {
			e.printStackTrace();
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
		}

		return resp;
	}
}
