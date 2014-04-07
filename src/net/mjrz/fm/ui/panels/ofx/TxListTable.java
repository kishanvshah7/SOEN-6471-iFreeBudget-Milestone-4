package net.mjrz.fm.ui.panels.ofx;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import net.mjrz.fm.ui.utils.TableHeaderRenderer;
import net.mjrz.fm.ui.utils.UIDefaults;

public class TxListTable extends JTable {

	private static final long serialVersionUID = 1L;

	public TxListTable(TxListModel model) {
		super(model);
		init();
	}

	private void init() {
		setSelectionForeground(Color.BLACK);
		setShowHorizontalLines(true);
		getTableHeader().setPreferredSize(new Dimension(0, 40));
		getTableHeader().setReorderingAllowed(false);
		setIntercellSpacing(new Dimension(0, 0));
		getTableHeader().setPreferredSize(new Dimension(0, 40));
		getTableHeader().setDefaultRenderer(
				new TableHeaderRenderer(UIDefaults.DEFAULT_TABLE_HEADER_COLOR,
						Color.WHITE));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowHeight(20);

		setGridColor(new Color(154, 191, 192));
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == TxListModel.IDX_ERR_MSG) {
			return new ImportTableIconCellRenderer();
		}
		return super.getCellRenderer(row, column);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		if (vColIndex == TxListModel.IDX_SKIP) {
			return super.prepareRenderer(renderer, rowIndex, vColIndex);
		}
		JLabel c = (JLabel) super
				.prepareRenderer(renderer, rowIndex, vColIndex);
		if (isCellSelected(rowIndex, vColIndex)) {
			java.awt.Font f = c.getFont();
			c.setFont(new java.awt.Font(f.getName(), java.awt.Font.BOLD, f
					.getSize()));
			c.setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
			c.setBorder(BorderFactory.createEmptyBorder());
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
}
