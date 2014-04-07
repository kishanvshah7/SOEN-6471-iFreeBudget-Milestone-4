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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.Messages;

public class AccountSummaryPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected String DATE_FORMAT = "EEE, MMM d, ''yy";
	protected JEditorPane acctSummaryDisplay;

	public AccountSummaryPanel() {

		super(new BorderLayout());

		initialize();
	}

	private void initialize() {
		acctSummaryDisplay = new JEditorPane();
		acctSummaryDisplay.setEditable(false);
		acctSummaryDisplay.setBackground(Color.WHITE);
		acctSummaryDisplay.setContentType("text/html");

		acctSummaryDisplay
				.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		add(acctSummaryDisplay, BorderLayout.CENTER);
		Border border = BorderFactory.createTitledBorder("Account summary");
		setBorder(border);
	}

	protected void updateAccountSummaryPane(Account account) {
		String html = getAccountSummaryHtml(account);
		acctSummaryDisplay.setText(html);
	}

	protected String getAccountSummaryHtml(Account a) {
		NumberFormat numberFormat = NumberFormat
				.getCurrencyInstance(SessionManager.getCurrencyLocale());

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

		StringBuilder html = new StringBuilder();

		Font f = getFont();

		String font = f.getFamily();
		String fontSize = "8.5";

		String color = net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX;
		html.append("<html><body style=\"font-family:" + font + ";font-size:"
				+ fontSize + "px\">");
		html.append("<TABLE style=\"border-color: #ffffff; border-width: 1px 1px 1px 1px; border-style: solid; \" "
				+ "WIDTH=60%");

		html.append("<tr>");

		html.append("<td width=30% style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Account name</div></td>");

		html.append("<td width=70% >" + a.getAccountName() + "</td>");
		html.append("</tr>");

		html.append("<tr>");
		html.append("<td width=30% style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Account number</div></td>");

		String anum = a.getAccountNumber();
		anum = (anum != null && anum.length() > 0) ? anum : "-NA-";
		html.append("<td width=70% >" + anum + "</td>");

		html.append("<tr>");

		html.append("<td width=30% style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Account created on</div></td>");

		html.append("<td width=70% >" + sdf.format(a.getStartDate()) + "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Starting balance</div></td>");

		html.append("<td>" + numberFormat.format(a.getStartingBalance())
				+ "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Current balance</div></td>");

		html.append("<td>" + numberFormat.format(a.getCurrentBalance())
				+ "</td>");
		html.append("</tr>");

		html.append("<tr>");

		html.append("<td style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">High balance</div></td>");

		if (a.getHighBalanceDate() != null) {
			html.append("<td>" + numberFormat.format(a.getHighBalance())
					+ Messages.getString(" on ")
					+ sdf.format(a.getHighBalanceDate()) + "</td>");
		}
		html.append("</tr>");

		html.append("</div></td>");
		html.append("</tr>");

		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString();
	}
}
