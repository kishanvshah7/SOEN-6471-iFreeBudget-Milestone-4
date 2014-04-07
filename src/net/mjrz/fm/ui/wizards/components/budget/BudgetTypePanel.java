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
import java.util.HashMap;

import javax.swing.JPanel;

import net.mjrz.fm.ui.wizards.components.WizardComponent;

@SuppressWarnings("serial")
public class BudgetTypePanel extends javax.swing.JPanel implements
		WizardComponent {

	/** Creates new form BudgetTypePanel */
	public BudgetTypePanel() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());

		jLabel1 = new javax.swing.JLabel();
		btypeCb = new javax.swing.JComboBox();
		jLabel2 = new javax.swing.JLabel();

		jLabel1.setText("Type");

		btypeCb.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"Monthly", "Weekly" }));
		btypeCb.setPreferredSize(new Dimension(100, 25));

		jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel2.setText("Select type of budget");
		jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		jLabel2.setBorder(null);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10, 10, 10, 10);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 1;
		gbc1.gridy = 0;

		JPanel center = new JPanel();
		center.setLayout(new GridBagLayout());
		center.add(jLabel2, gbc);
		center.add(btypeCb, gbc1);
		center.setBackground(Color.WHITE);

		add(center, BorderLayout.CENTER);
	}

	// Variables declaration - do not modify
	private javax.swing.JComboBox btypeCb;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;

	// End of variables declaration

	@Override
	public String getMessage() {
		return null;
	}

	@Override
	public String[][] getValues() {
		String[][] ret = new String[1][1];
		String sel = (String) btypeCb.getSelectedItem();
		if (sel != null) {
			String[] type = new String[1];
			type[0] = sel;
			ret[0] = type;
			return ret;
		}
		return null;
	}

	@Override
	public boolean isComponentValid() {
		return btypeCb.getSelectedIndex() >= 0;
	}

	@Override
	public void setComponentFocus() {
		btypeCb.requestFocusInWindow();
	}

	@Override
	public void updateComponentUI(HashMap<String, String[][]> values) {
		btypeCb.requestFocusInWindow();
	}

}
