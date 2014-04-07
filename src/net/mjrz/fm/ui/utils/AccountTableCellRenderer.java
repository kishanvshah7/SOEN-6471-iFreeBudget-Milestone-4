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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JTable;

import net.mjrz.fm.entity.beans.AttachmentRef;

public class AccountTableCellRenderer extends TxDefaultCellrenderer {

	private static final long serialVersionUID = 1L;

	private final MultiIcon iconParentCollapsedWithAttach = new MultiIcon(
			"icons/bullet_arrow_right.png", "icons/attach.png");

	private final MultiIcon iconParentExpandedWithAttach = new MultiIcon(
			"icons/bullet_arrow_down.png", "icons/attach.png");

	private final MultiIcon iconParentCollapsed = new MultiIcon(
			"icons/bullet_arrow_right.png", null);

	private final MultiIcon iconParentExpanded = new MultiIcon(
			"icons/bullet_arrow_down.png", null);

	private final MultiIcon iconAttach = new MultiIcon("icons/attach.png", null);

	public AccountTableCellRenderer() {
		super();
		setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		java.awt.Font f = getFont();

		TransactionTableModel model = (TransactionTableModel) table.getModel();

		row = table.convertRowIndexToModel(row);
		List<AttachmentRef> atts = model.getAttachments(row);

		Boolean isParent = model.isParent(row);
		Boolean isExpanded = model.isExpanded(row);
		boolean hasAttachment = false;

		if (isSelected) {
			setFont(new java.awt.Font(f.getName(), java.awt.Font.BOLD,
					f.getSize()));
		}
		else {
			setFont(new java.awt.Font(f.getName(), java.awt.Font.PLAIN,
					f.getSize()));
		}

		super.setRowColors(table, value, isSelected, hasFocus, row, column);

		if (atts != null && atts.size() > 0) {
			hasAttachment = true;
		}

		if (isParent) {
			if (isExpanded) {
				if (hasAttachment) {
					setIcon(iconParentExpandedWithAttach);
				}
				else {
					setIcon(iconParentExpanded);
				}
			}
			else {
				if (hasAttachment) {
					setIcon(iconParentCollapsedWithAttach);
				}
				else {
					setIcon(iconParentCollapsed);
				}
			}
		}
		else {
			if (hasAttachment) {
				setIcon(iconAttach);
			}
			else {
				setIcon(null);
			}
		}
		setText(value.toString());
		setIconTextGap(10);
		return this;
	}

	static class MultiIcon implements Icon {
		private Image img1;
		private Image img2;

		public MultiIcon(String icon1, String icon2) {
			try {
				img1 = ImageIO.read(getClass().getClassLoader().getResource(
						icon1));
				if (icon2 != null) {
					img2 = ImageIO.read(getClass().getClassLoader()
							.getResource(icon2));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			if (img2 != null)
				return 32;
			return 16;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2D = (Graphics2D) g;
			g2D.drawImage(img1, x, y, null);
			if (img2 != null) {
				g2D.drawImage(img2, x + 16, y, null);
			}
		}
	}
}
