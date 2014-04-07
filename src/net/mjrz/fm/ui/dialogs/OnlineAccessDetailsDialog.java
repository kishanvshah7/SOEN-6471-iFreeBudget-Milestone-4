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
package net.mjrz.fm.ui.dialogs;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddONLBDetailsAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.help.HelpUtil;
import net.mjrz.fm.ui.utils.SpringUtilities;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class OnlineAccessDetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private int curr = 0;
	private JButton save, cancel, help;
	private User user;
	private FinanceManagerUI parent;
	private JPasswordField p1, p2;
	private JTextField[] tflist;
	private JComboBox acctTypes;
	private Account account;

	private String[] types = { "", "CHECKING", "SAVINGS", "CREDIT CARD" };
	private ONLBDetails old = null;

	public OnlineAccessDetailsDialog(JFrame parent, User user, Account c) {
		super(parent, "Online access details");
		this.user = user;
		this.parent = (FinanceManagerUI) parent;
		this.account = c;

		tflist = new JTextField[5];
		for (int i = 0; i < tflist.length; i++) {
			tflist[i] = new JTextField(20);
			tflist[i].setPreferredSize(new Dimension(100, 20));
		}
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		add(getDetailsPane(), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);

		// this.setPreferredSize(new Dimension(500, 300));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		try {
			old = new FManEntityManager().getONLBFromAccountId(account
					.getAccountId());
			if (old != null) {
				populateFields();
			}
		}
		catch (Exception e) {
			// Ignore;
		}
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private void populateFields() {
		tflist[0].setText(old.getOrg());
		tflist[1].setText(old.getFid());
		tflist[2].setText(old.getOrgId());
		tflist[3].setText(old.getUrl());
		tflist[4].setText(old.getUser());
		for (int i = 0; i < types.length; i++) {
			if (old.getType().equals(types[i])) {
				acctTypes.setSelectedIndex(i);
				break;
			}
		}
	}

	private JPanel getDetailsPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new SpringLayout());

		ret.add(new JLabel("Org", JLabel.LEADING));
		ret.add(tflist[curr++]);

		ret.add(new JLabel("FID", JLabel.LEADING));
		ret.add(tflist[curr++]);

		ret.add(new JLabel("Routing number", JLabel.LEADING));
		ret.add(tflist[curr++]);

		ret.add(new JLabel("Provider url", JLabel.LEADING));
		ret.add(tflist[curr++]);

		ret.add(new JLabel("Account type", JLabel.LEADING));
		acctTypes = new JComboBox(types);
		acctTypes.setPreferredSize(new Dimension(100, 20));
		ret.add(acctTypes);

		ret.add(new JLabel("User id", JLabel.LEADING));
		ret.add(tflist[curr++]);

		p1 = new JPasswordField(20);
		p1.setPreferredSize(new Dimension(100, 20));
		ret.add(new JLabel("Password", JLabel.LEADING));
		ret.add(p1);

		p2 = new JPasswordField(20);
		p2.setPreferredSize(new Dimension(100, 20));
		ret.add(new JLabel("Re-enter password", JLabel.LEADING));
		ret.add(p2);

		SpringUtilities.makeGrid(ret, 8, 2, 2, 2, 2, 2);
		return ret;
	}

	private boolean validateFields() {
		char[] c1 = p1.getPassword();
		char[] c2 = p2.getPassword();
		if (c1.length == 0 || c2.length == 0) {
			return false;
		}
		if (c1.length != c2.length) {
			return false;
		}
		for (int i = 0; i < c1.length; i++) {
			if (c1[i] != c2[i]) {
				return false;
			}
		}
		for (int i = 0; i < tflist.length; i++) {
			if (i == 2)
				continue;
			if (tflist[i].getText() == null
					|| tflist[i].getText().length() == 0)
				return false;
		}
		return true;
	}

	public void save() {
		if (!validateFields()) {
			JOptionPane.showMessageDialog(OnlineAccessDetailsDialog.this,
					"Error in input", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else {
			SwingWorker worker = new SwingWorker<ActionResponse, Void>() {

				public ActionResponse doInBackground() throws Exception {
					ONLBDetails d = new ONLBDetails();
					d.setAccountId(account.getAccountId());
					d.setOrg(tflist[0].getText());
					d.setFid(tflist[1].getText());
					d.setUrl(tflist[3].getText());
					d.setType(acctTypes.getSelectedItem().toString());
					d.setUser(tflist[4].getText());
					d.setPassword(new String(p1.getPassword()));
					d.setOrgId(tflist[2].getText());

					ActionRequest req = new ActionRequest();
					req.setActionName("addONLBDetails");
					req.setUser(user);
					req.setProperty("ONLB", d);

					AddONLBDetailsAction action = new AddONLBDetailsAction();
					ActionResponse resp = action.executeAction(req);

					return resp;
				}

				public void done() {
					try {
						ActionResponse result = (ActionResponse) get();
						if (result.getErrorCode() == ActionResponse.NOERROR) {
							OnlineAccessDetailsDialog.this.dispose();
						}
						else {
							JOptionPane
									.showMessageDialog(
											OnlineAccessDetailsDialog.this,
											"Error saving: "
													+ result.getErrorMessage(),
											"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e) {
						Logger.getLogger(getClass()).error(e);
						return;
					}
				}
			};
			worker.execute();
		}
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		save = new JButton("Save");
		save.setMinimumSize(new Dimension(80, 20));
		save.setMnemonic(KeyEvent.VK_S);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});

		cancel = new JButton("Close");
		cancel.setMinimumSize(new Dimension(80, 20));
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		help = new JButton(tr("Help"));
		help.setActionCommand("Help");
		help.setMinimumSize(new Dimension(80, 20));
		help.setMnemonic(KeyEvent.VK_C);
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpUtil.loadHelpPage(OnlineAccessDetailsDialog.class.getName());
			}
		});

		ret.add(Box.createHorizontalGlue());
		ret.add(save);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancel);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(help);
		ret.add(Box.createHorizontalGlue());

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}
}
