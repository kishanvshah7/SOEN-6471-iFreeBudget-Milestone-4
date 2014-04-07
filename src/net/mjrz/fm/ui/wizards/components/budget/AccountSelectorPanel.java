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
package net.mjrz.fm.ui.wizards.components.budget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.wizards.components.WizardComponent;

@SuppressWarnings("serial")
public class AccountSelectorPanel extends JPanel implements WizardComponent {
	private JLabel nameLbl;
	private JTextField nameTf;
	private JList availList, selectedList;
	private MyListModel availModel, selectedModel;
	private JButton addB, removeB;
	private FManEntityManager em;
	private String errMsg = null;

	public AccountSelectorPanel() {
		em = new FManEntityManager();
		initialize();
		loadAccounts();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		add(getTopPanel(), BorderLayout.NORTH);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getBottomPanel(), BorderLayout.SOUTH);
	}

	private JPanel getTopPanel() {
		JPanel ret = new JPanel();
		nameLbl = new JLabel("Name");
		nameTf = new JTextField(20);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		// gbc.ipadx = 15;
		gbc.ipady = 25;
		gbc.weightx = 0.1;
		gbc.insets = new Insets(5, 5, 2, 2);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 1;
		gbc1.gridy = 0;
		gbc1.ipadx = 5;
		gbc1.insets = new Insets(10, 2, 2, 2);
		gbc1.weightx = 1;
		gbc1.anchor = GridBagConstraints.FIRST_LINE_START;

		ret.setLayout(new GridBagLayout());
		ret.add(nameLbl, gbc);
		ret.add(nameTf, gbc1);
		ret.add(Box.createHorizontalGlue());

		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private JPanel getCenterPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		availModel = new MyListModel();
		availList = new JList(availModel);
		availList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList l = (JList) e.getSource();
				int sel = l.getSelectedIndex();
				if (sel >= 0) {
					addB.setEnabled(true);
				}
				else {
					addB.setEnabled(false);
				}
			}
		});

		selectedModel = new MyListModel();
		selectedList = new JList(selectedModel);
		selectedList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList l = (JList) e.getSource();
				int sel = l.getSelectedIndex();
				if (sel >= 0) {
					removeB.setEnabled(true);
				}
				else {
					removeB.setEnabled(false);
				}
			}
		});

		JScrollPane sp1 = new JScrollPane(availList);
		sp1.setBorder(BorderFactory.createTitledBorder("Available"));
		sp1.setPreferredSize(new Dimension(300, 50));

		JScrollPane sp2 = new JScrollPane(selectedList);
		sp2.setBorder(BorderFactory.createTitledBorder("Selected"));
		sp2.setPreferredSize(new Dimension(300, 50));

		JPanel buttonPanel = getButtonControlPanel();
		ret.add(sp1);
		ret.add(buttonPanel);
		ret.add(sp2);
		return ret;
	}

	private void loadAccounts() {
		try {
			long uid = SessionManager.getSessionUserId();
			java.util.List l = em.getAccountsForUser(uid,
					AccountTypes.ACCT_TYPE_EXPENSE);
			if (l != null) {
				for (int i = 0; i < l.size(); i++) {
					availModel.addElementSorted(l.get(i));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JPanel getButtonControlPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		addB = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-next.png"));
		addB.setDisabledIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-next.png"));
		addB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddButtonActionPerformed(e);
			}
		});
		addB.setEnabled(false);

		removeB = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-previous.png"));
		removeB.setDisabledIcon(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/go-previous.png"));
		removeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRemoveButtonActionPerformed(e);
			}
		});
		removeB.setEnabled(false);

		ret.add(Box.createVerticalGlue());
		ret.add(addB);
		ret.add(removeB);
		ret.add(Box.createVerticalGlue());
		return ret;
	}

	private JPanel getBottomPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		JLabel doc = new JLabel();
		doc.setText("To create a budget, you must select atleast 1 of the available payee accounts");
		doc.setHorizontalAlignment(JLabel.CENTER);
		doc.setVerticalAlignment(JLabel.CENTER);
		ret.add(doc, BorderLayout.CENTER);
		ret.setPreferredSize(new Dimension(600, 100));
		return ret;
	}

	private void doAddButtonActionPerformed(ActionEvent e) {
		Object[] sel = availList.getSelectedValues();
		if (sel != null) {
			for (Object o : sel) {
				selectedModel.addElementSorted(o);
				availModel.removeElement(o);
			}
		}
	}

	private void doRemoveButtonActionPerformed(ActionEvent e) {
		Object[] sel = selectedList.getSelectedValues();
		if (sel != null) {
			for (Object o : sel) {
				availModel.addElementSorted(o);
				selectedModel.removeElement(o);
			}
		}
	}

	static class MyListModel extends DefaultListModel {
		public void addElementSorted(Object element) {
			Account acc = (Account) element;
			String toAdd = acc.getAccountName();
			int sz = getSize();
			int idx = -1;
			for (int i = 0; i < sz; i++) {
				Account tmp = (Account) super.getElementAt(i);
				String txt = tmp.getAccountName();
				int c = txt.compareTo(toAdd);
				if (c >= 0) {
					idx = i;
					break;
				}
				if (c < 0) {
					continue;
				}
			}
			if (idx >= 0) {
				super.add(idx, element);
			}
			else {
				super.addElement(element);
			}
		}
	}

	@Override
	public String getMessage() {
		return errMsg;
	}

	@Override
	public String[][] getValues() {
		String name = nameTf.getText().trim();
		String[][] ret = new String[2][1];
		String[] nameRow = { name };
		ret[0] = nameRow;

		int sz = selectedModel.size();
		String[] acctRow = new String[sz];
		for (int i = 0; i < sz; i++) {
			acctRow[i] = ((Account) selectedModel.get(i)).getAccountName();
		}
		ret[1] = acctRow;

		return ret;
	}

	@Override
	public boolean isComponentValid() {
		String name = nameTf.getText();
		if (name == null || name.trim().length() == 0) {
			errMsg = "Name cannot be empty";
			return false;
		}
		name = name.trim();
		if (name.length() > 32) {
			errMsg = "Name cannot be more than 32 characters in length";
			nameTf.setText("");
			return false;
		}
		if (selectedModel.size() == 0) {
			errMsg = "No accounts selected";
			return false;
		}
		return true;
	}

	@Override
	public void setComponentFocus() {
		nameTf.requestFocusInWindow();
	}

	@Override
	public void updateComponentUI(HashMap<String, String[][]> values) {
	}
}
