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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.ui.panels.AccountsTreePanel;

public class AccountsTreeDropTarget implements DropTargetListener {

	private DropTarget target;

	private JTree targetTree;

	private AccountsTreeModel model;

	FManEntityManager em;

	private AccountsTreePanel parentPanel;

	public AccountsTreeDropTarget(AccountsTreePanel panel) {
		parentPanel = panel;
		targetTree = panel.getAccountsTree();

		model = (AccountsTreeModel) targetTree.getModel();
		target = new DropTarget(targetTree, this);
		em = new FManEntityManager();
	}

	/*
	 * Drop Event Handlers
	 */
	private AccountCategory getNodeForEvent(DropTargetDragEvent dtde) {
		Point p = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath path = tree.getClosestPathForLocation(p.x, p.y);
		return (AccountCategory) path.getLastPathComponent();
	}

	public void dragEnter(DropTargetDragEvent dtde) {
		AccountCategory node = getNodeForEvent(dtde);
		if (model.isAccount(node.getCategoryId())) {
			dtde.rejectDrag();
		}
		else {
			dtde.acceptDrag(dtde.getDropAction());
		}
	}

	public void dragOver(DropTargetDragEvent dtde) {
		AccountCategory node = getNodeForEvent(dtde);
		if (model.isAccount(node.getCategoryId())) {
			dtde.rejectDrag();
		}
		else {
			dtde.acceptDrag(dtde.getDropAction());
		}
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
		AccountCategory parent = (AccountCategory) parentpath
				.getLastPathComponent();

		if (model.isAccount(parent.getCategoryId())) {
			dtde.rejectDrop();
			return;
		}

		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					Long[] p = (Long[]) tr.getTransferData(flavors[i]);
					Long l = p[p.length - 1];
					Account node = model.getAccount(l);

					if (node == null)
						continue;

					long parentMajorCategoryId = model
							.getMajorAccountCategory(parent);

					AccountCategory tmp = new AccountCategory(1,
							node.getAccountId(), node.getCategoryId());

					long nodeMajorCategoryId = model
							.getMajorAccountCategory(tmp);

					if (parentMajorCategoryId != nodeMajorCategoryId) {
						dtde.rejectDrop();
						parentPanel
								.showErrorDialog("Cannot change category type from: "
										+ AccountTypes.getAccountType(Long
												.valueOf(nodeMajorCategoryId))
										+ " to "
										+ AccountTypes.getAccountType(Long
												.valueOf(parentMajorCategoryId)));
						return;
					}

					dtde.acceptDrop(dtde.getDropAction());

					int r = em.updateAccountForCategory(node.getAccountId(),
							parent.getCategoryId());

					if (r != 1) {
						dtde.dropComplete(false);
						return;
					}

					model.removeAccount(node);

					node.setCategoryId(parent.getCategoryId());

					model.addAccount(node);

					dtde.dropComplete(true);
					return;
				}
			}
			dtde.dropComplete(false);
			// dtde.rejectDrop();
		}
		catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}
}
