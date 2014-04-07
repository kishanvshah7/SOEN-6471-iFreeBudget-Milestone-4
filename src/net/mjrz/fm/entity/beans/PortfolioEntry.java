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
package net.mjrz.fm.entity.beans;

import java.math.BigDecimal;
import java.util.Date;

public class PortfolioEntry {
	long id;
	long portfolioId;
	String symbol;
	String exchange;
	String name;
	BigDecimal numShares;
	BigDecimal pricePerShare;
	BigDecimal costBasis;
	BigDecimal daysGain;
	String change;
	Date lastUpdateTS;

	String market;
	String currencyLocale;
	Integer sf;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(long portfolioId) {
		this.portfolioId = portfolioId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getNumShares() {
		return numShares;
	}

	public void setNumShares(BigDecimal numShares) {
		this.numShares = numShares;
	}

	public BigDecimal getPricePerShare() {
		return pricePerShare;
	}

	public void setPricePerShare(BigDecimal pricePerShare) {
		this.pricePerShare = pricePerShare;
	}

	public BigDecimal getMarketValue() {
		if (sf == null || sf.equals(Integer.valueOf(0))) {
			return pricePerShare.multiply(numShares);
		}
		BigDecimal ret = pricePerShare.multiply(numShares).divide(
				new BigDecimal(sf));
		return ret;
	}

	public BigDecimal getCostBasis() {
		return costBasis;
	}

	public void setCostBasis(BigDecimal costBasis) {
		this.costBasis = costBasis;
	}

	public BigDecimal getDaysGain() {
		return daysGain;
	}

	public void setDaysGain(BigDecimal daysGain) {
		this.daysGain = daysGain;
	}

	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}

	public Date getLastUpdateTS() {
		return lastUpdateTS;
	}

	public void setLastUpdateTS(Date lastUpdateTS) {
		this.lastUpdateTS = lastUpdateTS;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getCurrencyLocale() {
		return currencyLocale;
	}

	public void setCurrencyLocale(String currencyLocale) {
		this.currencyLocale = currencyLocale;
	}

	public Integer getSf() {
		return sf;
	}

	public void setSf(Integer sf) {
		this.sf = sf;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(symbol);
		ret.append(":");
		ret.append(pricePerShare);
		ret.append(":");
		ret.append(numShares);
		ret.append(":");
		ret.append(currencyLocale);
		ret.append(":" + market + ":" + sf);

		return ret.toString();
	}
}
