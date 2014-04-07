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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class BudgetsTreeModel extends DefaultTreeModel {

	private static final long serialVersionUID = 1L;

	private FManEntityManager entityManager = null;

	private Logger logger = Logger.getLogger(BudgetsTreeModel.class);

	public BudgetsTreeModel(TreeNode root) {
		super(root);

		entityManager = new FManEntityManager();

		initialize();
	}

	private void initialize() {
		try {
			logger.info("Initializing budgets tree...");
			List<Budget> budgets = entityManager.getBudgets(SessionManager
					.getSessionUserId());
			if (budgets == null)
				return;

			logger.info("Num budgets = " + budgets.size());
			for (Budget b : budgets) {
				DefaultMutableTreeNode n = new DefaultMutableTreeNode(b);
				((DefaultMutableTreeNode) root).add(n);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}
}
