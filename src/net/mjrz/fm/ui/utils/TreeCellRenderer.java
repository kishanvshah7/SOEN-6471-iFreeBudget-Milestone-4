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
import javax.swing.tree.DefaultTreeCellRenderer;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer {
	static final long serialVersionUID = 0L;
	static final ImageIcon categoryIcon = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/ec/folder.png");
	static final ImageIcon categoryIconExpanded = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/ec/folder-open.png");
	static final ImageIcon moneyPlus = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/money_add.png");
	static final ImageIcon moneyMinus = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/money_delete.png");
	static final ImageIcon alertIcon = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/alert.png");
	static final ImageIcon lockIcon = new net.mjrz.fm.ui.utils.MyImageIcon(
			"icons/lock.png");

	public TreeCellRenderer() {
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		try {
			AccountsTreeModel model = (AccountsTreeModel) tree.getModel();
			AccountCategory ac = (AccountCategory) value;
			if (model.isAccount(ac.getCategoryId())) {
				Account a = model.getAccount(ac.getCategoryId());
				boolean alert = net.mjrz.fm.utils.AlertsCache.getInstance()
						.alertRaised(a.getAccountId());

				// System.out.println("Account " + value + " alert raised..." +
				// alert);
				if (alert) {
					setIcon(alertIcon);
				}
				else {
					if (a.getStatus() == AccountTypes.ACCOUNT_LOCKED) {
						setIcon(lockIcon);
					}
					else {
						if (a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY
								|| a.getAccountType() == AccountTypes.ACCT_TYPE_EXPENSE)
							setIcon(moneyMinus);
						else
							setIcon(moneyPlus);
					}
				}
			}
			else {
				ImageIcon ic = IconMap.getInstance()
						.getIcon(ac.getCategoryId());
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
		}
		catch (Exception e) {
			setIcon(categoryIcon);
		}
		return this;
	}
}
