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
package net.mjrz.fm.ui.graph;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;

public class AccountBalanceLineGraph extends PageableLineGraph {
	private static final long serialVersionUID = 1L;
	static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private final int WIDTH = 440;
	private List<TT> txList;
	private Account account;

	public AccountBalanceLineGraph(Account account, List<TT> txList) {
		this.txList = txList;
		this.account = account;
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
		getResponseObjects(account, txList, dates, values);

		super.initializeGraph(dates, values, WIDTH);
	}

	protected void highlightGraph(int gWidth, double x, double y, int index) {
		super.highlightGraph(gWidth, x, y, index);
	}

	protected String getXLegendString(int index) {
		int modelIndex = convertPageIndexToModel(index);
		StringBuilder ret = new StringBuilder();
		if (modelIndex < txList.size()) {
			TT t = txList.get(modelIndex);
			if (t.getFromAccountId() == account.getAccountId()) {
				ret.append("Transaction details:   " + t.getToName() + " / "
						+ sdf.format(t.getTxDate()) + " / "
						+ nf.format(t.getTxAmount()));
			}
			else {
				ret.append("Transaction details:   " + t.getFromName() + " / "
						+ sdf.format(t.getTxDate()) + " / "
						+ nf.format(t.getTxAmount()));
			}
		}
		return ret.toString();
	}

	protected String getYLegendString(int index) {
		int modelIndex = convertPageIndexToModel(index);
		StringBuilder ret = new StringBuilder();
		if (modelIndex < txList.size()) {
			TT t = txList.get(modelIndex);
			if (t.getFromAccountId() == account.getAccountId()) {
				ret.append("Account ending balance:   "
						+ nf.format(t.getFromAccountEndingBal()));
			}
			else {
				ret.append("Account ending balance:   "
						+ nf.format(t.getToAccountEndingBal()));
			}
		}
		return ret.toString();
	}

	private void getResponseObjects(Account account, List<TT> txList,
			ArrayList<String> dates, ArrayList<BigDecimal> values) {
		long acctId = account.getAccountId();
		int sz = txList.size();
		for (int i = 0; i < sz; i++) {
			TT t = (TT) txList.get(i);
			if (t.getFromAccountId() == acctId) {
				values.add(t.getFromAccountEndingBal());
			}
			else {
				values.add(t.getToAccountEndingBal());
			}
			dates.add(sdf.format(t.getTxDate()));
		}
	}
}
