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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.ui.panels.AccountsTreePanel;

public class AccountsTreeTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	private Account node;
	private transient AccountCategory parent;
	private FManEntityManager em = new FManEntityManager();
	private AccountsTreePanel parentPanel;
	private String msg = "";

	public AccountsTreeTransferHandler(AccountsTreePanel panel) {
		parentPanel = panel;
	}

	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	public Transferable createTransferable(JComponent c) {
		msg = "";
		JTree tree = (JTree) c;
		TreePath path = tree.getSelectionPath();

		if ((path == null) || (path.getPathCount() <= 1)) {
			return null;
		}
		AccountsTreeModel model = (AccountsTreeModel) tree.getModel();
		AccountCategory srcNode = (AccountCategory) path.getLastPathComponent();

		ArrayList<AccountCategory> ancestors = model.getAncestors(srcNode);
		int sz = ancestors.size();
		Long[] array = new Long[sz];
		for (int i = 0; i < sz; i++) {
			array[i] = ancestors.get(i).getCategoryId();
		}
		Transferable transferable = new TransferableTreeNode(array);
		return transferable;
	}

	public void exportDone(JComponent c, Transferable t, int action) {
		if (parentPanel == null || msg == null || msg.length() == 0)
			return;
		parentPanel.showErrorDialog(msg);
	}

	public boolean canImport(TransferSupport ts) {
		return isValidDropLocation(ts);
	}

	private boolean isValidDropLocation(TransferSupport ts) {
		DropLocation loc = ts.getDropLocation();

		Transferable tr = ts.getTransferable();

		java.awt.Point pt = loc.getDropPoint();

		JTree tree = (JTree) ts.getComponent();

		AccountsTreeModel model = (AccountsTreeModel) tree.getModel();

		TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		parent = (AccountCategory) parentpath.getLastPathComponent();

		if (model.isAccount(parent.getCategoryId())) {
			return false;
		}

		try {
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					Long[] p = (Long[]) tr.getTransferData(flavors[i]);
					Long l = p[p.length - 1];
					node = model.getAccount(l);

					if (node == null || !model.isAccount(node.getAccountId())) {
						msg = "Invalid drop location";
						continue;
					}

					long parentMajorCategoryId = model
							.getMajorAccountCategory(parent);

					AccountCategory tmp = new AccountCategory(1,
							node.getAccountId(), node.getCategoryId());

					long nodeMajorCategoryId = model
							.getMajorAccountCategory(tmp);

					if (parentMajorCategoryId != nodeMajorCategoryId) {
						msg = "Cannot change category type from: "
								+ AccountTypes.getAccountType(Long.valueOf(
										nodeMajorCategoryId).intValue())
								+ " to "
								+ AccountTypes.getAccountType(Long.valueOf(
										parentMajorCategoryId).intValue());
						return false;
					}
				}
			}
			msg = "";
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean importData(TransferSupport ts) {
		if (!canImport(ts)) {
			return false;
		}

		JTree tree = (JTree) ts.getComponent();

		AccountsTreeModel model = (AccountsTreeModel) tree.getModel();

		try {
			int r = em.updateAccountForCategory(node.getAccountId(),
					parent.getCategoryId());

			if (r != 1) {
				msg = "Unable to update category for account: "
						+ node.getAccountName();
				return false;
			}

			model.removeAccount(node);

			node.setCategoryId(parent.getCategoryId());

			model.addAccount(node);

			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}
