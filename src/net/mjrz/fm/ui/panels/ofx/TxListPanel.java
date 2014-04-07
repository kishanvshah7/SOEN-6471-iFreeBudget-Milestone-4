package net.mjrz.fm.ui.panels.ofx;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddAccountAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.crypto.CHelper;
import net.mjrz.fm.utils.indexer.IndexedEntity;
import net.mjrz.fm.utils.indexer.Indexer;
import net.mjrz.fm.utils.indexer.Indexer.IndexType;

import org.apache.log4j.Logger;

public class TxListPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTable txTable;
	private JEditorPane notes;
	private ImportProgressPanel parent;

	private JButton importB, closeB, editB, helpIconB;
	private JCheckBox cb;

	private Logger logger = Logger.getLogger(getClass());

	public TxListPanel(ImportProgressPanel parent) {
		super();
		this.parent = parent;
		initialize();
	}

	private void initialize() {
		initializeTable();

		super.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.8;
		gbc.weighty = 0.7;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(new JScrollPane(txTable), gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.8;
		gbc.weighty = 0.2;
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(getSouthPanel(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.8;
		gbc.weighty = 0.1;
		gbc.insets = new Insets(2, 10, 2, 10);
		gbc.anchor = GridBagConstraints.PAGE_START;
		add(getButtonPanel(), gbc);
	}

	private JPanel getSouthPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		notes = new JEditorPane();
		notes.setContentType("text/html");
		notes.setEditable(false);

		ret.add(new JScrollPane(notes));

		ret.setBorder(BorderFactory.createTitledBorder(""));

		return ret;
	}

	private void initializeTable() {
		TxListModel model = new TxListModel();
		txTable = new TxListTable(model);

		txTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						handleListSelectionEvent(e);
					}
				});

		txTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					showTxEditDialog();
				}
			}
		});
		txTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
					showTxEditDialog();
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				int d = (int) txTable.getPreferredScrollableViewportSize()
						.getWidth();
				for (int i = 0; i < txTable.getColumnModel().getColumnCount(); i++) {
					TableColumn column = txTable.getColumnModel().getColumn(i);
					int w = 0;
					if (i == 0 || i == 1) {
						w = 5 * d / 100;
						column.setMaxWidth(w);
						continue;
					}
					else if (i == 2) {
						w = 75 * d / 100;
					}
					else if (i == 3 || i == 4) {
						w = 10 * d / 100;
					}
					column.setPreferredWidth(w);
				}
			}
		});
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		importB = new JButton(Messages.getString("Import"));
		closeB = new JButton(Messages.getString("Close"));
		editB = new JButton(Messages.getString("Edit"));
		cb = new JCheckBox(tr("Reconcile balance?"));
		helpIconB = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/help.png"));

		helpIconB.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		helpIconB.setHorizontalAlignment(JButton.LEADING); // optional
		helpIconB.setBorderPainted(false);
		helpIconB.setContentAreaFilled(false);

		importB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doImportAction(e);
				}
				catch (Exception e1) {
					logger.error(MiscUtils.stackTrace2String(e1));
				}
			}
		});

		importB.setMnemonic(KeyEvent.VK_I);

		closeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.cleanupAndDispose();
			}
		});
		closeB.setMnemonic(KeyEvent.VK_C);

		helpIconB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane op = FinanceManagerUI.getNarrowOptionPane(50);
				op.setMessageType(JOptionPane.INFORMATION_MESSAGE);
				op.setMessage(tr("You can choose to set the target "
						+ "account balance to the statement ending "
						+ "balance in the imported OFX file by checking this item."));
				JDialog dialog = op.createDialog(parent, "Help");
				dialog.pack();
				dialog.setVisible(true);
			}
		});

		editB.setVisible(false);
		editB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTxEditDialog();
			}
		});
		editB.setMnemonic(KeyEvent.VK_E);

		ret.add(Box.createHorizontalStrut(5));
		ret.add(cb);
		ret.add(helpIconB);
		ret.add(Box.createHorizontalGlue());
		ret.add(editB);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(importB);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(closeB);
		ret.add(Box.createHorizontalStrut(5));
		return ret;
	}

	private void showTxEditDialog() {
		int viewRow = txTable.getSelectedRow();
		final int modelRow = txTable.convertRowIndexToModel(viewRow);
		TxObject obj = getTxTableModel().getTxObject(modelRow);

		obj.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				if (pce.getPropertyName().equals("match")) {
					getTxTableModel().setValueAt(pce.getNewValue(), modelRow,
							TxListModel.IDX_PAYEE);
				}
				if (pce.getPropertyName().equals("markup")) {
					String txt = (String) pce.getNewValue();
					notes.setText(txt);
					getTxTableModel().setValueAt(txt, modelRow,
							TxListModel.IDX_NOTES_MARKUP);
				}
				if (pce.getPropertyName().equals("notes")) {
					String txt = (String) pce.getNewValue();
					getTxTableModel().setValueAt(txt, modelRow,
							TxListModel.IDX_NOTES);
				}
				txTable.requestFocusInWindow();
			}
		});
		parent.showTxEditor(obj);
	}

	private void handleListSelectionEvent(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		int last = lsm.getMaxSelectionIndex();

		TxObject obj = this.getTxTableModel().getTxObject(last);

		if (obj == null) {
			return;
		}

		if (obj.getImportStatusMessage() == null) {
			notes.setText(obj.getNotes());
		}
		else {
			notes.setText("<html><body><font color=red>"
					+ obj.getImportStatusMessage() + "</font></body></html>");
		}

		if (obj.isDoImport())
			editB.setVisible(true);
		else
			editB.setVisible(false);
	}

	public void addTxObject(List<TxObject> txList) {
		if (txList == null) {
			return;
		}
		TxListModel model = getTxTableModel();
		for (TxObject tx : txList) {
			model.addTxObject(tx);
		}
	}

	private TxListModel getTxTableModel() {
		return (TxListModel) txTable.getModel();
	}

	private void doImportAction(ActionEvent e) throws Exception {
		TxListModel model = getTxTableModel();
		int sz = model.getRowCount();

		boolean valid = true;

		final List<TxHolder> dataList = new ArrayList<TxHolder>(sz);
		for (int i = 0; i < sz; i++) {
			TxObject o = model.getTxObject(i);
			if (!o.isDoImport()) {
				continue;
			}
			Transaction t = getTransaction(o);

			ActionResponse resp = parent.validateTransaction(t);
			if (resp.getErrorCode() != ActionResponse.NOERROR) {
				o.setImportStatusMessage(resp.getErrorMessage());
				model.setValueAt(resp.getErrorMessage(), i,
						TxListModel.IDX_ERR_MSG);
				valid = false;
			}
			dataList.add(new TxHolder(o, t));
		}

		if (valid) {
			startImport(dataList);
		}
		else {
			parent.showErrorDialog(tr("Errors were detected.\n "
					+ "Please check highlighted "
					+ "transactions and fix the problems.\n"
					+ "You can also select to skip importing the transactions"));
		}
	}

	private void startImport(final List<TxHolder> dataList) throws Exception {
		importB.setEnabled(false);

		SwingWorker<List<TxObject>, Void> worker = new SwingWorker<List<TxObject>, Void>() {
			@Override
			protected List<TxObject> doInBackground() throws Exception {
				net.mjrz.fm.ui.utils.NotificationHandler.pauseQueue(true);

				List<TxObject> errTx = new ArrayList<TxObject>();

				int sz = dataList.size();
				for (int i = 0; i < sz; i++) {
					TxHolder holder = dataList.get(i);

					try {
						TxObject o = holder.modelObj;
						Transaction t = holder.dataObj;

						ActionResponse r = parent.createTransaction(t);

						if (r.getErrorCode() == ActionResponse.NOERROR) {
							IndexedEntity ie = o.getMatch();
							Indexer.getIndexer().index(o.getOfxLine(), ie,
									IndexType.Account);
						}
						else {
							String msg = r.getErrorMessage() + ":"
									+ r.getErrorCode();

							logger.error(msg);
							o.setImportStatusMessage(msg);
							errTx.add(o);
						}
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
					}
				}
				return errTx;
			}

			@Override
			protected void done() {
				try {
					importB.setEnabled(true);
					List<TxObject> errTx = get();
					TxListModel model = getTxTableModel();
					parent.importDone();
					if (errTx.size() > 0) {
						model.clearAll();
						for (TxObject o : errTx) {
							model.addTxObject(o);
						}
					}
					else {
						parent.cleanupAndDispose();
					}
				}
				catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
				catch (ExecutionException e) {
					logger.error(e.getMessage());
				}
			}
		};
		worker.execute();
	}

	boolean shouldUpdateBalance() {
		return cb.isSelected();
	}

	private Transaction getTransaction(TxObject obj) throws Exception {

		Transaction t = new Transaction();
		t.setCreateDate(new Date());
		t.setTxDate(obj.getDate());
		t.setInitiatorId(SessionManager.getSessionUserId());
		t.setTxAmount(obj.getAmount());
		t.setTxNotes(obj.getNotes());
		t.setTxNotesMarkup(obj.getMarkup());
		t.setActivityBy("Created by user activity");
		t.setFitid(obj.getFitId());

		BigDecimal amt = obj.getAmount();
		if (amt.doubleValue() < 0) { // debit
			t.setFromAccountId(obj.getSource().getAccountId());
			IndexedEntity ie = obj.getMatch();
			Account to = checkAccount(ie.getName(), ie.getType());
			t.setToAccountId(to.getAccountId());
			t.setTxAmount(amt.negate());
		}
		else { // credit
			t.setToAccountId(obj.getSource().getAccountId());
			IndexedEntity ie = obj.getMatch();
			Account to = checkAccount(ie.getName(), ie.getType());
			t.setFromAccountId(to.getAccountId());
		}
		return t;
	}

	private Account checkAccount(String name, int type) throws Exception {
		FManEntityManager em = new FManEntityManager();
		Account a = em.getAccountFromName(SessionManager.getSessionUserId(),
				type, CHelper.encrypt(name));
		if (a != null) {
			return a;
		}
		else {
			User user = new User();
			user.setUid(SessionManager.getSessionUserId());

			String typeStr = AccountTypes.getAccountType(type);
			AccountCategory parent = FManEntityManager.getRootCategory(typeStr);

			a = new Account();
			a.setAccountName(name);
			a.setAccountType(type);
			a.setAccountNotes("<change me>");
			a.setCategoryId(parent.getCategoryId());
			a.setStartDate(new java.util.Date());
			a.setAccountParentType(a.getAccountType());
			a.setStartingBalance(new BigDecimal(0));
			a.setCurrentBalance(new BigDecimal(0d));
			a.setHighBalance(new BigDecimal(0d));
			a.setStatus(AccountTypes.ACCOUNT_ACTIVE);

			AddAccountAction action = new AddAccountAction();
			ActionResponse result = action.executeAction(user, a);
			if (result.getErrorCode() == ActionResponse.NOERROR) {
				return em.getAccount(SessionManager.getSessionUserId(),
						a.getAccountId());
			}
			else {
				logger.error("Create account errored."
						+ result.getErrorMessage());
				return null;
			}
		}
	}

	static class TxHolder {
		TxObject modelObj;
		Transaction dataObj;

		TxHolder(TxObject modelObj, Transaction dataObj) {
			this.modelObj = modelObj;
			this.dataObj = dataObj;
		}
	}
}
