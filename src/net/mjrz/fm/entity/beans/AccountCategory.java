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

import java.util.Date;

import net.mjrz.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public final class AccountCategory implements Comparable, Cloneable {
	private static final long serialVersionUID = 1L;
	long uid;
	Long categoryId;
	Long parentCategoryId;
	Date createdDate;
	String categoryName;

	public AccountCategory() {
		createdDate = new Date();
		categoryName = "";
	}

	public AccountCategory(long uid, Long id, Long pid) {
		this.categoryId = id;
		this.parentCategoryId = pid;
		categoryName = "default";
		createdDate = new Date();
		this.uid = uid;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(Long parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		if (Messages.hasTranslation(categoryName)) {
			ret.append(Messages.tr(categoryName));
		}
		else {
			ret.append(categoryName);
		}
		return ret.toString();
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + categoryId.hashCode();
		result = 37 * result + categoryName.hashCode();
		result = 37 * result + createdDate.hashCode();
		return result;
	}

	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (!(that instanceof AccountCategory)) {
			return false;
		}
		AccountCategory tmp = (AccountCategory) that;
		boolean ret = categoryId.equals(tmp.getCategoryId());
		return ret;
	}

	public int compareTo(Object o) {
		if (this == o)
			return 0;
		AccountCategory c = (AccountCategory) o;
		int ret = categoryId.compareTo(c.getCategoryId());
		return ret;
	}

	public Object clone() throws CloneNotSupportedException {
		AccountCategory obj = (AccountCategory) super.clone();
		obj.setCreatedDate(new Date(getCreatedDate().getTime()));

		return obj;
	}
}
