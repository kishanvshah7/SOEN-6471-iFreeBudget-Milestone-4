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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Ofx2Xml {
	private StringBuffer xml = new StringBuffer();
	private Logger logger = Logger.getLogger(Ofx2Xml.class.getName());

	public Ofx2Xml(Reader reader) throws Exception {
		BufferedReader in = new BufferedReader(reader);
		readFile(in);
	}

	private void readFile(BufferedReader in) throws Exception {
		try {
			readHeader(in);
			readRest(in);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			if (in != null)
				in.close();
		}
	}

	private void readHeader(BufferedReader in) throws Exception {
		logger.info("Attempting to read ofx header");

		xml.append("<?xml version=\"1.0\"?>\n");
		xml.append("<?OFX ");

		try {
			String s;
			while (true) {
				s = in.readLine();
				if (s == null) {
					break;
				}
				if (s.indexOf("<") == 0) {
					break;
				}
				if (s.trim().length() == 0)
					continue;

				s = s.replace(":", "=\"");
				xml.append(s);
				xml.append("\" ");
			}
			xml.append("?>\n");

			if (s != null) {
				try {
					eat(s, 0);
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
				}
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
	}

	private void readRest(BufferedReader in) throws Exception {
		try {
			while (true) {
				String s = in.readLine();
				if (s == null)
					break;
				s = s.trim();
				if (s.length() == 0)
					continue;
				eat(s, 0);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			throw e;
		}
	}

	private void eat(String s, int pos) throws Exception {
		if (s.charAt(0) != '<') {
			logger.warn("Exception at : eat(" + s + ", " + pos + ")");
			throw new IOException("Error in input");
		}

		int epos = s.indexOf('<', pos + 1);

		if (pos < 0 || pos >= s.length()) {
			return;
		}
		if (epos < 0) {
			epos = s.length();
		}

		String tag = s.substring(pos, epos);
		handleLine(tag);
		eat(s, epos);
	}

	private void handleLine(String s) throws Exception {
		if (s.charAt(0) != '<') {
			logger.warn("Invalid tag: " + s);
			throw new IOException("Parse error in input data0" + s);
		}
		int pos = s.indexOf('>');
		if (pos <= 0 || pos + 1 > s.length()) {
			logger.warn("Invalid tag: handleLine(" + s + ")");
			throw new IOException("Parse error in input data1: " + s);
		}
		String stag = s.substring(0, pos + 1);
		String data = getData(s, pos + 1);
		String etag = getEndTag(s, stag, data);

		if (stag.indexOf("</") == 0) {
			String last = xml.substring(xml.length() - (stag.length() + 1),
					xml.length() - 1);
			if (last != null && last.equals(stag)) {
				// the ofx response is almost well formed xml
				// but we generate end tag since most fi ofx is not well formed
				// so skip the end tag
				// (note: +1 above because we add newline char after a complete
				// tag :-)
				// crazzzzyyyy
				return;
			}
		}
		xml.append(stag);
		xml.append(data);
		xml.append(etag);

		// logger.info(stag + "****" + etag);
		xml.append("\n");
	}

	private String getEndTag(String s, String tag, String data) {
		if (s.charAt(1) == '/') {
			return "";
		}
		if (data.length() == 0)
			return "";

		int start = tag.length() + data.length();
		int pos = s.indexOf("</", start);
		if (pos >= 0 && pos >= start) {
			return s.substring(pos);
		}
		return tag.replace("<", "</");
	}

	private String getData(String s, int pos) {
		String ret = null;
		int idx = s.indexOf('<', pos);
		if (idx < 0) {
			ret = s.substring(pos);
		}
		else
			ret = s.substring(pos, idx);

		ret = MiscUtils.encodeHtmlChars(ret);
		return ret;
	}

	public String getXmlString() {
		return xml.toString();
	}
}
