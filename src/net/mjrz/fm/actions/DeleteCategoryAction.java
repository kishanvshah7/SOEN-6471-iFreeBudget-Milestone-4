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

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.AccountCategory;

public class DeleteCategoryAction {

	public ActionResponse execute(ActionRequest request) {
		ActionResponse resp = new ActionResponse();
		AccountCategory c = (AccountCategory) request
				.getProperty("ACCOUNTCATEGORY");

		if (c.getCategoryId() == AccountTypes.ACCT_TYPE_ROOT
				|| c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_INCOME
				|| c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_CASH
				|| c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_EXPENSE
				|| c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_LIABILITY) {

			resp.setErrorCode(1004);
			resp.setErrorMessage("Cannot delete category: "
					+ c.getCategoryName());
			// return "Cannot remove category: " + c.getCategoryName();
			return resp;
		}
		boolean success = false;
		try {
			if (FManEntityManager.isCategoryPopulated(c.getCategoryId())) {
				resp.setErrorCode(1005);
				resp.setErrorMessage("Category has children...");
				return resp;
			}

			success = new FManEntityManager().deleteAccountCategory(c);
			if (success) {
				resp.setErrorCode(ActionResponse.NOERROR);
				return resp;
			}
			else {
				resp.setErrorCode(ActionResponse.GENERAL_ERROR);
				resp.setErrorMessage("Failed to delete category. Unknown reason");
				return resp;
			}
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage(e.getMessage());
			return resp;
		}
	}

}
