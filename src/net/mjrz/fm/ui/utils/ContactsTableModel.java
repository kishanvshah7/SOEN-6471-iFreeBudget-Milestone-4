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
public class ContactsTableModel extends AbstractTableModel implements
		SortableTableModel {

	private static final long serialVersionUID = 7588322598899296618L;

	/** Vector of Object[], this are the datas of the table */
	Vector datas = null; // new Vector();

	/** Indicates which columns are visible */
	boolean[] columnsVisible = new boolean[6];

	/** Column names */
	String columnsName[] = { "id", "Name", "Phone", "Email" };

	int[] sortInfo;

	/** Constructor */
	public ContactsTableModel() {
		columnsVisible[0] = false;
		columnsVisible[1] = true;
		columnsVisible[2] = true;
		columnsVisible[3] = true;

		sortInfo = new int[columnsName.length];
		for (int i = 0; i < columnsName.length; i++) {
			sortInfo[i] = NONE;
		}
		datas = new Vector();
	}

	/**
	 * This functiun converts a column number in the table to the right number
	 * of the datas.
	 */
	protected int getNumber(int col) {
		int n = col; // right number to return
		int i = 0;
		do {
			if (!(columnsVisible[i]))
				n++;
			i++;
		}
		while (i < n);
		// If we are on an invisible column,
		// we have to go one step further
		while (!(columnsVisible[n]))
			n++;
		return n;
	}

	// *** TABLE MODEL METHODS ***

	public int getColumnCount() {
		int n = 0;
		for (int i = 0; i < 6; i++)
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

	public Object getContactId(int row) {
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
		}
		fireTableRowsInserted(row, row);
	}

	public void updateRow(long oldId, Object[] rowData)
			throws ArrayIndexOutOfBoundsException {
		int i = 0;
		int sz = datas.size();
		for (; i < sz; i++) {
			Object[] srow = (Object[]) datas.get(i);
			if (srow[0].equals(Long.toString(oldId))) {
				break;
			}
		}
		if (i >= 0 && i < datas.size()) {
			datas.setElementAt(rowData, i);
			this.fireTableRowsUpdated(i, i);
		}
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

	public Vector getDataVector() {
		return datas;
	}

	/* Methods to manage sort info */
	public void resetSortInfo() {
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

	public void setSortInfo(int col, int direction) {
		resetSortInfo();
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length) {
			resetSortInfo();
			return;
		}
		if (direction != ASC && direction != DESC) {
			return;
		}
		sortInfo[col] = direction;
	}

	public int getSortInfo(int col) {
		col = getNumber(col);
		if (col < 0 || col >= sortInfo.length)
			return -1;
		return sortInfo[col];
	}
}
