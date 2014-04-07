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

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class FieldCbModel implements ComboBoxModel {

	private String[] fieldsList = new String[2];

	private Object selectedItem = null;

	public FieldCbModel() {
		fieldsList[0] = "Amount";
		fieldsList[1] = "Date";

		selectedItem = fieldsList[0];
	}

	public void setSelectedItem(Object anItem) {
		this.selectedItem = anItem;
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public int getSize() {
		return fieldsList.length;
	}

	public Object getElementAt(int index) {
		if (index > fieldsList.length)
			return null;
		return fieldsList[index];
	}

	public void addListDataListener(ListDataListener l) {
	}

	public void removeListDataListener(ListDataListener l) {
	}
}
