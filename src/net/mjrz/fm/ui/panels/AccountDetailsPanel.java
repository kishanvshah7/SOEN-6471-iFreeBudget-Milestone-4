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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.ui.utils.GuiUtilities;

public class AccountDetailsPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;

	protected javax.swing.JTextField acctNameTf;
	protected net.mjrz.fm.ui.utils.REditor acctNotesTa;
	protected javax.swing.JTextField acctNumTf;
	protected javax.swing.JTextField acctSBTf;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JScrollPane jScrollPane2;

	/** Creates new form AccountDetailsPanel */
	public AccountDetailsPanel() {
		initComponents();
		initTextComponents();
	}

	private void initTextComponents() {
		GuiUtilities.setupTextComponent(this.acctNameTf);
		GuiUtilities.setupTextComponent(this.acctNumTf);
		GuiUtilities.setupTextComponent(this.acctSBTf);
	}

	public String validateFields() throws Exception {
		try {
			String aname = acctNameTf.getText();
			if (aname == null || aname.length() == 0) {
				return tr("Account name cannot be empty");
			}
			String anotes = acctNotesTa.getText();
			if (anotes == null) {
				return tr("Account notes cannot be null");
			}
			validateBalanceField();
			return null;
		}
		catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	public void initializeFields() {
		acctNameTf.setText("");
		acctNumTf.setText("");
		acctSBTf.setText("");
		acctNotesTa.setText("");
		if (SwingUtilities.isEventDispatchThread()) {
			acctNameTf.requestFocusInWindow();
		}
	}

	protected boolean validateBalanceField() throws Exception {
		String sbal = acctSBTf.getText();
		if (sbal == null || sbal.trim().length() == 0)
			sbal = "0";

		Double.parseDouble(sbal);
		return true;
	}

	protected String getBalanceLabelText() {
		return "Starting balance";
	}

	public void populateAccountData(Account account) throws Exception {
		account.setAccountName(acctNameTf.getText());

		account.setAccountNotes(acctNotesTa.getText());

		account.setStartDate(new java.util.Date());

		account.setStatus(AccountTypes.ACCOUNT_ACTIVE);

		account.setAccountNumber(acctNumTf.getText());

		populateBalanceData(account);
	}

	protected void populateBalanceData(Account account) throws Exception {
		String sbal = acctSBTf.getText();
		if (sbal == null || sbal.trim().length() == 0)
			sbal = "0";

		BigDecimal sb = new BigDecimal(sbal);

		account.setStartingBalance(sb);

		account.setCurrentBalance(sb);
	}

	public void setPanelFocus() {
		acctNameTf.requestFocusInWindow();
	}

	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		acctNameTf = new javax.swing.JTextField(20);
		acctNumTf = new javax.swing.JTextField(20);
		acctSBTf = new javax.swing.JTextField(20);
		jScrollPane2 = new javax.swing.JScrollPane();
		acctNotesTa = new net.mjrz.fm.ui.utils.REditor();
		acctNotesTa.setBorder(BorderFactory.createTitledBorder(tr("Notes")));

		jLabel1.setText("Name");
		jLabel2.setText("Account number");
		jLabel3.setText(getBalanceLabelText());
		jLabel4.setText("Notes");

		jScrollPane2.setViewportView(acctNotesTa);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.2;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(jLabel1, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.8;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(acctNameTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(jLabel2, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(acctNumTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(jLabel3, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(acctSBTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.weighty = 1;
		gbc.ipady = 50;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(acctNotesTa, gbc);
	}
}
