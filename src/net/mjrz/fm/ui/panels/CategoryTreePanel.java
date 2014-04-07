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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.mjrz.fm.entity.AccountCategoryMap;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.User;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CategoryTreePanel extends JPanel {
	static final long serialVersionUID = 0L;

	private JTree tree = null;
	private transient AccountCategoryMap map = null;
	private JTextField name = null;
	private MyTreeModelListener treeModelListener;
	private JDialog parent;

	public CategoryTreePanel(JDialog parent, User user) {
		initialize(user);
		this.parent = parent;
	}

	private void initialize(User user) {
		map = new AccountCategoryMap(user.getUid(), "Categories");

		setLayout(new BorderLayout());
		buildTreePanel();

		add(buildTreePanel(), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);

		setPreferredSize(new Dimension(400, 400));
	}

	private JPanel buildTreePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new GridLayout(1, 1));

		treeModelListener = new MyTreeModelListener();
		map.addTreeModelListener(treeModelListener);

		tree = new JTree(map);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(getTreeCellRenderer());

		JScrollPane sp = new JScrollPane(tree);
		ret.add(sp);
		ret.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		return ret;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		JButton add = new JButton("Add");
		add.addActionListener(new ButtonHandler());
		JButton close = new JButton("Close");
		close.addActionListener(new ButtonHandler());
		JButton remove = new JButton("Remove");
		remove.addActionListener(new ButtonHandler());
		JButton rename = new JButton("Rename");
		rename.addActionListener(new ButtonHandler());

		name = new JTextField(20);

		ret.add(Box.createHorizontalGlue());
		ret.add(name);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(add);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(remove);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(rename);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(close);

		return ret;
	}

	static int count = 15;

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			String nameVal = name.getText();
			if (cmd.equals("Add")) {
				AccountCategory parent = (AccountCategory) tree
						.getLastSelectedPathComponent();
				if (nameVal == null || nameVal.length() == 0) {
					JOptionPane.showMessageDialog(CategoryTreePanel.this,
							"Invalid name. Cannot be empty", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (nameVal != null && nameVal.length() > 30) {
					JOptionPane.showMessageDialog(CategoryTreePanel.this,
							"Invalid name. Maximum 30 characters", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (parent == null)
					return;

				AccountCategory newcategory = new AccountCategory(1, null,
						parent.getCategoryId());

				try {
					FManEntityManager em = new FManEntityManager();
					Long id = em.getNextAccountCategoryId();
					newcategory.setCategoryId(id);
				}
				catch (Exception ex) {
					ex.printStackTrace();
					return;
				}
				newcategory.setCategoryName(name.getText());

				map.addCategory(parent, newcategory, true);
				name.setText("");
			}
			if (cmd.equals("Remove")) {
				AccountCategory node = (AccountCategory) tree
						.getLastSelectedPathComponent();
				if (node == null)
					return;
				String status = map.removeNodeFromParent(node);
				if (status != null && status.length() > 0) {
					JOptionPane.showMessageDialog(CategoryTreePanel.this,
							status, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			if (cmd.equals("Rename")) {
				if (nameVal == null || nameVal.length() == 0) {
					JOptionPane.showMessageDialog(CategoryTreePanel.this,
							"Invalid name. Cannot be empty", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				if (parent == null)
					return;

				AccountCategory node = (AccountCategory) tree
						.getLastSelectedPathComponent();
				if (node == null)
					return;
				String status = map.renameNode(node, nameVal);
				if (status != null && status.length() > 0) {
					JOptionPane.showMessageDialog(CategoryTreePanel.this,
							status, "Error", JOptionPane.ERROR_MESSAGE);
				}
				name.setText("");
			}
			if (cmd.equals("Close")) {
				parent.dispose();
			}
		}
	}

	class MyTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			tree.expandPath(e.getTreePath());
			tree.scrollPathToVisible(new TreePath(e.getPath()));
			tree.validate();
			tree.updateUI();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			tree.expandPath(e.getTreePath());
			tree.scrollPathToVisible(new TreePath(e.getPath()));
			tree.validate();
			tree.updateUI();
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			tree.removeSelectionPath(e.getTreePath());
			tree.validate();
			tree.updateUI();
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	private DefaultTreeCellRenderer getTreeCellRenderer() {
		ImageIcon icon = new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/category.png");

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		if (icon != null) {
			renderer.setLeafIcon(icon);
			renderer.setOpenIcon(icon);
			renderer.setClosedIcon(icon);
		}
		return renderer;
	}

	public static void dispose() {
	}
}
