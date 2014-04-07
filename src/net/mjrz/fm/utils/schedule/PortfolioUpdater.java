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
package net.mjrz.fm.utils.schedule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Portfolio;
import net.mjrz.fm.entity.beans.PortfolioEntry;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.panels.PortfolioPanel;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.scheduler.task.BasicTask;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class PortfolioUpdater extends BasicTask {
	private long initTs;

	private long lastRunTime;

	private long nextRunTime;

	private boolean stopped;

	private User user;

	private static Timer timer = null;

	private long DELAY = 3 * 60 * 1000;

	private final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

	private static final String URL_QUOTE_DATA = "http://finance.yahoo.com/d/quotes.csv?s=@&f=sl1c1";

	private static Logger logger = Logger.getLogger(PortfolioUpdater.class
			.getName());

	private FManEntityManager em = new FManEntityManager();

	private PortfolioPanel parent = null;

	public PortfolioUpdater(String name) {
		super(name);
	}

	public PortfolioUpdater(String name, PortfolioPanel panel, User user)
			throws Exception {
		super(name);
		parent = panel;
		this.user = user;
	}

	public void executeTask() {
		try {
			parent.updateMsg(
					"<html><font color=\"red\">Updating portfolio</font></html>",
					true);
			update();
			parent.updateMsg("Last update: " + sdf.format(new Date()), false);
		}
		catch (Exception e) {
			try {
				throw e;
			}
			catch (Exception ex) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void update() throws Exception {
		try {
			List portfolioList = em.getPortfolio(user.getUid());
			int sz = portfolioList.size();
			for (int i = 0; i < sz; i++) {
				Portfolio p = (Portfolio) portfolioList.get(i);

				List entries = em.getPortfolioEntries(p.getId());
				List<PortfolioEntry> deleteList = new ArrayList<PortfolioEntry>();

				int isz = entries.size();

				StringBuffer symbol = new StringBuffer();

				for (int j = 0; j < isz; j++) {
					PortfolioEntry entry = (PortfolioEntry) entries.get(j);
					if (entry.getNumShares().intValue() == 0) {
						deleteList.add(entry);
						continue;
					}
					symbol.append(entry.getSymbol());
					if (j < isz - 1)
						symbol.append(",");
				}
				String symbolStr = symbol.toString();
				if (symbolStr.length() == 0)
					continue;

				String query = URL_QUOTE_DATA.replace("@", symbolStr);
				updatePortfolio(entries, query);

				deletePortfolioEntries(deleteList);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
	}

	private void deletePortfolioEntries(List<PortfolioEntry> deleteList)
			throws Exception {
		if (deleteList == null || deleteList.size() == 0) {
			return;
		}
		for (PortfolioEntry e : deleteList) {
			logger.info("Deleting portfolio entry: " + e.getSymbol());
			int r = em.deletePortfolioEntry(e);
			parent.removePortfolioEntry(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updatePortfolio(List entries, String query) {
		BufferedReader in = null;
		try {
			URL url = new URL(query);
			System.out.println(query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int status = conn.getResponseCode();
			if (status == 200) {
				in = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				int count = 0;
				while (true) {
					String line = in.readLine();
					// System.out.println("Received line: " + line);
					if (line == null || count >= entries.size())
						break;
					updateEntry((PortfolioEntry) entries.get(count), line);
					count++;
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception ignore) {
				}
			}
		}
	}

	private void updateEntry(PortfolioEntry entry, String values) {
		ArrayList<String> split = MiscUtils.splitString(values, ",");
		if (split == null || split.size() != 3)
			return;

		String symbol = split.get(0);
		String price = split.get(1);
		String change = split.get(2);
		try {
			BigDecimal pricebd = new BigDecimal(price);

			symbol = MiscUtils.trimChars(symbol, '"');
			change = MiscUtils.trimChars(change, '"');
			BigDecimal changeBD = toNumeric(change);
			if (entry.getSymbol().equalsIgnoreCase(symbol)) {
				entry.setPricePerShare(pricebd);
				entry.setChange(change);
				entry.setLastUpdateTS(new Date());
				if (changeBD != null) {
					entry.setDaysGain(changeBD.multiply(entry.getNumShares()));
				}
				em.addPortfolioEntry(entry); // Save the changes
				parent.updatePortfolio(entry, false);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private BigDecimal toNumeric(String s) {
		try {
			BigDecimal bd = new BigDecimal(s);
			return bd;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
