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
package net.mjrz.fm.ui.panels.portfolio;

import java.awt.BorderLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;

public class StockDetailsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JEditorPane display;
	private JButton optionsButton;

	ArrayList<String> data;

	public StockDetailsPanel() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		display = new JEditorPane();
		display.setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		display.setContentType("text/html");
		display.setEditable(false);

		add(new JScrollPane(display), BorderLayout.CENTER);
	}

	public void setData(ArrayList<String> data) {
		display.setText("");

		Font f = getFont();
		String font = f.getFamily();
		String fontSize = "8.5";

		// NumberFormat numberFormat = NumberFormat.getNumberInstance();

		int lastTradeIndex = StockRequestMapper.getIndexFromLabel("Last trade");
		int changeIndex = StockRequestMapper
				.getIndexFromLabel("Percent change");

		String lastTrade = MiscUtils.trimChars(data.get(lastTradeIndex).trim(),
				'"');
		String pctChange = MiscUtils.trimChars(data.get(changeIndex).trim(),
				'"');

		String change = lastTrade + " ( " + pctChange + " )";
		String color = "#ffffff";
		if (pctChange.charAt(0) == '+') {
			color = "#00ff00";
		}
		else {
			color = "#ff0000";
		}

		StringBuilder html = new StringBuilder();
		html.append("<html>");
		html.append("<body style=\"font-family:" + font + ";font-size:"
				+ fontSize + "px\">");
		html.append("<table width=100%");

		html.append("<tr bgcolor=\"" + color + "\" colspan=\"2\">");
		html.append("<td>");
		html.append(change);
		html.append("</td>");
		html.append("</tr>");

		for (int i = 0; i < data.size(); i += 2) {
			String currLbl = StockRequestMapper.labels[i][1];
			String curr = data.get(i);
			curr = MiscUtils.trimChars(curr, '"');

			String cell = getCell(currLbl, curr);

			html.append("<tr>");
			html.append(cell);

			String next = null;
			if (i + 1 < data.size()) {
				next = data.get(i + 1);
				next = MiscUtils.trimChars(next, '"');

				String nextLbl = StockRequestMapper.labels[i + 1][1];
				String nextCell = getCell(nextLbl, next);
				html.append(nextCell);
			}
			html.append("</tr>");
		}
		html.append("</table>");
		html.append("</html>");
		display.setText(html.toString());
	}

	private String getCell(String label, String value) {
		StringBuilder ret = new StringBuilder();
		NumberFormat numberFormat = NumberFormat.getNumberInstance();

		ret.append("<td width=30% style=\"background-color:"
				+ net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX
				+ ";\">"
				+ "<div style=\"background-color:"
				+ net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_HEADER_COLOR_HEX
				+ ";color: #ffffff; font-weight: bold\">" + label
				+ "</div></td>");

		try {
			String currNum = numberFormat.format(Double.parseDouble(value));
			ret.append("<td>");
			ret.append(currNum);
			ret.append("</td>");
		}
		catch (Exception e) {
			ret.append("<td>");
			ret.append(value);
			ret.append("</td>");
		}

		return ret.toString();
	}
}
