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

public class TT {
	public static enum IsParent {
		NO(0), YES(1);
		private int index;

		private IsParent(int index) {
			this.index = index;
		}

		public int getVal() {
			return index;
		}
	}

	private long txId;
	private String fitid;
	private long fromAccountId;
	private long toAccountId;
	private long initiatorId;
	private BigDecimal txAmount;
	private String txNotes;
	private String txNotesMarkup;
	private Date txDate;
	private Date createDate;
	private int txStatus;
	private String activityBy;
	private BigDecimal fromAccountEndingBal;
	private BigDecimal toAccountEndingBal;
	private String fromName;
	private String toName;
	private Long parentTxId;
	private int isParent;
	private transient boolean isDecorated;

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

	public long getTxId() {
		return txId;
	}

	public void setTxId(long txId) {
		this.txId = txId;
	}

	public String getFitid() {
		return fitid;
	}

	public void setFitid(String fitid) {
		this.fitid = fitid;
	}

	public long getFromAccountId() {
		return fromAccountId;
	}

	public void setFromAccountId(long fromAccountId) {
		this.fromAccountId = fromAccountId;
	}

	public long getToAccountId() {
		return toAccountId;
	}

	public void setToAccountId(long toAccountId) {
		this.toAccountId = toAccountId;
	}

	public long getInitiatorId() {
		return initiatorId;
	}

	public void setInitiatorId(long initiatorId) {
		this.initiatorId = initiatorId;
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

	public Date getTxDate() {
		return txDate;
	}

	public void setTxDate(Date txDate) {
		this.txDate = txDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getTxStatus() {
		return txStatus;
	}

	public void setTxStatus(int txStatus) {
		this.txStatus = txStatus;
	}

	public String getActivityBy() {
		return activityBy;
	}

	public void setActivityBy(String activityBy) {
		this.activityBy = activityBy;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public String getToName() {
		return toName;
	}

	public void setToName(String toName) {
		this.toName = toName;
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

	public boolean isDecorated() {
		return isDecorated;
	}

	public void setDecorated(boolean isDecorated) {
		this.isDecorated = isDecorated;
	}

	public String getTxNotesMarkup() {
		return txNotesMarkup;
	}

	public void setTxNotesMarkup(String txNotesMarkup) {
		this.txNotesMarkup = txNotesMarkup;
	}
}
