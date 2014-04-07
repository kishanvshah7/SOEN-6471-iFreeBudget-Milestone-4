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

import java.util.Set;

public class Budget {
	public final static int WEEKLY = 1;
	public final static int MONTHLY = 2;

	private long id;
	private String name;
	private int type;
	private Set<BudgetedAccount> accounts;
	private long uid;

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Set<BudgetedAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(Set<BudgetedAccount> accounts) {
		this.accounts = accounts;
	}

	public static int getTypeFromString(String type) {
		if (type.equals("Weekly")) {
			return WEEKLY;
		}
		return MONTHLY;
	}

	public static String getTypeAsString(int type) {
		if (type == WEEKLY) {
			return "Weekly";
		}
		return "Monthly";
	}

	public String toString() {
		return name;
	}
}
