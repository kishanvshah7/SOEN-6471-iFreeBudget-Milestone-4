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
package net.mjrz.fm.ui.panels.prefs;

import java.util.Map;

public interface PreferencesPanel {
	String UIPREFS_SUFFIX = "UIPrefs";
	String DATE_FORMAT_PROPERTY = "UIPrefs.DateFormat";
	String TX_SPLIT_LOC = "UIPrefs.TxSplit.Location";
	String ACCT_SPLIT_LOC = "UIPrefs.AcctSplit.Location";
	String DEFAULT_DATE_FORMAT = "EEE, MMM d, ''yy";
	String LAST_DIR_PATH = "UIPrefs.LastDirPath";
	String MAIN_WINDOW_WIDTH = "UIPrefs.MainWindowWidth";
	String MAIN_WINDOW_HEIGHT = "UIPrefs.MainWindowHeight";
	String LAST_STOCK_EXCH = "UIPrefs.InvestmentEntryDialog.LastExchange";

	public Map<String, Object> getPreferences();
}
