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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetBudgetSummaryAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.BudgetedAccount;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class BudgetViewerDialog extends JDialog {
	private JList budgetList;
	private JEditorPane displayPane;
	private JButton closeButton, saveButton, deleteButton, editButton;
	private FManEntityManager em = null;
	private FinanceManagerUI parent;
	private static final NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());
	private static final String SUMMARY = "{SUMMARY}";
	private GregorianCalendar reportDate = null;
	private static Logger logger = Logger.getLogger(BudgetViewerDialog.class
			.getName());

	private SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, ''yy");

	private final int WIDTH = 850;
	private final int HEIGHT = 600;

	public BudgetViewerDialog(JFrame parent) {
		super(parent, tr("Budget viewer"), true);

		reportDate = new GregorianCalendar();

		this.parent = (FinanceManagerUI) parent;

		em = new FManEntityManager();

		initialize();
	}

	public GregorianCalendar getReportDate() {
		return reportDate;
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		java.util.List<Budget> bList = null;
		try {
			bList = (java.util.List<Budget>) em.getBudgets(SessionManager
					.getSessionUserId());
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			bList = new ArrayList<Budget>();
		}

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(5, 5));

		DefaultListModel model = new DefaultListModel();
		for (Budget o : bList) {
			model.addElement(o);
		}

		budgetList = new JList(model);
		budgetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		budgetList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doListSelectionValueChanged(e);
			}
		});

		displayPane = new JEditorPane();
		displayPane.setEditable(false);
		displayPane.setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		displayPane.setContentType("text/html");
		displayPane.setBorder(BorderFactory.createTitledBorder("Summary"));
		displayPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() != EventType.ACTIVATED) {
					return;
				}
				String desc = e.getDescription();
				if (desc != null && desc.equals("prev")) {
					doPreviousClickAction(e);
				}
				else if (desc != null && desc.equals("next")) {
					doNextClickAction(e);
				}
			}
		});

		JScrollPane listSp = new JScrollPane(budgetList);
		listSp.setPreferredSize(new Dimension(200, HEIGHT));
		listSp.setBorder(BorderFactory.createTitledBorder("Budgets"));

		cp.add(listSp, BorderLayout.WEST);
		cp.add(new JScrollPane(displayPane), BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
	}

	private void doPreviousClickAction(HyperlinkEvent e) {
		Budget curr = (Budget) budgetList.getSelectedValue();
		if (curr == null) {
			return;
		}
		if (curr.getType() == Budget.MONTHLY) {
			reportDate.add(Calendar.MONTH, -1);
		}
		else {
			reportDate.add(Calendar.WEEK_OF_YEAR, -1);
		}
		updateDisplay();
	}

	private void doNextClickAction(HyperlinkEvent e) {
		Budget curr = (Budget) budgetList.getSelectedValue();
		if (curr == null) {
			return;
		}
		if (curr.getType() == Budget.MONTHLY) {
			reportDate.add(Calendar.MONTH, 1);
		}
		else {
			reportDate.add(Calendar.WEEK_OF_YEAR, 1);
		}
		updateDisplay();
	}

	private void doListSelectionValueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		/* Selection has changed, change report date to today */
		reportDate.setTime(new Date());

		deleteButton.setEnabled(true);
		editButton.setEnabled(true);
		updateDisplay();
	}

	public void updateDisplay() {
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
			@Override
			protected String doInBackground() throws Exception {
				String data = getDisplayData();
				return data;
			}

			public void done() {
				try {
					String data = get();
					displayPane.setText(data);
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
			}
		};
		worker.execute();
	}

	private String getDisplayData() {
		Budget b = (Budget) budgetList.getSelectedValue();

		// syncBudget(b);

		if (b == null)
			return "";

		ActionRequest req = new ActionRequest();
		req.setActionName("getBudgetSummary");
		req.setUser(parent.getUser());
		req.setProperty("BUDGET", b);

		if (reportDate == null) {
			reportDate = new GregorianCalendar();
		}

		req.setProperty("DATE", reportDate.getTime());

		GetBudgetSummaryAction action = new GetBudgetSummaryAction();
		String data = null;
		try {
			ActionResponse resp = action.executeAction(req);
			if (resp.getErrorCode() == ActionResponse.NOERROR) {
				b = (Budget) resp.getResult("BUDGET");
				Date st = (Date) resp.getResult("START");
				Date en = (Date) resp.getResult("END");
				data = getDisplayString(b, st, en);
			}
		}
		catch (Exception e1) {
			logger.error(MiscUtils.stackTrace2String(e1));
		}
		return data;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(getDeleteButton());
		buttonPanel.add(Box.createHorizontalStrut(5));
		buttonPanel.add(getEditButton());
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(getSaveButton());
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(getCloseButton());
		buttonPanel.add(Box.createHorizontalStrut(10));
		return buttonPanel;
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText("Save");
			saveButton.setMnemonic(KeyEvent.VK_S);
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSaveButtonActionPerformed(e);
				}
			});
		}
		return saveButton;
	}

	private String getNavigatorString() {
		StringBuilder ret = new StringBuilder();
		ret.append("<tr>");
		ret.append("<td colspan=4 align=left>");
		ret.append("<div>");
		ret.append("<a href=prev>&lt;Previous</a>");
		ret.append("&nbsp;&nbsp;&nbsp;&nbsp;");
		ret.append("<a href=next>Next&gt;</a>");
		ret.append("</div>");
		ret.append("</td>");
		ret.append("</tr>");
		return ret.toString();
	}

	private String getDisplayString(Budget b, Date startDt, Date endDt)
			throws Exception {
		Set<BudgetedAccount> set = b.getAccounts();
		Font f = getFont();

		String font = f.getFamily();
		String fontSize = "8.5";

		StringBuilder html = new StringBuilder();
		html.append("<html><body style=\"font-family:" + font + ";font-size:"
				+ fontSize + "px\"><br>");
		html.append(SUMMARY);
		html.append("<br><br>");
		html.append("<center>");
		html.append("<TABLE WIDTH=100% BORDER=0>");
		html.append(getNavigatorString());
		html.append("<tr bgcolor=\""
				+ net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX
				+ "\">");
		html.append("<td><div style=\"color: #ffffff; font-weight: bold\">Account</div></td>");
		html.append("<td><div style=\"color: #ffffff; font-weight: bold\">Amount allocated</div></td>");
		html.append("<td><div style=\"color: #ffffff; font-weight: bold\">Amount spent</div></td>");
		html.append("<td><div style=\"color: #ffffff; font-weight: bold\">Savings</div></td>");
		html.append("</tr>");

		int i = 0;
		BigDecimal actualTotal = new BigDecimal(0);
		BigDecimal allocatedTotal = new BigDecimal(0);
		BigDecimal savingsTotal = new BigDecimal(0);
		for (BudgetedAccount a : set) {
			Account acct = em.getAccount(SessionManager.getSessionUserId(),
					a.getAccountId());

			if (acct == null)
				continue;

			if (i++ % 2 == 0) {
				html.append("<tr>");
			}
			else {
				html.append("<tr bgcolor=\"#eaeaea\">");
			}
			html.append("<td>" + acct.getAccountName() + "</td>");
			html.append("<td>" + numFormat.format(a.getAllocatedAmount())
					+ "</td>");
			html.append("<td>" + numFormat.format(a.getActualAmount())
					+ "</td>");

			BigDecimal s = a.getAllocatedAmount().subtract(a.getActualAmount());

			if (s.intValue() < 0) {
				html.append("<td><div style=\"color: red;\">"
						+ numFormat.format(s) + "</div></td>");
			}
			else {
				html.append("<td><div style=\"color: black;\">"
						+ numFormat.format(s) + "</div></td>");
			}

			html.append("</tr>");

			actualTotal = actualTotal.add(a.getActualAmount());
			allocatedTotal = allocatedTotal.add(a.getAllocatedAmount());
			savingsTotal = savingsTotal.add(s);
		}
		html.append("<tr><td colspan=4 valign=top><hr></td></tr>");
		html.append("<tfoot>");
		html.append("<tr>");
		html.append("<td aligh=right><b>Totals</b></td>");
		html.append("<td><div style=\"color: black;font-weight: bold\">"
				+ numFormat.format(allocatedTotal) + "</div></td>");
		html.append("<td><div style=\"color: black;font-weight: bold\">"
				+ numFormat.format(actualTotal) + "</div></td>");

		if (savingsTotal.intValue() < 0) {
			html.append("<td><div style=\"color: red;font-weight: bold\">"
					+ numFormat.format(savingsTotal) + "</div></td>");
		}
		else {
			html.append("<td><div style=\"color: black;font-weight: bold\">"
					+ numFormat.format(savingsTotal) + "</div></td>");
		}
		html.append("</tr>");
		html.append("<tfoot>");
		html.append("</table>");
		html.append("</center></body></html>");

		String summary = getSummaryString(b, startDt, endDt, savingsTotal);
		int pos = html.indexOf(SUMMARY);
		if (pos >= 0) {
			html.replace(pos, pos + SUMMARY.length(), summary);
		}
		return (html.toString());
	}

	private String getSummaryString(Budget b, Date start, Date end,
			BigDecimal savings) {
		StringBuilder html = new StringBuilder();
		String color = net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX;
		html.append("<TABLE style=\"border-color: #ffffff; border-width: 1px 1px 1px 1px; border-style: solid; \" "
				+ "WIDTH=100%");

		html.append("<tr>");

		html.append("<td width=30% style=\"background-color:" + color + ";\">"
				+ "<div style=\"background-color:" + color
				+ ";color: #ffffff; font-weight: bold\">Name</div></td>");

		html.append("<td width=70% >" + b.getName() + "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:" + color + ";\">"
				+ "<div style=\"background-color:" + color
				+ ";color: #ffffff; font-weight: bold\">Budget type</div></td>");

		html.append("<td>" + Budget.getTypeAsString(b.getType()) + "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:" + color + ";\">"
				+ "<div style=\"background-color:" + color
				+ ";color: #ffffff; font-weight: bold\">Dates</div></td>");

		html.append("<td>" + getDateDisplayString(start, end) + "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:" + color + ";\">"
				+ "<div style=\"background-color:" + color
				+ ";color: #ffffff; font-weight: bold\">Savings</div></td>");

		if (savings.compareTo(new BigDecimal(0)) < 0) {
			html.append("<td><div style=\"color: red;\">");
		}
		else {
			html.append("<td><div style=\"color: black;\">");
		}
		html.append(numFormat.format(savings));
		html.append("</div></td>");
		html.append("</tr>");

		html.append("</table>");
		return html.toString();
	}

	private String getDateDisplayString(Date s, Date e) {
		return new StringBuilder().append(sdf.format(s)).append(" to ")
				.append(sdf.format(e)).toString();
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText("Close");
			closeButton.setMnemonic(KeyEvent.VK_C);
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
			getRootPane().setDefaultButton(closeButton);
		}
		return closeButton;
	}

	private JButton getDeleteButton() {
		if (deleteButton == null) {
			deleteButton = new JButton();
			deleteButton.setText("Remove");
			deleteButton.setMnemonic(KeyEvent.VK_R);
			deleteButton.setEnabled(false);
			deleteButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doDeleteButtonActionPerformed(e);
				}
			});
		}
		return deleteButton;
	}

	private JButton getEditButton() {
		if (editButton == null) {
			editButton = new JButton();
			editButton.setText("Edit");
			editButton.setMnemonic(KeyEvent.VK_R);
			editButton.setEnabled(false);
			editButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showEditBudgetDialog();
				}
			});
		}
		return editButton;
	}

	public void showEditBudgetDialog() {
		Budget b = getCurrentSelection();
		JDialog d = BudgetEditorDialog.getInstance(this, b);
		d.setPreferredSize(new Dimension(700, 400));
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private void doSaveButtonActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(BudgetViewerDialog.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (file.exists()) {
				int ans = JOptionPane
						.showConfirmDialog(
								BudgetViewerDialog.this,
								Messages.getString("File with same name already exists. Replace?"),
								Messages.getString("Save As"),
								JOptionPane.YES_NO_OPTION);

				if (ans != JOptionPane.OK_OPTION) {
					return;
				}
			}
			String data = displayPane.getText();

			BufferedWriter out = null;
			try {
				File outfile = new File(file.getAbsolutePath() + ".html");
				out = new BufferedWriter(new FileWriter(outfile));
				out.write(data);
			}
			catch (Exception ex) {
				logger.error(MiscUtils.stackTrace2String(ex));
			}
			finally {
				if (out != null) {
					try {
						out.close();
					}
					catch (IOException e1) {
						logger.error(MiscUtils.stackTrace2String(e1));
					}
				}
			}
		}
	}

	private void doDeleteButtonActionPerformed(ActionEvent e) {
		int sel = budgetList.getSelectedIndex();
		if (sel >= 0) {
			try {
				DefaultListModel model = (DefaultListModel) budgetList
						.getModel();
				Budget b = (Budget) model.get(sel);
				em.deleteBudget(b);
				model.remove(sel);
			}
			catch (Exception e1) {
				logger.error(MiscUtils.stackTrace2String(e1));
			}
		}
	}

	private Budget getCurrentSelection() {
		int sel = budgetList.getSelectedIndex();
		if (sel >= 0) {
			DefaultListModel model = (DefaultListModel) budgetList.getModel();
			Budget b = (Budget) model.get(sel);
			return b;
		}
		return null;
	}

	private void syncBudget(Budget b) {
		Set<BudgetedAccount> set = b.getAccounts();
		Iterator<BudgetedAccount> iter = set.iterator();
		boolean isDirty = false;
		while (iter.hasNext()) {
			BudgetedAccount a = iter.next();
			try {
				Account acct = em.getAccount(SessionManager.getSessionUserId(),
						a.getAccountId());
				if (acct == null) {
					iter.remove();
					isDirty = true;
				}
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
			finally {
				if (isDirty) {
					try {
						em.updateBudget(b);
					}
					catch (Exception e) {
						logger.error(MiscUtils.stackTrace2String(e));
					}
				}
			}
		}
	}
}
