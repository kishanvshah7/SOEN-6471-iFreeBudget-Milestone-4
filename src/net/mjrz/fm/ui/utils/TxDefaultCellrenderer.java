package net.mjrz.fm.ui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TxDefaultCellrenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	private Font bold = null;
	private Font normal = null;

	public TxDefaultCellrenderer() {
		super();
		setOpaque(true);
		normal = getFont();
		bold = (new java.awt.Font(normal.getName(), java.awt.Font.BOLD,
				normal.getSize()));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

		if (isSelected) {
			setFont(bold);
		}
		else {
			setFont(normal);
		}

		setBorder(BorderFactory.createEmptyBorder());
		setRowColors(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		setText(value.toString());

		return this;
	}

	protected void setRowColors(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int rowIndex, int vColIndex) {
		TransactionTableModel model = (TransactionTableModel) table.getModel();
		if (isSelected) {
			setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
			setForeground(Color.black);
		}
		else {
			Color c = model.getRowColor(rowIndex);
			if (c != null) {
				setBackground(c);
				setForeground(Color.black);
			}
			else {
				if (rowIndex % 2 == 0) {
					setBackground(new Color(234, 234, 234));
					setForeground(Color.black);
				}
				else {
					setBackground(Color.WHITE);
					setForeground(Color.black);
				}
			}
		}
	}
}
