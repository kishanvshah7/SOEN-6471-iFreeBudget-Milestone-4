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

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NetWorthHistory {
	long id;
	long uid;
	Date date;
	BigDecimal assets;
	BigDecimal liabs;
	BigDecimal netWorth;

	public BigDecimal getAssets() {
		return assets;
	}

	public void setAssets(BigDecimal assets) {
		this.assets = assets;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getLiabs() {
		return liabs;
	}

	public void setLiabs(BigDecimal liabs) {
		this.liabs = liabs;
	}

	public BigDecimal getNetWorth() {
		return netWorth;
	}

	public void setNetWorth(BigDecimal netWorth) {
		this.netWorth = netWorth;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}
}
