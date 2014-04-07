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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.BudgetedAccount;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.utils.MiscUtils;

public class GetBudgetSummaryAction {
	private static Logger logger = Logger.getLogger(GetBudgetSummaryAction.class
			.getName());
	
	public GetBudgetSummaryAction() {
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			Budget b = (Budget) request.getProperty("BUDGET");
			User u = request.getUser();
			Date d = (Date) request.getProperty("DATE");

			GregorianCalendar start = new GregorianCalendar();
			start.setTime(d);
			start.set(Calendar.HOUR_OF_DAY, 0);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.SECOND, 0);

			GregorianCalendar end = new GregorianCalendar();
			end.setTime(d);
			end.set(Calendar.HOUR_OF_DAY, 23);
			end.set(Calendar.MINUTE, 59);
			end.set(Calendar.SECOND, 59);

			if (b.getType() == Budget.MONTHLY) {
				start.set(Calendar.DAY_OF_MONTH, 1);
				int max = end.getActualMaximum(Calendar.DAY_OF_MONTH);
				end.set(Calendar.DAY_OF_MONTH, max);
			}
			else {
				adjustWeekDates(start, end);
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
			
			String startS = sdf.format(start.getTime());
			String endS = sdf.format(end.getTime());

			FManEntityManager em = new FManEntityManager();

			Set<BudgetedAccount> set = b.getAccounts();
			for (BudgetedAccount a : set) {
				long acctId = a.getAccountId();
				List txList = em.getTransactionsTo(u.getUid(), acctId, startS,
						endS);
				BigDecimal actual = new BigDecimal(0);
				if (txList != null) {
					for (Object o : txList) {
						Transaction t = (Transaction) o;
						BigDecimal amt = t.getTxAmount();
						actual = actual.add(amt);
					}
					a.setActualAmount(actual);
				}
			}
			resp.addResult("BUDGET", b);
			resp.addResult("START", start.getTime());
			resp.addResult("END", end.getTime());
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Unable to add alert, reason = "
					+ e.getMessage());
		}
		return resp;
	}

	private void adjustWeekDates(GregorianCalendar st, GregorianCalendar en) {
		int dow = st.get(Calendar.DAY_OF_WEEK);
		int toAdd = st.getActualMaximum(Calendar.DAY_OF_WEEK);
		toAdd = toAdd - dow;
		for (int i = 1; i < dow; i++) {
			st.add(Calendar.DATE, -1);
		}
		for (int i = 0; i < toAdd; i++) {
			en.add(Calendar.DATE, 1);
		}
	}
}
