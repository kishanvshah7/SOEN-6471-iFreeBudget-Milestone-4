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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddCategoryAction;
import net.mjrz.fm.actions.DeleteAccountAction;
import net.mjrz.fm.actions.DeleteCategoryAction;
import net.mjrz.fm.actions.ExportAccountToCsvFileAction;
import net.mjrz.fm.actions.ExportAccountToTabFileAction;
import net.mjrz.fm.actions.GetAccountListAction;
import net.mjrz.fm.actions.RemoveAlertAction;
import net.mjrz.fm.actions.RenameCategoryAction;
import net.mjrz.fm.actions.UpdateAccountAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AlertsEntityManager;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.ONLBDetails;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.search.newfilter.Filter;
import net.mjrz.fm.search.newfilter.NewFilterUtils;
import net.mjrz.fm.search.newfilter.Order;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.dialogs.SetupAlertDialog;
import net.mjrz.fm.ui.dialogs.StatementDownloadDialog;
import net.mjrz.fm.ui.utils.AccountsTreeModel;
import net.mjrz.fm.ui.utils.ImagePreview;
import net.mjrz.fm.ui.utils.ImportCategoryIcon;
import net.mjrz.fm.ui.utils.TreeCellRenderer;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AccountsTreePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private AccountsTreeModel model = null;
	private JTree accountsTree = null;
	private User user;
	private FinanceManagerUI parent = null;
	private FManEntityManager em = null;
	private static Logger logger = Logger.getLogger(AccountsTreePanel.class
			.getName());

	public AccountsTreePanel(FinanceManagerUI parent, User user) {
		super();
		this.user = user;
		this.parent = parent;
		em = new FManEntityManager();

		initialize();
	}

	private void initialize() {
		super.setBackground(Color.white);
		super.setLayout(new GridBagLayout());

		populateAccountsTree();

		JScrollPane sp = new JScrollPane(accountsTree);
		sp.setBorder(BorderFactory.createLineBorder(Color.white));
		sp.setBackground(Color.white);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.5;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.weighty = 1;

		add(sp, gbc);
	}

	/**
	 * Updates the node representing the account in account tree after some
	 * action that modifies the account
	 * 
	 * @param a
	 *            Account to update
	 * @param added
	 *            If the new node was added or an update on existing node
	 */
	public void updateTree(Account a, boolean added) {
		if (a == null) {
			AccountCategory ac = (AccountCategory) model.getRoot();
			model.updateCategory(ac);
		}
		if (added) {
			model.addAccount(a);
		}
		else {
			// model.updateAccount(a, true);
			model.updateAccount(a, false);
		}
	}

	/**
	 * Reload the complete accounts tree
	 */
	public void reloadTree() {
		populateAccountsTree();
	}

	@SuppressWarnings("unchecked")
	private void populateAccountsTree() {
		try {
			GetAccountListAction action = new GetAccountListAction();
			ActionResponse resp = action.getAccountList(user);
			List accountList = (List) resp.getResult("RESULTSET");

			model = new AccountsTreeModel(user.getUid(),
					AccountTypes.DEFAULT_ROOT_NAME, accountList);
			model.addTreeModelListener(new MTreeModelListener());

			if (model != null && accountsTree != null) {
				accountsTree.setModel(model);
			}
			if (model != null && accountsTree == null) {
				accountsTree = new JTree(model);
				accountsTree.setCellRenderer(new TreeCellRenderer());
				accountsTree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
			}
			if (model == null && accountsTree == null) {
				accountsTree = new JTree();
				accountsTree.setCellRenderer(new TreeCellRenderer());
				accountsTree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
			}
			accountsTree.setDragEnabled(true);
			accountsTree.setDropMode(javax.swing.DropMode.ON);
			accountsTree
					.setTransferHandler(new net.mjrz.fm.ui.utils.AccountsTreeTransferHandler(
							this));
			accountsTree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					handleAccountsTreeSelection(e);
				}
			});
			accountsTree.addMouseListener(new MouseHandler());
			accountsTree.requestFocusInWindow();
			accountsTree.setRowHeight(25);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return;
		}
	}

	public int getNumAccounts() {
		if (model == null)
			return 0;

		return model.getNumAccounts();
	}

	public JTree getAccountsTree() {
		return accountsTree;
	}

	private void showMenu(Account a, int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		JMenuItem mItem = new JMenuItem(Messages.getString("New transaction"));
		mItem.setActionCommand("AddTxForAccount");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Account History"));
		mItem.setActionCommand("Account History");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Summary"));
		mItem.setActionCommand("Summary");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		// if(a.getAccountType() == AccountTypes.ACCT_TYPE_CASH ||
		// a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
		mItem = new JMenuItem(Messages.getString("Graph"));
		mItem.setActionCommand("Graph");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);
		// }

		popup.add(new JSeparator(JSeparator.HORIZONTAL));

		if (a.getAccountType() == AccountTypes.ACCT_TYPE_CASH
				|| a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
			mItem = new JMenuItem(Messages.getString("Setup online access"));
			mItem.setActionCommand("Setup online access");
			mItem.addActionListener(new PopupMenuHandler());
			popup.add(mItem);

			mItem = new JMenuItem(Messages.getString("Download statement"));
			mItem.setActionCommand("Download statement");
			mItem.addActionListener(new PopupMenuHandler());
			popup.add(mItem);

			popup.add(new JSeparator(JSeparator.HORIZONTAL));
		}

		if (a.getAccountNumber() != null && a.getAccountNumber().length() > 0) {
			JMenu submenu = new JMenu(Messages.getString("Export"));

			mItem = new JMenuItem(Messages.getString("CSV format"));
			mItem.setActionCommand("ExportAccountCsv");
			mItem.addActionListener(new PopupMenuHandler());
			submenu.add(mItem);

			mItem = new JMenuItem(Messages.getString("Tab format"));
			mItem.setActionCommand("ExportAccountTab");
			mItem.addActionListener(new PopupMenuHandler());
			submenu.add(mItem);

			popup.add(submenu);

		}

		mItem = new JMenuItem(Messages.getString("Edit"));
		mItem.setActionCommand("EditAccount");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		if (a.getStatus() == AccountTypes.ACCOUNT_ACTIVE) {
			mItem = new JMenuItem(Messages.getString("Lock"));
			mItem.setActionCommand("LockAccount");
			mItem.addActionListener(new PopupMenuHandler());
			popup.add(mItem);
		}
		else if (a.getStatus() == AccountTypes.ACCOUNT_LOCKED) {
			mItem = new JMenuItem(Messages.getString("Unlock"));
			mItem.setActionCommand("UnLockAccount");
			mItem.addActionListener(new PopupMenuHandler());
			popup.add(mItem);
		}

		mItem = new JMenuItem(Messages.getString("Delete"));
		mItem.setActionCommand("Delete");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		popup.add(new JSeparator(JSeparator.HORIZONTAL));

		if (a != null) {
			try {
				if (!AlertsEntityManager.hasAlert(a.getAccountId())) {
					mItem = new JMenuItem(Messages.getString("Add alert"));
					mItem.setActionCommand("AddAlert");
					mItem.addActionListener(new PopupMenuHandler());
					popup.add(mItem);
				}
				else {
					mItem = new JMenuItem(Messages.getString("Edit alert"));
					mItem.setActionCommand("EditAlert");
					mItem.addActionListener(new PopupMenuHandler());
					popup.add(mItem);

					mItem = new JMenuItem(Messages.getString("Remove alert"));
					mItem.setActionCommand("RemoveAlert");
					mItem.addActionListener(new PopupMenuHandler());
					popup.add(mItem);
				}
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}

		// mItem = new JMenuItem(Messages.getString("Address"));
		// mItem.setActionCommand("AccountAddress");
		// mItem.addActionListener(new PopupMenuHandler());
		// popup.add(mItem);

		popup.pack();
		popup.show(accountsTree, x, y);
	}

	private void showCategoryPopup(int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		// JMenuItem mItem = new
		// JMenuItem(Messages.getString("All transactions"));
		// mItem.setActionCommand("All transactions");
		// mItem.addActionListener(new PopupMenuHandler());
		// popup.add(mItem);

		JMenuItem mItem = new JMenuItem(Messages.getString("Add account"));
		mItem.setActionCommand("Add account");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Add transaction"));
		mItem.setActionCommand("Add transaction");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		popup.pack();
		popup.show(accountsTree, x, y);
	}

	private void showGeneralCategoryPopup(int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		JMenuItem mItem = new JMenuItem(Messages.getString("Change icon"));
		mItem.setActionCommand("SetIcon");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Add subcategory"));
		mItem.setActionCommand("AddSubcategory");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Add account"));
		mItem.setActionCommand("Add account");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		mItem = new JMenuItem(Messages.getString("Rename"));
		mItem.setActionCommand("RenameCategory");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		popup.add(new JSeparator(JSeparator.HORIZONTAL));

		mItem = new JMenuItem(Messages.getString("Delete"));
		mItem.setActionCommand("DeleteCategory");
		mItem.addActionListener(new PopupMenuHandler());
		popup.add(mItem);

		popup.pack();
		popup.show(accountsTree, x, y);
	}

	class PopupMenuHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			final AccountCategory ac = (AccountCategory) accountsTree
					.getLastSelectedPathComponent();

			if (ac == null)
				return;
			Account a = model.getAccount(ac.getCategoryId());
			if (cmd.equals("All transactions")) {
				PageControlPanel instance = PageControlPanel
						.getInstance(parent);
				instance.makePageRequest(instance.getDefaultFilter());
			}
			if (cmd.equals("Add account")) {
				parent.showNewAccountDialog(ac);
				return;
			}
			if (cmd.equals("Add transaction")) {
				parent.showNewTransactionDialog();
				return;
			}
			if (cmd.equals("AddTxForAccount")) {
				if (a == null)
					return;
				parent.showNewTransactionDialog(a);
				return;
			}
			if (cmd.equals("Account History")) {
				if (a == null)
					return;
				loadAccountHistory(a);
				return;
			}
			if (cmd.equals("Summary")) {
				if (a == null)
					return;
				parent.updateSummaryTab(a);

				return;
			}
			if (cmd.equals("Delete")) {
				if (a == null)
					return;
				ActionRequest req = new ActionRequest();
				req.setUser(user);
				req.setActionName("deleteAccount");
				req.setProperty("ACCOUNTID", a.getAccountId());
				DeleteAccountAction d = new DeleteAccountAction();
				ActionResponse resp = null;
				try {
					if (showConfirmDialog(Messages
							.getString("Are you sure you want to delete account: ")
							+ a.getAccountName())) {
						resp = d.executeAction(req);
						if (resp.getErrorCode() == ActionResponse.NOERROR) {
							model.removeAccount(a);
							parent.updateSummary();
							// parent.reloadTxHistory(null); // use default
							// filter to reload all transactions
							PageControlPanel instance = PageControlPanel
									.getInstance(parent);
							instance.makePageRequest(instance
									.getDefaultFilter());
						}
						else {
							showErrorDialog(resp.getErrorMessage());
						}
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
					if (resp != null)
						showErrorDialog(resp.getErrorMessage());
					else
						showErrorDialog(Messages
								.getString("Error deleting account"));
				}
				return;
			}
			if (cmd.equals("Setup online access")) {
				if (a == null)
					return;
				parent.showOnlAccessSetupDialog(a);
			}
			if (cmd.equals("EditAccount")) {
				if (a == null)
					return;
				parent.showEditAccountDialog(a);
			}
			if (cmd.equals("ExportAccountTab")) {
				if (a == null)
					return;
				exportAccount(a, cmd);
				return;
			}
			if (cmd.equals("ExportAccountCsv")) {
				if (a == null)
					return;
				exportAccount(a, cmd);
				return;
			}
			if (cmd.equals("LockAccount")) {
				if (a == null)
					return;
				setAccountStatus(a, AccountTypes.ACCOUNT_LOCKED);
			}
			if (cmd.equals("UnLockAccount")) {
				if (a == null)
					return;
				setAccountStatus(a, AccountTypes.ACCOUNT_ACTIVE);
			}
			if (cmd.equals("AddAlert")) {
				if (a == null)
					return;
				parent.showAddAlertDialog(SetupAlertDialog.ADD, a);
			}
			if (cmd.equals("EditAlert")) {
				if (a == null)
					return;
				parent.showAddAlertDialog(SetupAlertDialog.UPDATE, a);
			}
			if (cmd.equals("AccountAddress")) {
				if (a == null)
					return;
				parent.showAddContactDialog();
			}
			if (cmd.equals("RemoveAlert")) {
				if (a == null)
					return;
				removeAlert(a);
			}
			if (cmd.equals("Download statement")) {
				if (a == null)
					return;
				try {
					ONLBDetails details = em.getONLBFromAccountId(a
							.getAccountId());
					if (details == null) {
						showErrorDialog(Messages
								.getString("Account not setup for online access"));
					}
					else {
						StatementDownloadDialog d = new StatementDownloadDialog(
								parent, user, details);
						d.pack();
						d.setLocationRelativeTo(parent);
						d.setVisible(true);
					}
				}
				catch (Exception ex) {
					logger.error(ex.getMessage());
				}
			}
			if (cmd.equals("DeleteCategory")) {
				if (!model.isEmpty(ac)) {
					// showErrorDialog("Category has either sub categories or accounts defined within it");
					JOptionPane op = FinanceManagerUI.getNarrowOptionPane(75);
					op.setMessageType(JOptionPane.ERROR_MESSAGE);
					op.setMessage(Messages
							.getString("This category has either sub categories or accounts defined under it.\n"
									+ "Please delete the sub entities first before deleting this category"));
					JDialog dialog = op.createDialog(parent,
							Messages.getString("Error"));
					dialog.pack();
					dialog.setVisible(true);
				}
				else {
					boolean confirm = showConfirmDialog(Messages
							.getString("Delete category")
							+ ": "
							+ ac.getCategoryName() + " ?");
					if (!confirm)
						return;

					ActionRequest req = new ActionRequest();
					req.setActionName("deleteCategory");
					req.setProperty("ACCOUNTCATEGORY", ac);

					DeleteCategoryAction action = new DeleteCategoryAction();
					ActionResponse resp = action.execute(req);
					if (resp.getErrorCode() == ActionResponse.NOERROR) {
						model.removeCategory(ac);
					}
					else {
						showErrorDialog(resp.getErrorMessage()
								+ " [error code " + resp.getErrorCode() + "]");
					}
				}
			}
			if (cmd.equals("RenameCategory")) {
				final String s = (String) JOptionPane.showInputDialog("Name");
				if (s == null)
					return;

				SwingWorker worker = new SwingWorker<ActionResponse, Void>() {
					public ActionResponse doInBackground() throws Exception {
						ActionRequest req = new ActionRequest();
						req.setActionName("renameCategory");
						req.setProperty("CATEGORYNAME", s);
						req.setProperty("ACCOUNTCATEGORY", ac);

						ActionResponse resp = new RenameCategoryAction()
								.execute(req);
						return resp;
					}

					public void done() {
						try {
							ActionResponse resp = get();
							if (resp.getErrorCode() == ActionResponse.NOERROR) {
								model.updateCategory(ac);
							}
							else {
								showErrorDialog(resp.getErrorMessage()
										+ " [error code " + resp.getErrorCode()
										+ "]");
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				worker.execute();
			}
			if (cmd.equals("AddSubcategory")) {
				final String s = (String) JOptionPane.showInputDialog(Messages
						.getString("Name"));
				if (s == null)
					return;

				SwingWorker worker = new SwingWorker<ActionResponse, Void>() {
					public ActionResponse doInBackground() throws Exception {
						ActionRequest req = new ActionRequest();
						req.setActionName("addCategory");
						req.setProperty("CATEGORYNAME", s);
						req.setProperty("PARENTCATEGORY", ac);

						ActionResponse resp = new AddCategoryAction()
								.execute(req);
						return resp;
					}

					public void done() {
						try {
							ActionResponse resp = get();
							if (resp.getErrorCode() == ActionResponse.NOERROR) {
								AccountCategory newcategory = (AccountCategory) resp
										.getResult("NEWCATEGORY");
								model.addCategory(newcategory);
							}
							else {
								showErrorDialog(resp.getErrorMessage()
										+ " [error code " + resp.getErrorCode()
										+ "]");
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				worker.execute();
			}

			//
			if (cmd.equals("CategoryHistory")) {
				loadCategoryHistory(ac);
			}
			//

			// Account blanace dialog
			if (cmd.equals("Graph")) {
				if (a == null)
					return;
				parent.showBalanceDialog(a);
			}
			//

			//
			if (cmd.equals("SetIcon")) {
				importIcon(ac);
			}
			//
		}
	}

	private void loadCategoryHistory(AccountCategory ac) {
		if (model.isRoot(ac)) {
			PageControlPanel instance = PageControlPanel.getInstance(parent);
			instance.makePageRequest(instance.getDefaultFilter());
			return;
		}
		List<AccountCategory> list = model.getChildCategories(ac);
		Filter filter = NewFilterUtils.getCategoryFilter(ac.getCategoryName(),
				list);
		filter.addOrder(new Order("Date", Order.DESC));
		filter.addOrder(new Order("createDate", Order.DESC));

		try {
			PageControlPanel.getInstance(parent).makePageRequest(filter);
		}
		catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return;
	}

	private boolean showConfirmDialog(String msg) {
		int n = JOptionPane.showConfirmDialog(parent, msg,
				Messages.getString("Confirm"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		return n == JOptionPane.YES_OPTION;
	}

	public void showErrorDialog(String msg) {
		JOptionPane.showMessageDialog(parent, msg, Messages.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
	}

	private void handleMouseEvent(Object object, MouseEvent e) {
		accountsTree.setSelectionRow((Integer) object);
		AccountCategory ac = (AccountCategory) accountsTree
				.getLastSelectedPathComponent();
		if (ac == null)
			return;
		Account a = model.getAccount(ac.getCategoryId());
		if (e.isPopupTrigger()) {
			if (a == null) {
				if (ac.getCategoryId() == AccountTypes.ACCT_TYPE_ROOT)
					showCategoryPopup((int) e.getX(), (int) e.getY());
				else {
					showGeneralCategoryPopup((int) e.getX(), (int) e.getY());
				}
			}
			else {
				showMenu(a, (int) e.getX(), (int) e.getY());
			}
		}
	}

	private void updateSummaryTab(Account a) {
		if (a == null)
			return;
		parent.updateSummaryTab(a);
		return;
	}

	private void removeAlert(Account a) {
		ActionRequest req = new ActionRequest();
		req.setActionName("removeAlertAction");
		req.setProperty("ACCOUNTID", a.getAccountId());

		RemoveAlertAction action = new RemoveAlertAction();
		try {
			ActionResponse response = action.executeAction(req);
			if (response.getErrorCode() != ActionResponse.NOERROR) {
				showErrorDialog(response.getErrorMessage());
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void loadAccountHistory(Account a) {
		Filter filter = NewFilterUtils.getAccountFilter(a);
		filter.addOrder(new Order("Date", Order.DESC));
		filter.addOrder(new Order("createDate", Order.DESC));

		// Filter filter = FilterUtils.getDecoratorFilter("228,152,121");
		// filter.addOrder(new Order("Date", Order.DESC));
		// filter.addOrder(new Order("createDate", Order.DESC));

		try {
			PageControlPanel.getInstance(parent).makePageRequest(filter);
		}
		catch (Exception ex) {
			logger.error(MiscUtils.stackTrace2String(ex));
		}
	}

	private void setAccountStatus(Account a, int status) {
		a.setStatus(status);
		ActionRequest req = new ActionRequest();
		req.setUser(user);
		req.setActionName("updateAccount");
		req.setProperty("ACCOUNT", a);

		UpdateAccountAction action = new UpdateAccountAction();
		ActionResponse resp = null;
		try {
			resp = action.executeAction(req);
			if (!resp.hasErrors()) {
				parent.reloadAccountList(a, false);
			}
		}
		catch (Exception ex) {
			if (resp != null)
				showErrorDialog(resp.getErrorMessage());
			else
				showErrorDialog(Messages.getString("Error updating account"));
		}
		return;
	}

	public Account getSelectedAccount() {
		final AccountCategory ac = (AccountCategory) accountsTree
				.getLastSelectedPathComponent();

		if (ac == null)
			return null;
		Account a = model.getAccount(ac.getCategoryId());
		return a;
	}

	private void exportAccount(Account a, String type) {
		try {
			JFileChooser fc = new JFileChooser();

			int returnVal = fc.showSaveDialog(parent);
			File sel = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				sel = fc.getSelectedFile();
				if (sel.exists()) {
					int ans = JOptionPane.showConfirmDialog(parent,
							tr("File with same name already exists. Replace?"),
							tr("Save As"), JOptionPane.YES_NO_OPTION);

					if (ans != JOptionPane.OK_OPTION) {
						return;
					}
				}
				ActionRequest req = new ActionRequest();
				req.setActionName("exportAccountToTabFile");
				req.setProperty("ACCOUNT", a);
				req.setProperty("DESTFILE", sel.getAbsolutePath());
				req.setUser(this.user);
				if (type.equals("ExportAccountTab")) {
					ExportAccountToTabFileAction action = new ExportAccountToTabFileAction();
					ActionResponse resp = action.executeAction(req);
				}
				else if (type.equals("ExportAccountCsv")) {
					ExportAccountToCsvFileAction action = new ExportAccountToCsvFileAction();
					// ActionResponse resp = action.executeAction(req);
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void importIcon(AccountCategory ac) {
		try {
			File curr = new File("./icons/ec/");
			JFileChooser fc = new JFileChooser(curr);
			fc.addChoosableFileFilter(new ImageFilter());
			fc.setAcceptAllFileFilterUsed(false);

			ImagePreview preview = new ImagePreview(fc);
			fc.setAccessory(preview);
			fc.addPropertyChangeListener(preview);

			int returnVal = fc.showOpenDialog(parent);
			File sel = null;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				sel = fc.getSelectedFile();
				ImportCategoryIcon ici = new ImportCategoryIcon();
				if (!ici.importIcon(ac, sel)) {
					this.showErrorDialog(ici.getErrMsg());
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	class MTreeModelListener implements TreeModelListener {

		public void treeNodesChanged(TreeModelEvent e) {
			// TODO Auto-generated method stub

		}

		public void treeNodesInserted(final TreeModelEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					accountsTree.expandPath(e.getTreePath());
				}
			});
		}

		public void treeNodesRemoved(final TreeModelEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					accountsTree.expandPath(e.getTreePath());
				}
			});
		}

		public void treeStructureChanged(final TreeModelEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					accountsTree.expandPath(e.getTreePath());
				}
			});
		}
	}

	private void handleAccountsTreeSelection(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();
		if (path == null)
			return;

		accountsTree.expandPath(path);

		AccountCategory ac = (AccountCategory) path.getLastPathComponent();
		if (ac == null)
			return;
		Account a = model.getAccount(ac.getCategoryId());
		parent.switchView(FinanceManagerUI.TX_HISTORY_VIEW_NAME);
		if (a != null) {
			loadAccountHistory(a);
			updateSummaryTab(a);
		}
		else {
			loadCategoryHistory(ac);
		}
	}

	class MouseHandler extends java.awt.event.MouseAdapter {
		public void mousePressed(MouseEvent e) {
			handleEvent(e);
		}

		public void mouseReleased(MouseEvent e) {
			handleEvent(e);
		}

		public void mouseClicked(MouseEvent e) {
			handleEvent(e);
		}

		private void handleEvent(MouseEvent e) {
			Object object = accountsTree.getRowForLocation((int) e.getX(),
					(int) e.getY());
			handleMouseEvent(object, e);
		}
	}

	static class ImageFilter extends FileFilter {

		// Accept all directories and all gif, jpg, tiff, or png files.
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			int pos = f.getName().lastIndexOf('.');
			String extension = null;
			if (pos >= 0 && pos + 1 < f.getName().length())
				extension = f.getName().substring(pos + 1).toLowerCase();

			if (extension != null) {
				if (extension.equals("tiff") || extension.equals("tif")
						|| extension.equals("gif") || extension.equals("jpeg")
						|| extension.equals("jpg") || extension.equals("png")) {
					return true;
				}
				else {
					return false;
				}
			}

			return false;
		}

		// The description of this filter
		public String getDescription() {
			return "Image files";
		}
	}
}
