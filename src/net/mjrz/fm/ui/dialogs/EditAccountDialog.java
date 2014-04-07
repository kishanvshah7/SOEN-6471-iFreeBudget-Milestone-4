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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.UpdateAccountAction;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.EditAccountDetailsPanel;
import net.mjrz.fm.ui.utils.GuiUtilities;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class EditAccountDialog extends JDialog implements KeyListener {

	private static final long serialVersionUID = 0L;

	private JButton newAcctB, cancelB;

	private FinanceManagerUI parent = null;

	private User user = null;

	private NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	private Account updateAccount = null;

	private EditAccountDetailsPanel acctDetailsPanel = null;

	public EditAccountDialog(FinanceManagerUI parent, User user, Account account) {
		super(parent, "Edit Account", true);
		this.parent = parent;
		this.user = user;
		this.updateAccount = account;
		initialize();
		try {
			acctDetailsPanel.populateFields(updateAccount);
		}
		catch (Exception e) {
			Logger.getLogger(EditAccountDialog.class.getName()).error(e);
			dispose();
		}
	}

	public void setDialogFocus() {
		acctDetailsPanel.setPanelFocus();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());

		center.add(getAcctDetailsPane(), BorderLayout.CENTER);

		add(center, BorderLayout.CENTER);
		add(getButtonPane(), BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		GuiUtilities.addWindowClosingActionMap(this);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setSize(new Dimension(450, 400));
			}
		});
	}

	private JPanel getAcctDetailsPane() {
		acctDetailsPanel = new EditAccountDetailsPanel();
		return acctDetailsPanel;
	}

	private JPanel getButtonPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		newAcctB = new JButton(tr("Save"));
		newAcctB.setActionCommand("Save");
		newAcctB.setMinimumSize(new Dimension(80, 20));
		newAcctB.setMnemonic(KeyEvent.VK_S);
		newAcctB.addActionListener(new ButtonHandler());
		getRootPane().setDefaultButton(newAcctB);

		cancelB = new JButton(tr("Cancel"));
		cancelB.setActionCommand("Cancel");
		cancelB.setMinimumSize(new Dimension(80, 20));
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ButtonHandler());
		ret.add(Box.createHorizontalGlue());
		ret.add(newAcctB);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(cancelB);
		ret.add(Box.createHorizontalStrut(10));
		return ret;
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Cancel")) {
				dispose();
			}
			if (cmd.equals("Save")) {
				try {
					String result = validateFields();
					if (result == null) {
						updateAccount(updateAccount);
					}
					else {
						showErrorDialog(result);
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					return;
				}
			}
		}
	}

	private void showErrorDialog(String msg) {
		JOptionPane.showMessageDialog(this, msg, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private void updateAccount(Account selected) throws Exception {
		if (selected == null)
			return;

		ActionResponse resp = null;

		try {
			acctDetailsPanel.populateAccountData(selected);
			Account a = null;
			a = (Account) selected.clone();
			if (a == null)
				return;

			ActionRequest req = new ActionRequest();
			req.setUser(user);
			req.setActionName("updateAccount"); //$NON-NLS-1$
			req.setProperty("ACCOUNT", a); //$NON-NLS-1$
			if (!a.getAccountName().equals(selected.getAccountName()))
				req.setProperty("VALIDATENAME", true); //$NON-NLS-1$
			UpdateAccountAction action = new UpdateAccountAction();

			resp = action.executeAction(req);
			if (!resp.hasErrors()) {
				parent.reloadAccountList(a, false);
				parent.updateSummaryTab(a);
				dispose();
			}
			else {
				showErrorDialog(resp.getErrorMessage());
			}
			return;
		}
		catch (Exception ex) {
			if (resp != null)
				showErrorDialog(resp.getErrorMessage());
			else
				showErrorDialog(tr("Error updating account: ") + ex.getMessage()); //$NON-NLS-1$
		}
	}

	private String validateFields() throws Exception {
		return acctDetailsPanel.validateFields();
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER) {
		}
		if (key == KeyEvent.VK_ESCAPE) {
			dispose();
		}
		if (key == KeyEvent.VK_TAB) {
			java.awt.Component c = getFocusOwner();
			if (c != null
					&& c.getClass().getName().equals("javax.swing.JTextArea")) {
				newAcctB.requestFocusInWindow();
			}
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
