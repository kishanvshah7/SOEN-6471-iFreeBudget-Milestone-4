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

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CurrencyTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	Vector<Object> datas = null;

	boolean[] columnsVisible = new boolean[5];

	String columnsName[] = { "AccountId", "Country", "Symbol",
			"1 US Dollar ($) = ", "Last updated" };

	int[] sortInfo;

	public static final int NONE = 0;
	public static final int ASC = 1;
	public static final int DESC = 2;

	public CurrencyTableModel() {
		columnsVisible[0] = false;
		columnsVisible[1] = true;
		columnsVisible[2] = true;
		columnsVisible[3] = true;
		columnsVisible[4] = true;

		sortInfo = new int[columnsName.length];
		for (int i = 0; i < columnsName.length; i++) {
			sortInfo[i] = NONE;
		}
		datas = new Vector<Object>();
	}

	/**
	 * This functiun converts a column number in the table to the right number
	 * of the datas.
	 */
	protected int getNumber(int col) {
		int n = col;
		int i = 0;
		do {
			if (!(columnsVisible[i]))
				n++;
			i++;
		}
		while (i < n);
		while (!(columnsVisible[n]))
			n++;
		return n;
	}

	// *** TABLE MODEL METHODS ***

	public int getColumnCount() {
		int n = 0;
		for (int i = 0; i < columnsName.length; i++)
			if (columnsVisible[i])
				n++;
		return n;
	}

	public int getRowCount() {
		return datas.size();
	}

	public Object getValueAt(int row, int col) {
		if (datas.size() == 0 || row >= datas.size())
			return null;
		Object[] array = (Object[]) (datas.elementAt(row));
		return array[getNumber(col)];
	}

	public Object getId(int row) {
		if (row < 0 || row >= datas.size())
			return null;
		Object[] array = (Object[]) (datas.elementAt(row));
		return array[0];
	}

	public String getColumnName(int col) {
		return columnsName[getNumber(col)];
	}

	public void insertRow(int row, Object[] rowData)
			throws ArrayIndexOutOfBoundsException {
		if (row < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (row < datas.size()) {
			datas.insertElementAt(rowData, row);
		}
		if (row >= datas.size()) {
			datas.add(rowData);
			row = datas.size() - 1;
		}
		this.fireTableRowsInserted(row, row);
	}

	public void updateRow(Object[] rowData, int row)
			throws ArrayIndexOutOfBoundsException {
		if (row < 0 || row >= datas.size()) {
			throw new ArrayIndexOutOfBoundsException("Index: " + row);
		}
		datas.setElementAt(rowData, row);
		this.fireTableRowsUpdated(row, row);
	}

	public void setRowCount(int rowCount) {
		if (rowCount == 0) {
			datas.removeAllElements();
		}
		if (rowCount < datas.size()) {
			int sz = datas.size();
			for (int i = rowCount; i < sz; i++) {
				datas.remove(i);
			}
		}
		this.fireTableRowsDeleted(rowCount - 1, datas.size() - 1);
	}

	public void removeRow(int row) {
		if (row < 0 || row >= datas.size())
			return;
		datas.remove(row);
		this.fireTableRowsDeleted(row, row);
	}

	public void removeRows(int srow, int erow) {
		if (srow < 0 || srow >= datas.size())
			return;
		if (erow < 0 || erow >= datas.size())
			return;
		if (srow > erow)
			return;

		for (int i = erow; i >= srow; i--) {
			datas.remove(i);
		}

		this.fireTableRowsDeleted(srow, erow);
	}

	public Vector<Object> getDataVector() {
		return datas;
	}

	/* Methods to manage sort info */
	private void resetSortInfo() {
		for (int i = 0; i < sortInfo.length; i++) {
			sortInfo[i] = NONE;
		}
	}

	public void setSortInfo(int col) {
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length) {
			resetSortInfo();
			return;
		}
		int prev = sortInfo[col];
		resetSortInfo();
		switch (prev) {
		case NONE:
			sortInfo[col] = ASC;
			break;
		case ASC:
			sortInfo[col] = DESC;
			break;
		case DESC:
			sortInfo[col] = ASC;
			break;
		}
	}

	public int getSortInfo(int col) {
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length)
			return -1;
		return sortInfo[col];
	}
}
