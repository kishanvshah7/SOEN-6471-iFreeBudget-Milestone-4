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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.help.HelpUtil;
import net.mjrz.fm.ui.panels.AttachmentsPanel;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.AccountCbEntry;
import net.mjrz.fm.ui.utils.AccountCbKeySelectionManager;
import net.mjrz.fm.ui.utils.DateChooser;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.MyTabPaneUI;
import net.mjrz.fm.ui.utils.REditor;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NewTransactionDialog extends JDialog {

	static final long serialVersionUID = 1L;

	private JTextField txAmtTf;

	private DateChooser txDateTf;

	private REditor txNotesTa;

	private JButton newTxB, cancelB;

	private FinanceManagerUI parent = null;

	private User user = null;

	private AttachmentsPanel attachmentsPanel;

	private ArrayList<Transaction> txList;

	private JTabbedPane detailsTab = null;

	private FManEntityManager em = new FManEntityManager();

	private String errorMsg = null;

	public static final int NEW_TX = 1;

	private int txType = 1;

	private JRadioButton incomeRb, expenseRb, transferRb;

	private ButtonGroup txTypeButtonGroup;

	private JComboBox fromCb, toCb;

	private JCheckBox addMore;

	/* Account on which transaction is being created */
	private Account account;

	private static Logger logger = Logger.getLogger(NewTransactionDialog.class
			.getName());

	public NewTransactionDialog() {
		initialize();
	}

	public NewTransactionDialog(JFrame parent, User u, Account a) {
		super(parent, "Transaction", true);

		this.parent = (FinanceManagerUI) parent;
		this.user = u;
		txType = NEW_TX;
		initialize();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setAccount(a);
	}

	public NewTransactionDialog(JFrame parent, User u) {
		this(parent, u, null);
	}

	private void initialize() {
		txList = new ArrayList<Transaction>();

		initLayout();

		switch (txType) {
		case NEW_TX:
			this.setTitle(tr("New transaction"));
			break;
		default:
			this.setTitle(tr("Transaction"));
		}

		GuiUtilities.addWindowClosingActionMap(this);
	}

	private void initLayout() {
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.anchor = GridBagConstraints.FIRST_LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.insets = new Insets(10, 10, 0, 10);
		// g1.weightx = 1;
		g1.gridwidth = 3;
		cp.add(getTxTypePanel(), g1);

		g1.gridx = 0;
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.insets = new Insets(10, 10, 0, 10);
		g1.gridwidth = 1;
		g1.weightx = 0.7;
		cp.add(getAccountSelectorsPanel(), g1);

		GridBagConstraints g2 = new GridBagConstraints();
		g2.gridx = 1;
		g2.gridy = 1;
		g2.anchor = GridBagConstraints.LINE_START;
		g2.fill = GridBagConstraints.HORIZONTAL;
		g2.insets = new Insets(10, 0, 0, 10);
		g2.gridwidth = 1;
		g2.weightx = 0.3;
		cp.add(getTxDatePane(), g2);

		GridBagConstraints g3 = new GridBagConstraints();
		g3.gridx = 0;
		g3.gridy = 2;
		g3.fill = GridBagConstraints.BOTH;
		g3.weighty = 1;
		g3.gridwidth = 3;
		g3.insets = new Insets(10, 10, 0, 10);
		cp.add(getNotesPane(), g3);

		GridBagConstraints g4 = new GridBagConstraints();
		g4.gridx = 0;
		g4.gridy = 3;
		g4.fill = GridBagConstraints.HORIZONTAL;
		g4.gridwidth = 3;
		g4.insets = new Insets(10, 10, 10, 10);
		cp.add(getButtonPane(), g4);
	}

	private JComboBox getAccountSelector(String type) {
		DefaultComboBoxModel dcbm = new DefaultComboBoxModel();
		if (type.equalsIgnoreCase("from")) {
			fromCb = new JComboBox(dcbm);
			fromCb.setKeySelectionManager(new AccountCbKeySelectionManager());
			return fromCb;
		}
		toCb = new JComboBox(dcbm);
		toCb.setKeySelectionManager(new AccountCbKeySelectionManager());
		return toCb;
	}

	@SuppressWarnings("unchecked")
	private void loadAccounts(JComboBox cb, int... type) {
		try {
			DefaultComboBoxModel dcbm = (DefaultComboBoxModel) cb.getModel();
			dcbm.removeAllElements();

			long uid = SessionManager.getSessionUserId();

			java.util.List<Account> accts = new ArrayList<Account>();
			for (int i = 0; i < type.length; i++) {
				List<Account> tmp = null;
				if (type[i] == -1) {
					tmp = em.getAccountsForUser(uid);
				}
				else {
					tmp = em.getAccountsForUser(uid, type[i]);
				}
				accts.addAll(tmp);
			}

			Collections.sort(accts);

			if (accts != null) {
				int sz = accts.size();
				int sel = 0;
				for (int i = 0; i < sz; i++) {
					Account a = (Account) accts.get(i);
					AccountCbEntry ae = new AccountCbEntry(a);
					dcbm.addElement(ae);
					if (account != null
							&& account.getAccountId() == a.getAccountId()) {
						sel = i;
					}
				}
				cb.setSelectedIndex(sel);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private JTabbedPane getNotesPane() {
		detailsTab = new JTabbedPane();
		detailsTab.setUI(new MyTabPaneUI());

		detailsTab.addTab("Notes", getTxNotesPane());

		detailsTab.addTab("Attachments", getAttachmentsPanel());

		return detailsTab;
	}

	private JPanel getAccountSelectorsPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		fromCb = getAccountSelector("From");
		toCb = getAccountSelector("To");

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.fill = GridBagConstraints.NONE;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.insets = new Insets(10, 0, 0, 10);
		ret.add(new JLabel(tr("From")), g1);

		g1.gridx = 1;
		g1.gridy = 0;
		g1.weightx = 0.7;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.insets = new Insets(10, 10, 0, 10);
		ret.add(fromCb, g1);

		g1.gridx = 0;
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.NONE;
		g1.weightx = 0;
		g1.insets = new Insets(10, 0, 10, 10);
		ret.add(new JLabel(tr("To")), g1);

		g1.gridx = 1;
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.insets = new Insets(10, 10, 10, 10);
		ret.add(toCb, g1);

		ret.setBorder(BorderFactory.createTitledBorder(""));
		return ret;
	}

	private JPanel getTxDatePane() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridBagLayout());

		txAmtTf = new JTextField(10);
		net.mjrz.fm.ui.utils.GuiUtilities.setupTextComponent(txAmtTf);

		txDateTf = new DateChooser(false, SessionManager.getDateFormat());
		txDateTf.setDate(new Date());

		JLabel amtLbl = new JLabel(tr("Amount"));
		JLabel dateLbl = new JLabel(tr("Date"));

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.fill = GridBagConstraints.NONE;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.insets = new Insets(10, 0, 0, 10);
		ret.add(amtLbl, g1);

		g1.gridx = 1;
		g1.gridy = 0;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.weightx = 1;
		g1.insets = new Insets(10, 10, 0, 10);
		ret.add(txAmtTf, g1);

		g1.gridx = 0;
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.NONE;
		g1.weightx = 0;
		g1.insets = new Insets(10, 0, 10, 10);
		ret.add(dateLbl, g1);

		g1.gridx = 1;
		g1.gridy = 1;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.weightx = 1;
		g1.insets = new Insets(10, 10, 10, 10);
		ret.add(txDateTf, g1);

		ret.setBorder(BorderFactory.createTitledBorder(""));
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
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		newTxB = new JButton(tr("Save"));
		newTxB.setActionCommand("Save");
		newTxB.setMnemonic(KeyEvent.VK_S);
		newTxB.addActionListener(new ButtonHandler());
		getRootPane().setDefaultButton(newTxB);

		cancelB = new JButton(tr("Close"));
		cancelB.setActionCommand("Close");
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ButtonHandler());

		addMore = new JCheckBox(tr("Add more"));
		addMore.setVisible(false);

		ret.add(Box.createHorizontalStrut(10));
		ret.add(addMore);
		ret.add(Box.createHorizontalGlue());
		ret.add(newTxB);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(cancelB);
		ret.add(Box.createHorizontalStrut(10));

		return ret;
	}

	private JPanel getAttachmentsPanel() {
		attachmentsPanel = new AttachmentsPanel(txType);
		return attachmentsPanel;
	}

	private JPanel getTxTypePanel() {
		JPanel ret = new JPanel();

		incomeRb = new JRadioButton(tr("Income"));
		incomeRb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					loadAccounts(fromCb, AccountTypes.ACCT_TYPE_INCOME);
					loadAccounts(toCb, -1);
				}
			}
		});
		expenseRb = new JRadioButton(tr("Expense"));
		expenseRb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int args[] = { AccountTypes.ACCT_TYPE_CASH,
							AccountTypes.ACCT_TYPE_LIABILITY };
					loadAccounts(fromCb, args);
					loadAccounts(toCb, AccountTypes.ACCT_TYPE_EXPENSE);
				}
			}
		});

		transferRb = new JRadioButton(tr("Transfer"));
		transferRb.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					loadAccounts(fromCb, -1);
					loadAccounts(toCb, -1);
				}
			}
		});

		JLabel l = new JLabel(tr("Type"));

		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		ret.add(l);
		ret.add(Box.createHorizontalStrut(17));
		ret.add(incomeRb);
		ret.add(Box.createHorizontalStrut(17));
		ret.add(expenseRb);
		ret.add(Box.createHorizontalStrut(17));
		ret.add(transferRb);
		ret.add(Box.createHorizontalStrut(17));

		txTypeButtonGroup = new ButtonGroup();
		txTypeButtonGroup.add(incomeRb);
		txTypeButtonGroup.add(expenseRb);
		txTypeButtonGroup.add(transferRb);

		ret.setBorder(BorderFactory.createTitledBorder(tr("Transaction type")));

		return ret;
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Close")) {
				dispose();
			}
			if (cmd.equals("Help")) {
				HelpUtil.loadHelpPage(NewTransactionDialog.class.getName());
			}
			if (cmd.equals("Save")) {
				errorMsg = null;
				txList.clear();

				Transaction t = getTransactionObj();
				if (t == null) {
					JOptionPane
							.showMessageDialog(
									NewTransactionDialog.this,
									(errorMsg != null && errorMsg.length() > 0) ? errorMsg
											: "Error in input", "Error",
									JOptionPane.ERROR_MESSAGE);
					logger.error("Unable to create transaction");
					return;
				}
				txList.add(t);
				createNestedTransaction();
			}
		}
	}

	private Transaction getTransactionObj() {
		try {
			BigDecimal txAmt = new BigDecimal(txAmtTf.getText());
			AccountCbEntry from = ((AccountCbEntry) fromCb.getSelectedItem());
			AccountCbEntry to = ((AccountCbEntry) toCb.getSelectedItem());
			if (from == null || to == null) {
				return null;
			}
			Date txDt = txDateTf.getDate();

			Transaction t = new Transaction();
			t.setCreateDate(new Date());
			t.setTxDate(txDt);
			t.setFromAccountId(from.getAccount().getAccountId());
			t.setToAccountId(to.getAccount().getAccountId());
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
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	private void createNestedTransaction() {
		try {
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
						String msg = tr("Invalid number for transaction amount");
						result.setErrorCode(ActionResponse.TX_CREATE_ERROR);
						result.setErrorMessage(msg);
					}
					catch (Exception e) {
						String msg = tr("Unable to create transaction") + " - "
								+ tr("Error in input");
						result.setErrorCode(ActionResponse.TX_CREATE_ERROR);
						result.setErrorMessage(msg);
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
							else if (addMore.isVisible()
									&& !addMore.isSelected()) {
								dispose();
								return;
							}
						}
						else {
							JOptionPane.showMessageDialog(parent,
									result.getErrorMessage(), tr("Error"),
									JOptionPane.ERROR_MESSAGE);
							txList.clear();
						}
					}
					catch (Exception ex) {
						return;
					}
					finally {
						newTxB.setEnabled(true);
						cancelB.setEnabled(true);
						attachmentsPanel.clear();
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

	private boolean showAddMoreDialog() {
		int n = JOptionPane.showConfirmDialog(this,
				tr("Do you want to add more transactions?"), tr("Add more"),
				JOptionPane.YES_NO_OPTION);

		return n == JOptionPane.YES_OPTION;
	}

	private void setAccount(Account a) {
		if (a == null) {
			this.expenseRb.setSelected(true);
			return;
		}
		this.account = a;
		int type = a.getAccountType();

		switch (type) {

		case AccountTypes.ACCT_TYPE_INCOME:
			this.incomeRb.setSelected(true);
			break;

		case AccountTypes.ACCT_TYPE_EXPENSE:
			this.expenseRb.setSelected(true);
			break;
		default:
			this.transferRb.setSelected(true);
		}
	}
}
