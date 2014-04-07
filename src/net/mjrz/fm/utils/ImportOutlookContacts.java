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

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Contact;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ImportOutlookContacts {

	public ImportOutlookContacts() {

	}

	public int[] importFile(long uid, File f) throws Exception {
		try {
			CSVReader reader = new CSVReader();
			reader.read(f);
			ArrayList<ArrayList<String>> tokens = reader.getTokens();
			HashMap<Integer, String> header = reader.getHeader();

			int count = loadContacts(uid, header, tokens);

			int[] ret = { tokens.size(), count };

			return ret;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	private void printHeader(HashMap<Integer, String> header) {
		Set<Integer> keys = header.keySet();
		for (Integer i : keys) {
			System.out.println("->" + i + ":" + header.get(i));
		}
	}

	private int loadContacts(long uid, HashMap<Integer, String> headermap,
			ArrayList<ArrayList<String>> tokens) throws Exception {
		FManEntityManager em = new FManEntityManager();
		int count = 0;
		for (ArrayList<String> linedata : tokens) {
			if (count == 0) {
				count++;// First line is the header data
				continue;
			}
			Contact ab = buildAddrBookEntry(headermap, linedata);
			ab.setUserId(uid);
			if (ab.getFullName() == null
					|| ab.getFullName().trim().length() == 0
					|| ab.getEmail() == null
					|| ab.getEmail().trim().length() == 0) {
				continue;
			}
			em.addContact(ab, false);
			count++;
		}
		return count;
	}

	private Contact buildAddrBookEntry(HashMap<Integer, String> headermap,
			ArrayList<String> linedata) {
		Contact entry = new Contact();
		int sz = linedata.size();
		for (int i = 0; i < sz; i++) {
			String ditem = linedata.get(i);
			String headeritem = headermap.get(i);
			String methodname = ZProperties.getProperty(headeritem);
			// System.out.println(ditem + ":" + headeritem + ":" + methodname);
			if (methodname != null && methodname.length() > 0) {
				invokeAddrBookMethod(entry, methodname, ditem);
			}
		}
		return entry;
	}

	private void invokeAddrBookMethod(Contact a, String mname, String val) {
		try {
			Class c = a.getClass();
			Class[] ptypes = { "".getClass() };
			Method m = c.getMethod(mname, ptypes);
			if (m != null) {
				Object[] args = new Object[1];
				args[0] = MiscUtils.trimChars(val, '"');
				m.invoke(a, args);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
