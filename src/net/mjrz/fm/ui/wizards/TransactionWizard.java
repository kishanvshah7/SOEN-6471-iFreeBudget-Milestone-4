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
package net.mjrz.fm.ui.wizards;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.FutureTransaction;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.wizards.components.WizardComponent;
import net.mjrz.fm.ui.wizards.components.transaction.FromAccountPanel;
import net.mjrz.fm.ui.wizards.components.transaction.ToAccountPanel;
import net.mjrz.fm.ui.wizards.components.transaction.TransactionTypePanel;
import net.mjrz.fm.ui.wizards.components.transaction.TxDetailsPanel;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class TransactionWizard extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private WizardProgressPanel progressPanel;
	private JButton next, previous, cancel;
	private JCheckBox another;

	private WizardComponent[] wizardComponents;

	private JPanel centerPanel;

	private HashMap<String, String[][]> values = null;

	private static final String[] PANELS = { "TransactionType", "FromPanel",
			"ToPanel", "Details" };

	private JLabel[] progressLabels;

	private int curr = 0;

	private static Logger logger = Logger.getLogger(TransactionWizard.class
			.getName());

	private FManEntityManager em;

	private SimpleDateFormat sdf = SessionManager.getDateFormat();

	private FinanceManagerUI parent;

	private final int width = 700;
	private final int height = 350;

	public TransactionWizard(FinanceManagerUI parent) {
		super(parent, "Transaction", true);
		this.parent = parent;

		em = new FManEntityManager();
		progressLabels = new JLabel[PANELS.length];
		progressLabels[0] = new JLabel(tr("1. Select transaction type"));
		progressLabels[1] = new JLabel(tr("2. Select from account"));
		progressLabels[2] = new JLabel(tr("3. Select to account"));
		progressLabels[3] = new JLabel(tr("4. Details"));

		wizardComponents = new WizardComponent[PANELS.length];
		wizardComponents[0] = new TransactionTypePanel();
		wizardComponents[1] = new FromAccountPanel(this);
		wizardComponents[2] = new ToAccountPanel(this);
		wizardComponents[3] = new TxDetailsPanel();

		values = new HashMap<String, String[][]>();

		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		add(getProgressPanel(), BorderLayout.WEST);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
		boldenLabel(curr);
		manageButtonState();

		setTitle(tr("New transaction"));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(width, height));
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				TransactionWizard.class.getClassLoader().getResource(
						"icons/icon_money.png")));
		// setResizable(false);
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel getCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setLayout(new CardLayout());
		centerPanel.add((JPanel) wizardComponents[0], PANELS[0]);
		centerPanel.add((JPanel) wizardComponents[1], PANELS[1]);
		centerPanel.add((JPanel) wizardComponents[2], PANELS[2]);
		centerPanel.add((JPanel) wizardComponents[3], PANELS[3]);

		return centerPanel;
	}

	private JPanel getProgressPanel() {
		progressPanel = new WizardProgressPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

		for (int i = 0; i < progressLabels.length; i++) {
			progressPanel.add(progressLabels[i]);
			progressPanel.add(Box.createVerticalStrut(10));
		}
		progressPanel.add(Box.createVerticalGlue());

		progressPanel.setBorder(BorderFactory.createTitledBorder(tr("Progress")
				+ ": "));
		progressPanel.setBackground(UIDefaults.DEFAULT_COLOR);
		progressPanel.setPreferredSize(new Dimension(225, height));
		return progressPanel;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		another = new JCheckBox(tr("Add another transaction"));
		another.setVisible(false);

		next = new JButton(tr("Next") + " >");
		next.setActionCommand("next");
		next.addActionListener(this);
		next.setMnemonic(KeyEvent.VK_N);
		// next.setPreferredSize(new Dimension(85, 25));

		previous = new JButton("< " + tr("Back"));
		previous.setActionCommand("back");
		previous.setMnemonic(KeyEvent.VK_B);
		previous.addActionListener(this);
		// previous.setPreferredSize(new Dimension(85, 25));

		cancel = new JButton(tr("Cancel"));
		cancel.setActionCommand("cancel");
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(this);
		// cancel.setPreferredSize(new Dimension(85, 25));

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(another);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(previous);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(next);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(cancel);
		buttonPanel.add(Box.createHorizontalStrut(10));

		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		return buttonPanel;
	}

	private void manageButtonState() {
		if (curr > 0)
			previous.setEnabled(true);
		else
			previous.setEnabled(false);

		if (curr < PANELS.length - 1) {
			next.setEnabled(true);
		}
		else
			next.setEnabled(false);

		if (curr == PANELS.length - 1) {
			cancel.setActionCommand("finish");
			cancel.setText(tr("Finish"));
			cancel.setMnemonic(KeyEvent.VK_F);
			another.setVisible(true);
		}
		else {
			cancel.setActionCommand("cancel");
			cancel.setText(tr("Cancel"));
			cancel.setMnemonic(KeyEvent.VK_C);
			another.setVisible(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("next")) {
			boolean valid = wizardComponents[curr].isComponentValid();
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						wizardComponents[curr].getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String[][] v = wizardComponents[curr].getValues();
			for (int i = 0; i < v.length; i++) {
				values.put(PANELS[curr], v);
			}
			curr++;
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			wizardComponents[curr].updateComponentUI(values);
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
			return;
		}

		if (cmd.equals("back")) {
			if (curr >= PANELS.length)
				curr = PANELS.length - 1;

			values.remove(PANELS[curr]);
			curr--;
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			wizardComponents[curr].updateComponentUI(values);
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
		}
		if (cmd.equals("cancel")) {
			dispose();
		}
		if (cmd.equals("finish")) {
			boolean valid = wizardComponents[curr].isComponentValid();
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						wizardComponents[curr].getMessage(), tr("Error"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[][] v = wizardComponents[curr].getValues();
			for (int i = 0; i < v.length; i++) {
				values.put(PANELS[curr], v);
			}
			curr++;
			finish();
		}
	}

	private void boldenLabel(int idx) {
		for (int i = 0; i < progressLabels.length; i++) {
			Font f = progressLabels[i].getFont();
			Font fn = null;
			if (i == idx && f.getStyle() == Font.PLAIN) {
				fn = new Font(f.getFamily(), Font.BOLD, f.getSize());
				progressLabels[i].setForeground(Color.WHITE);
			}
			else {
				fn = new Font(f.getFamily(), Font.PLAIN, f.getSize());
				progressLabels[i].setForeground(Color.BLACK);
			}
			progressLabels[i].setFont(fn);
		}
	}

	private void printValues() {
		Set<String> keys = values.keySet();
		for (String k : keys) {
			String[][] vals = values.get(k);
			System.out.println("Key: " + k);
			for (int i = 0; i < vals.length; i++) {
				System.out.println("\t" + vals[i][0] + " - " + vals[i][1]);
			}
		}
	}

	private FutureTransaction getScheduledTransaction(String[][] recurringInfo) {
		String[] isRecurring = recurringInfo[0];
		if (isRecurring == null || isRecurring[1].equals("false")) {
			return null;
		}
		try {
			Date end = sdf.parse(recurringInfo[3][1]);
			int freq = Integer.parseInt(recurringInfo[2][1]);
			String freqUnit = recurringInfo[1][1];

			FutureTransaction t = new FutureTransaction();
			t.setEndDate(end);
			t.setFrequency(freq);
			t.setFrequencyUnit(freqUnit);
			t.setHoldStatus(FutureTransaction.NOHOLD);
			return t;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	private void createTransaction() {
		try {
			SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
				Transaction t = null;

				public ActionResponse doInBackground() throws Exception {
					ActionResponse result = new ActionResponse();
					try {
						String[][] from = values.get("FromPanel");
						String[][] to = values.get("ToPanel");
						String[][] details = values.get("Details");

						if (from == null || to == null) {
							result.setErrorCode(ActionResponse.INVALID_TX);
							result.setErrorMessage("Selected account is invalid");
							return result;
						}

						Long fromId = Long.parseLong(from[0][1]);
						Long toId = Long.parseLong(to[0][1]);

						BigDecimal txAmt = new BigDecimal(details[0][1]);
						Date txDt = sdf.parse(details[1][1]);
						String notes = details[2][1];
						String notesMarkup = details[3][1];

						long uid = net.mjrz.fm.services.SessionManager
								.getSessionUserId();

						t = new Transaction();

						t.setCreateDate(new Date());
						t.setTxDate(txDt);
						t.setFromAccountId(fromId);
						t.setToAccountId(toId);
						t.setInitiatorId(uid);
						t.setTxAmount(txAmt);
						t.setTxNotesMarkup(notesMarkup);
						t.setTxNotes(notes);
						t.setActivityBy("Created by user activity");

						// ActionRequest req = new ActionRequest();
						// req.setActionName("addTransaction");
						// req.setUser(em.getUser(uid));
						// req.setProperty("TRANSACTION", t);
						// req.setProperty("UPDATETX", Boolean.valueOf(false));
						//
						// AddTransactionAction action = new
						// AddTransactionAction();
						// result = action.executeAction(req);

						ArrayList<Transaction> txList = new ArrayList<Transaction>();
						txList.add(t);

						ActionRequest req = new ActionRequest();
						req.setActionName("addTransaction");
						req.setUser(em.getUser(uid));
						req.setProperty("TXLIST", txList);
						req.setProperty("UPDATETX", Boolean.valueOf(false));

						AddNestedTransactionsAction action = new AddNestedTransactionsAction();
						result = action.executeAction(req);

						return result;
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(TransactionWizard.this,
								tr("Invalid number for transaction amount"),
								tr("Error"), JOptionPane.ERROR_MESSAGE);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(TransactionWizard.this,
								tr("Error occured") + ": " + e.getMessage(),
								tr("Error"), JOptionPane.ERROR_MESSAGE);
					}
					return result;
				}

				public void done() {
					try {
						ActionResponse result = (ActionResponse) get();
						if (result == null)
							return;
						if (result.getErrorCode() == ActionResponse.NOERROR) {
							PageControlPanel instance = PageControlPanel
									.getInstance(parent);
							if (t != null) {
								instance.makePageRequest(parent
										.getCurrentFilter());
								parent.reloadAccountList(t.getFromAccountId(),
										false);
								parent.reloadAccountList(t.getToAccountId(),
										false);
							}
							else {
								instance.makePageRequest(instance
										.getDefaultFilter());
							}
							parent.updateStatusPane();
							if (!another.isSelected())
								dispose();
							else {
								curr = 0;
								CardLayout cl = (CardLayout) centerPanel
										.getLayout();
								wizardComponents[curr]
										.updateComponentUI(values);
								cl.show(centerPanel, PANELS[curr]);
								wizardComponents[curr].setComponentFocus();
								manageButtonState();
								boldenLabel(curr);
							}
						}
						else {
							JOptionPane.showMessageDialog(parent,
									result.getErrorMessage(), tr("Error"),
									JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception ex) {
						return;
					}
					finally {
					}
				}
			};
			worker.execute();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					tr("Unable to save transaction") + e.getMessage(),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void finish() {
		createTransaction();
	}

	public String[][] getValue(String panelName) {
		return values.get(panelName);
	}

	public void accountAdded(Account a) {
		try {
			parent.reloadAccountList(a, true);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}
}
