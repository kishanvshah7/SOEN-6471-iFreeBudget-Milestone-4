package net.mjrz.fm.ui.utils;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class BucketCellRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	public BucketCellRenderer() {
		super();
		setOpaque(true);
		java.awt.Font f = getFont();
		setFont(new java.awt.Font(f.getName(), java.awt.Font.BOLD, f.getSize()));
		this.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
				UIDefaults.DEFAULT_TABLE_HEADER_COLOR));
		setBackground(UIDefaults.GROUP_ROW_COLOR);
		setForeground(Color.black);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (isSelected) {
			setBackground(UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);
			setForeground(Color.white);
		}
		else {
			setBackground(UIDefaults.GROUP_ROW_COLOR);
			setForeground(Color.black);
		}
		setText(value.toString());

		return this;
	}
}
