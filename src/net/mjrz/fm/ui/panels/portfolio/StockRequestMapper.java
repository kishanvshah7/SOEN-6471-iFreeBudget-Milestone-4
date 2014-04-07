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
package net.mjrz.fm.ui.panels.portfolio;

public class StockRequestMapper {

	private static final String URL_QUOTE_DATA = "http://finance.yahoo.com/d/quotes.csv?s=@&f=nl1oj1hgkjva2c1p2dba";

	public static final String[][] labels = { { "n", "Name" },
			{ "l1", "Last trade" }, { "o", "Open" }, { "j1", "Market Cap" },
			{ "h", "High" }, { "g", "Low" }, { "k", "52Wk High" },
			{ "j", "52Wk Low" }, { "v", "Volume" }, { "a2", "Average Vol" },
			{ "c1", "Change" }, { "p2", "Percent change" },
			{ "d", "Divident/Share" }, { "b", "Bid" }, { "a", "Ask" } };

	public static String getParam(String longForm) {
		for (String[] row : labels) {
			if (row[1].equals(longForm)) {
				return row[0];
			}
		}
		return null;
	}

	public static int getIndexFromLabel(String label) {
		for (int i = 0; i < labels.length; i++) {
			String[] row = labels[i];
			if (row[1].equals(label)) {
				return i;
			}
		}
		return -1;
	}
}
