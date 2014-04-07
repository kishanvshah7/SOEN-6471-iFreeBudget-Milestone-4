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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddNestedTransactionsAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.exceptions.TransactionNotFoundException;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.utils.DateChooser;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.ui.utils.REditor;
import net.mjrz.fm.ui.utils.SpringUtilities;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class QuickTransactionDialog extends JDialog {
	private JLabel txAmtLbl, txDateLbl;
	private JTextField txAmtTf;
	private DateChooser txDateTf;
	private REditor txNotesTa;
	private JButton newTxB, cancelB;
	private FinanceManagerUI parent = null;
	private User user = null;
	private SimpleDateFormat sdf = SessionManager.getDateFormat();
	private Transaction curr = null;

	private JPanel glass;

	static final long serialVersionUID = 0L;

	private Account from = null;
	private Account to = null;

	public QuickTransactionDialog() {
		initialize();
	}

	public QuickTransactionDialog(JFrame parent, User u, long txId, int txType)
			throws Exception {
		super(parent, "Transaction", true);

		this.parent = (FinanceManagerUI) parent;
		this.user = u;

		/* Initialize fields with the transaction fields */
		FManEntityManager em = new FManEntityManager();
		try {
			curr = em.getTransaction(user, txId);
			if (curr != null) {
				from = em.getAccount(user.getUid(), curr.getFromAccountId());
				to = em.getAccount(user.getUid(), curr.getToAccountId());
			}
			else {
				throw new TransactionNotFoundException();
			}
		}
		catch (Exception e) {
			throw e;
		}

		initialize();
		postInitialize();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	}

	private void postInitialize() {
		NumberFormat numFormat = NumberFormat
				.getCurrencyInstance(SessionManager.getCurrencyLocale());

		txAmtTf.setText(numFormat.format(curr.getTxAmount()));
		txNotesTa.setText(curr.getTxNotes());
	}

	public void setUser(User u) {
		this.user = u;
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(getTxDetailsPane(), BorderLayout.NORTH);
		cp.add(getTxNotesPane(), BorderLayout.CENTER);
		cp.add(getButtonPane(), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(400, 400));

		this.setTitle("New transaction");
		glass = (JPanel) getGlassPane();
		glass.setLayout(new GridBagLayout());

		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
	}

	private JPanel getTxDetailsPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		JPanel south = new JPanel();
		south.setLayout(new SpringLayout());

		txDateLbl = new JLabel(tr("Date"), JLabel.LEADING);
		txAmtLbl = new JLabel(tr("Amount"), JLabel.LEADING);

		south.add(new JLabel(tr("Account"), JLabel.LEADING));
		south.add(new JLabel(from.getAccountName()));
		south.add(new JLabel(tr("Payee"), JLabel.LEADING));
		south.add(new JLabel(to.getAccountName()));
		south.add(txDateLbl);
		south.add(getTxDatePane());
		south.add(txAmtLbl);
		south.add(getTxAmtPane());
		SpringUtilities.makeCompactGrid(south, 4, 2, 0, 0, 5, 5);

		south.setBorder(BorderFactory.createTitledBorder("Details:"));

		ret.add(south, BorderLayout.CENTER);

		return ret;
	}

	private JPanel getTxDatePane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));
		txDateTf = new DateChooser(false, sdf);
		txDateTf.setDate(new Date());

		ret.add(txDateTf);

		return ret;
	}

	private JPanel getTxAmtPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));
		txAmtTf = new JTextField(10);
		GuiUtilities.removeCustomMouseListener(txAmtTf);
		ret.add(txAmtTf);
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
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));
		newTxB = new JButton(tr("Save"));
		newTxB.setActionCommand("Save");

		newTxB.setMinimumSize(new Dimension(80, 20));
		newTxB.setMnemonic(KeyEvent.VK_S);
		newTxB.addActionListener(new ButtonHandler());

		cancelB = new JButton(tr("Close"));
		cancelB.setActionCommand("Close");

		cancelB.setMinimumSize(new Dimension(80, 20));
		cancelB.setMnemonic(KeyEvent.VK_C);
		cancelB.addActionListener(new ButtonHandler());

		ret.add(Box.createHorizontalGlue());
		if (newTxB != null)
			ret.add(newTxB);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancelB);
		ret.add(Box.createHorizontalStrut(5));
		return ret;
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("Close")) {
				dispose();
			}
			if (cmd.equals("Save")) {
				createTransaction();
			}
		}
	}

	private void createTransaction() {
		try {
			newTxB.setEnabled(false);
			cancelB.setEnabled(false);
			SwingWorker<ActionResponse, Void> worker = new SwingWorker<ActionResponse, Void>() {
				public ActionResponse doInBackground() throws Exception {
					ActionResponse result = new ActionResponse();
					try {
						NumberFormat numFormat = NumberFormat
								.getCurrencyInstance(SessionManager
										.getCurrencyLocale());

						BigDecimal txAmt = new BigDecimal(numFormat.parse(
								txAmtTf.getText()).doubleValue());

						if (from == null || to == null) {
							result.setErrorCode(ActionResponse.INVALID_TX);
							result.setErrorMessage("Selected account is invalid");
							return result;
						}

						Date txDt = txDateTf.getDate();

						Transaction t = new Transaction();
						t.setCreateDate(new Date());
						t.setTxDate(txDt);
						t.setFromAccountId(from.getAccountId());
						t.setToAccountId(to.getAccountId());
						t.setInitiatorId(user.getUid());
						t.setTxAmount(txAmt);
						t.setTxNotesMarkup(txNotesTa.getText());
						t.setTxNotes(txNotesTa.getPlainText());
						t.setActivityBy("Created by user activity");
						t.setIsParent(TT.IsParent.NO.getVal());

						java.util.List<Transaction> txList = new ArrayList<Transaction>();
						txList.add(t);

						// AddTransactionAction action = new
						// AddTransactionAction();
						ActionRequest req = new ActionRequest();
						req.setActionName("addTransaction");
						req.setUser(user);
						req.setProperty("TXLIST", txList);
						req.setProperty("UPDATETX", Boolean.valueOf(false));

						AddNestedTransactionsAction action = new AddNestedTransactionsAction();
						result = action.executeAction(req);
						if (result.getErrorCode() == ActionResponse.NOERROR) {
							result.addResult("TX", t);
						}

						return result;
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(
								QuickTransactionDialog.this,
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
							PageControlPanel instance = PageControlPanel
									.getInstance(parent);

							Transaction t = (Transaction) result
									.getResult("TX");

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
							dispose();
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
						newTxB.setEnabled(true);
						cancelB.setEnabled(true);
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
}
