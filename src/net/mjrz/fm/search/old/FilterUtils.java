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
package net.mjrz.fm.search.old;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.types.EString;
import net.mjrz.fm.utils.ZProperties;

public class FilterUtils {

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static void main(String args[]) {
		System.out.println(DATE_RANGE.LastMonth.getCode() + ":"
				+ DATE_RANGE.LastMonth + ":" + DATE_RANGE.get(1));
	}

	public enum DATE_RANGE {
		Today(1), Yesterday(2), LastWeek(3), LastMonth(4);

		private static final Map<Integer, DATE_RANGE> lookup = new HashMap<Integer, DATE_RANGE>();

		static {
			for (DATE_RANGE s : EnumSet.allOf(DATE_RANGE.class))
				lookup.put(s.getCode(), s);
		}

		private int code;

		private DATE_RANGE(int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}

		public static DATE_RANGE get(int code) {
			return lookup.get(code);
		}
	}

	public static Filter getAccountFilter(String name) {
		Filter topLvlFilter = new Filter();

		ArrayList<String> l1 = new ArrayList<String>();
		l1.add(name);

		/* Add toAccountId filter */
		Predicate p = Predicate.getPredicate("toAccountId", "accountName", l1,
				"in", EString.class.getName());

		/* Add sub filter */
		Filter subFltr = new Filter();
		subFltr.setFilterObject("Account");
		ArrayList<String> selectWhat = new ArrayList<String>();
		selectWhat.add("accountId");
		Predicate p1 = Predicate.getPredicate("accountName", "accountName", l1,
				"in", EString.class.getName());
		subFltr.addPredicate(p1, Filter.OR_OPERATOR);
		subFltr.setSelectWhat(selectWhat);
		/* End sub filter */
		p.addSubClause(subFltr);
		topLvlFilter.addPredicate(p, Filter.OR_OPERATOR);
		/* End toAccountId filter */

		/* Add fromAccountId selects */
		Predicate pFrom = Predicate.getPredicate("fromAccountId",
				"accountName", l1, "in", EString.class.getName());
		/* create sub filter */
		Filter subFltr1 = new Filter();
		subFltr1.setFilterObject("Account");
		Predicate p2 = Predicate.getPredicate("accountName", "accountName", l1,
				"in", EString.class.getName());
		subFltr1.addPredicate(p2, Filter.OR_OPERATOR);
		subFltr1.breakGroup(Filter.AND_OPERATOR);
		subFltr1.setSelectWhat(selectWhat);
		/* End sub filter */
		pFrom.addSubClause(subFltr1);
		topLvlFilter.addPredicate(pFrom, Filter.OR_OPERATOR);
		/* End fromAccountId filter */

		topLvlFilter.breakGroup(Filter.AND_OPERATOR);

		ArrayList<String> l2 = new ArrayList<String>();
		l2.add("0");
		Predicate p3 = Predicate.getPredicate("parentTxId", "parentTxId", l2,
				"in", Long.class.getName());
		topLvlFilter.addPredicate(p3, Filter.AND_OPERATOR);
		topLvlFilter.breakGroup(Filter.AND_OPERATOR);

		topLvlFilter.setFilterName(name);

		return topLvlFilter;
	}

	public static Filter getCategoryFilter(String name,
			List<AccountCategory> list) {
		Filter topLvlFilter = new Filter();

		ArrayList<String> l1 = new ArrayList<String>();
		for (AccountCategory tmp : list) {
			l1.add(String.valueOf(tmp.getCategoryId()));
		}

		/* Add toAccountId filter */
		Predicate p = Predicate.getPredicate("toAccountId", "categoryId", l1,
				"in", Long.class.getName());

		/* Add sub filter */
		Filter subFltr = new Filter();
		subFltr.setFilterObject("Account");
		ArrayList<String> selectWhat = new ArrayList<String>();
		selectWhat.add("accountId");
		Predicate p1 = Predicate.getPredicate("categoryId", "categoryId", l1,
				"in", EString.class.getName());
		subFltr.addPredicate(p1, Filter.OR_OPERATOR);
		subFltr.setSelectWhat(selectWhat);
		/* End sub filter */
		p.addSubClause(subFltr);
		topLvlFilter.addPredicate(p, Filter.OR_OPERATOR);
		/* End toAccountId filter */

		/* Add fromAccountId selects */
		Predicate pFrom = Predicate.getPredicate("fromAccountId", "categoryId",
				l1, "in", Long.class.getName());
		/* create sub filter */
		Filter subFltr1 = new Filter();
		subFltr1.setFilterObject("Account");
		Predicate p2 = Predicate.getPredicate("categoryId", "categoryId", l1,
				"in", EString.class.getName());
		subFltr1.addPredicate(p2, Filter.OR_OPERATOR);
		subFltr1.breakGroup(Filter.AND_OPERATOR);
		subFltr1.setSelectWhat(selectWhat);
		/* End sub filter */
		pFrom.addSubClause(subFltr1);
		topLvlFilter.addPredicate(pFrom, Filter.OR_OPERATOR);
		/* End fromAccountId filter */

		topLvlFilter.breakGroup(Filter.AND_OPERATOR);

		ArrayList<String> l2 = new ArrayList<String>();
		l2.add("0");
		Predicate p3 = Predicate.getPredicate("parentTxId", "parentTxId", l2,
				"in", Long.class.getName());
		topLvlFilter.addPredicate(p3, Filter.AND_OPERATOR);
		topLvlFilter.breakGroup(Filter.AND_OPERATOR);

		topLvlFilter.setFilterName(name);

		return topLvlFilter;
	}

	public static Filter getDecoratorFilter(String color) {
		Filter topLvlFilter = new Filter();

		ArrayList<String> l1 = new ArrayList<String>();
		l1.add(color);

		/* Add toAccountId filter */
		Predicate p = Predicate.getPredicate("txId", "color", l1, "in",
				String.class.getName());

		/* Add sub filter */
		Filter subFltr = new Filter();
		subFltr.setFilterObject("TxDecorator");
		ArrayList<String> selectWhat = new ArrayList<String>();
		selectWhat.add("txId");
		Predicate p1 = Predicate.getPredicate("color", "color", l1, "in",
				String.class.getName());
		subFltr.addPredicate(p1, Filter.OR_OPERATOR);
		subFltr.setSelectWhat(selectWhat);
		/* End sub filter */
		p.addSubClause(subFltr);
		topLvlFilter.addPredicate(p, Filter.OR_OPERATOR);
		/* End toAccountId filter */

		topLvlFilter.setFilterName(color);

		return topLvlFilter;
	}

	public static String saveFilter(Filter filter, String filterName) {
		String msg = "";
		if (filter.validateFilter()) {
			String fname = filterName;
			if (fname == null)
				return "Invalid filter name";
			if (!validateName(filterName)) {
				msg = "Invalid filter name, spaces and special characters are not allowed";
			}
			else {
				try {
					String dir = ZProperties.getProperty("FMHOME")
							+ net.mjrz.fm.Main.PATH_SEPARATOR + "filters";
					if (filter.saveFilter(dir, fname)) {
						// parent.addFilterToMenu(filter);
					}
					else {
						msg = "Failed to save filter. Check if a filter with same name already exists";
					}
				}
				catch (Exception ex) {
					msg = "Exception - " + ex.getMessage();
					ex.printStackTrace();
				}
			}
		}
		return msg;
	}

	private static boolean validateName(String s) {
		if (s == null || s.trim().length() == 0)
			return false;

		s = s.trim();
		int sz = s.length();
		boolean ret = true;
		for (int i = 0; i < sz; i++) {
			char c = s.charAt(i);
			if (Character.isLetterOrDigit(c) || c == ' ') {
				continue;
			}
			else {
				ret = false;
				break;
			}
		}
		return ret;
	}

	public static ArrayList<String> translateDateRanges(String range) {
		ArrayList<String> ret = new ArrayList<String>();
		if (range.equals(FilterUtils.DATE_RANGE.Yesterday.toString())) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.add(Calendar.DATE, -1);
			ret.add(sdf.format(gc.getTime()));
		}
		else if (range.equals(FilterUtils.DATE_RANGE.Today.toString())) {
			Date d = new Date();
			ret.add(sdf.format(d));
		}
		else if (range.equals(FilterUtils.DATE_RANGE.LastWeek.toString())) {
			GregorianCalendar gc = new GregorianCalendar();
			for (int i = 0; i < 7; i++) {
				gc.add(Calendar.DATE, -1);
				ret.add(sdf.format(gc.getTime()));
			}
		}
		else if (range.equals(FilterUtils.DATE_RANGE.LastMonth.toString())) {
			GregorianCalendar gc = new GregorianCalendar();
			for (int i = 0; i < 30; i++) {
				gc.add(Calendar.DATE, -1);
				ret.add(sdf.format(gc.getTime()));
			}
			ret.add(sdf.format(gc.getTime()));
		}
		return ret;
	}
}
