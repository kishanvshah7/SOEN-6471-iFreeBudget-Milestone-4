package net.mjrz.fm.search.newfilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.mjrz.fm.entity.beans.types.EDouble;
import net.mjrz.fm.entity.beans.types.EString;
import net.mjrz.fm.utils.crypto.CHelper;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

public class Filter implements Filterable {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String name;

	private List<Predicate> predicates;

	private List<OperatorType> operators;

	private List<String> selectWhat;

	private String filterObject;

	private List<Order> order;

	private Logger log = Logger.getLogger(Filter.class);

	public Filter(String filterObject, List<String> select) {
		name = System.currentTimeMillis() + ".xml";
		predicates = new ArrayList<Predicate>();
		operators = new ArrayList<OperatorType>();

		this.filterObject = filterObject;
		this.selectWhat = select;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getSelectWhat() {
		return selectWhat;
	}

	public void setSelectWhat(List<String> selectWhat) {
		this.selectWhat = selectWhat;
	}

	public String getFilterObject() {
		return filterObject;
	}

	public void setFilterObject(String filterObject) {
		this.filterObject = filterObject;
	}

	public String getSelectString(boolean count) {
		if (count) {
			return " count(R) ";
		}
		StringBuilder builder = new StringBuilder(" R ");
		if (selectWhat != null) {
			for (int i = 0; i < selectWhat.size(); i++) {
				builder.append("." + selectWhat.get(i));
				if (i < selectWhat.size() - 1) {
					builder.append(",");
				}
			}
		}
		return builder.toString();
	}

	public String printFilter(boolean count) {
		StringBuilder query = new StringBuilder();

		query.append("select " + getSelectString(count) + " from "
				+ getFilterObject() + " R ");

		int sz = predicates.size();
		int opSz = sz - 1;

		if (sz > 0) {
			query.append(" where ");
			for (int i = 0; i < sz; i++) {
				Predicate p = predicates.get(i);
				query.append(p);
				query.append(Predicate.SPACE);
				if (i < opSz) {
					query.append(operators.get(i));
					query.append(Predicate.SPACE);
				}
				query.append("\n");
			}
		}

		if (!count) {
			if (order != null) {
				query.append(" order by ");
				int curr = 0;
				sz = order.size();
				for (Order o : order) {
					query.append(o);
					if (curr < sz - 1) {
						query.append(",");
					}
					curr++;
				}
			}
		}
		log.debug("* " + query);
		return query.toString();
	}

	public void addPredicate(Predicate p, OperatorType oper) {
		predicates.add(p);
		operators.add(oper);
	}

	@Override
	public String getSubstitutionString() {
		return printFilter(false);
	}

	@Override
	public String getValue() {
		return printFilter(false);
	}

	public Query getQueryObject(Session s, boolean countQuery) throws Exception {
		try {
			Query q = null;
			if (countQuery) {
				q = s.createQuery(printFilter(countQuery));
			}
			else {
				q = s.createQuery(printFilter(countQuery));
			}

			int sz = predicates.size();
			for (int i = 0; i < sz; i++) {
				Predicate p = predicates.get(i);
				setQueryParameters(q, p);
			}
			return q;
		}
		finally {
			count = 0;
		}
	}

	private void setQueryParameters(Query q, Predicate p) throws Exception {
		List<Filterable> values = p.getValues();
		int sz = values.size();

		for (int i = 0; i < sz; i++) {
			Filterable f = values.get(i);
			if (f instanceof Filter) {
				Filter filter = (Filter) f;
				List<Predicate> pList = filter.predicates;
				for (Predicate pred : pList) {
					setQueryParameters(q, pred);
				}
			}
			else {
				setQueryParameter(q, p.getType(), f);
			}
		}
	}

	private int count = 0;

	private void setQueryParameter(Query q, String type, Filterable p)
			throws Exception {
		if (type.equals(Double.class.getName())) {
			Double d = Double.parseDouble(p.getValue());
			q.setDouble(count, d);
			count++;
		}
		else if (type.equals(Date.class.getName())) {
			Date d = sdf.parse(p.getValue());
			q.setDate(count, d);
			count++;
		}
		else if (type.equals(EString.class.getName())) {
			q.setString(count, CHelper.encrypt(p.getValue()));
			count++;
		}
		else if (type.equals(String.class.getName())) {
			q.setString(count, p.getValue());
			count++;
		}
		else if (type.equals(EDouble.class.getName())) {
			q.setString(count, CHelper.encrypt(p.getValue()));
			count++;
		}
		else if (type.equals(Long.class.getName())) {
			q.setLong(count, Long.parseLong(p.getValue()));
			count++;
		}
	}

	public void addOrder(Order o) {
		if (order == null)
			order = new ArrayList<Order>();
		order.add(o);
	}

	public void clearOrder() {
		if (order != null)
			order.clear();
	}

	public List<Order> getOrder() {
		return order;
	}

	public static void main(String args[]) {
		String aname = "SovBank";
		
		Filter f = new Filter("TT", null);

		List<String> sel = new ArrayList<String>();
		sel.add("accountId");

		Filter sub = new Filter("Account", sel);
		sub.addPredicate(Predicate.create("accountName", aname,
				RelationType.LIKE, EString.class.getName()), OperatorType.AND);

		Predicate p1 = Predicate.create("fromAccountId", sub, RelationType.IN,
				Long.class.getName());

		Predicate p2 = Predicate.create("toAccountId", sub, RelationType.IN,
				Long.class.getName());

		Predicate p3 = Predicate.create("txNotes", "keywords", RelationType.LIKE,
				EString.class.getName());
		
		f.addPredicate(p1, OperatorType.OR);
		f.addPredicate(p2, OperatorType.OR);
		f.addPredicate(p3, OperatorType.AND);

		System.out.println(f.printFilter(false));		
	}
}
