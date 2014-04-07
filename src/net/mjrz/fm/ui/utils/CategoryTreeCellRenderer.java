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

import javax.swing.ImageIcon;
import javax.swing.JTree;

import net.mjrz.fm.entity.beans.AccountCategory;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */

public class CategoryTreeCellRenderer extends TreeCellRenderer {

	private static final long serialVersionUID = 1L;

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		try {
			AccountCategory ac = (AccountCategory) value;
			ImageIcon ic = IconMap.getInstance().getIcon(ac.getCategoryId());
			if (ic != null) {
				setIcon(ic);
			}
			else {
				if (expanded) {
					setIcon(categoryIconExpanded);
				}
				else {
					setIcon(categoryIcon);
				}
			}
		}
		catch (Exception e) {
			setIcon(categoryIcon);
		}
		return this;
	}
}
