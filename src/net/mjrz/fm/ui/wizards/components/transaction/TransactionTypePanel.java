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
package net.mjrz.fm.ui.wizards.components.transaction;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.ui.wizards.components.WizardComponent;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class TransactionTypePanel extends JPanel implements WizardComponent {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JComboBox trTypesCb = null;
	String[] types = { tr("I received an income"),
			tr("I paid for an expense using my checking/savings account"),
			tr("I paid for an expense using my credit card account"),
			tr("I paid my credit card/loan bill") };

	/**
	 * This is the default constructor
	 */
	public TransactionTypePanel() {
		super();
		initialize();
		setBackground(Color.WHITE);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.ipadx = 132;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.insets = new Insets(70, 6, 89, 25);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(70, 8, 91, 10);
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipady = 8;
		gridBagConstraints.gridx = 0;
		jLabel = new JLabel();
		jLabel.setText(tr("Select type") + ": ");
		// this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		this.add(getCurrCb(), gridBagConstraints1);
		this.setBackground(java.awt.Color.WHITE);
		this.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Select transaction type"));
	}

	/**
	 * This method initializes langCb
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getCurrCb() {
		if (trTypesCb == null) {
			trTypesCb = new JComboBox(types);
		}
		return trTypesCb;
	}

	public String[][] getValues() {
		String[][] ret = new String[1][1];
		int sel = trTypesCb.getSelectedIndex();
		String[] accts = new String[2];
		if (sel >= 0) {
			switch (sel) {
			case 0:
				accts[0] = String.valueOf(AccountTypes.ACCT_TYPE_INCOME);
				accts[1] = String.valueOf(AccountTypes.ACCT_TYPE_CASH);
				break;
			case 1:
				accts[0] = String.valueOf(AccountTypes.ACCT_TYPE_CASH);
				accts[1] = String.valueOf(AccountTypes.ACCT_TYPE_EXPENSE);
				break;
			case 2:
				accts[0] = String.valueOf(AccountTypes.ACCT_TYPE_LIABILITY);
				accts[1] = String.valueOf(AccountTypes.ACCT_TYPE_EXPENSE);
				break;
			case 3:
				accts[0] = String.valueOf(AccountTypes.ACCT_TYPE_CASH);
				accts[1] = String.valueOf(AccountTypes.ACCT_TYPE_LIABILITY);
				break;
			}
		}
		else {
			accts[0] = String.valueOf(AccountTypes.ACCT_TYPE_ROOT);
			accts[1] = String.valueOf(AccountTypes.ACCT_TYPE_ROOT);
		}
		ret[0] = accts;
		return ret;
	}

	public boolean isComponentValid() {
		return trTypesCb.getSelectedIndex() >= 0;
	}

	public String getMessage() {
		return "";
	}

	public void setComponentFocus() {
		trTypesCb.requestFocusInWindow();
	}

	public void updateComponentUI(HashMap<String, String[][]> values) {
		trTypesCb.requestFocusInWindow();
	}

	private void printValues(HashMap<String, String[][]> values) {
		Set<String> keys = values.keySet();
		for (String k : keys) {
			String[][] vals = values.get(k);
			System.out.println("Key: " + k);
			for (int i = 0; i < vals.length; i++) {
				System.out.println("\t" + vals[i][0] + " - " + vals[i][1]);
			}
		}
	}
}
