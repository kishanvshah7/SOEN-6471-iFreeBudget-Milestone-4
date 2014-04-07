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
package net.mjrz.fm.ui.panels;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.BudgetsTreeModel;

public class BudgetsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private BudgetsTreeModel budgetsTreeModel;
	private JTree budgetsTree;

	public BudgetsPanel() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		populateBudgetsTree();
		add(budgetsTree, BorderLayout.CENTER);
	}

	private void populateBudgetsTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Budgets");
		budgetsTreeModel = new BudgetsTreeModel(rootNode);
		budgetsTree = new JTree(budgetsTreeModel);
		budgetsTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		budgetsTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getNewLeadSelectionPath();
				if (path == null)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				Object obj = node.getUserObject();
				if (obj instanceof Budget) {
					Budget budget = (Budget) obj;
					handleBudgetsTreeSelection(budget);
				}
			}
		});
	}

	private void handleBudgetsTreeSelection(Budget budget) {
		FinanceManagerUI parent = (FinanceManagerUI) SwingUtilities
				.getWindowAncestor(this);
		parent.switchView(FinanceManagerUI.BUDGET_SUMMARY_VIEW_NAME);
	}
}
