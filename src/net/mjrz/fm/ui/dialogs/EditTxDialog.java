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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.DateChooser;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.REditor;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class EditTxDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private long txId = 0;
	private Boolean isParent = false;

	private FManEntityManager entityManager = null;

	private JComboBox payeeCb;
	private DateChooser dateTf;
	private JTextField amtTf;
	private REditor notesTa;
	private JLabel msgLbl;

	private JButton cancelB, okB;

	private DefaultComboBoxModel cbModel = null;

	private Transaction transaction;

	private NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	private SimpleDateFormat sdf = SessionManager.getDateFormat();

	public EditTxDialog(FinanceManagerUI parent, long txId, Boolean isParent) {
		super(parent, tr("Edit transaction"), true);
		this.txId = txId;
		this.isParent = isParent;
		entityManager = new FManEntityManager();

		initialize();

		try {
			initializeData();
		}
		catch (Exception e) {
			Logger.getLogger(EditTxDialog.class).error(
					MiscUtils.stackTrace2String(e));
		}
	}

	@SuppressWarnings("rawtypes")
	private void initializeData() throws Exception {
		transaction = entityManager.getTransaction(
				SessionManager.getSessionUserId(), txId);
		// transaction.setFitid(null);

		if (isParent) {
			transaction.setIsParent(TT.IsParent.YES.getVal());
		}
		else {
			transaction.setIsParent(TT.IsParent.NO.getVal());
		}

		java.util.List accounts = entityManager
				.getAccountsForUser(SessionManager.getSessionUserId());

		int sz = accounts.size();
		int sel = 0;
		for (int i = 0; i < sz; i++) {
			Account a = (Account) accounts.get(i);
			if (a.getAccountId() == transaction.getToAccountId()) {
				sel = i;
			}
			cbModel.addElement(a);
		}
		amtTf.setText(numFormat.format(transaction.getTxAmount()));

		notesTa.setText(getNotesText(transaction));

		payeeCb.setSelectedIndex(sel);
		dateTf.setDate(transaction.getTxDate());
	}

	private String getNotesText(Transaction t) {
		String markup = t.getTxNotesMarkup();
		if (markup == null || markup.length() == 0) {
			return t.getTxNotes();
		}
		return markup;
	}

	private void initialize() {
		Container cp = this.getContentPane();
		cp.setLayout(new BorderLayout());

		cbModel = new DefaultComboBoxModel();

		payeeCb = new JComboBox(cbModel);

		dateTf = new DateChooser(false, sdf);

		amtTf = new JTextField();

		notesTa = new REditor();

		msgLbl = new JLabel();

		cp.add(getCenterPanel(), BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.getRootPane().setDefaultButton(okB);
		GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel getCenterPanel() {
		JPanel cp = new JPanel();
		cp.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(new JLabel(tr("Payee")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(payeeCb, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(new JLabel(tr("Amount")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(amtTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(new JLabel(tr("Date")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(5, 5, 5, 5);
		cp.add(dateTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.5;
		gbc.insets = new Insets(5, 5, 5, 5);

		JScrollPane sp = new JScrollPane(notesTa);
		sp.setBorder(BorderFactory.createTitledBorder(tr("Notes")));
		cp.add(sp, gbc);

		return cp;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		cancelB = new JButton(tr("Close"));
		cancelB.setMinimumSize(new Dimension(80, 20));
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditTxDialog.this.dispose();
			}
		});

		okB = new JButton(tr("Save"));
		okB.setMinimumSize(new Dimension(80, 20));
		okB.setMnemonic(KeyEvent.VK_S);
		okB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					doOkButtonActionPerformed(e);
				}
				catch (Exception e1) {
					Logger.getLogger(EditTxDialog.class).error(e);
				}
			}
		});

		ret.add(msgLbl);
		ret.add(Box.createHorizontalGlue());
		ret.add(okB);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(cancelB);

		return ret;
	}

	public void doOkButtonActionPerformed(ActionEvent e) throws Exception {
		msgLbl.setText("");

		Double amt = numFormat.parse(amtTf.getText()).doubleValue();

		Account sel = (Account) cbModel.getSelectedItem();

		User user = new User();
		user.setUid(SessionManager.getSessionUserId());

		transaction.setTxAmount(new BigDecimal(amt));
		transaction.setTxNotesMarkup(notesTa.getText());
		transaction.setTxNotes(notesTa.getPlainText());
		transaction.setToAccountId(sel.getAccountId());
		transaction.setTxDate(sdf.parse(dateTf.getText()));

		java.util.List<Transaction> txList = new ArrayList<Transaction>();
		txList.add(transaction);

		ActionRequest req = new ActionRequest();
		req.setActionName("addTransaction");
		req.setUser(user);
		req.setProperty("TXLIST", txList);
		req.setProperty("UPDATETX", Boolean.valueOf(true));

		AddNestedTransactionsAction action = new AddNestedTransactionsAction();
		ActionResponse result = action.executeAction(req);
		if (result.getErrorCode() == ActionResponse.NOERROR) {
			postProcess(result, txList);
		}
		else {
			String msg = "Unable to update transaction for transaction";
			Logger.getLogger(EditTxDialog.class).error(msg + txId);
			msgLbl.setText(msg);
		}
	}

	private void postProcess(ActionResponse result,
			java.util.List<Transaction> txList) throws Exception {
		FinanceManagerUI parent = (FinanceManagerUI) this.getParent();

		Long sourceAcctId = (Long) result.getResult("SOURCEACCOUNTID");
		PageControlPanel instance = PageControlPanel.getInstance(parent);
		instance.makePageRequest(instance.getDefaultFilter());

		parent.updateStatusPane();
		if (sourceAcctId != null) {
			parent.reloadAccountList(sourceAcctId, false);
		}
		for (Transaction t : txList) {
			parent.updateAccountInTree(t.getToAccountId());
		}
		dispose();

	}
}
