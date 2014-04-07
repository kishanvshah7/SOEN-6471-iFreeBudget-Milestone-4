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
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class CSVReader {
	ArrayList<String> lines = null;
	ArrayList<ArrayList<String>> tokens = null;

	/**
	 * @return the lines
	 */
	public ArrayList<String> getLines() {
		return lines;
	}

	/**
	 * @return the tokens
	 */
	public ArrayList<ArrayList<String>> getTokens() {
		return tokens;
	}

	public CSVReader() {
		lines = new ArrayList<String>();
		tokens = new ArrayList<ArrayList<String>>();
	}

	public void read(File f) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			while (true) {
				String s = in.readLine();
				if (s == null)
					break;
				lines.add(s);
				tokens.add(getTokens(s, ","));
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) {
			}
		}
	}

	public void readStream(InputStream is) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			while (true) {
				String s = in.readLine();
				if (s == null)
					break;
				lines.add(s);
				tokens.add(getTokens(s, ","));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<Integer, String> getHeader() {
		String header = lines.get(0);
		StringTokenizer st = new StringTokenizer(header, ",");
		HashMap<Integer, String> ret = new HashMap<Integer, String>();
		int count = 0;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			String tok = MiscUtils.trimChars(s, '"');
			tok = tok.replaceAll(" ", "_");
			ret.put(count, tok);
			count++;
		}
		return ret;
	}

	private ArrayList<String> getTokens(String s, String delim) {
		ArrayList<String> ret = new ArrayList<String>();
		int pos = 0;
		int lastIdx = 0;
		while (pos < s.length()) {
			if (s.charAt(pos) == ',') {
				String tmp = s.substring(lastIdx, pos);
				addToken(ret, tmp);
				lastIdx = pos + 1;
			}
			pos++;
		}
		// System.out.println(ret);
		return ret;
	}

	private void addToken(ArrayList<String> tokens, String tok) {
		if (tokens.size() == 0) {
			tokens.add(tok);
			return;
		}
		String x = tokens.get(tokens.size() - 1);
		if (x.length() > 0 && x.charAt(0) == '"'
				&& x.charAt(x.length() - 1) != '"') {
			StringBuffer rem = new StringBuffer(
					tokens.remove(tokens.size() - 1));
			rem.append(tok);
			tokens.add(rem.toString());
		}
		else {
			tokens.add(tok);
		}
	}

	public static void main(String args[]) {
		new CSVReader();
	}
}
