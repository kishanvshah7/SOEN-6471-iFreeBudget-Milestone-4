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
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.wizards.components.WizardComponent;

@SuppressWarnings("serial")
public class BudgetAmountsPanel extends javax.swing.JPanel implements
		WizardComponent {

	/** Creates new form BudgetAmountsPanel */
	public BudgetAmountsPanel() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout(10, 10));

		jScrollPane1 = new javax.swing.JScrollPane();
		// jTable1 = new javax.swing.JTable() {
		// public Component prepareRenderer(TableCellRenderer renderer,
		// int rowIndex, int vColIndex) {
		// JLabel c = (JLabel) super.prepareRenderer(renderer, rowIndex,
		// vColIndex);
		// if (isCellSelected(rowIndex, vColIndex)) {
		// java.awt.Font f = c.getFont();
		// c.setFont(new java.awt.Font(f.getName(),
		// java.awt.Font.BOLD, f.getSize()));
		// c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
		// c.setBorder(BorderFactory.createEmptyBorder());
		// return c;
		// }
		// if (rowIndex % 2 == 0) { // && !isCellSelected(rowIndex,
		// // vColIndex)) {
		// c.setBackground(new Color(234, 234, 234));
		// } else {
		// c.setBackground(Color.WHITE);
		// }
		// return c;
		// }
		// };
		// jTable1.setSelectionForeground(Color.BLACK);
		// jTable1.setGridColor(new Color(154, 191, 192));
		// jTable1.getTableHeader().setPreferredSize(new Dimension(0, 40));
		// jTable1.getTableHeader().setDefaultRenderer(new
		// TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
		// Color.WHITE));
		// jTable1.getTableHeader().setReorderingAllowed(false);
		// jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// jTable1.setRowHeight(30);

		jTable1 = new javax.swing.JTable();
		jTable1.setSelectionForeground(Color.BLACK);
		jTable1.setGridColor(new Color(154, 191, 192));
		jTable1.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		jTable1.setRowHeight(20);
		jTable1.setBackground(Color.WHITE);

		tableModel = new MyTableModel();
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				computeTotalAndDisplay();
			}
		});

		jTable1.setModel(tableModel);

		jTable1.setColumnSelectionAllowed(true);
		jScrollPane1.setViewportView(jTable1);
		jTable1.getColumnModel()
				.getSelectionModel()
				.setSelectionMode(
						javax.swing.ListSelectionModel.SINGLE_SELECTION);
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(250);
		jTable1.getColumnModel().getColumn(1).setPreferredWidth(50);
		jTable1.getParent().setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		center.add(jScrollPane1, BorderLayout.CENTER);
		center.setBackground(UIDefaults.DEFAULT_PANEL_BG_COLOR);

		add(center, BorderLayout.CENTER);
		add(getSouthPanel(), BorderLayout.SOUTH);
	}

	private void computeTotalAndDisplay() {
		int sz = tableModel.getRowCount();
		BigDecimal total = new BigDecimal(0);
		for (int i = 0; i < sz; i++) {
			Object obj = tableModel.getValueAt(i, 1);
			if (obj instanceof java.math.BigDecimal) {
				BigDecimal bd = (BigDecimal) tableModel.getValueAt(i, 1);
				total = total.add(bd);
			}
		}
		if (total.intValue() > 0) {
			totalLbl.setVisible(true);
			totalValueLbl.setVisible(true);
			totalValueLbl.setText(numFormat.format(total));
		}
		else {
			totalLbl.setVisible(false);
			totalValueLbl.setVisible(false);
		}
	}

	static class MyTableModel extends DefaultTableModel {
		boolean[] canEdit = new boolean[] { false, true };

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return canEdit[columnIndex];
		}

		public MyTableModel() {
			super(new Object[][] {}, new String[] { "Account",
					"Amount allocated" });
		}

		public void addRow(Object[] rowData) {
			super.addRow(rowData);
		}

		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 1) {
				return BigDecimal.class;
			}
			return String.class;
		}
	}

	private JPanel getSouthPanel() {
		JPanel ret = new JPanel();
		Font f = getFont();
		totalLbl = new javax.swing.JLabel();
		totalLbl.setText("Total: ");
		totalLbl.setHorizontalAlignment(JLabel.TRAILING);
		totalLbl.setVerticalAlignment(JLabel.TOP);
		totalLbl.setVisible(false);
		totalLbl.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));

		totalValueLbl = new JLabel("3999");
		totalValueLbl.setHorizontalAlignment(JLabel.TRAILING);
		totalValueLbl.setVerticalAlignment(JLabel.TOP);
		totalValueLbl.setBorder(BorderFactory
				.createLineBorder(Color.LIGHT_GRAY));
		totalValueLbl.setVisible(false);
		totalValueLbl.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		totalValueLbl.setPreferredSize(new Dimension(200, 20));

		JPanel top1 = new JPanel();
		top1.setLayout(new BoxLayout(top1, BoxLayout.X_AXIS));
		top1.add(Box.createHorizontalGlue());
		top1.add(totalLbl);
		top1.add(totalValueLbl);
		top1.add(Box.createHorizontalStrut(25));

		msgLbl = new JLabel(
				"Enter the amount you want to allocate for each account");
		msgLbl.setHorizontalAlignment(JLabel.CENTER);

		ret.setLayout(new BorderLayout());
		ret.add(top1, BorderLayout.NORTH);
		ret.add(msgLbl, BorderLayout.CENTER);

		ret.setPreferredSize(new Dimension(400, 100));
		return ret;
	}

	// Variables declaration - do not modify
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTable jTable1;
	private javax.swing.JLabel msgLbl, totalLbl, totalValueLbl;
	private MyTableModel tableModel;
	private String errMsg;
	private static final NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	// End of variables declaration

	private String[][] getAmounts() {
		int sz = tableModel.getRowCount();
		String[][] ret = new String[sz][];
		for (int i = 0; i < sz; i++) {
			String n = (String) tableModel.getValueAt(i, 0);
			String v = ((BigDecimal) tableModel.getValueAt(i, 1)).toString();
			String[] row = { n, v };
			ret[i] = row;
		}
		return ret;
	}

	@Override
	public String getMessage() {
		return errMsg;
	}

	@Override
	public String[][] getValues() {
		return getAmounts();
	}

	@Override
	public boolean isComponentValid() {
		errMsg = null;
		boolean ret = true;
		int sz = tableModel.getRowCount();
		for (int i = 0; i < sz; i++) {
			Object o = tableModel.getValueAt(i, 1);
			String n = (String) tableModel.getValueAt(i, 0);
			if (o == null) {
				errMsg = "Invalid amount for account " + n;
				ret = false;
				break;
			}
			else {
				try {
					new BigDecimal(o.toString());
				}
				catch (Exception e) {
					ret = false;
					errMsg = "Invalid amount for account " + n;
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public void setComponentFocus() {
	}

	@Override
	public void updateComponentUI(HashMap<String, String[][]> values) {
		tableModel.setRowCount(0);
		String[][] value = values.get("Select accounts");
		// String[] acctTypes = value[0];
		String[] aNames = value[1];
		if (aNames != null) {
			for (int i = 0; i < aNames.length; i++) {
				Object[] rowData = { aNames[i], "" };
				tableModel.addRow(rowData);
			}
		}
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.getContentPane().setLayout(new BorderLayout());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.getContentPane().add(new BudgetAmountsPanel(),
						BorderLayout.CENTER);
				f.setSize(600, 400);
				f.setVisible(true);
			}
		});
	}
}
