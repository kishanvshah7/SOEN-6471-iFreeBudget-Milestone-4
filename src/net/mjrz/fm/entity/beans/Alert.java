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
package net.mjrz.fm.entity.beans;

import java.math.BigDecimal;
import java.text.NumberFormat;

import net.mjrz.fm.services.SessionManager;

public class Alert {
	long id;
	long accountId;
	int alertType;
	BigDecimal amount;
	String conditional;
	String range;
	int status;

	public static final int ALERT_TYPE_ACCOUNT = 1;
	public static final int ALERT_TYPE_CATEGORY = 2;

	public static final int ALERT_NONE = 3;
	public static final int ALERT_RAISED = 4;
	public static final int ALERT_CLEARED = 5;

	public static final String EXCEEDS = "Exceeds";
	public static final String FALLS_BELOW = "Falls below";

	public static final String[] RANGE = { "This week", "This month", "Today" };

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public int getAlertType() {
		return alertType;
	}

	public void setAlertType(int alertType) {
		this.alertType = alertType;
	}

	public String getConditional() {
		return conditional;
	}

	public void setConditional(String conditional) {
		this.conditional = conditional;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Amount ");
		ret.append(conditional);
		ret.append(" ");
		ret.append(NumberFormat.getCurrencyInstance(
				SessionManager.getCurrencyLocale()).format(amount));
		// if(range != null) {
		// ret.append(" for ");
		// ret.append(range);
		// }
		return ret.toString();
	}
}
