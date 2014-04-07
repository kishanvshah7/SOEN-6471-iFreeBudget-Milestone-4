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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ExpReportDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private JPanel buttonPanel, titlePanel;
	private JButton saveButton, closeButton;
	private JEditorPane displayPane;

	private JTable dataTable = null;
	private DefaultTableModel dataTableModel = null;

	protected String[] expReportHeaderCols = { "Category", "Expense Type",
			"Num occurances", "Total amount", "Minumum", "Maximum" };
	protected String[] earningsReportHeaderCols = { "Category", "Income Type",
			"Num occurances", "Total amount", "Minumum", "Maximum" };

	// private Vector<String> colData;
	// private Vector<Vector<String>> rowData;

	private String htmlData = null;

	private NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());
	private static Logger logger = Logger.getLogger(ExpReportDialog.class
			.getName());
	private String[] header = null;
	public static final String title = "Cash flow report";

	public ExpReportDialog(JFrame parent, ActionResponse resp, String title) {
		super(parent, title, true); //$NON-NLS-1$
		header = getHeader();
		try {
			buildTableData(resp);
			htmlData = buildCashFlowHtml(resp);
		}
		catch (Exception e) {
			e.printStackTrace();
			dispose();
		}
		initialize();
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(getCenterPanel(), BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	@SuppressWarnings("serial")
	private JPanel getCenterPanel() {
		JPanel ret = new JPanel();

		Font f = getFont();
		String font = f.getFamily();
		String fontSize = "8.5";

		displayPane = new JEditorPane();
		displayPane.setContentType("text/html");
		displayPane.setEditable(false);
		displayPane.setBackground(Color.WHITE);
		displayPane.setFont(f);
		displayPane.setForeground(Color.BLACK);

		StringBuilder html = new StringBuilder(
				"<html><body style=\"font-family:" + font + ";font-size:"
						+ fontSize + "px\">");
		html.append(htmlData);
		html.append("</body></html>");
		displayPane.setText(html.toString());

		displayPane.setBorder(BorderFactory.createTitledBorder("Report:"));

		dataTable = new JTable(dataTableModel) {
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
					return c;
				}
				if (rowIndex % 2 == 0) {
					c.setBackground(new Color(234, 234, 234));
				}
				else {
					c.setBackground(Color.WHITE);
				}
				return c;
			}
		};
		dataTable.setSelectionBackground(Color.BLUE);
		dataTable.setSelectionForeground(Color.BLUE);
		dataTable.setShowVerticalLines(false);
		dataTable.setGridColor(new Color(154, 191, 192));
		dataTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		dataTable.setRowHeight(20);
		dataTable.setRowSorter(setupRowSorter());

		JScrollPane tableSp = new JScrollPane(dataTable);
		tableSp.setBorder(BorderFactory
				.createTitledBorder("Expense report by category:"));

		ret.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		gbc1.weighty = 0.4;
		gbc1.weightx = 1;
		ret.add(new JScrollPane(displayPane), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		gbc1.weighty = 0.6;
		ret.add(tableSp, gbc1);

		return ret;
	}

	private RowSorter<TableModel> setupRowSorter() {
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(
				dataTableModel);

		rowSorter.setComparator(0, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		rowSorter.setComparator(1, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		rowSorter.setComparator(2, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		rowSorter.setComparator(3, bdComparator);
		rowSorter.setComparator(4, bdComparator);
		rowSorter.setComparator(5, bdComparator);
		return rowSorter;
	}

	transient Comparator<String> bdComparator = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			try {
				BigDecimal o1D = BigDecimal.valueOf(numFormat.parse(o1)
						.doubleValue());
				BigDecimal o2D = BigDecimal.valueOf(numFormat.parse(o2)
						.doubleValue());
				return o1D.compareTo(o2D);
			}
			catch (ParseException e) {
				logger.error(MiscUtils.stackTrace2String(e));
				return 0;
			}
		}
	};

	protected String getReportTitle() {
		return "Cash flow Report - Generated on " + new java.util.Date();
	}

	private JPanel getTitlePanel() {
		JLabel titleLbl = new JLabel();
		if (titlePanel == null) {
			titleLbl.setText(getReportTitle());
			titlePanel = new JPanel();
			titlePanel.setLayout(new FlowLayout());
			titlePanel.add(titleLbl, null);
			titlePanel.setBackground(UIDefaults.DEFAULT_COLOR);
		}
		titlePanel.setBorder(javax.swing.BorderFactory
				.createLineBorder(Color.BLACK));
		return titlePanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(getSaveButton());
			buttonPanel.add(Box.createHorizontalStrut(5));
			buttonPanel.add(getCloseButton());
		}
		return buttonPanel;
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText("Save");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					doSaveButtonActionPerformed(e);
				}
			});
		}
		return saveButton;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton();
			closeButton.setText("Close");
			closeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		super.getRootPane().setDefaultButton(closeButton);
		return closeButton;
	}

	private String buildCashFlowHtml(ActionResponse resp) {
		BigDecimal totalInc = (BigDecimal) resp.getResult("TOTALINCOME");
		BigDecimal totalExp = (BigDecimal) resp.getResult("TOTALSPENDING");
		BigDecimal savings = (BigDecimal) resp.getResult("SAVINGS");
		Double pctSavings = (Double) resp.getResult("PCTSAVINGS");

		String[] range = (String[]) resp.getResult("RANGE");

		StringBuffer ret = new StringBuffer();

		String rpt = "<p><b>Report for: " + range[0] + " to " + range[1]
				+ "</b></p><br>";
		ret.append(rpt);
		ret.append("<TABLE WIDTH=60% BORDER=0>");

		ret.append("<tr>" + "<td>Total income for the period</td>" + "<td>");

		ret.append(numFormat.format(totalInc));

		ret.append("</td>" + "</tr>" + "<tr>"
				+ "<td>Total spending for the period</td>" + "<td>");
		ret.append(numFormat.format(totalExp));

		ret.append("</td>" + "</tr>" + "<tr>"
				+ "<td>Percent of income spent</td>" + "<td>");
		ret.append(roundDouble(pctSavings, 2) + "%");

		ret.append("</td>" + "</tr>" + "<tr>" + "<td>Amount saved</td>"
				+ "<td>");
		ret.append(numFormat.format(savings));

		ret.append("</td>" + "</tr>" + "</table>");
		return ret.toString();
	}

	protected String[] getHeader() {
		return expReportHeaderCols;
	}

	@SuppressWarnings("unchecked")
	private void buildTableData(ActionResponse response) throws Exception {
		dataTableModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		for (int i = 0; i < header.length; i++) {
			dataTableModel.addColumn(header[i]);
		}

		HashMap<Long, ArrayList<Transaction>> data = (HashMap<Long, ArrayList<Transaction>>) response
				.getResult("EXPENSELIST");
		Set<Entry<Long, ArrayList<Transaction>>> eset = data.entrySet();
		for (Entry<Long, ArrayList<Transaction>> e : eset) {
			Long l = e.getKey();
			ArrayList<Transaction> txList = e.getValue();

			String aname = FManEntityManager.getAccountName(l);
			String cname = FManEntityManager.getCategoryName(l);
			Integer num = txList.size();
			BigDecimal sum = new BigDecimal("0.0");
			BigDecimal min = new BigDecimal(0.0d);
			BigDecimal max = new BigDecimal(0.0d);

			for (Transaction t : txList) {
				BigDecimal curr = t.getTxAmount();
				sum = sum.add(curr);
				if (curr.compareTo(max) > 0)
					max = curr;
				if (curr.compareTo(min) < 0 || min.doubleValue() == 0d)
					min = curr;
			}
			Vector<Object> row = new Vector<Object>();
			row.add(cname);
			row.add(aname);
			row.add(num);
			row.add(numFormat.format(sum));
			row.add(numFormat.format(min));
			row.add(numFormat.format(max));
			// rowData.add(row);
			dataTableModel.addRow(row);
		}
	}

	private static final double roundDouble(double d, int places) {
		return Math.round(d * Math.pow(10, (double) places))
				/ Math.pow(10, (double) places);
	}

	private void doSaveButtonActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File target = fc.getSelectedFile();
			String html = getReportAsHtml();
			writeToFile(target, html);
		}
	}

	private void writeToFile(File file, String data) {
		BufferedWriter out = null;
		try {
			StringBuilder fname = new StringBuilder(file.getAbsolutePath());

			String ext = MiscUtils.getExtension(file);
			if (ext == null || !ext.equals("html")) {
				fname.append(".html");
			}

			out = new BufferedWriter(new FileWriter(new File(fname.toString())));
			out.write(data);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

	private String getReportAsHtml() {
		StringBuilder ret = new StringBuilder();
		ret.append("<html>");
		ret.append("<body style=\"font-family:Courier; font-size: 80%\">");
		ret.append("<p style=\"font-family:verdana\">");
		ret.append(htmlData);
		ret.append("<table width=\"100%\">");

		ret.append("<tr bgcolor=\"#c0c0c0\">");
		for (int i = 0; i < header.length; i++) {
			ret.append("<td>");
			ret.append("<b>" + header[i] + "</b>");
			ret.append("</td>");
		}
		ret.append("</tr>");

		int sz = 0;
		Vector<Vector<String>> rowData = dataTableModel.getDataVector();
		for (Vector<String> row : rowData) {
			if (sz % 2 != 0)
				ret.append("<tr bgcolor=\"#eaeaea\">");
			else
				ret.append("<tr>");

			for (Object s : row) {
				ret.append("<td>");
				ret.append(s);
				ret.append("</td>");
			}
			ret.append("</tr>");
			sz++;
		}
		ret.append("</table>");
		ret.append("</p>");
		ret.append("</body>");
		ret.append("</html>");

		return ret.toString();
	}
}
