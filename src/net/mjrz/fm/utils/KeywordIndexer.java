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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class KeywordIndexer {
	private static HashMap<String, ArrayList<Long>> index = new HashMap<String, ArrayList<Long>>();
	private static String[] reject = { "the", "from", "and", "that", "was",
			"for", "this", "then" };

	public static void addToIndex(String s, long txId) {
		if (s == null || s.length() == 0)
			return;
		ArrayList<String> toks = MiscUtils.splitString(s, " ");
		int sz = toks.size();
		for (int i = 0; i < sz; i++) {
			String tok = toks.get(i);
			tok = tok.trim().toLowerCase();

			if (tok.length() <= 2)
				continue;

			if (isRejected(tok))
				continue;

			addTokenToIndex(tok, txId);
		}
	}

	private static void addTokenToIndex(String tok, long txId) {
		ArrayList<Long> idxs = index.get(tok);
		if (idxs != null) {
			idxs.add(txId);
		}
		else {
			idxs = new ArrayList<Long>();
			idxs.add(txId);
			index.put(tok, idxs);
		}
	}

	public static void removeFromIndex(String s, long txId) {
		if (s == null || s.length() == 0)
			return;
		ArrayList<String> toks = MiscUtils.splitString(s, " ");

		int sz = toks.size();
		for (int i = 0; i < sz; i++) {
			String tok = toks.get(i);
			tok = tok.trim().toLowerCase();

			removeTokenFromIndex(tok, txId);
		}
	}

	private static void removeTokenFromIndex(String tok, long txId) {
		ArrayList<Long> idxs = index.get(tok);
		if (idxs != null) {
			idxs.remove(txId);
		}
	}

	public static HashSet<Long> retrieve(ArrayList<String> values) {
		HashSet<Long> ret = new HashSet<Long>();
		for (String s : values) {
			List<Long> match = lookup(s);
			if (match != null)
				ret.addAll(match);
		}
		// System.out.println("Match: " + ret);
		return ret;
	}

	public static HashSet<Long> retrieve(String words) {
		HashSet<Long> ret = new HashSet<Long>();
		if (words == null || words.length() == 0)
			return ret;

		ArrayList<String> keys = MiscUtils.splitString(words, " ");
		for (String key : keys) {
			List<Long> match = lookup(key);
			if (match != null)
				ret.addAll(match);
		}
		// System.out.println("Match: " + ret);
		return ret;
	}

	private static List<Long> lookup(String key) {
		if (key == null || key.length() == 0 || key.length() < 3)
			return null;

		List<Long> match = index.get(key.toLowerCase());
		return match;
	}

	private static boolean isRejected(String s) {
		for (int i = 0; i < reject.length; i++) {
			if (s.equalsIgnoreCase(reject[i]))
				return true;
		}
		return false;
	}
}
