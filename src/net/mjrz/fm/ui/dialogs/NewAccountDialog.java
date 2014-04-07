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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddAccountAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AccountCategoryMap;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.help.HelpUtil;
import net.mjrz.fm.ui.panels.AccountDetailsPanel;
import net.mjrz.fm.ui.utils.CategoryTreeCellRenderer;
import net.mjrz.fm.ui.utils.GuiUtilities;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NewAccountDialog extends JDialog implements KeyListener {

	private static final long serialVersionUID = 0L;

	private JButton newAcctB, cancelB;

	private FinanceManagerUI parent = null;

	private JTree tree = null;

	private User user = null;

	private transient AccountCategoryMap map = null;

	private AccountDetailsPanel acctDetailsPanel = null;

	private JSplitPane sp = null;

	private JCheckBox addMore = null;

	private static NewAccountDialog instance = null;

	public static synchronized NewAccountDialog getInstance(
			FinanceManagerUI parent, User user, AccountCategory parentCategory) {
		if (instance == null) {
			instance = new NewAccountDialog(parent, user, parentCategory);
		}
		return instance;
	}

	public static void cleanup() {
		instance = null;
	}

	private NewAccountDialog(FinanceManagerUI parent, User user,
			AccountCategory parentCategory) {
		super(parent, tr("New Account"), false);
		this.parent = parent;
		this.user = user;
		initialize();

		if (parentCategory != null) {
			ArrayList<AccountCategory> ancestors = map
					.getAncestors(parentCategory);
			TreePath path = new TreePath(ancestors.toArray());
			tree.setSelectionPath(path);
		}
	}

	public void setDialogFocus() {
		acctDetailsPanel.setPanelFocus();
	}

	private void initialize() {
		map = new AccountCategoryMap(user.getUid(), "Categories");
		map.setUser(user);

		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.ipadx = 100;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.weighty = 1;
		add(buildTreePanel(), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(getAcctDetailsPane(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridwidth = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;

		add(getButtonPane(), gbc);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel buildTreePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		tree = new JTree(map);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setVisibleRowCount(10);
		tree.setRowHeight(25);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(getTreeCellRenderer());
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getNewLeadSelectionPath();
				if (path == null)
					return;

				tree.expandPath(path);
				acctDetailsPanel.initializeFields();
			}
		});

		JScrollPane sp = new JScrollPane(tree);
		ret.add(sp);
		ret.setBorder(BorderFactory.createTitledBorder(tr("Categories:")));

		return ret;
	}

	private JPanel getAcctDetailsPane() {
		acctDetailsPanel = new AccountDetailsPanel();
		acctDetailsPanel
				.setBorder(BorderFactory.createTitledBorder("Details:"));
		return acctDetailsPanel;
	}

	private JPanel getButtonPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		addMore = new JCheckBox(tr("Add more"));
		addMore.setVisible(false);

		newAcctB = new JButton(tr("Save"));
		newAcctB.setActionCommand("Save");
		newAcctB.setMinimumSize(new Dimension(80, 20));
		newAcctB.setMnemonic(KeyEvent.VK_S);
		newAcctB.addActionListener(new ButtonHandler());
		getRootPane().setDefaultButton(newAcctB);

		cancelB = new JButton(tr("Close"));
		cancelB.setActionCommand("Cancel");
		cancelB.setMinimumSize(new Dimension(80, 20));
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ButtonHandler());

		ret.add(addMore);
		ret.add(Box.createHorizontalGlue());
		ret.add(newAcctB);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancelB);
		ret.add(Box.createHorizontalStrut(5));

		ret.setBorder(BorderFactory.createTitledBorder(""));
		return ret;
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Cancel")) {
				dispose();
			}
			if (cmd.equals("Help")) {
				HelpUtil.loadHelpPage(NewAccountDialog.class.getName());
			}
			if (cmd.equals("Save")) {
				try {
					String result = validateFields();
					if (result.length() == 0) {
						createAccount();
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
		JOptionPane.showMessageDialog(this, msg, tr("Error"),
				JOptionPane.ERROR_MESSAGE);
	}

	private boolean showAddMoreDialog() {
		int n = JOptionPane.showConfirmDialog(this,
				tr("Do you want to add more accounts?"), tr("Add more"),
				JOptionPane.YES_NO_OPTION);

		return n == JOptionPane.YES_OPTION;
	}

	private void createAccount() throws Exception {
		Account a = new Account();
		acctDetailsPanel.populateAccountData(a);

		AccountCategory catgr = (AccountCategory) tree
				.getLastSelectedPathComponent();
		AccountCategory root = map.getRootCategory(catgr);

		a.setAccountType(root.getCategoryId().intValue());
		a.setCategoryId(catgr.getCategoryId());

		a.setAccountParentType(a.getAccountType());

		AddAccountAction action = new AddAccountAction();

		ActionResponse result = action.executeAction(user, a);

		if (result.getErrorCode() == ActionResponse.NOERROR) {
			parent.reloadAccountList(a, true);
			parent.updateStatusPane();
			if (!addMore.isVisible()) {
				if (!showAddMoreDialog()) {
					dispose();
					return;
				}
				else {
					addMore.setSelected(true);
					addMore.setVisible(true);
				}
			}
			else if (addMore.isVisible() && !addMore.isSelected()) {
				dispose();
				return;
			}
			acctDetailsPanel.initializeFields();
		}
		else {
			JOptionPane.showMessageDialog(this, result.getErrorMessage(),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private String validateFields() throws Exception {
		try {
			String valid = acctDetailsPanel.validateFields();
			if (valid != null && valid.length() > 0) {
				return valid;
			}
			AccountCategory catgr = (AccountCategory) tree
					.getLastSelectedPathComponent();
			if (catgr == null) {
				return tr("No category selected");
			}

			AccountCategory root = map.getRootCategory(catgr);

			if (root == null)
				return tr("Invalid account type");

			String typeCheck = "";
			if (!(root.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_INCOME)
					&& !(root.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_CASH)
					&& !(root.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_EXPENSE)
					&& !(root.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_LIABILITY)) {

				typeCheck = tr("Invalid account type");
			}
			return typeCheck;
		}
		catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	private DefaultTreeCellRenderer getTreeCellRenderer() {
		return new CategoryTreeCellRenderer();
		// ImageIcon icon = new
		// net.mjrz.fm.ui.utils.MyImageIcon("icons/category.png");
		//
		// DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		// if (icon != null) {
		// renderer.setLeafIcon(icon);
		// renderer.setOpenIcon(icon);
		// renderer.setClosedIcon(icon);
		// }
		// return renderer;
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

	@Override
	public void dispose() {
		super.dispose();
		cleanup();
	}
}
