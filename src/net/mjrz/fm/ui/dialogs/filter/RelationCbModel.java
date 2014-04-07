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

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;

public class RelationCbModel extends DefaultComboBoxModel {

	private static final long serialVersionUID = 1L;

	private Object selectedItem = null;

	private ArrayList<String> relations = null;

	public RelationCbModel() {
		relations = new ArrayList<String>();
		addDefaultRelations();
		for (int i = 0; i < relations.size(); i++) {
			this.addElement(relations.get(i));
		}
	}

	private void addDefaultRelations() {
		relations.add("Equals");
		relations.add("In");
		relations.add("Not equals");
		relations.add("Greater than");
		relations.add("Lesser than");
	}

	private void addDateRelations() {
		relations.add("In");
		relations.add("Greater than");
		relations.add("Lesser than");
	}

	public void updateModel(String field) {
		relations.clear();
		removeAllElements();
		if (field.equals("Date")) {
			addDateRelations();
		}
		else {
			addDefaultRelations();
		}
		for (int i = 0; i < relations.size(); i++) {
			this.addElement(relations.get(i));
		}
	}
}
