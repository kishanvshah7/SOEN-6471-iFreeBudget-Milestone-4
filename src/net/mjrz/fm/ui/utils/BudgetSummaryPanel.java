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
package net.mjrz.fm.ui.utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetBudgetSummaryAction;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.BudgetedAccount;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class BudgetSummaryPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JEditorPane displayPane = null;

	private GregorianCalendar reportDate = null;

	private static final Logger logger = Logger
			.getLogger(BudgetSummaryPanel.class);

	private Budget budget = null;

	private static final NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	private FManEntityManager entityManager = new FManEntityManager();

	private static final String SUMMARY = "{SUMMARY}";

	public BudgetSummaryPanel() {
		super();
		setLayout(new BorderLayout());

		displayPane = new JEditorPane();
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

		add(displayPane, BorderLayout.CENTER);
	}

	public void setDisplayData(Budget budget) {
		this.budget = budget;
		User user = new User();
		user.setUid(SessionManager.getSessionUserId());

		final ActionRequest req = new ActionRequest();
		req.setActionName("getBudgetSummary");
		req.setUser(user);
		req.setProperty("BUDGET", budget);

		if (reportDate == null) {
			reportDate = new GregorianCalendar();
		}

		req.setProperty("DATE", reportDate.getTime());

		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

			@Override
			protected String doInBackground() throws Exception {
				GetBudgetSummaryAction action = new GetBudgetSummaryAction();
				String data = null;
				try {
					ActionResponse resp = action.executeAction(req);
					if (resp.getErrorCode() == ActionResponse.NOERROR) {
						Budget tmp = (Budget) resp.getResult("BUDGET");
						Date st = (Date) resp.getResult("START");
						Date en = (Date) resp.getResult("END");
						data = getDisplayString(tmp, st, en);
					}
				}
				catch (Exception e1) {
					logger.error(MiscUtils.stackTrace2String(e1));
				}
				return data;
			}

			@Override
			protected void done() {
				try {
					String data = get();
					displayPane.setText(data);
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
					displayPane.setText("<font color=red>Error occured</font>");
				}
			}
		};
		worker.execute();
	}

	private void doPreviousClickAction(HyperlinkEvent e) {
		Budget curr = budget;
		if (curr == null) {
			return;
		}
		if (curr.getType() == Budget.MONTHLY) {
			reportDate.add(Calendar.MONTH, -1);
		}
		else {
			reportDate.add(Calendar.WEEK_OF_YEAR, -1);
		}
		setDisplayData(curr);
	}

	private void doNextClickAction(HyperlinkEvent e) {
		Budget curr = budget;
		if (curr == null) {
			return;
		}
		if (curr.getType() == Budget.MONTHLY) {
			reportDate.add(Calendar.MONTH, 1);
		}
		else {
			reportDate.add(Calendar.WEEK_OF_YEAR, 1);
		}
		setDisplayData(curr);
	}

	//
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
		html.append("<TABLE WIDTH=70% BORDER=0>");
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
			Account acct = entityManager.getAccount(
					SessionManager.getSessionUserId(), a.getAccountId());

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
		html.append("</body></html>");

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
				+ "WIDTH=50%");

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
		SimpleDateFormat sdf = SessionManager.getDateFormat();
		return new StringBuilder().append(sdf.format(s)).append(" to ")
				.append(sdf.format(e)).toString();
	}
}
