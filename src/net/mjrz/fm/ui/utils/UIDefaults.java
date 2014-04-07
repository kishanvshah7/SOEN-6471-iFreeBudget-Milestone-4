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

import java.awt.Color;

public class UIDefaults {
	public static final String PRODUCT_TITLE = "iFreeBudget";

	public static final String API_URL = "http://ifreebudget.appspot.com/ifreebudget/api/";
	
	public static final String PRODUCT_URL = "http://www.ifreebudget.com";
	public static final String PRODUCT_DOCUMENTATION_URL = "http://www.ifreebudget.com/2010/01/ifreebudget-frequently-asked-questions.html";
	public static final String LATEST_VERSION_URL = "http://www.ifreebudget.com/dist/latest.php";
	public static final String PRODUCT_DOWNLOAD_URL = "http://sourceforge.net/projects/ifreebudget/files/";

	public static final String FINANCE_MANAGER_TITLE = "iFreeBudget - Finance Manager";
	public static final String PORTFOLIO_MANAGER_TITLE = "iFreeBudget - Portfolio Manager";
	public static final String ADDRESSBOOK_MANAGER_TITLE = "iFreeBudget - Address book";

	// public static final Color DEFAULT_COLOR = new Color(91, 151, 220);
	// public static final Color DEFAULT_COLOR = new Color(149, 185, 199);
	public static final Color DEFAULT_COLOR = new Color(111, 139, 186);

	// public static final Color DEFAULT_TABLE_HEADER_COLOR = new Color(184,
	// 199, 200);
	// public static final Color DEFAULT_TABLE_HEADER_COLOR = new Color(49, 153,
	// 157);
	// public static final Color DEFAULT_TABLE_HEADER_COLOR = new Color(149,
	// 188, 180);
	public static final Color DEFAULT_TABLE_HEADER_COLOR = new Color(111, 139,
			186);
	public static final String DEFAULT_TABLE_HEADER_COLOR_HEX = "6F8BBA";

	// public static final Color DEFAULT_TABLE_ROW_SEL_COLOR = new Color(209,
	// 215, 224);
	public static final Color DEFAULT_TABLE_ROW_SEL_COLOR = new Color(169, 193,
			232);

	// public static final Color CHILD_TX_ROW_COLOR = new Color(192, 192, 192);
	public static final Color CHILD_TX_ROW_COLOR = new Color(238, 213, 183);
	public static final Color GROUP_ROW_COLOR = new Color(245, 245, 245);

	public static final Color DEFAULT_PANEL_BG_COLOR = Color.WHITE;

	public static String color2String(Color color) {
		StringBuilder str = new StringBuilder();
		str.append(color.getRed());
		str.append(",");
		str.append(color.getGreen());
		str.append(",");
		str.append(color.getBlue());

		return str.toString();
	}
}
