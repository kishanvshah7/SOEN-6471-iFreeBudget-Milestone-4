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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TxHistoryTable extends JTable {
	private static final long serialVersionUID = 1L;

	final AccountTableCellRenderer accountCellRenderer = new AccountTableCellRenderer();
	final BucketCellRenderer bucketCellRenderer = new BucketCellRenderer();
	final TxDefaultCellrenderer defaultRenderer = new TxDefaultCellrenderer();
	final TxIconCellRenderer txIconRenderer = new TxIconCellRenderer();

	public TxHistoryTable(TransactionTableModel txTableModel) {
		super(txTableModel);

		initDefaults();
	}

	private void initDefaults() {
		setSelectionForeground(Color.BLACK);

		// setGridColor(new Color(154, 191, 192));
		setShowGrid(false);
		setShowHorizontalLines(true);

		getTableHeader().setPreferredSize(new Dimension(0, 40));
		getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		getTableHeader().setReorderingAllowed(false);
		setRowHeight(30);
		setIntercellSpacing(new Dimension(0, 0));
	}

	private TransactionTableModel getTxTableModel() {
		return (TransactionTableModel) super.getModel();
	}

	// Add cell renderers
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		TransactionTableModel model = getTxTableModel();
		if (!model.isTransactionRow(row)) {
			return bucketCellRenderer;
		}
		else if (column == 0) {
			return txIconRenderer;
		}
		else if (column == 1) {
			return accountCellRenderer;
		}
		return defaultRenderer;
	}
}
