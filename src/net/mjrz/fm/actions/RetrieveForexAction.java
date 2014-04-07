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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import net.mjrz.fm.entity.CurrencyEntityManager;
import net.mjrz.fm.entity.beans.CurrencyMonitor;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class RetrieveForexAction {
	static final String URL = "http://finance.yahoo.com/d/quotes.csv?s=USD@=X&f=l1";

	static final String DEFAULT_SYMBOL = "INR";

	private static Logger logger = Logger.getLogger(RetrieveForexAction.class
			.getName());

	private CurrencyMonitor getCurrencyMonitor(CurrencyEntityManager em, long id)
			throws Exception {
		return em.getMonitoredCurrency(id);
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		BufferedReader in = null;
		try {
			CurrencyEntityManager em = new CurrencyEntityManager();

			String id = (String) request.getProperty("ID");

			CurrencyMonitor cm = getCurrencyMonitor(em, Long.valueOf(id));

			String code = cm.getCode();

			if (code == null || code.length() == 0)
				code = DEFAULT_SYMBOL;

			String tmp = URL.replace("@", code);

			ActionResponse resp = new ActionResponse();

			URL url = new URL(tmp);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			int status = conn.getResponseCode();

			if (status == 200) {
				in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));

				StringBuilder value = new StringBuilder();

				while (true) {
					String line = in.readLine();

					if (line == null)
						break;

					value.append(line);
				}
				cm.setLastUpdateValue(new BigDecimal(value.toString()));

				cm.setLastUpdateTs(new Date());

				em.updateCurrencyMonitor(cm);

				resp.addResult("CURRENCYMONITOR", cm);
			}
			else {
				resp.setErrorCode(ActionResponse.GENERAL_ERROR);
				resp.setErrorMessage("HTTP Error [" + status + "]");
			}
			return resp;
		}
		catch (Exception e) {
			String st = MiscUtils.stackTrace2String(e);
			logger.error(st);
			throw e;
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}

}
