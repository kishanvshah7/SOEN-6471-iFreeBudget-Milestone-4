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
package net.mjrz.fm.ui.wizards.components.transaction;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.ui.panels.NewAccountPanel;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.wizards.TransactionWizard;
import net.mjrz.fm.ui.wizards.components.WizardComponent;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class FromAccountPanel extends javax.swing.JPanel implements
		WizardComponent {

	private static final long serialVersionUID = 1L;
	protected javax.swing.JList accountsList;
	protected DefaultListModel accountsListModel;
	protected javax.swing.JLabel jLabel1, newAcctLbl;
	protected javax.swing.JScrollPane jScrollPane1;
	protected FManEntityManager em;
	protected String type = null;
	protected TransactionWizard wizard = null;

	public FromAccountPanel(TransactionWizard wizard) {
		em = new FManEntityManager();
		initComponents();
		setBackground(Color.WHITE);
		this.wizard = wizard;
	}

	private void initComponents() {
		setLayout(new GridBagLayout());
		jLabel1 = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();

		accountsListModel = new DefaultListModel();
		accountsList = new javax.swing.JList(accountsListModel);

		accountsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountsList.setBorder(BorderFactory.createTitledBorder(""));

		setBorder(javax.swing.BorderFactory.createTitledBorder(""));

		jLabel1.setText(tr("Select account") + ": ");
		jScrollPane1.setViewportView(accountsList);

		newAcctLbl = new JLabel();
		newAcctLbl.setText("<html><a href=\"#\">Add new account</a></html>");
		newAcctLbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				showNewAccountDialog();
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		add(jLabel1, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.9;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.ABOVE_BASELINE;
		gbc.insets = new Insets(1, 10, 1, 10);
		add(new JScrollPane(accountsList), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		add(newAcctLbl, gbc);
	}

	@SuppressWarnings("unchecked")
	protected void updateList(String type) {
		Long uid = net.mjrz.fm.services.SessionManager.getSessionUserId();
		java.util.List<Account> l = null;
		try {
			int from = 0;
			try {
				from = Integer.parseInt(type);
			}
			catch (NumberFormatException e) {
				System.out.println("Error parsing number " + from);
				from = 0;
			}
			l = (java.util.List<Account>) em.getAccountsForUser(uid, from);
			if (l != null) {
				java.util.Collections.sort(l);
				accountsListModel.clear();
				for (Account a : l) {
					accountsListModel.addElement(a);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[][] getValues() {
		String[][] ret = new String[1][1];
		int sel = accountsList.getSelectedIndex();
		String[] row = { "", "" };
		if (sel >= 0) {
			Account a = (Account) accountsList.getSelectedValue();
			row[0] = "FROMACCTID";
			row[1] = String.valueOf(a.getAccountId());
		}
		ret[0] = row;
		return ret;
	}

	String msg = "";

	public boolean isComponentValid() {
		msg = "";
		if (accountsList.getModel().getSize() == 0) {
			msg = tr("No accounts available");
			return false;
		}
		int sel = accountsList.getSelectedIndex();
		if (sel < 0) {
			msg = tr("No accounts selected");
			return false;
		}
		return true;
	}

	public String getMessage() {
		return msg;
	}

	public void setComponentFocus() {
		if (accountsList.getModel().getSize() > 0) {
			accountsList.setSelectedIndex(0);
		}
		accountsList.requestFocusInWindow();
	}

	public void updateComponentUI(HashMap<String, String[][]> values) {
		String[][] value = values.get("TransactionType");
		String[] acctTypes = value[0];
		type = acctTypes[0];
		updateList(type);
	}

	private void printValues(HashMap<String, String[][]> values) {
		Set<String> keys = values.keySet();
		for (String k : keys) {
			String[][] vals = values.get(k);
			System.out.println("Key: " + k);
			for (int i = 0; i < vals.length; i++) {
				System.out.println("\t" + vals[i][0] + " - " + vals[i][1]);
			}
		}
	}

	protected String getType() {
		return type;
	}

	//
	private void showNewAccountDialog() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JDialog d = new JDialog(SwingUtilities
						.getWindowAncestor(FromAccountPanel.this),
						tr("New Account"),
						Dialog.ModalityType.APPLICATION_MODAL);

				List<AccountCategory> from = null;
				AccountCategory parent = null;
				try {
					from = FManEntityManager.getRootCategoies();

					if (type != null) {
						for (AccountCategory c : from) {
							if (c.getCategoryId() == Integer
									.parseInt(getType())) {
								parent = c;
								break;
							}
						}
					}
				}
				catch (Exception e) {
					Logger.getLogger(this.getClass()).error(e);
				}
				NewAccountPanel p = new NewAccountPanel(d, parent);
				d.getContentPane().add(p);
				d.pack();
				d.setSize(new Dimension(375, 250));
				d.setLocationRelativeTo(FromAccountPanel.this);
				d.setVisible(true);
				d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				GuiUtilities.addWindowClosingActionMap(d);
				ActionResponse res = p.getResult();
				if (res != null && res.getErrorCode() == ActionResponse.NOERROR) {

					Account a = (Account) res.getResult("ACCOUNT");
					if (a != null) {
						updateList(type);
						wizard.accountAdded(a);
						accountsList.setSelectedValue(a, true);
					}
				}
			}
		});
	}
	//
}
