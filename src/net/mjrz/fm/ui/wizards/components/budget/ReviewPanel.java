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
package net.mjrz.fm.ui.wizards.components.budget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.wizards.components.WizardComponent;

@SuppressWarnings("serial")
public class ReviewPanel extends javax.swing.JPanel implements WizardComponent {
	private JEditorPane displayPane;
	private static final NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	public ReviewPanel() {
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		displayPane = new JEditorPane();
		displayPane.setContentType("text/html");
		displayPane.setEditable(false);
		displayPane.setBackground(Color.WHITE);
		add(new JScrollPane(displayPane), BorderLayout.CENTER);
	}

	private void buildHtml(String[][] data) {
		StringBuilder html = new StringBuilder();

		Font f = getFont();
		String font = f.getFamily();
		String fontSize = "8.5";

		html.append("<body style=\"font-family:" + font + ";font-size:"
				+ fontSize + "px\"><br>");
		html.append("<br><br>");
		html.append("<center>");
		html.append("<TABLE WIDTH=80% FRAME=NONE RULES=ROWS>");
		html.append("<tr bgcolor=\"#"
				+ net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX
				+ "\">");
		html.append("<td><b><font color=#FFFFFF>Account</font></b></td><td><b><font color=#FFFFFF>Amount allocated</font></b></td>");
		html.append("</tr>");

		BigDecimal total = new BigDecimal(0);
		for (int i = 0; i < data.length; i++) {
			String[] row = data[i];
			BigDecimal curr = new BigDecimal(row[1]);
			if (i % 2 == 0) {
				html.append("<tr>");
			}
			else {
				html.append("<tr bgcolor=\"#eaeaea\">");
			}
			html.append("<td>" + row[0] + "</td>");
			html.append("<td>" + numFormat.format(curr) + "</td>");
			html.append("</tr>");
			total = total.add(curr);
		}
		html.append("<tr><td aligh=right><b>Total</b></td><td><b>"
				+ numFormat.format(total) + "</b></td></tr>");
		html.append("</table>");
		html.append("</center></body></html>");
		displayPane.setText(html.toString());
	}

	@Override
	public String getMessage() {
		return null;
	}

	@Override
	public String[][] getValues() {
		String[][] data = new String[1][];
		String[] row = { "a", "b" };
		data[0] = row;
		return data;
	}

	@Override
	public boolean isComponentValid() {
		return true;
	}

	@Override
	public void setComponentFocus() {
	}

	@Override
	public void updateComponentUI(HashMap<String, String[][]> values) {
		buildHtml(values.get("Set amounts"));
	}
}
