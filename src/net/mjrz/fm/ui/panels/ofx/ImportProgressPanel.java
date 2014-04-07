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
package net.mjrz.fm.ui.panels.ofx;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddTransactionAction;
import net.mjrz.fm.actions.GetAccountListAction;
import net.mjrz.fm.actions.UpdateAccountAction;
import net.mjrz.fm.actions.ValidateTxAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.AccountsTreeModel;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.TreeCellRenderer;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.Ofx2Xml;
import net.mjrz.fm.utils.XMLProcessor;
import net.mjrz.fm.utils.indexer.Indexer;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ImportProgressPanel extends JDialog {

	private static final long serialVersionUID = 1L;

	private ArrayList<TxObject> txList;

	private User user;

	private FManEntityManager em;

	private FinanceManagerUI parent;

	private JTree accountsTree = null;

	private AccountsTreeModel model = null;

	private XMLProcessor xmlProcessor = null;

	private JDialog accountSelectorDialog = null;

	private static Logger logger = Logger.getLogger(ImportProgressPanel.class
			.getName());

	private TxListPanel txListPanel;

	private transient Reader reader;

	public ImportProgressPanel(FinanceManagerUI parent, User user,
			File importFile) throws Exception {
		this(parent, user);
		try {
			reader = new BufferedReader(new FileReader(importFile));
			initialize(reader);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public ImportProgressPanel(FinanceManagerUI parent, User user, String xml)
			throws Exception {
		this(parent, user);
		try {
			reader = new BufferedReader(new StringReader(xml));
			initialize(reader);
		}
		catch (Exception e) {
			throw e;
		}
	}

	public ImportProgressPanel(FinanceManagerUI parent, User user)
			throws Exception {
		super(parent, false);

		try {
			this.user = user;
			this.parent = parent;
			em = new FManEntityManager();
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void initialize(Reader reader) throws Exception {
		loadTxList(reader);

		txListPanel = new TxListPanel(this);
		txListPanel.addTxObject(txList);

		setLayout(new BorderLayout());

		ImportAccountSummaryPanel iasp = new ImportAccountSummaryPanel();
		iasp.updateAccountSummaryPane(xmlProcessor.getAccount(), txList);
		add(iasp, BorderLayout.NORTH);
		add(txListPanel, BorderLayout.CENTER);

		this.setPreferredSize(new Dimension(850, 600));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GuiUtilities.addWindowClosingActionMap(this);
	}

	public void cleanupAndDispose() {
		// Finally update the ledger balance.
		if (txListPanel.shouldUpdateBalance()) {
			updateLedgerBalance();
		}

		net.mjrz.fm.ui.utils.NotificationHandler.pauseQueue(false);
		dispose();
	}

	void importDone() {
		try {
			PageControlPanel instance = PageControlPanel.getInstance(parent);
			instance.makePageRequest(instance.getDefaultFilter());
			parent.reloadAccountList(AccountTypes.ACCT_TYPE_ROOT);
			parent.updateStatusPane();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}

		Indexer.getIndexer().finalizeIndex(Indexer.IndexType.Account);
	}

	private void updateLedgerBalance() {
		try {
			Account a = xmlProcessor.getAccount();
			BigDecimal ledgerBal = xmlProcessor.getLedgerBalance();
			if (ledgerBal == null)
				return;

			a.setCurrentBalance(ledgerBal);

			ActionRequest req = new ActionRequest();
			req.setUser(user);
			req.setActionName("updateAccount");
			req.setProperty("ACCOUNT", a);
			UpdateAccountAction action = new UpdateAccountAction();
			ActionResponse resp = action.executeAction(req);
			if (resp.getErrorCode() != ActionResponse.NOERROR) {
				logger.error("Failed to update ledger balance during import");
				String msg = resp.getErrorMessage();
				if (msg != null) {
					logger.error(msg);
				}
			}
			else {
				parent.reloadAccountList(a, false);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void loadTxList(Reader reader) throws Exception {
		try {
			if (xmlProcessor == null) {
				Ofx2Xml c = new Ofx2Xml(reader);
				String xml = c.getXmlString();

				xmlProcessor = new XMLProcessor(xml, user);
			}

			String validate = xmlProcessor.validate();
			if (!validate.equals("")) { //$NON-NLS-1$
				throw new Exception(
						Messages.getString("Error importing statement. ") + validate); //$NON-NLS-1$
			}

			Account a = xmlProcessor.getAccount();
			if (a == null) {
				showAccountSelector();
			}
			else {
				xmlProcessor.processXML();
				txList = xmlProcessor.getTxList();
			}
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void showAccountSelector() throws Exception {
		accountSelectorDialog = new JDialog(parent,
				Messages.getString("Select account"), true); //$NON-NLS-1$
		accountSelectorDialog.setLayout(new BorderLayout());
		accountSelectorDialog.setPreferredSize(new Dimension(500, 400));
		accountSelectorDialog.add(getAccountPane(), BorderLayout.CENTER);
		accountSelectorDialog.pack();
		accountSelectorDialog.setLocationRelativeTo(parent);
		accountSelectorDialog.setVisible(true);
	}

	private JPanel getAccountPane() throws Exception {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		populateTree();

		JScrollPane sp = new JScrollPane(accountsTree);
		ret.add(sp);

		JPanel in = new JPanel();
		JButton b = new JButton(Messages.getString("Select")); //$NON-NLS-1$
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AccountCategory ac = (AccountCategory) accountsTree
						.getLastSelectedPathComponent();
				if (ac == null)
					return;
				Account a = model.getAccount(ac.getCategoryId());

				if (a != null) {
					try {
						em.updateAccountNumber(a.getAccountId(),
								xmlProcessor.getAccountNumber());
						loadTxList(reader);
						accountSelectorDialog.dispose();
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(ImportProgressPanel.this,
								Messages.getString("Unknown error"),
								Messages.getString("Error"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JButton c = new JButton(Messages.getString("Close")); //$NON-NLS-1$
		c.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accountSelectorDialog.dispose();
			}
		});

		in.add(b);
		in.add(c);

		ret.add(in);
		ret.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("Account List:"))); //$NON-NLS-1$
		return ret;
	}

	@SuppressWarnings("unchecked")//$NON-NLS-1$
	private void populateTree() {
		try {
			GetAccountListAction action = new GetAccountListAction();
			ActionResponse resp = action.getAccountList(user);
			List accountList = (List) resp.getResult("RESULTSET"); //$NON-NLS-1$

			model = new AccountsTreeModel(user.getUid(),
					Messages.getString("From"), accountList); //$NON-NLS-1$
			if (model != null && accountsTree != null) {
				accountsTree.setModel(model);
			}
			else if (model != null && accountsTree == null) {
				accountsTree = new JTree(model);
				accountsTree.setCellRenderer(new TreeCellRenderer());
				accountsTree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
			}
			else if (model == null && accountsTree == null) {
				accountsTree = new JTree();
				accountsTree.setCellRenderer(new TreeCellRenderer());
				accountsTree.getSelectionModel().setSelectionMode(
						TreeSelectionModel.SINGLE_TREE_SELECTION);
			}
			accountsTree.validate();
			accountsTree.updateUI();
		}
		catch (Exception e) {
			logger.error(e);
			return;
		}
	}

	ActionResponse createTransaction(Transaction t) throws Exception {
		ActionRequest req = new ActionRequest();
		req.setActionName("addTransaction"); //$NON-NLS-1$
		req.setUser(user);
		req.setProperty("TRANSACTION", t); //$NON-NLS-1$
		req.setProperty("UPDATETX", Boolean.valueOf(false)); //$NON-NLS-1$

		AddTransactionAction action = new AddTransactionAction();
		ActionResponse result = action.executeAction(req);

		return result;
	}

	ActionResponse validateTransaction(Transaction t) throws Exception {
		ActionRequest req = new ActionRequest();
		req.setActionName("validateTransaction");
		req.setUser(user);
		req.setProperty("TRANSACTION", t);
		ValidateTxAction action = new ValidateTxAction();
		ActionResponse result = action.executeAction(req);

		return result;
	}

	void showTxEditor(final TxObject txObject) {
		ImportTxEditorPanel panel = new ImportTxEditorPanel(txObject);

		panel.loadPayeeList();

		JDialog txEditorDialog = new JDialog(ImportProgressPanel.this,
				Messages.getString("Edit transaction"), true);

		txEditorDialog.setResizable(false);

		txEditorDialog.setLayout(new BorderLayout());

		txEditorDialog.setPreferredSize(new Dimension(400, 350));

		txEditorDialog.add(panel, BorderLayout.CENTER);

		txEditorDialog.pack();

		txEditorDialog.getRootPane().setDefaultButton(panel.getDefaultButton());

		txEditorDialog.setLocationRelativeTo(parent);

		txEditorDialog.setVisible(true);

		GuiUtilities.addWindowClosingActionMap(txEditorDialog);
	}

	void showErrorDialog(String msg) {
		JOptionPane.showMessageDialog(this, msg, Messages.getString("Error"),
				JOptionPane.ERROR_MESSAGE);
	}
}
