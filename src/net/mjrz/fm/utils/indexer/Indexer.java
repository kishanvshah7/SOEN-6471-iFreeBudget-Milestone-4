package net.mjrz.fm.utils.indexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class Indexer {
	public static final int MINLENGTH = 3;
	private static Indexer instance = new Indexer();

	private Logger logger = Logger.getLogger(Indexer.class);

	private Map<IndexType, Index> idxMap = null;

	public static Indexer getIndexer() {
		return instance;
	}

	private Indexer() {
		idxMap = new HashMap<IndexType, Index>();
		loadIndexes();
	}

	public void index(String line, IndexedEntity entityName, IndexType type) {
		Index idx = idxMap.get(type);
		if (idx == null) {
			idx = new Index();
			idxMap.put(type, idx);
		}
		String[] split = line.split(" ");
		for (int i = 0; i < split.length; i++) {
			String token = split[i];
			idx.index(token, entityName);
		}
	}

	public void printDebugInfo(IndexType type) {
		Index idx = idxMap.get(type);
		if (idx == null) {
			return;
		}
		System.out.println(idx.getDebugInfo());
	}

	public MatchedEntity match(IndexType type, String line) {
		Index idx = idxMap.get(type);
		if (idx == null) {
			return null;
		}

		List<MatchedEntity> meList = new ArrayList<MatchedEntity>();
		String split[] = line.split(" ");
		for (String s : split) {
			if (s.length() <= MINLENGTH) {
				continue;
			}

			MatchedEntity me = idx.bestMatch(s);
			if (me != null) {
				meList.add(me);
			}
		}

		int sz = meList.size();
		if (meList.size() == 0) {
			return null;
		}
		Collections.sort(meList);
		return meList.get(sz - 1);
	}

	public void finalizeIndex(IndexType type) {
		Index idx = idxMap.get(type);
		if (idx == null) {
			return;
		}

		try {
			idx.save();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void loadIndexes() {
		try {
			Index index = new Index();
			index.load(IndexType.Account);
			idxMap.put(IndexType.Account, index);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public static enum IndexType {
		Account(1);
		private int type;

		private IndexType(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}
}
