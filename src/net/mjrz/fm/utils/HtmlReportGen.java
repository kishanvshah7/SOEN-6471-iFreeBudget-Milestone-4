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
package net.mjrz.fm.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.search.newfilter.Filter;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class HtmlReportGen {
	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"EEE, MMM d, ''yy");

	public HtmlReportGen() {

	}

	@SuppressWarnings("unchecked")
	public String generateReport(File file, List txHistory, Filter f)
			throws Exception {
		StringBuffer ret = new StringBuffer();
		ret.append("<html>");
		ret.append(getHtmlHead());
		ret.append("<body style=\"font-family:Courier; font-size: 80%\">");
		ret.append("<h2>Transactions Posted Summary Report</h2>");
		ret.append(getReportSummary(f, txHistory.size()));
		ret.append("<BR><HR><BR>");
		ret.append(getReportBody(txHistory));
		ret.append("<BR>");
		ret.append("</body>");
		ret.append("</html>");

		BufferedWriter out = null;
		try {
			String ext = getExtension("Ext" + file.getName());
			StringBuffer fname = new StringBuffer(file.getAbsolutePath());
			if (ext == null || !ext.equals("html")) {
				fname.append(".html");
			}
			File outfile = new File(fname.toString());
			out = new BufferedWriter(new FileWriter(outfile));
			out.write(ret.toString());
			return outfile.getAbsolutePath();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private String getExtension(String fname) {
		int pos = fname.lastIndexOf('.');
		if (pos >= 0 && pos + 1 < fname.length()) {
			String ext = fname.substring(pos + 1);
			return ext;
		}
		return null;
	}

	private String getHtmlHead() {
		StringBuffer ret = new StringBuffer();
		ret.append("<head><title>Mjrz.net - Personal Finance Manager - Report</title></head>");

		return ret.toString();
	}

	private String getReportSummary(Filter f, int num) {
		StringBuffer ret = new StringBuffer();
		ret.append("<table border=\"1\" rules=\"rows\" frame=\"box\" width=\"30%\"");
		ret.append("<tr bgcolor=\"#c0c0c0\"><td colspan=\"2\"><b>Summary</b></td></tr>");

		ret.append("<tr><td>Filter</td>");
		if (f != null)
			ret.append("<td>" + f.toString() + "</td>");
		else
			ret.append("<td>-NA-</td>");
		ret.append("</tr>");

		ret.append("<tr><td>Date</td>");
		ret.append("<td>" + sdf.format(new Date()) + "</td>");
		ret.append("</tr>");

		ret.append("<tr><td>Total records</td><td>" + num + "</td></tr>");
		ret.append("</table>");

		return ret.toString();
	}

	private String getReportBody(List list) {
		StringBuffer ret = new StringBuffer();
		ret.append("<table border=\"1\" rules=\"rows\" frame=\"box\" width=\"80%\"");

		ret.append("<tr bgcolor=\"#c0c0c0\">");
		ret.append("<td><b>Date</b></td>");
		ret.append("<td><b>From</b></td>");
		ret.append("<td><b>To</b></td>");
		ret.append("<td><b>Amount</b></td>");
		ret.append("<td><b>Status</b></td>");
		ret.append("</tr>");

		int sz = list.size();
		for (int i = 0; i < sz; i++) {
			TT t = (TT) list.get(i);
			StringBuffer tmp = new StringBuffer();
			if (i % 2 == 0) {
				tmp.append("<tr bgcolor=\"#eaeaea\">");
			}
			else {
				tmp.append("<tr bgcolor=\"#FFFFFF\">");
			}
			/* index 0 is the tx id number... ignored */
			tmp.append("<td>" + sdf.format(t.getTxDate()) + "</td>");
			tmp.append("<td>" + t.getFromName() + "</td>");
			tmp.append("<td>" + t.getToName() + "</td>");
			tmp.append("<td>" + t.getTxAmount() + "</td>");
			tmp.append("<td>" + AccountTypes.getTxStatus(t.getTxStatus())
					+ "</td>");
			tmp.append("</tr>");
			ret.append(tmp);
		}

		ret.append("</table>");
		ret.append("<b>* End *</b>");
		return ret.toString();
	}
}
