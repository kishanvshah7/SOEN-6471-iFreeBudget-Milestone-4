package net.mjrz.fm.ui.utils;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;

import org.apache.log4j.Logger;

public class AccountCbEntry implements Comparable<AccountCbEntry> {
	private Account account = null;
	private String categoryName = null;
	private static final Logger logger = Logger.getLogger(AccountCbEntry.class);

	public AccountCbEntry(Account a) {
		this.account = a;
		try {
			categoryName = FManEntityManager.getCategoryName(a.getAccountId());
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account a) {
		this.account = a;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString() {
//		return categoryName + " :: " + account.getAccountName();
		return account.getAccountName() + " ( " + categoryName + " )";
	}

	@Override
	public int compareTo(AccountCbEntry other) {
		return categoryName.compareTo(other.categoryName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result
				+ ((categoryName == null) ? 0 : categoryName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AccountCbEntry)) {
			return false;
		}
		AccountCbEntry other = (AccountCbEntry) obj;
		if (account == null) {
			if (other.account != null) {
				return false;
			}
		}
		else if (!account.equals(other.account)) {
			return false;
		}
		if (categoryName == null) {
			if (other.categoryName != null) {
				return false;
			}
		}
		else if (!categoryName.equals(other.categoryName)) {
			return false;
		}
		return true;
	}
}
