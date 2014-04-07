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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class RetrieveQuoteAction {

	// private static final String URL_QUOTE_DATA =
	// "http://finance.yahoo.com/d/quotes.csv?s=@&f=nl1oj1hgkjva2c1p2";

	public static final String URL_QUOTE_DATA = "http://finance.yahoo.com/d/quotes.csv?s=@&f=nl1oj1hgkjva2c1p2dba";

	private static final String URL_HISTORICAL_DATA = "http://ichart.finance.yahoo.com/table.csv?";

	private static final String DEFAULT_QUOTE = "MOT";

	private static Logger logger = Logger.getLogger(RetrieveQuoteAction.class
			.getName());

	public RetrieveQuoteAction() {
	}

	private String getQueryString(String symbol) {

		GregorianCalendar gc = new GregorianCalendar();
		StringBuffer ret = new StringBuffer(URL_HISTORICAL_DATA);

		String mo = "" + gc.get(Calendar.MONTH);
		if (mo.length() == 1)
			mo = "0" + mo;

		String dt = "" + gc.get(Calendar.DATE);
		if (dt.length() == 1)
			dt = "0" + dt;

		int endyr = gc.get(Calendar.YEAR);
		int startyr = (gc.get(Calendar.YEAR) - 1);

		ret.append("s=" + symbol);
		ret.append("&");

		ret.append("a=" + mo);
		ret.append("&");
		ret.append("b=" + dt);
		ret.append("&");
		ret.append("c=" + startyr);
		ret.append("&");

		ret.append("d=" + mo);
		ret.append("&");
		ret.append("e=" + dt);
		ret.append("&");
		ret.append("f=" + endyr);
		ret.append("&");
		ret.append("g=d&ignore=.csv");

		// System.out.println(ret);
		return ret.toString();
	}

	public ActionResponse executeAction(ActionRequest request) throws Exception {
		BufferedReader in = null;
		try {
			String symbol = (String) request.getProperty("SYMBOL");
			if (symbol == null || symbol.length() == 0)
				symbol = DEFAULT_QUOTE;

			String tmp = URL_QUOTE_DATA.replace("@", symbol);

			ActionResponse resp = new ActionResponse();

			URL url = new URL(tmp);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int status = conn.getResponseCode();
			if (status == 200) {
				in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				while (true) {
					String line = in.readLine();
					if (line == null)
						break;
					ArrayList<String> data = parseData(line, ",");
					getHistoricalData(symbol, resp);
					resp.addResult("QUOTEDATA", data);
					resp.addResult("SYMBOL", symbol);
				}
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

	private void getHistoricalData(String symbol, ActionResponse response)
			throws Exception {
		ArrayList<String> date = new ArrayList<String>();
		ArrayList<BigDecimal> price = new ArrayList<BigDecimal>();

		String tmpurl = getQueryString(symbol);
		URL url = new URL(tmpurl);

		BufferedReader in = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int status = conn.getResponseCode();
			if (status == 200) {
				in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				StringBuffer responseBody = new StringBuffer();
				while (true) {
					String line = in.readLine();
					if (line == null)
						break;
					responseBody.append(line);
					responseBody.append("\r\n");
				}
				StringTokenizer st = new StringTokenizer(
						responseBody.toString(), "\r\n");
				int i = 0;
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					i++;
					if (i == 1) // first line is header
						continue;

					ArrayList<String> split = MiscUtils.splitString(tok.trim(),
							",");

					if (split.size() < 5)
						continue;
					String dt = split.get(0).replace('-', '/');
					date.add(dt);

					price.add(new BigDecimal(split.get(4))); // Day close
				}

				response.addResult("HISTDATES", date);
				response.addResult("HISTPRICE", price);
			}
			else {
				logger.info("HttpRequest failed. Code=" + status);
			}
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private ArrayList<String> parseData(String s, String delim) {
		// System.out.println("-> " + s);
		ArrayList<String> ret = new ArrayList<String>();
		int pos = 0;
		int lastIdx = 0;
		while (pos < s.length()) {
			if (s.charAt(pos) == ',') {
				String tmp = s.substring(lastIdx, pos);
				addToken(ret, tmp);
				lastIdx = pos + 1;
			}
			pos++;
		}
		if (lastIdx < s.length()) {
			ret.add(s.substring(lastIdx));
		}
		return ret;
	}

	private void addToken(ArrayList<String> tokens, String tok) {
		if (tokens.size() == 0) {
			tokens.add(tok);
			return;
		}
		String x = tokens.get(tokens.size() - 1);
		if (x.length() > 0 && x.charAt(0) == '"'
				&& x.charAt(x.length() - 1) != '"') {
			StringBuffer rem = new StringBuffer(
					tokens.remove(tokens.size() - 1));
			rem.append(tok);
			tokens.add(rem.toString());
		}
		else {
			tokens.add(tok);
		}
	}

	public static void main(String args[]) throws Exception {
		RetrieveQuoteAction a = new RetrieveQuoteAction();
		a.executeAction(new ActionRequest());
	}
}
