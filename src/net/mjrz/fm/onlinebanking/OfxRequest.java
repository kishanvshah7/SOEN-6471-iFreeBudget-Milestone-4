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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public abstract class OfxRequest {

	String org;
	String fid;
	String accountId;
	String accountType;
	String user;
	String password;

	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public abstract String getMessage() throws Exception;

	public OfxRequest(String org, String fid, String accountid, String user,
			String password, String acctType) {
		this.org = org;
		this.fid = fid;
		this.accountId = accountid;
		this.accountType = acctType;
		this.user = user;
		this.password = password;
	}

	public String getRequestString() throws Exception {
		return getOfxHeader() + "<OFX>" + getSignonRequest(user, password)
				+ getMessage() + "</OFX>";
	}

	private String getSignonRequest(String user, String pass) {
		StringBuffer ret = new StringBuffer();
		ret.append("<SIGNONMSGSRQV1>");
		ret.append("<SONRQ>");
		ret.append("<DTCLIENT>" + sdf.format(new Date()));
		ret.append("<USERID>" + user);
		ret.append("<USERPASS>" + pass);
		ret.append("<LANGUAGE>ENG");
		ret.append("<FI>");
		ret.append("<ORG>" + org);
		ret.append("<FID>" + fid);
		ret.append("</FI>");
		ret.append("<APPID>QWIN");
		ret.append("<APPVER>1600");
		ret.append("</SONRQ>");
		ret.append("</SIGNONMSGSRQV1>");

		return ret.toString();
	}

	public String getOfxHeader() {
		StringBuffer ret = new StringBuffer();

		ret.append("OFXHEADER:100");
		ret.append("\r\n");
		ret.append("DATA:OFXSGML");
		ret.append("\r\n");
		ret.append("VERSION:102");
		ret.append("\r\n");
		ret.append("SECURITY:NONE");
		ret.append("\r\n");
		ret.append("ENCODING:USASCII");
		ret.append("\r\n");
		ret.append("CHARSET:1252");
		ret.append("\r\n");
		ret.append("COMPRESSION:NONE");
		ret.append("\r\n");
		ret.append("OLDFILEUID:NONE");
		ret.append("\r\n");
		ret.append("NEWFILEUID:" + genuuid());
		ret.append("\r\n\r\n");

		// System.out.println("Header length: " + ret.length());
		return ret.toString();
	}

	public String genuuid() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString().toUpperCase();
	}

	public String getDtStart() {
		GregorianCalendar gc = new GregorianCalendar();
		gc.add(Calendar.MONTH, -1);

		String month = "";
		String date = "";

		int mo = gc.get(Calendar.MONTH);
		mo += 1;
		if (mo < 10) {
			month = "0" + mo;
		}
		else {
			month = "" + mo;
		}
		int dt = gc.get(Calendar.DATE);
		dt += 1;
		if (dt < 10) {
			date = "0" + dt;
		}
		else {
			date = "" + dt;
		}

		String dtstart = gc.get(Calendar.YEAR) + month + date;

		return dtstart;
	}
}
