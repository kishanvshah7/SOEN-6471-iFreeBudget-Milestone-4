package net.mjrz.fm.utils.indexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GenInp {
	public static void main(String args[]) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader("ofx.txt"));
		// BufferedWriter out = new BufferedWriter(new FileWriter("in.txt"));

		List<String> slist = new ArrayList<String>();

		while (true) {
			String s = in.readLine();
			if (s == null) {
				break;
			}
			int pos = s.indexOf("<NAME>");
			if (pos == 0) {
				int start = pos + "<NAME>".length();
				slist.add(s.substring(start));
				continue;
			}
			pos = s.indexOf("<MEMO>");
			if (pos == 0) {
				int sz = slist.size();
				if (sz > 0) {
					String toAdd = slist.remove(sz - 1);
					int start = pos + "<MEMO>".length();
					toAdd = toAdd + " " + s.substring(start);
					slist.add(toAdd);
				}
				continue;
			}
		}
		in.close();
		for (String s : slist) {
			System.out.println(s);
		}
	}
}
