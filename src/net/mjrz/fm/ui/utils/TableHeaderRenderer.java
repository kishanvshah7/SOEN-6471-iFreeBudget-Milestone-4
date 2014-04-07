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
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
@SuppressWarnings("serial")
public class TableHeaderRenderer extends DefaultTableCellRenderer {
	Color background;
	Color foreground;
	static final ImageIcon ASC_ICON = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/arrow_up.gif");
	static final ImageIcon DESC_ICON = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/arrow_down.gif");

	public TableHeaderRenderer(Color background, Color foreground) {
		super();
		super.setFont(new Font("Lucida", Font.BOLD, 12));
		this.background = background;
		this.foreground = foreground;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		setText("<html><b>" + value + "</b></html>");
		setOpaque(true);
		setBackground(background);
		setForeground(foreground);

		TableModel tm = table.getModel();
		if (tm instanceof SortableTableModel) {
			int sortInfo = ((SortableTableModel) tm).getSortInfo(column);
			if (sortInfo == SortableTableModel.ASC) {
				setIcon(ASC_ICON);
			}
			if (sortInfo == SortableTableModel.DESC) {
				setIcon(DESC_ICON);
			}
			if (sortInfo == SortableTableModel.NONE) {
				setIcon(null);
			}
			setHorizontalTextPosition(JLabel.LEFT);
			setIconTextGap(10);
		}

		return this;
	}
}
