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
package net.mjrz.fm.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.PortfolioEntry;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.dialogs.InvestmentEntryDialog;
import net.mjrz.fm.utils.MiscUtils;

public class GetPortfolioValueAction {
	public GetPortfolioValueAction() {
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		ActionResponse resp = new ActionResponse();
		InputStream in = null;
		try {
			FManEntityManager em = new FManEntityManager();
			Long pid = (Long) request.getProperty("PORTFOLIOID");
			List entries = em.getPortfolioEntries(pid);

			Properties props = new Properties();
			in = InvestmentEntryDialog.class.getClassLoader().getResourceAsStream("resources/se.properties");
			props.load(in);

			HashMap<Locale, BigDecimal> values = new HashMap<Locale, BigDecimal>();
			Locale defaultLocale = SessionManager.getCurrencyLocale();

			for (Object o : entries) {
				PortfolioEntry entry = (PortfolioEntry) o;

				String market = entry.getMarket();
				if (market == null || market.length() == 0) {
					BigDecimal val = values.get(defaultLocale);
					if (val == null) {
						values.put(defaultLocale, entry.getMarketValue());
					}
					else {
						BigDecimal newValue = val.add(entry.getMarketValue());
						values.put(defaultLocale, newValue);
					}
					continue;
				}
				String marketProperties = props.getProperty(market);
				if (marketProperties == null)
					continue;

				ArrayList<String> split = MiscUtils.splitString(
						marketProperties, "^");
				if (split.size() != 4)
					continue;

				String localeStr = split.get(3); // currency locale

				Locale locale = new Locale(localeStr, localeStr.toUpperCase());

				BigDecimal val = entry.getMarketValue();

				int sf = entry.getSf();

				BigDecimal existingValue = values.get(locale);

				if (existingValue == null) {
					values.put(locale, val);
				}
				else {
					if (sf > 0) {
						BigDecimal newValue = val.divide(new BigDecimal(sf));
						newValue = existingValue.add(newValue);
						values.put(locale, newValue);
					}
				}
			}

			StringBuilder ret = new StringBuilder("Number of entries = "
					+ entries.size() + ", Total market value = ");
			Set<Locale> keys = values.keySet();
			int count = 0;
			for (Locale k : keys) {
				NumberFormat numFormat = NumberFormat.getCurrencyInstance(k);
				BigDecimal val = values.get(k);
				ret.append(numFormat.format(val));
				if (count++ < keys.size() - 1)
					ret.append(" : ");
			}

			resp.setErrorCode(ActionResponse.NOERROR);
			resp.addResult("PVALUE", ret.toString());
			return resp;
		}
		catch (Exception e) {
			resp.setErrorCode(ActionResponse.GENERAL_ERROR);
			resp.setErrorMessage("Error occured. Unable to calculate portfolio value");
			return resp;
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
