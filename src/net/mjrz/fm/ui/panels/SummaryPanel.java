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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.utils.MyTabPaneUI;
import net.mjrz.fm.ui.utils.UIDefaults;

import org.apache.log4j.Logger;

public final class SummaryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabs;
	private NWSummaryPanel nwSummaryPanel;
	private TxDetailsPanel txDetailsPanel;
	private CurrencyExPanel currencyExPanel;
	private AccountSummaryPanel acctSummaryPanel;

	public SummaryPanel() {
		super(new BorderLayout());
		setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		initialize();
	}

	private void initialize() {
		tabs = new JTabbedPane();
		tabs.setUI(new MyTabPaneUI());

		nwSummaryPanel = new NWSummaryPanel();
		tabs.addTab(tr("Summary"), nwSummaryPanel);

		txDetailsPanel = new TxDetailsPanel();
		tabs.addTab(tr("Details"), txDetailsPanel);

		currencyExPanel = new CurrencyExPanel();
		tabs.addTab("Currency", currencyExPanel);

		add(tabs, BorderLayout.CENTER);
	}

	public void updateSummary(User user) {
		nwSummaryPanel.updateSummary(user);
	}

	public void updateTxDetails(String txId) {
		txDetailsPanel.updateTx(txId);
		tabs.setSelectedIndex(1);
	}

	public void updateAccountTab(Account a) {
		try {
			if (acctSummaryPanel == null) {
				acctSummaryPanel = new AccountSummaryPanel();
				acctSummaryPanel.updateAccountSummaryPane(a);
				tabs.addTab(a.getAccountName(), acctSummaryPanel);
				int idx = tabs.getTabCount();
				tabs.setSelectedIndex(idx - 1);
			}
			else {
				updateAccountSummaryPane(a);
				tabs.addTab(a.getAccountName(), acctSummaryPanel);
				tabs.setSelectedIndex(tabs.getTabCount() - 1);
			}
		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

	private void updateAccountSummaryPane(Account a) {
		if (acctSummaryPanel != null) {
			acctSummaryPanel.updateAccountSummaryPane(a);
		}
	}
}
