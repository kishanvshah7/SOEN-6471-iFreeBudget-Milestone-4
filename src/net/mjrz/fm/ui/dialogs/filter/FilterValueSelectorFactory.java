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
package net.mjrz.fm.ui.dialogs.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;

import javax.swing.JComponent;
import javax.swing.JDialog;

public class FilterValueSelectorFactory {

	public static Object getSelectorValue(JComponent parent, String field) {
		JDialog d = new JDialog();
		d.setTitle("Select:");
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		FilterValueSelector selectorPanel = null;
		if (field.equals("Date")) {
			d.setTitle("Select");
			selectorPanel = new DateSelectorPanel(d);
		}
		if (field.equals("Amount")) {
			d.setTitle("Amount");
			selectorPanel = new AmountSelectorPanel(d);
		}

		if (selectorPanel != null) {
			d.setLayout(new BorderLayout());
			d.add((Component) selectorPanel, BorderLayout.CENTER);
			d.pack();
			d.setLocationRelativeTo(parent);
			d.setModalityType(ModalityType.APPLICATION_MODAL);
			d.setVisible(true);

			return selectorPanel.getSelectedObject();
		}
		return null;
	}

}
