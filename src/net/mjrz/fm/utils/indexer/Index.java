package net.mjrz.fm.utils.indexer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import net.mjrz.fm.entity.IndexEntityManager;
import net.mjrz.fm.entity.beans.IndexedEntityBean;
import net.mjrz.fm.utils.indexer.Indexer.IndexType;

public class Index {
	private HashMap<String, Set<IndexedEntity>> idx;

	public Index() {
		idx = new HashMap<String, Set<IndexedEntity>>();
	}

	public MatchedEntity bestMatch(String token) {
		Set<IndexedEntity> list = idx.get(token);
		if (list == null) {
			return null;
		}
		int total = 0;
		int tmp = 0;
		for (IndexedEntity ie : list) {
			tmp += ie.getCount();
		}
		total = tmp;

		double last = Double.valueOf(0.0d);

		IndexedEntity best = null;
		for (IndexedEntity ie : list) {
			double currCount = Double.valueOf(ie.getCount());
			double currR = currCount / total;
			double nonCurrR = (total - currCount) / total;

			double val = currR / (currR + nonCurrR);

			if (val > last) {
				last = val;
				best = ie;
			}
		}

		MatchedEntity me = new MatchedEntity(best, new BigDecimal(last));
		return me;
	}

	public void index(String token, IndexedEntity entity) {
		if (!validateToken(token)) {
			return;
		}

		Set<IndexedEntity> list = idx.get(token);
		if (list == null) {
			list = new TreeSet<IndexedEntity>();
			idx.put(token, list);
		}

		boolean found = false;
		Iterator<IndexedEntity> it = list.iterator();
		while (it.hasNext()) {
			IndexedEntity ie = it.next();
			if (ie.equals(entity)) {
				ie.setCount(ie.getCount() + 1);
				found = true;
			}
		}

		if (!found) {
			list.add(entity);
		}
	}

	private boolean validateToken(String token) {
		if (token == null || token.length() <= Indexer.MINLENGTH) {
			return false;
		}
		boolean isValid = false;
		for (int i = 0; i < token.length(); i++) {
			if (Character.isLetter(token.charAt(i))) {
				isValid = true;
				break;
			}
		}
		return isValid;
	}

	void load(IndexType type) throws Exception {
		List<IndexedEntityBean> list = new IndexEntityManager().getIndex(type);
		for (IndexedEntityBean ieb : list) {
			readLine(ieb);
		}
		// String info = this.getDebugInfo();
		// System.out.println("Debug info = " + info);
	}

	private void readLine(IndexedEntityBean ieb) throws Exception {
		String tok = ieb.getToken();

		Set<IndexedEntity> ieSet = idx.get(tok);
		if (ieSet == null) {
			ieSet = new TreeSet<IndexedEntity>();
			idx.put(tok, ieSet);
		}

		IndexedEntity ie = new IndexedEntity();
		ie.setId(ieb.getId());
		ie.setName(ieb.getName());
		ie.setType(ieb.getType());
		ie.setCount(ieb.getOccuranceCount());

		ieSet.add(ie);
	}

	void save() throws Exception {
		IndexEntityManager em = new IndexEntityManager();

		Set<Entry<String, Set<IndexedEntity>>> eSet = idx.entrySet();
		for (Entry<String, Set<IndexedEntity>> e : eSet) {
			String tok = e.getKey();

			Set<IndexedEntity> ieSet = e.getValue();

			Iterator<IndexedEntity> it = ieSet.iterator();

			while (it.hasNext()) {
				IndexedEntity ie = it.next();
				em.addIndexedEntity(IndexType.Account, tok, ie);
			}
		}
	}

	public String getDebugInfo() {
		StringBuilder ret = new StringBuilder();
		ret.append("Num tokens = " + idx.size() + "\n");
		Set<String> keys = idx.keySet();
		for (String s : keys) {
			Set<IndexedEntity> set = idx.get(s);
			ret.append("Token = " + s + ":" + set.size() + "\n");
			for (IndexedEntity ie : set) {
				ret.append("\t" + ie.toString() + "\n");
			}
			ret.append("\n_____________________________\n");
		}
		return ret.toString();
	}
}
