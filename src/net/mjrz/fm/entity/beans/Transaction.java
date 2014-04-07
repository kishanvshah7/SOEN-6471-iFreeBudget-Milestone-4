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
public class Transaction implements FManEntity {
	long txId;
	String fitid;
	long fromAccountId;
	long toAccountId;
	long initiatorId;
	BigDecimal txAmount;
	String txNotes;
	String txNotesMarkup;
	Date txDate;
	Date createDate;
	int txStatus;
	String activityBy;

	Long parentTxId;

	BigDecimal fromAccountEndingBal;
	BigDecimal toAccountEndingBal;

	private int isParent;

	public int getTxStatus() {
		return txStatus;
	}

	public void setTxStatus(int txStatus) {
		this.txStatus = txStatus;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getTxDate() {
		return txDate;
	}

	public void setTxDate(Date txDate) {
		this.txDate = txDate;
	}

	public long getFromAccountId() {
		return fromAccountId;
	}

	public void setFromAccountId(long fromAccountId) {
		this.fromAccountId = fromAccountId;
	}

	public long getTxId() {
		return txId;
	}

	public void setTxId(long id) {
		this.txId = id;
	}

	public long getInitiatorId() {
		return initiatorId;
	}

	public void setInitiatorId(long initiatorId) {
		this.initiatorId = initiatorId;
	}

	public long getToAccountId() {
		return toAccountId;
	}

	public void setToAccountId(long toAccountId) {
		this.toAccountId = toAccountId;
	}

	public BigDecimal getTxAmount() {
		return txAmount;
	}

	public void setTxAmount(BigDecimal txAmount) {
		this.txAmount = txAmount;
	}

	public String getTxNotes() {
		return txNotes;
	}

	public void setTxNotes(String txNotes) {
		this.txNotes = txNotes;
	}

	public BigDecimal getFromAccountEndingBal() {
		return fromAccountEndingBal;
	}

	public void setFromAccountEndingBal(BigDecimal fromAccountEndingBal) {
		this.fromAccountEndingBal = fromAccountEndingBal;
	}

	public BigDecimal getToAccountEndingBal() {
		return toAccountEndingBal;
	}

	public void setToAccountEndingBal(BigDecimal toAccountEndingBal) {
		this.toAccountEndingBal = toAccountEndingBal;
	}

	public String getFitid() {
		return fitid;
	}

	public void setFitid(String fitid) {
		this.fitid = fitid;
	}

	public String getActivityBy() {
		return activityBy;
	}

	public void setActivityBy(String activityBy) {
		this.activityBy = activityBy;
	}

	public Long getParentTxId() {
		return parentTxId;
	}

	public void setParentTxId(Long parentTxId) {
		this.parentTxId = parentTxId;
	}

	public int getIsParent() {
		return isParent;
	}

	public void setIsParent(int isParent) {
		this.isParent = isParent;
	}

	public String getTxNotesMarkup() {
		return txNotesMarkup;
	}

	public void setTxNotesMarkup(String txNotesMarkup) {
		this.txNotesMarkup = txNotesMarkup;
	}

	// Interface methods - FManEntity
	@Override
	public String getPKColumnName() {
		return "txId";
	}

	@Override
	public Object getPK() {
		return getTxId();
	}

	@Override
	public void setPK(Object pk) {
		setTxId((Long) pk);
	}
}
