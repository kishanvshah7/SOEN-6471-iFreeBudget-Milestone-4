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
package net.mjrz.fm.ui.wizards.components;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 *
 */
import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class LangPanel extends JPanel implements WizardComponent {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JComboBox langCb = null;
	private String[] languages = null;
	private Locale[] locales = null;

	private static final String DEFAULT_LANGUAGE = "English";
	
	private static final Logger logger = Logger.getLogger(LangPanel.class);

	/**
	 * This is the default constructor
	 */
	public LangPanel() {
		super();
		locales = getAvailableLanguages();
		languages = new String[locales.length];
		for (int i = 0; i < locales.length; i++) {
			languages[i] = locales[i].getDisplayLanguage();
		}
		initialize();
	}

	private Locale[] getAvailableLanguages() {
		List<Locale> ret = new ArrayList<Locale>();
		ret.add(new Locale("en", "US"));
		ret.add(new Locale("ca", "ES"));
		ret.add(new Locale("de", "DE"));
		ret.add(new Locale("nl", "NL"));
		ret.add(new Locale("fr", "FR"));
		ret.add(new Locale("es", "ES"));
		ret.add(new Locale("it", "IT"));
		ret.add(new Locale("pl", "PL"));
		
		Collections.sort(ret, new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				return o1.getDisplayLanguage().compareTo(o2.getDisplayLanguage());
			}
		});
		
		Locale[] arr = new Locale[ret.size()];
		return ret.toArray(arr);
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
		gridBagConstraints1.insets = new Insets(86, 6, 89, 20);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(85, 8, 91, 6);
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipady = 8;
		gridBagConstraints.gridx = 0;
		jLabel = new JLabel();
		jLabel.setText(tr("Select language: "));
		Font f = jLabel.getFont().deriveFont(Font.BOLD);
		jLabel.setFont(f);
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		this.add(getLangCb(), gridBagConstraints1);
		this.setBackground(java.awt.Color.WHITE);
		this.setBorder(javax.swing.BorderFactory
				.createTitledBorder(tr("Select language")));
	}

	/**
	 * This method initializes langCb
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getLangCb() {
		if (langCb == null) {
			langCb = new JComboBox(languages);
		}
		langCb.setSelectedItem(DEFAULT_LANGUAGE);
		return langCb;
	}

	public String[][] getValues() {
		String[][] ret = new String[1][1];

		String[] row = new String[2];
		row[0] = "LANGUAGE";

		Locale lang = (Locale) locales[langCb.getSelectedIndex()];
		row[1] = lang.getLanguage() + "_" + lang.getCountry();

		ret[0] = row;

		return ret;
	}

	public boolean isComponentValid() {
		return langCb.getSelectedIndex() >= 0;
	}

	public String getMessage() {
		return "";
	}

	public void setComponentFocus() {
		langCb.requestFocusInWindow();
	}

	public void updateComponentUI(HashMap<String, String[][]> values) {
	}
}
