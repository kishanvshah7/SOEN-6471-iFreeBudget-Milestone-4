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

import javax.swing.AbstractListModel;

import net.mjrz.fm.entity.beans.types.EString;

public class FilterListModel extends AbstractListModel {

	private ArrayList<FilterEntry> filterList;

	public FilterListModel() {
		filterList = new ArrayList<FilterEntry>();
	}

	public int getSize() {
		return filterList.size();
	}

	public Object getElementAt(int index) {
		if (index >= filterList.size())
			return null;
		return filterList.get(index);
	}

	public void removeElementAt(int index) {
		if (index < 0 || index >= filterList.size())
			return;

		filterList.remove(index);
		int index0 = index - 1;
		if (index0 < 0)
			index0 = 0;
		fireIntervalRemoved(this, index0, index);
	}

	public void removeAllElements() {
		filterList.clear();
		fireIntervalRemoved(this, 0, 0);
	}

	public void append(FilterEntry filterEntry) {
		filterList.add(filterEntry);
		int sz = filterList.size();
		fireContentsChanged(this, sz - 2, sz - 1);
	}

	static class FilterEntry {
		private String key;
		private String rel;
		private String values;
		private String type;

		FilterEntry() {
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
			setType();
		}

		public String getRel() {
			return rel;
		}

		public void setRel(String rel) {
			this.rel = rel;
		}

		public String getValues() {
			return values;
		}

		public void setValues(String values) {
			this.values = values;
		}

		public String getType() {
			return type;
		}

		public String getDBRel() {
			if (rel.equals("Equals")) {
				return "=";
			}
			else if (rel.equals("Not equals")) {
				return "!=";
			}
			return rel;
		}

		public String getDBKey() {
			if (key.equalsIgnoreCase("Date")) {
				return "txDate";
			}
			else if (key.equalsIgnoreCase("Amount")) {
				return "txAmount";
			}
			else if (key.equalsIgnoreCase("From")) {
				return "fromAccountId";
			}
			else if (key.equalsIgnoreCase("To")) {
				return "toAccountId";
			}
			return key;
		}

		private void setType() {
			if (key == null) {
				type = null;
				return;
			}
			if (key.equalsIgnoreCase("Date")) {
				type = java.util.Date.class.getName();
			}
			else if (key.equalsIgnoreCase("From")) {
				type = EString.class.getName();
			}
			else if (key.equalsIgnoreCase("To")) {
				type = EString.class.getName();
			}
			else if (key.equals("Amount")) {
				type = Double.class.getName();
			}
			else {
				type = EString.class.getName();
			}
		}

		@Override
		public String toString() {
			return key + " " + rel + " " + values;
		}
	}
}
