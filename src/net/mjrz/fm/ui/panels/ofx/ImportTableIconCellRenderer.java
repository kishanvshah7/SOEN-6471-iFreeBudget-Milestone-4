package net.mjrz.fm.ui.panels.ofx;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ImportTableIconCellRenderer extends JLabel implements
		TableCellRenderer {

	private static final long serialVersionUID = 1L;

	private final ImageIcon errIcon = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/alert.png");

	public ImportTableIconCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

		String msg = (String) value;
		if (msg == null || msg.length() == 0) {
			this.setIcon(null);
		}
		else {
			this.setIcon(errIcon);
		}
		return this;
	}
}
