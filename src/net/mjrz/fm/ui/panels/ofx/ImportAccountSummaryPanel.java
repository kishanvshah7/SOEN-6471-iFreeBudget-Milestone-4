package net.mjrz.fm.ui.panels.ofx;

import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.panels.AccountSummaryPanel;

public class ImportAccountSummaryPanel extends AccountSummaryPanel {

	private static final long serialVersionUID = 1L;

	public ImportAccountSummaryPanel() {
		super();

	}

	protected void updateAccountSummaryPane(Account account,
			java.util.List<TxObject> objList) throws CloneNotSupportedException {

		if (account == null || objList == null) {
			return;
		}

		String html = getAccountSummaryHtml(account, objList);
		acctSummaryDisplay.setText(html);
	}

	protected String getAccountSummaryHtml(Account a, List<TxObject> objList) {
		BigDecimal projectedBal = a.getCurrentBalance();
		for (TxObject obj : objList) {
			projectedBal = projectedBal.add(obj.getAmount());
		}

		NumberFormat numberFormat = NumberFormat
				.getCurrencyInstance(SessionManager.getCurrencyLocale());

		// SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

		StringBuilder html = new StringBuilder();

		Font f = getFont();

		String font = f.getFamily();
		String fontSize = "8.5";

		String color = net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX;
		html.append("<html><body style=\"font-family:" + font + ";font-size:"
				+ fontSize + "px\">");
		html.append("<TABLE style=\"border-color: #ffffff; border-width: 1px 1px 1px 1px; border-style: solid; \" "
				+ "WIDTH=100%");

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
				+ ";color: #ffffff; font-weight: bold\">Projected balance</div></td>");

		html.append("<td>" + numberFormat.format(projectedBal) + "</td>");
		html.append("</tr>");

		html.append("<tr>");
		html.append("<td width=30% style=\"background-color:"
				+ color
				+ ";\">"
				+ "<div style=\"background-color:"
				+ color
				+ ";color: #ffffff; font-weight: bold\">Transactions to import</div></td>");

		html.append("<td width=70% >" + objList.size() + "</td>");
		html.append("</tr>");

		html.append("</table>");
		html.append("</body>");
		html.append("</html>");
		return html.toString();
	}

}
