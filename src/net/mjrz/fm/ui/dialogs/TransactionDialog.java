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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MaskFormatter;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.actions.GetAccountListAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.exceptions.InvalidAccountException;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.help.HelpUtil;
import net.mjrz.fm.ui.panels.AttachmentsPanel;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.AccountsTreeModel;
import net.mjrz.fm.ui.utils.CalendarWidget;
import net.mjrz.fm.ui.utils.CalendarWidgetListener;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.MyTabPaneUI;
import net.mjrz.fm.ui.utils.REditor;
import net.mjrz.fm.ui.utils.SpringUtilities;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class TransactionDialog extends JDialog implements ItemListener,
		CalendarWidgetListener {
	private JTextField txAmtTf;
	private JFormattedTextField txDateTf;

	// private JTextArea txNotesTa;
	private REditor txNotesTa;

	private JButton newTxB, cancelB, helpB, splitB;
	private JTree fromAccountTree, toAccountTree;
	private FinanceManagerUI parent = null;
	private User user = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private MaskFormatter dateMaskFormatter;
	private JToggleButton calButton;
	private CalendarWidget calendar;
	private AttachmentsPanel attachmentsPanel;

	private JTable txSplitTable = null;
	private MyTableModel tableModel = null;

	private String searchTargetTree = null;

	private ArrayList<Transaction> txList;

	private JTabbedPane detailsTab = null;
	private FManEntityManager em = new FManEntityManager();

	private String errorMsg = null;

	private StringBuilder searchString = null;

	// boolean clone = false;

	static final long serialVersionUID = 0L;

	public static final int NEW_TX = 1;
	// public static final int EDIT_TX = 2;
	// public static final int CLONE_TX = 3;
	// public static final int VIEW_TX = 4;

	private int txType = 1;

	private static Logger logger = Logger.getLogger(TransactionDialog.class
			.getName());

	public TransactionDialog() {
		initialize();
	}

	public TransactionDialog(JFrame parent, User u) {
		super(parent, "Transaction", true);

		this.parent = (FinanceManagerUI) parent;
		this.user = u;
		txType = NEW_TX;
		initialize();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public void setUser(User u) {
		this.user = u;
	}

	private void initDateMaskFormatter() {
		try {
			dateMaskFormatter = new MaskFormatter("####-##-##");
			dateMaskFormatter.setPlaceholderCharacter('#');
		}
		catch (Exception e) {
			// Ignore exception
		}
	}

	private void initialize() {
		txList = new ArrayList<Transaction>();
		searchString = new StringBuilder();

		initDateMaskFormatter();

		initLayout();
		setPreferredSize(new Dimension(600, 500));

		switch (txType) {
		case NEW_TX:
			this.setTitle(tr("New transaction"));
			break;
		default:
			this.setTitle(tr("Transaction"));
		}

		GuiUtilities.addWindowClosingActionMap(this);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				TransactionDialog.this.updateSearchString(e);
				JTree tree = null;
				if (searchTargetTree == null || searchTargetTree.equals("from")) {
					tree = fromAccountTree;
				}
				else if (searchTargetTree.equals("to")) {
					tree = toAccountTree;
				}
				doSearch(tree);
			}
		});
	}

	private void initLayout() {
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.weightx = 0.6;
		// g1.weighty = 0.4;
		g1.fill = GridBagConstraints.BOTH;
		cp.add(getAccountsPane(), g1);

		GridBagConstraints g2 = new GridBagConstraints();
		g2.gridx = 0;
		g2.gridy = 1;
		g2.fill = GridBagConstraints.HORIZONTAL;
		cp.add(getTxDatePane(), g2);

		GridBagConstraints g3 = new GridBagConstraints();
		g3.gridx = 0;
		g3.gridy = 2;
		g3.fill = GridBagConstraints.BOTH;
		g3.weightx = 0.4;
		g3.weighty = 1;
		cp.add(getNotesPane(), g3);

		GridBagConstraints g4 = new GridBagConstraints();
		g4.gridx = 0;
		g4.gridy = 3;
		g4.fill = GridBagConstraints.HORIZONTAL;
		cp.add(getButtonPane(), g4);
	}

	private JTabbedPane getNotesPane() {
		detailsTab = new JTabbedPane();
		detailsTab.setUI(new MyTabPaneUI());

		detailsTab.addTab("Notes", getTxNotesPane());
		detailsTab.addTab("Splits", getTxSplitPanel());
		detailsTab.addTab("Attachments", getAttachmentsPanel());

		return detailsTab;
	}

	private JPanel getAccountsPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		final AccountsTreeModel from = populateFromTree();
		if (from != null)
			fromAccountTree = new NavigableTree(from);
		else
			fromAccountTree = new NavigableTree();

		fromAccountTree.setVisibleRowCount(10);
		fromAccountTree.setEditable(false);
		fromAccountTree.setCellRenderer(new TreeCellRenderer());
		fromAccountTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		fromAccountTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				searchTargetTree = "from";
			}
		});

		JScrollPane sp1 = new JScrollPane(fromAccountTree);
		sp1.setBorder(BorderFactory.createTitledBorder("Account"));

		AccountsTreeModel to = populateToTree();
		if (to != null)
			toAccountTree = new NavigableTree(to);
		else
			toAccountTree = new NavigableTree();

		toAccountTree.setVisibleRowCount(10);
		toAccountTree.setEditable(false);
		toAccountTree.setCellRenderer(new TreeCellRenderer());
		toAccountTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		toAccountTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				searchTargetTree = "to";
				doTreeSelectionValueChanged(e, toAccountTree);
			}
		});

		JScrollPane sp2 = new JScrollPane(toAccountTree);
		sp2.setBorder(BorderFactory.createTitledBorder("Payee"));

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.BOTH;
		g1.weightx = 0.5;
		g1.weighty = 0.5;
		ret.add(sp1, g1);

		GridBagConstraints g2 = new GridBagConstraints();
		g2.gridx = 1;
		g2.gridy = 0;
		g2.anchor = GridBagConstraints.LINE_START;
		g2.fill = GridBagConstraints.BOTH;
		g2.weightx = 0.5;
		g2.weighty = 0.5;
		ret.add(sp2, g2);

		return ret;
	}

	private void doTreeSelectionValueChanged(TreeSelectionEvent e, JTree tree) {
		if (txList != null && txList.size() > 0) {
			detailsTab.setSelectedIndex(1);
		}
	}

	private JPanel getTxDatePane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		ret.add(new JLabel("Amount"));
		txAmtTf = new JTextField(10);
		net.mjrz.fm.ui.utils.GuiUtilities.setupTextComponent(txAmtTf);
		txAmtTf.setPreferredSize(new Dimension(100, 25));
		ret.add(txAmtTf);
		ret.add(Box.createHorizontalStrut(25));

		ret.add(new JLabel(tr("Date")));
		txDateTf = new JFormattedTextField(dateMaskFormatter);
		txDateTf.setText(sdf.format(new Date()));
		txAmtTf.setPreferredSize(new Dimension(100, 25));
		net.mjrz.fm.ui.utils.GuiUtilities.setupTextComponent(txDateTf);

		calButton = new JToggleButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/calendar.png"));
		calButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		calButton.setHorizontalAlignment(JButton.LEADING); // optional
		calButton.setBorderPainted(false);
		calButton.setContentAreaFilled(false);
		calButton.addItemListener(this);
		ret.add(txDateTf);
		ret.add(calButton);

		splitB = new JButton(tr("Add"));
		splitB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSplitButtonActionPerformed(e);
			}
		});
		ret.add(Box.createHorizontalGlue());
		ret.add(splitB);

		return ret;
	}

	private void doSplitButtonActionPerformed(ActionEvent e) {
		try {
			AccountCategory fromNode = (AccountCategory) fromAccountTree
					.getLastSelectedPathComponent();
			AccountCategory toNode = (AccountCategory) toAccountTree
					.getLastSelectedPathComponent();

			if (toNode == null) {
				throw new InvalidAccountException("No account selected");
			}
			Account from = ((AccountsTreeModel) fromAccountTree.getModel())
					.getAccount(fromNode.getCategoryId());
			Account to = ((AccountsTreeModel) toAccountTree.getModel())
					.getAccount(toNode.getCategoryId());

			BigDecimal txAmt = new BigDecimal(txAmtTf.getText());
			Date txDt = sdf.parse(txDateTf.getText());

			for (Transaction t : txList) {
				if (t.getToAccountId() == to.getAccountId()) {
					ArrayList<Object> row = new ArrayList<Object>();
					row.add(to);
					row.add(txAmt);

					t.setTxAmount(txAmt);
					t.setTxDate(txDt);
					tableModel.updateRow(row);
					return;
				}
			}
			ArrayList<Object> row = new ArrayList<Object>();
			row.add(to);
			row.add(txAmt);

			tableModel.addRow(row);
			fromAccountTree.setEnabled(false);
			calButton.setEnabled(false);
			this.txDateTf.setEditable(false);

			Transaction t = new Transaction();
			t.setCreateDate(new Date());
			t.setTxDate(txDt);
			t.setFromAccountId(from.getAccountId());
			t.setToAccountId(to.getAccountId());
			t.setInitiatorId(user.getUid());
			t.setTxAmount(txAmt);
			t.setTxNotes(txNotesTa.getPlainText());
			t.setTxNotesMarkup(txNotesTa.getText());
			t.setActivityBy("Created by user activity");

			txList.add(t);
			detailsTab.setSelectedIndex(1);
		}
		catch (InvalidAccountException ex) {
			JOptionPane.showMessageDialog(TransactionDialog.this,
					tr("Invalid account selection"), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(TransactionDialog.this,
					tr("Invalid number for transaction amount"), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		catch (java.text.ParseException ex) {
			JOptionPane.showMessageDialog(TransactionDialog.this,
					tr("Invalid date"), "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex) {
			logger.error(MiscUtils.stackTrace2String(ex));
		}
	}

	@SuppressWarnings("serial")
	private JPanel getTxSplitPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new SpringLayout());

		tableModel = new MyTableModel();
		txSplitTable = new JTable() {
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				JLabel c = (JLabel) super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (isCellSelected(rowIndex, vColIndex)) {
					java.awt.Font f = c.getFont();
					c.setFont(new java.awt.Font(f.getName(),
							java.awt.Font.BOLD, f.getSize()));
					c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
					c.setBorder(BorderFactory.createEmptyBorder());
					return c;
				}
				if (rowIndex % 2 != 0) {
					c.setBackground(new Color(234, 234, 234));
				}
				else {
					c.setBackground(Color.WHITE);
				}
				return c;
			}

			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(getWidth(), 60);
			}
		};
		txSplitTable.setModel(tableModel);
		txSplitTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		txSplitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		txSplitTable.getSelectionModel().addListSelectionListener(
				new TableSelectionListener());
		txSplitTable.addMouseListener(new MouseHandler());

		JScrollPane sp = new JScrollPane(txSplitTable);

		txSplitTable.getParent().setBackground(
				UIDefaults.DEFAULT_PANEL_BG_COLOR);

		ret.add(sp);
		SpringUtilities.makeCompactGrid(ret, 1, 1, 0, 0, 10, 10);
		return ret;
	}

	private JPanel getTxNotesPane() {
		JPanel txPane = new JPanel();
		txPane.setLayout(new BoxLayout(txPane, BoxLayout.PAGE_AXIS));

		txNotesTa = new REditor();

		txPane.add(txNotesTa);

		Border border = BorderFactory.createTitledBorder(tr("Notes"));
		txPane.setBorder(border);
		return txPane;
	}

	private JPanel getButtonPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		newTxB = new JButton(tr("Save"));
		newTxB.setActionCommand("Save");
		newTxB.setMinimumSize(new Dimension(80, 20));
		newTxB.setMnemonic(KeyEvent.VK_S);
		newTxB.addActionListener(new ButtonHandler());
		getRootPane().setDefaultButton(newTxB);

		cancelB = new JButton(tr("Close"));
		cancelB.setActionCommand("Close");

		cancelB.setMinimumSize(new Dimension(80, 20));
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ButtonHandler());

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.weightx = 0.8;
		g1.fill = GridBagConstraints.BOTH;
		ret.add(Box.createHorizontalGlue(), g1);

		g1.gridx = 2;
		g1.gridy = 0;
		g1.weightx = 0.1;
		g1.fill = GridBagConstraints.BOTH;
		g1.insets = new Insets(1, 3, 1, 3);
		ret.add(newTxB, g1);

		g1.gridx = 3;
		g1.gridy = 0;
		g1.weightx = 0.1;
		g1.insets = new Insets(1, 3, 1, 3);
		g1.fill = GridBagConstraints.BOTH;
		ret.add(cancelB, g1);

		return ret;
	}

	private void updateSearchString(KeyEvent e) {
		int code = e.getKeyCode();
		if (code == KeyEvent.VK_BACK_SPACE) {
			searchString.deleteCharAt(searchString.length() - 1);
		}
		else if (code == KeyEvent.VK_F3) {
			return;
		}
		else {
			char c = e.getKeyChar();
			if (MiscUtils.isPrintableChar(c)) {
				searchString.append(e.getKeyChar());
			}
		}
	}

	private void doSearch(JTree tree) {
		if (tree == null)
			return;

		AccountsTreeModel model = (AccountsTreeModel) tree.getModel();
		TreePath path = model.getNearestMatch(searchString.toString());
		if (path != null) {
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(path);
			tree.scrollPathToVisible(path);
			tree.updateUI();
		}
	}

	private JPanel getAttachmentsPanel() {
		attachmentsPanel = new AttachmentsPanel(txType);
		return attachmentsPanel;
	}

	private AccountsTreeModel populateFromTree() {
		try {
			GetAccountListAction action = new GetAccountListAction();
			ActionResponse resp = action.getAccountList(user);
			java.util.List accountList = (java.util.List) resp
					.getResult("RESULTSET");

			AccountsTreeModel model = new AccountsTreeModel(user.getUid(),
					tr("Account"), accountList);
			return model;
		}
		catch (Exception e) {
			return null;
		}
	}

	private AccountsTreeModel populateToTree() {
		try {
			GetAccountListAction action = new GetAccountListAction();
			ActionResponse resp = action.getAccountList(user);
			java.util.List accountList = (java.util.List) resp
					.getResult("RESULTSET");

			AccountsTreeModel model = new AccountsTreeModel(user.getUid(),
					tr("Payee"), accountList);
			return model;
		}
		catch (Exception e) {
			return null;
		}
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Close")) {
				dispose();
			}
			if (cmd.equals("Help")) {
				HelpUtil.loadHelpPage(TransactionDialog.class.getName());
			}
			if (cmd.equals("Save")) {
				errorMsg = null;
				if (txList == null || txList.size() == 0) {
					Transaction t = getTransactionObj();
					if (t == null) {
						JOptionPane
								.showMessageDialog(
										TransactionDialog.this,
										(errorMsg != null && errorMsg.length() > 0) ? errorMsg
												: "Error in input", "Error",
										JOptionPane.ERROR_MESSAGE);
						logger.error("Unable to create transaction");
						return;
					}
					txList.add(t);
				}
				createNestedTransaction();
			}
		}
	}

	private Transaction getTransactionObj() {
		try {
			BigDecimal txAmt = new BigDecimal(txAmtTf.getText());

			AccountCategory fromNode = (AccountCategory) fromAccountTree
					.getLastSelectedPathComponent();
			AccountCategory toNode = (AccountCategory) toAccountTree
					.getLastSelectedPathComponent();

			if (fromNode == null) {
				errorMsg = "Please select source account";
				return null;
			}
			if (toNode == null) {
				errorMsg = "Please select payee account";
				return null;
			}
			Account from = ((AccountsTreeModel) fromAccountTree.getModel())
					.getAccount(fromNode.getCategoryId());
			Account to = ((AccountsTreeModel) toAccountTree.getModel())
					.getAccount(toNode.getCategoryId());

			if (from == null || to == null) {
				// result.setErrorMessage("Selected account is invalid");
				return null;
			}

			Date txDt = sdf.parse(txDateTf.getText());

			Transaction t = new Transaction();
			t.setCreateDate(new Date());
			t.setTxDate(txDt);
			t.setFromAccountId(from.getAccountId());
			t.setToAccountId(to.getAccountId());
			t.setInitiatorId(user.getUid());
			t.setTxAmount(txAmt);
			t.setTxNotes(txNotesTa.getPlainText());
			t.setTxNotesMarkup(txNotesTa.getText());
			t.setActivityBy("Created by user activity");
			return t;
		}
		catch (NumberFormatException e) {
			errorMsg = "Invalid value for amount";
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
		catch (ParseException e) {
			errorMsg = "Invalid value for date";
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	private void syncFields() throws Exception {
		syncAmtField();
		int rowCount = tableModel.getRowCount();
		Date txDt = sdf.parse(txDateTf.getText());
		if (rowCount > 0) {
			for (int i = 0; i < rowCount; i++) {
				Account a = (Account) tableModel.getValueAt(i, 0);

				BigDecimal val = (BigDecimal) tableModel.getValueAt(i, 1);

				for (Transaction t : txList) {
					if (t.getToAccountId() == a.getAccountId()) {
						t.setTxAmount(val);
						t.setTxDate(txDt);
					}
					t.setTxNotes(txNotesTa.getText());
				}
			}
		}
	}

	private void syncAmtField() throws Exception {
		Account to = getSelectedItem(toAccountTree);
		if (to == null)
			return;

		int rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Account a = (Account) tableModel.getValueAt(i, 0);
			if (a.getAccountId() == to.getAccountId()) {
				tableModel.setValueAt(new BigDecimal(txAmtTf.getText()), i, 1);
			}
		}
	}

	private Account getSelectedItem(JTree tree) {
		AccountCategory node = (AccountCategory) tree
				.getLastSelectedPathComponent();

		if (node == null)
			return null;

		Account acct = ((AccountsTreeModel) tree.getModel()).getAccount(node
				.getCategoryId());

		return acct;
	}

	private void createNestedTransaction() {
		try {
			syncFields();
			newTxB.setEnabled(false);
			cancelB.setEnabled(false);
			SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
				public ActionResponse doInBackground() throws Exception {
					ActionResponse result = new ActionResponse();
					try {
						java.util.List<String> atList = attachmentsPanel
								.getAttachmentsList();

						int sz = txList.size();
						for (int i = 0; i < sz; i++) {
							if (i == 0 && sz > 1) {
								txList.get(i).setIsParent(
										TT.IsParent.YES.getVal());
							}
							else {
								txList.get(i).setIsParent(
										TT.IsParent.NO.getVal());
							}
						}

						ActionRequest req = new ActionRequest();
						req.setActionName("addTransaction");
						req.setUser(user);
						req.setProperty("TXLIST", txList);

						if (atList != null)
							req.setProperty("ATTACHMENTS", atList);

						req.setProperty("UPDATETX", Boolean.valueOf(false));

						AddNestedTransactionsAction action = new AddNestedTransactionsAction();
						result = action.executeAction(req);
						return result;
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(TransactionDialog.this,
								"Invalid number for transaction amount",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
					return result;
				}

				public void done() {
					try {
						ActionResponse result = (ActionResponse) get();
						if (result == null)
							return;
						if (result.getErrorCode() == ActionResponse.NOERROR) {
							Long sourceAcctId = (Long) result
									.getResult("SOURCEACCOUNTID");
							PageControlPanel instance = PageControlPanel
									.getInstance(parent);
							instance.makePageRequest(instance
									.getDefaultFilter());
							parent.updateStatusPane();
							if (sourceAcctId != null) {
								parent.reloadAccountList(sourceAcctId, false);
							}
							for (Transaction t : txList) {
								parent.updateAccountInTree(t.getToAccountId());
							}
							dispose();

						}
						else {
							JOptionPane.showMessageDialog(parent,
									result.getErrorMessage(), tr("Error"),
									JOptionPane.ERROR_MESSAGE);
							txList.clear();
							tableModel.clear();
						}
					}
					catch (Exception ex) {
						return;
					}
					finally {
						newTxB.setEnabled(true);
						cancelB.setEnabled(true);
					}
				}
			};
			worker.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					tr("Unable to save transaction") + e.getMessage(),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	static class TreeCellRenderer extends DefaultTreeCellRenderer {
		static final long serialVersionUID = 0L;
		ImageIcon categoryIcon, accountIcon;

		public TreeCellRenderer() {
			categoryIcon = new net.mjrz.fm.ui.utils.MyImageIcon(
					"icons/category.png");
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			try {
				AccountsTreeModel model = (AccountsTreeModel) tree.getModel();
				AccountCategory ac = (AccountCategory) value;

				if (model.isAccount(ac.getCategoryId())) {
					Account a = model.getAccount(ac.getCategoryId());
					if (a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY
							|| a.getAccountType() == AccountTypes.ACCT_TYPE_EXPENSE)
						setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
								"icons/money_delete.png"));
					else
						setIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
								"icons/money_add.png"));
				}
				else {
					setIcon(categoryIcon);
				}
			}
			catch (Exception e) {
				logger.error(e);
				setIcon(categoryIcon);
			}
			return this;
		}
	}

	public void setDate(Date date) {
		if (date != null) {
			this.txDateTf.setText(sdf.format(date));
		}
		this.calButton.setSelected(false);
	}

	public void setAccount(Account a) {
		int type = a.getAccountType();
		if (type == AccountTypes.ACCT_TYPE_CASH
				|| type == AccountTypes.ACCT_TYPE_INCOME
				|| type == AccountTypes.ACCT_TYPE_LIABILITY) {

			AccountsTreeModel model = (AccountsTreeModel) fromAccountTree
					.getModel();
			AccountCategory container = model.getAccountCategory(a
					.getAccountId());
			TreePath fromPath = model.getPath(container);

			if (fromPath != null) {
				fromAccountTree.setExpandsSelectedPaths(true);
				fromAccountTree.setSelectionPath(fromPath);
				fromAccountTree.scrollPathToVisible(fromPath);
				fromAccountTree.updateUI();
			}
		}
		else {
			AccountsTreeModel model = (AccountsTreeModel) toAccountTree
					.getModel();
			AccountCategory container = model.getAccountCategory(a
					.getAccountId());
			TreePath path = model.getPath(container);

			if (path != null) {
				toAccountTree.setExpandsSelectedPaths(true);
				toAccountTree.setSelectionPath(path);
				toAccountTree.scrollPathToVisible(path);
				toAccountTree.updateUI();
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (calButton.isSelected()) {
			PointerInfo pinfo = MouseInfo.getPointerInfo();
			calendar = new CalendarWidget(this.parent, this, false);
			calendar.setLocation(pinfo.getLocation());
			calendar.pack();
			calendar.setSize(new Dimension(210, 200));
			calendar.setVisible(true);
		}
		else {
			if (calendar != null) {
				calendar = null;
			}
		}
	}

	private void setTreeSelection(JTree tree, Account a) {
		AccountsTreeModel model = (AccountsTreeModel) tree.getModel();
		AccountCategory container = model.getAccountCategory(a.getAccountId());
		TreePath fromPath = model.getPath(container);
		if (fromPath != null) {
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionPath(fromPath);
			tree.scrollPathToVisible(fromPath);
			tree.updateUI();
		}
	}

	private static class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private String[] columnNames = { Messages.getString("Payee"),
				Messages.getString("Amount") };

		private ArrayList<ArrayList<Object>> data = new ArrayList<ArrayList<Object>>();

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			if (row >= data.size() || col > columnNames.length)
				return null;

			return data.get(row).get(col);
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			data.get(rowIndex).set(columnIndex, aValue);
		}

		public void updateRow(ArrayList<Object> row) {
			int sz = data.size();
			int i = 0;
			for (; i < sz; i++) {
				ArrayList<Object> dataRow = data.get(i);
				if (dataRow.get(0).equals(row.get(0))) {
					setValueAt(row.get(1), i, 1);
					break;
				}
			}
			this.fireTableCellUpdated(i, 1);
		}

		public void addRow(ArrayList<Object> row) {
			data.add(row);
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}

		public void removeRow(int rowNum) {
			if (rowNum >= data.size())
				return;
			data.remove(rowNum);
			fireTableRowsDeleted(rowNum, rowNum);
		}

		public void clear() {
			data.clear();
			fireTableDataChanged();
		}
	}

	public class TableSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (e.getSource() == txSplitTable.getSelectionModel()
					&& txSplitTable.getRowSelectionAllowed()) {
				if (e.getValueIsAdjusting())
					return;
				int sel = txSplitTable.getSelectionModel()
						.getLeadSelectionIndex();
				if (sel >= 0) {
					sel = txSplitTable.convertRowIndexToModel(sel);
					Object val = tableModel.getValueAt(sel, 0);
					if (val != null) {
						setTreeSelection(toAccountTree, (Account) val);
						txAmtTf.setText(tableModel.getValueAt(sel, 1)
								.toString());
					}
				}
			}
		}
	}

	private void showMenu(int x, int y) {
		JPopupMenu popup;
		popup = new JPopupMenu();

		JMenuItem mItem = new JMenuItem(Messages.getString("Remove"));
		popup.add(mItem);
		mItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = txSplitTable.getSelectedRow();
				if (sel < 0)
					return;
				sel = txSplitTable.convertRowIndexToModel(sel);
				tableModel.removeRow(sel);
			}
		});

		popup.pack();
		popup.show(txSplitTable, x, y);
	}

	class MouseHandler extends java.awt.event.MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			// Do not allow to remove first element.
			if (tableModel.getRowCount() > 1)
				showMenu(e.getX(), e.getY());
		}
	}

	static class NavigableTree extends JTree {
		private static final long serialVersionUID = 1L;

		public NavigableTree() {
			super();
		}

		public NavigableTree(TreeModel model) {
			super(model);
		}

		public TreePath getNextMatch(String prefix, int startingRow,
				Position.Bias bias) {
			AccountsTreeModel model = (AccountsTreeModel) getModel();
			// TreePath path = model.getNearestMatch(searchString.toString());
			TreePath path = model.getNearestMatch(prefix);
			return path;
		}
	}
}
