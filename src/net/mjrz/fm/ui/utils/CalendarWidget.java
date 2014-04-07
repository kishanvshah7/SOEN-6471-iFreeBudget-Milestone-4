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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.mjrz.fm.utils.BuildCalendar;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CalendarWidget extends JDialog implements ChangeListener,
		ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTable calTable;

	private JComboBox monthsCb;

	private String[] yearsList;

	private SpinnerModel spinnerModel;

	private JSpinner yearSpinner;

	private CalTableModel tableModel;

	private CalendarWidgetListener parent;

	private Calendar today;

	private boolean showHours = false;

	private JSpinner hrSpinner = null;

	private JSpinner minSpinner = null;

	private static String[] MONTHS = { "January", "February", "March", "April",
			"May", "June", "July", "August", "September", "October",
			"November", "December" };

	public CalendarWidget(Window owner, Component parent, boolean showHours) {
		super(owner);

		this.setModalityType(ModalityType.MODELESS);

		this.setTitle("Calendar");

		this.parent = (CalendarWidgetListener) parent;

		today = Calendar.getInstance();

		yearsList = new String[1];

		yearsList[0] = "" + today.get(Calendar.YEAR);

		this.showHours = showHours;

		initialize();
	}

	private void initialize() {
		getContentPane().setLayout(new BorderLayout(5, 5));

		getContentPane().add(getCalendarPanel(), BorderLayout.CENTER);

		if (showHours)
			getContentPane().add(getTimePanel(), BorderLayout.SOUTH);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		this.setUndecorated(true);

		this.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
	}

	private void dispose2(Date date) {
		parent.setDate(date);
		super.dispose();
	}

	public void dispose() {
		parent.setDate(null);
		super.dispose();
	}

	private JPanel getCalendarPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		calTable = new JTable();
		tableModel = new CalTableModel();
		calTable.setModel(tableModel);
		calTable.setRowHeight(20);
		calTable.setSelectionBackground(Color.WHITE);
		calTable.setSelectionForeground(Color.BLACK);
		calTable.setRowSelectionAllowed(false);
		calTable.setCellSelectionEnabled(true);
		calTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		calTable.setShowHorizontalLines(true);
		calTable.setShowVerticalLines(false);
		calTable.getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		calTable.addMouseListener(this);
		calTable.getTableHeader().setReorderingAllowed(false);

		for (int i = 0; i < 7; i++) {
			TableColumn column = calTable.getColumnModel().getColumn(i);
			column.setMinWidth(0);
			column.setMaxWidth(30);
			column.setPreferredWidth(30);
			column.setCellRenderer(new CalTableCellRenderer());
		}

		ret.add(new JScrollPane(calTable), BorderLayout.CENTER);
		ret.add(getSpinnerPanel(), BorderLayout.NORTH);

		// ret.setPreferredSize(new Dimension(210, 200));
		ret.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		return ret;
	}

	private JPanel getSpinnerPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(1, 2, 5, 5));

		monthsCb = new JComboBox(MONTHS);
		monthsCb.setSelectedIndex(today.get(Calendar.MONTH));

		monthsCb.addActionListener(this);
		ret.add(monthsCb);

		yearSpinner = new JSpinner();

		spinnerModel = new YearSpinnerModel();

		spinnerModel.addChangeListener(this);

		yearSpinner.setModel(spinnerModel);

		ret.add(yearSpinner);

		ret.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
		return ret;
	}

	private JPanel getTimePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		hrSpinner = new JSpinner();
		ArrayList<Integer> values = new ArrayList<Integer>();
		for (int i = 0; i < 24; i++) {
			values.add(i);
		}
		SpinnerListModel hrModel = new SpinnerListModel(values);
		hrSpinner.setModel(hrModel);
		JLabel hrLbl = new JLabel(tr("Hour"));
		hrLbl.setLabelFor(hrSpinner);

		minSpinner = new JSpinner();
		ArrayList<Integer> minValues = new ArrayList<Integer>();
		for (int i = 0; i < 60; i++) {
			minValues.add(i);
		}
		SpinnerListModel minModel = new SpinnerListModel(minValues);
		minSpinner.setModel(minModel);
		JLabel minLbl = new JLabel(tr("Minute"));
		minLbl.setLabelFor(minSpinner);

		Calendar c = Calendar.getInstance();
		int hr = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);

		hrSpinner.setValue(hr);
		minSpinner.setValue(min);

		ret.add(Box.createHorizontalGlue());
		ret.add(hrLbl);
		ret.add(hrSpinner);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(minLbl);
		ret.add(minSpinner);
		ret.add(Box.createHorizontalGlue());
		return ret;
	}

	static class CalTableCellRenderer implements TableCellRenderer {
		public CalTableCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {

			JLabel l = new JLabel(value.toString(), JLabel.CENTER);
			l.setOpaque(true);
			if (isSelected) {
				l.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
				l.setForeground(Color.WHITE);
			}
			else {
				l.setBackground(Color.WHITE);
				l.setForeground(Color.BLACK);
			}
			Integer val = (Integer) value;
			if (val.intValue() == 0) {
				l.setText("");
			}
			return l;
		}
	}

	class CalTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private Vector<String> colNames;

		private int dateArr[][] = null;

		BuildCalendar calendar = null;

		public CalTableModel() {
			calendar = new BuildCalendar();

			colNames = new Vector<String>();
			colNames.add("S");
			colNames.add("M");
			colNames.add("T");
			colNames.add("W");
			colNames.add("T");
			colNames.add("F");
			colNames.add("S");

			calendar.setCalendar(today.get(Calendar.YEAR),
					today.get(Calendar.MONTH), today.get(Calendar.DATE));
			dateArr = calendar.getCalendarArray();
		}

		void setCalendar(int year, int month) {
			calendar.setCalendar(year, month, 1);
			dateArr = calendar.getCalendarArray();
			calTable.setModel(this);
			fireTableDataChanged();
		}

		public String getColumnName(int idx) {
			return colNames.get(idx);
		}

		public int getColumnCount() {
			return 7;
		}

		public int getRowCount() {
			return dateArr.length;
		}

		public Object getValueAt(int rowIdx, int colIdx) {
			if (!(rowIdx < dateArr.length)) {
				return null;
			}
			int[] row = dateArr[rowIdx];
			if (!(colIdx < row.length)) {
				return null;
			}

			return row[colIdx];
		}
	}

	static class YearSpinnerModel extends SpinnerListModel {
		private static final long serialVersionUID = 1L;
		String currState[] = null;

		public YearSpinnerModel() {
			currState = new String[1];
			Calendar cal = new GregorianCalendar();
			currState[0] = String.valueOf(cal.get(Calendar.YEAR));
		}

		public Object getNextValue() {
			String curr = currState[0];
			int x = Integer.parseInt(curr) + 1;
			String ret = null;
			if (x <= 2099) {
				ret = "" + x;
			}
			else {
				ret = "1980";
			}
			return ret;
		}

		public Object getPreviousValue() {
			String curr = currState[0];
			int x = Integer.parseInt(curr) - 1;
			String ret = null;
			if (x >= 1980) {
				ret = "" + x;
			}
			else {
				ret = "2099";
			}
			return ret;
		}

		public Object getValue() {
			return currState[0];
		}

		public void setValue(Object value) {
			currState[0] = (String) value;
			this.fireStateChanged();
		}
	}

	public void stateChanged(ChangeEvent e) {
		String date = (String) yearSpinner.getModel().getValue();
		int year = Integer.parseInt(date);
		int month = monthsCb.getSelectedIndex();
		if (month < 0)
			month = 0;
		tableModel.setCalendar(year, month);
	}

	public void actionPerformed(ActionEvent e) {
		int month = monthsCb.getSelectedIndex();
		if (month < 0)
			month = 0;
		String date = (String) yearSpinner.getModel().getValue();
		int year = Integer.parseInt(date);
		tableModel.setCalendar(year, month);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 1) {
			int row = calTable.getSelectedRow();
			int col = calTable.getSelectedColumn();
			row = calTable.convertRowIndexToModel(row);
			col = calTable.convertColumnIndexToModel(col);
			int mon = monthsCb.getSelectedIndex();
			if (mon < 0)
				mon = 0;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR,
					Integer.parseInt((String) spinnerModel.getValue()));
			cal.set(Calendar.MONTH, mon);
			cal.set(Calendar.DATE, (Integer) tableModel.getValueAt(row, col));

			if (showHours) {
				Integer hr = (Integer) hrSpinner.getModel().getValue();
				Integer min = (Integer) minSpinner.getModel().getValue();
				cal.set(Calendar.HOUR_OF_DAY, hr);
				cal.set(Calendar.MINUTE, min);
				cal.set(Calendar.SECOND, 0);
			}
			else {
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			dispose2(cal.getTime());
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}
}
