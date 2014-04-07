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
package net.mjrz.fm.onlinebanking;

import net.mjrz.fm.entity.beans.ONLBDetails;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class OfxCCRequest extends OfxRequest {

	public OfxCCRequest(ONLBDetails details) {

		super(details.getOrg(), details.getFid(), details.getAccountId() + "",
				details.getUser(), details.getPassword(), details.getType());
	}

	public OfxCCRequest(String org, String fid, String accountid, String user,
			String password, String acctType) {
		super(org, fid, accountid, user, password, acctType);
	}

	public String getMessage() throws Exception {
		String acctnum = "";
		try {
			acctnum = net.mjrz.fm.entity.FManEntityManager
					.getAccountNumber(Long.parseLong(accountId));
		}
		catch (Exception e) {
			throw e;
		}

		StringBuffer ret = new StringBuffer();
		ret.append("<CREDITCARDMSGSRQV1>");
		ret.append("<CCSTMTTRNRQ>");
		ret.append("<TRNUID>" + genuuid());
		ret.append("<CCSTMTRQ>");
		ret.append("<CCACCTFROM>");
		ret.append("<ACCTID>" + acctnum);
		ret.append("</CCACCTFROM>");
		ret.append("<INCTRAN>");
		ret.append("<DTSTART>" + getDtStart());
		// ret.append("<DTSTART>20070501");
		// ret.append("<DTEND>20070501");
		ret.append("<INCLUDE>Y");
		ret.append("</INCTRAN>");
		ret.append("</CCSTMTRQ>");
		ret.append("</CCSTMTTRNRQ>");
		ret.append("</CREDITCARDMSGSRQV1>");

		return ret.toString();
	}
}
