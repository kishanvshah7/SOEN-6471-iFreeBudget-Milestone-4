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
package net.mjrz.fm.ui.panels;

import java.math.BigDecimal;
import java.text.NumberFormat;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.services.SessionManager;

public class EditAccountDetailsPanel extends AccountDetailsPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EditAccountDetailsPanel() {
		super();
	}

	protected String getBalanceLabelText() {
		return "Balance";
	}

	public void populateFields(Account account) throws Exception {
		acctNameTf.setText(account.getAccountName());
		acctNotesTa.setText(account.getAccountNotes());
		acctNumTf.setText(account.getAccountNumber());
		NumberFormat cf = NumberFormat.getCurrencyInstance(SessionManager
				.getCurrencyLocale());
		acctSBTf.setText(cf.format(account.getCurrentBalance()));
	}

	protected void populateBalanceData(Account account) throws Exception {
		String sbal = acctSBTf.getText();
		if (sbal == null || sbal.trim().length() == 0)
			sbal = "0";

		NumberFormat cf = NumberFormat.getCurrencyInstance(SessionManager
				.getCurrencyLocale());
		BigDecimal sb = null;
		try {
			Number num = cf.parse(sbal);
			sb = new BigDecimal(num.doubleValue());
		}
		catch (Exception e) {
			sb = new BigDecimal(sbal);
		}

		account.setCurrentBalance(sb);
	}

	protected boolean validateBalanceField() throws Exception {
		String sbal = acctSBTf.getText();
		if (sbal == null || sbal.trim().length() == 0)
			sbal = "0";

		try {
			NumberFormat cf = NumberFormat.getCurrencyInstance(SessionManager
					.getCurrencyLocale());
			cf.parse(sbal);
		}
		catch (Exception e) {
			Double.parseDouble(sbal);
		}
		return true;
	}
}
