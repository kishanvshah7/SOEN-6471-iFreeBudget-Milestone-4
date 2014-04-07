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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mjrz.fm.ui.panels.CurrencyExPanel.LocaleCurrencyWrapper;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CurrPanel extends JPanel implements WizardComponent {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JComboBox<LocaleCurrencyWrapper> currCb = null;
	private TreeSet<LocaleCurrencyWrapper> locales = null;
	private static final String DEFAULT_LOC = "en_US";
	private int defaultIndex = 0;

	/**
	 * This is the default constructor
	 */
	public CurrPanel() {
		super();
		locales = getAvailableCurrencies();

		initialize();
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
		jLabel.setText(tr("Select currency: "));
		Font f = jLabel.getFont().deriveFont(Font.BOLD);
		jLabel.setFont(f);
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.add(jLabel, gridBagConstraints);
		this.add(getCurrCb(), gridBagConstraints1);
		this.setBackground(java.awt.Color.WHITE);
		this.setBorder(javax.swing.BorderFactory
				.createTitledBorder(tr("Select currency")));
	}

	private TreeSet<LocaleCurrencyWrapper> getAvailableCurrencies() {
		Locale[] loc = Locale.getAvailableLocales();
		TreeSet<LocaleCurrencyWrapper> tmp = new TreeSet<LocaleCurrencyWrapper>();

		for (int i = 0; i < loc.length; i++) {
			try {
				Locale l = loc[i];
				Currency c = Currency.getInstance(l);
				if (c != null) {
					LocaleCurrencyWrapper w = new LocaleCurrencyWrapper(l, c);
					tmp.add(w);
				}
			} catch (Exception e) {
				// ignore
			}
		}
		return tmp;
	}

	/**
	 * This method initializes langCb
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox<LocaleCurrencyWrapper> getCurrCb() {
		if (currCb == null) {
			LocaleCurrencyWrapper[] arr = new LocaleCurrencyWrapper[locales
					.size()];
			locales.toArray(arr);

			int i = 0;
			for (; i < arr.length; i++) {
				LocaleCurrencyWrapper l = (LocaleCurrencyWrapper) arr[i];
				String slocale = l.getLocale().getLanguage() + "_"
						+ l.getLocale().getCountry();
				if (slocale.equals(DEFAULT_LOC)) {
					break;
				}
			}
			currCb = new JComboBox<LocaleCurrencyWrapper>(arr);
			if (i < currCb.getItemCount()) {
				currCb.setSelectedIndex(i);
			}
		}
		return currCb;
	}

	public String[][] getValues() {
		String[][] ret = new String[1][1];

		String[] row = new String[2];
		row[0] = "CURRENCY";

		LocaleCurrencyWrapper l = (LocaleCurrencyWrapper) currCb
				.getSelectedItem();
		row[1] = l.getLocale().getLanguage() + "_" + l.getLocale().getCountry();

		ret[0] = row;

		return ret;
	}

	public boolean isComponentValid() {
		return currCb.getSelectedIndex() >= 0;
	}

	public String getMessage() {
		return String.valueOf("");
	}

	// class LocaleCurrencyWrapper implements Comparable<LocaleCurrencyWrapper>
	// {
	// Locale l;
	// Currency c;
	//
	// public LocaleCurrencyWrapper(Locale locale, Currency currency) {
	// l = locale;
	// c = currency;
	// }
	// @Override
	// public int compareTo(LocaleCurrencyWrapper other) {
	// LocaleCurrencyWrapper tmp = (LocaleCurrencyWrapper) other;
	//
	// String lhs = l.getDisplayCountry();
	// String rhs = tmp.l.getDisplayCountry();
	//
	// return lhs.compareTo(rhs);
	// }
	// public Locale getLocale() {
	// return l;
	// }
	// public String toString() {
	// return l.getDisplayCountry() + " ( " + c.getSymbol() + " ) ";
	// }
	// }

	public void setComponentFocus() {
		currCb.requestFocusInWindow();
	}

	public void updateComponentUI(HashMap<String, String[][]> values) {
	}
}
