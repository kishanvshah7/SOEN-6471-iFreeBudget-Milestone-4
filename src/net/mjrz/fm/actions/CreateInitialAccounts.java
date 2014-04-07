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

import static net.mjrz.fm.utils.Messages.tr;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.utils.ImportCategoryIcon;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class CreateInitialAccounts {
	private static Logger logger = Logger.getLogger(CreateInitialAccounts.class
			.getName());

	@SuppressWarnings("unchecked")
	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		try {
			User u = (User) request.getUser();
			FManEntityManager em = new FManEntityManager();

			createBaseCategories(u, em);

			List l = em.getAccountCategories(u.getUid());

			for (int i = 0; i < l.size(); i++) {
				AccountCategory c = (AccountCategory) l.get(i);
				if (c.getCategoryName().equals(
						AccountTypes
								.getAccountType(AccountTypes.ACCT_TYPE_CASH))) {
					createCashSubCategories(u, c);
				}
				if (c.getCategoryName()
						.equals(AccountTypes
								.getAccountType(AccountTypes.ACCT_TYPE_EXPENSE))) {
					createExpenseSubCategories(u, c);
				}
				if (c.getCategoryName().equals(
						AccountTypes
								.getAccountType(AccountTypes.ACCT_TYPE_INCOME))) {
					createIncomeAccount(u, c);
				}
				if (c.getCategoryName()
						.equals(AccountTypes
								.getAccountType(AccountTypes.ACCT_TYPE_LIABILITY))) {
					createLiabilitySubCategories(c);
				}
			}
			return resp;

		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		return resp;
	}

	private void createIncomeAccount(User user, AccountCategory c) {
		try {
			Date now = new Date();
			Account a = new Account();
			a.setAccountName(tr("Primary Job"));
			a.setAccountNotes(tr("This is place holder for primary income account. You can rename/edit/delete this account"));
			a.setAccountType(AccountTypes.ACCT_TYPE_INCOME);
			a.setCurrentBalance(new BigDecimal(0d));
			a.setHighBalance(new BigDecimal(0d));
			a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
			a.setCategoryId(c.getCategoryId());
			a.setAccountParentType(a.getAccountType());
			a.setStartDate(now);
			a.setHighBalanceDate(now);
			new AddAccountAction().executeAction(user, a);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void createBaseCategories(User user, FManEntityManager em)
			throws Exception {
		AccountCategory i = new AccountCategory(user.getUid(),
				Long.valueOf(AccountTypes.ACCT_TYPE_INCOME),
				Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
		i.setCategoryName(AccountTypes
				.getAccountType(AccountTypes.ACCT_TYPE_INCOME));

		AccountCategory c = new AccountCategory(user.getUid(),
				Long.valueOf(AccountTypes.ACCT_TYPE_CASH),
				Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
		c.setCategoryName(AccountTypes
				.getAccountType(AccountTypes.ACCT_TYPE_CASH));

		AccountCategory e = new AccountCategory(user.getUid(),
				Long.valueOf(AccountTypes.ACCT_TYPE_EXPENSE),
				Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
		e.setCategoryName(AccountTypes
				.getAccountType(AccountTypes.ACCT_TYPE_EXPENSE));

		AccountCategory l = new AccountCategory(user.getUid(),
				Long.valueOf(AccountTypes.ACCT_TYPE_LIABILITY),
				Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));

		l.setCategoryName(AccountTypes
				.getAccountType(AccountTypes.ACCT_TYPE_LIABILITY));

		em.addAccountCategory(i);
		em.addAccountCategory(c);
		em.addAccountCategory(e);
		em.addAccountCategory(l);
	}

	private void createExpenseSubCategories(User user, AccountCategory c) {
		ImportCategoryIcon ici = new ImportCategoryIcon();

		ActionRequest req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Shopping"));
		req.setProperty("PARENTCATEGORY", c);

		ActionResponse addResponse = new AddCategoryAction().execute(req);

		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			Date now = new Date();

			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/groceries.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			try {
				Account a = new Account();
				a.setAccountName(tr("Groceries"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Clothes"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Dining"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/restaurant.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			Date now = new Date();
			try {
				Account a = new Account();
				a.setAccountName(tr("Restaurant"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Cafeteria at work"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Bills"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/bills.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			Date now = new Date();
			try {
				Account a = new Account();
				a.setAccountName(tr("Rent"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Wireless bill"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Internet and Tv"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Electricity"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
			try {
				Account a = new Account();
				a.setAccountName(tr("Heating"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Travel"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/globe.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			Date now = new Date();
			try {
				Account a = new Account();
				a.setAccountName(tr("Gas"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Insurance"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Maintenance"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Misc Purchases"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/misc.png");
			ici.importIcon(added, f);
		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Entertainment"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/entertainment.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			Date now = new Date();
			try {
				Account a = new Account();
				a.setAccountName(tr("Movies"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
			try {
				Account a = new Account();
				a.setAccountName(tr("Music and games"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("School"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory added = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			String f = String.valueOf("icons/ec/school.png");
			ici.importIcon(added, f);

			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");

			Date now = new Date();
			try {
				Account a = new Account();
				a.setAccountName(tr("Books"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

			try {
				Account a = new Account();
				a.setAccountName(tr("Supplies"));
				a.setAccountNotes("");
				a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
				a.setCurrentBalance(new BigDecimal(0d));
				a.setHighBalance(new BigDecimal(0d));
				a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
				a.setCategoryId(nc.getCategoryId());
				a.setAccountParentType(a.getAccountType());
				a.setStartDate(now);
				a.setHighBalanceDate(now);
				new AddAccountAction().executeAction(user, a);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}

		}
	}

	private void createLiabilitySubCategories(AccountCategory c) {
		ActionRequest req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Credit cards"));
		req.setProperty("PARENTCATEGORY", c);

		new AddCategoryAction().execute(req);

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Loans"));
		req.setProperty("PARENTCATEGORY", c);

		new AddCategoryAction().execute(req);
	}

	private void createCashSubCategories(User user, AccountCategory c)
			throws Exception {
		Date now = new Date();

		ActionRequest req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Checking"));
		req.setProperty("PARENTCATEGORY", c);

		ActionResponse addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			Account a = new Account();
			a.setAccountName(tr("Primary checking"));
			a.setAccountNotes(tr("This is place holder for primary checking account. You can rename/edit/delete this account"));
			a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
			a.setCurrentBalance(new BigDecimal(0d));
			a.setHighBalance(new BigDecimal(0d));
			a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
			a.setCategoryId(nc.getCategoryId());
			a.setAccountParentType(a.getAccountType());
			a.setStartDate(now);
			a.setHighBalanceDate(now);
			new AddAccountAction().executeAction(user, a);
		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Savings"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			Account a = new Account();
			a.setAccountName(tr("Primary savings"));
			a.setAccountNotes(tr("This is place holder for primary savings account. You can rename/edit/delete this account"));
			a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
			a.setCurrentBalance(new BigDecimal(0d));
			a.setHighBalance(new BigDecimal(0d));
			a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
			a.setCategoryId(nc.getCategoryId());
			a.setAccountParentType(a.getAccountType());
			a.setStartDate(now);
			a.setHighBalanceDate(now);
			new AddAccountAction().executeAction(user, a);
		}

		req = new ActionRequest();
		req.setActionName("addCategory");
		req.setProperty("CATEGORYNAME", tr("Cash in hand"));
		req.setProperty("PARENTCATEGORY", c);

		addResponse = new AddCategoryAction().execute(req);
		if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
			AccountCategory nc = (AccountCategory) addResponse
					.getResult("NEWCATEGORY");
			Account a = new Account();
			a.setAccountName(tr("Wallet"));
			a.setAccountNotes(tr("This is place holder for cash in hand accounts. You can rename/edit/delete this account"));
			a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
			a.setCurrentBalance(new BigDecimal(0d));
			a.setHighBalance(new BigDecimal(0d));
			a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
			a.setCategoryId(nc.getCategoryId());
			a.setAccountParentType(a.getAccountType());
			a.setStartDate(now);
			a.setHighBalanceDate(now);
			new AddAccountAction().executeAction(user, a);
		}
	}
}
