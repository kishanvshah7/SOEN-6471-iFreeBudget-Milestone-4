package net.mjrz.fm.search.newfilter;

import java.util.ArrayList;
import java.util.List;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.types.EString;

public class NewFilterUtils {

	public static Filter getAccountFilter(Account account) {
		String name = account.getAccountName();
		String catId = String.valueOf(account.getCategoryId());

		Filter f = new Filter("TT", null);

		List<String> sel = new ArrayList<String>();
		sel.add("accountId");
		Filter sub = new Filter("Account", sel);
		Predicate subPred = Predicate.create("accountName", name,
				RelationType.EQUALS, EString.class.getName());

		sub.addPredicate(subPred, OperatorType.AND);
		sub.addPredicate(Predicate.create("categoryId", catId,
				RelationType.EQUALS, Long.class.getName()), OperatorType.AND);

		Predicate p1 = Predicate.create("fromAccountId", sub, RelationType.IN,
				Long.class.getName());

		Predicate p2 = Predicate.create("toAccountId", sub, RelationType.IN,
				Long.class.getName());

		f.addPredicate(p1, OperatorType.OR);
		f.addPredicate(p2, OperatorType.AND);

		f.setName(account.getAccountName());
		return f;
	}

	public static Filter getCategoryFilter(String categoryName, List<AccountCategory> list) {

		List<Filterable> l1 = new ArrayList<Filterable>();
		for (AccountCategory tmp : list) {
			l1.add(new PredicateValue(RelationType.EQUALS, String.valueOf(tmp.getCategoryId())));
		}

		Filter f = new Filter("TT", null);

		List<String> subFilterSelect = new ArrayList<String>();
		subFilterSelect.add("accountId");

		Filter sub = new Filter("Account", subFilterSelect);
		Predicate subPred = Predicate.create("categoryId", l1, RelationType.IN,
				Long.class.getName());
		sub.addPredicate(subPred, OperatorType.AND);

		Predicate p1 = Predicate.create("fromAccountId", sub, RelationType.IN,
				Long.class.getName());

		Predicate p2 = Predicate.create("toAccountId", sub, RelationType.IN,
				Long.class.getName());

		f.addPredicate(p1, OperatorType.OR);
		f.addPredicate(p2, OperatorType.AND);

		f.setName(categoryName);
		
		return f;
	}
}
