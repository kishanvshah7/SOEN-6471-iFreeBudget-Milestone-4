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
package net.mjrz.fm.ui.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.AccountCategoryMap;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AccountsTreeModel implements TreeModel {
	private AccountCategory root = new AccountCategory(1, Long.valueOf(-1),
			Long.valueOf(-1));

	private LinkedHashMap<Long, AccountCategory> map;

	private java.util.List<Account> accountList;

	private ArrayList<TreeModelListener> listener;

	private final String SPACE = " ";

	public AccountsTreeModel(long uid, String type,
			java.util.List<Account> alist) {
		listener = new ArrayList<TreeModelListener>();

		map = new AccountCategoryMap(uid, AccountTypes.DEFAULT_ROOT_NAME)
				.getMap();

		root.setCategoryName(type);

		accountList = alist;
		java.util.Collections.sort(accountList);

		for (Account a : alist) {
			AccountCategory owner = map.get(a.getCategoryId());
			AccountCategory newac = new AccountCategory(uid, a.getAccountId(),
					owner.getCategoryId());
			newac.setCategoryName(a.toString());
			map.put(newac.getCategoryId(), newac);
		}
	}

	public TreePath getNearestMatch(String accountName) {
		if (accountName == null || accountName.length() == 0)
			return null;

		Set<Entry<Long, AccountCategory>> set = map.entrySet();
		AccountCategory target = null;
		for (Entry<Long, AccountCategory> e : set) {
			String tmp = e.getValue().getCategoryName();
			String regex = ".*" + accountName + ".*";

			Pattern p = Pattern.compile(regex, Pattern.UNICODE_CASE
					| Pattern.CASE_INSENSITIVE);

			boolean matches = p.matcher(tmp).matches();
			// System.out.println(regex + "," + tmp + "=" + matches);
			if (matches) {
				target = e.getValue();
				break;
			}
		}
		if (target != null) {
			return getPath(target);
		}
		return null;
	}

	/* Interface methods */
	public boolean isRoot(AccountCategory a) {
		return a.equals(root);
	}

	public Object getRoot() {
		return root;
	}

	public int getChildCount(Object parent) {
		AccountCategory p = (AccountCategory) parent;
		return this.getChildren(p).size();
	}

	public boolean isLeaf(Object node) {
		AccountCategory p = (AccountCategory) node;
		return this.getChildren(p).size() == 0;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	public Object getChild(Object parent, int index) {
		ArrayList<AccountCategory> children = this
				.getChildren((AccountCategory) parent);
		if (index < 0 || index >= children.size())
			return null;
		return children.get(index);
	}

	public int getIndexOfChild(Object parent, Object child) {
		AccountCategory p = (AccountCategory) parent;
		ArrayList<AccountCategory> children = this.getChildren(p);
		return children.indexOf(child);
	}

	public void addTreeModelListener(TreeModelListener l) {
		listener.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listener.remove(l);
	}

	/* End Interface methods */

	/**
	 * Returns list containing all child categories of the input node. Accounts
	 * and empty categories (leaf nodes) are omitted.
	 * 
	 * @param a
	 * @return
	 */
	public List<AccountCategory> getChildCategories(AccountCategory a) {
		ArrayList<AccountCategory> list = new ArrayList<AccountCategory>();
		getAllDescendents(list, a);
		List<AccountCategory> ret = new ArrayList<AccountCategory>();
		for (AccountCategory ac : list) {
			if (!isLeaf(ac) || ac.equals(a)) {
				ret.add(ac);
			}
		}
		return ret;
	}

	public ArrayList<AccountCategory> getChildren(AccountCategory a) {
		ArrayList<AccountCategory> ret = new ArrayList<AccountCategory>();
		Long parentId = a.getCategoryId();
		Set<Long> keys = map.keySet();
		for (Long i : keys) {
			AccountCategory tmp = map.get(i);
			if (tmp.getParentCategoryId().longValue() == parentId.longValue())
				ret.add(tmp);
		}
		return ret;
	}

	public ArrayList<AccountCategory> getAncestors(AccountCategory a) {
		ArrayList<AccountCategory> ret = new ArrayList<AccountCategory>();
		ret.add(a);
		if (a.equals(root)) {
			return ret;
		}
		while (true) {
			AccountCategory p = getParent(a);
			if (p == null)
				break;
			ret.add(0, p);
			a = p;
		}
		ret.add(0, root);
		return ret;
	}

	public void getAllDescendents(ArrayList<AccountCategory> list,
			AccountCategory c) {
		ArrayList<AccountCategory> children = getChildren(c);
		for (AccountCategory t : children) {
			getAllDescendents(list, t);
		}
		list.add(c);
	}

	public boolean isEmpty(AccountCategory ac) {
		return getChildCount(ac) == 0;
	}

	public TreePath getPath(AccountCategory node) {
		ArrayList<AccountCategory> ancestors = getAncestors(node);
		return new TreePath(ancestors.toArray());
	}

	public AccountCategory getParent(AccountCategory c) {
		AccountCategory parent = map.get(c.getParentCategoryId());
		return parent;
	}

	public void removeCategory(AccountCategory a) {
		ArrayList<AccountCategory> ancestors = getAncestors(a);
		ancestors.remove(a);

		AccountCategory parent = getParent(a);

		ArrayList<AccountCategory> siblings = getChildren(parent);
		int idx = 0;
		for (int i = 0; i < siblings.size(); i++) {
			AccountCategory ac = (AccountCategory) siblings.get(i);
			if (ac.getCategoryId().equals(a.getCategoryId())) {
				idx = i;
				break;
			}
		}
		int[] index = new int[1];
		index[0] = idx;

		map.remove(a.getCategoryId());

		TreeModelEvent ev = new TreeModelEvent(this, new TreePath(
				ancestors.toArray()), index, new Object[] { a });

		// TreeModelEvent ev = new TreeModelEvent(this, ancestors.toArray());

		for (TreeModelListener l : listener) {
			l.treeStructureChanged(ev);
		}
	}

	public void addAccount(Account a) {
		AccountCategory ac = new AccountCategory(a.getOwnerId(),
				a.getAccountId(), a.getCategoryId());
		ac.setCategoryName(a.getAccountName());

		accountList.add(a);

		addCategory(ac);
	}

	public void removeAccount(Account a) {
		boolean success = accountList.remove(a);
		if (success) {
			AccountCategory ac = map.get(a.getAccountId());
			removeCategory(ac);
		}
	}

	/**
	 * @param account
	 *            Account to update in the model
	 * @param silent
	 *            To fire tree update events or not
	 */
	public void updateAccount(Account account, boolean silent) {
		int idx = accountList.indexOf(account);
		if (idx >= 0) {
			accountList.set(idx, account);
		}
		if (!silent) {
			AccountCategory ac = new AccountCategory(account.getOwnerId(),
					account.getAccountId(), account.getCategoryId());
			ac.setCategoryName(account.getAccountName());
			updateCategory(ac);
		}
	}

	public void updateCategory(AccountCategory a) {
		map.put(a.getCategoryId(), a);

		AccountCategory parent = getParent(a);
		ArrayList<AccountCategory> ancestors = getAncestors(a);

		ArrayList<AccountCategory> siblings = getChildren(parent);
		int idx = 0;
		for (int i = 0; i < siblings.size(); i++) {
			AccountCategory ac = (AccountCategory) siblings.get(i);
			if (ac.getCategoryId().equals(a.getCategoryId())) {
				idx = i;
				break;
			}
		}
		int[] index = new int[1];
		index[0] = idx;

		ancestors.remove(ancestors.size() - 1);
		TreeModelEvent ev = new TreeModelEvent(this, new TreePath(
				ancestors.toArray()), index, new Object[] { a });

		for (TreeModelListener l : listener) {
			// System.out.println("Tree structure changed event ..." +
			// a.getCategoryName());
			l.treeStructureChanged(ev);
		}
	}

	public void addCategory(AccountCategory a) {
		map.put(a.getCategoryId(), a);
		AccountCategory parent = getParent(a);
		ArrayList<AccountCategory> ancestors = getAncestors(a);

		ArrayList<AccountCategory> siblings = getChildren(parent);
		int idx = 0;
		for (int i = 0; i < siblings.size(); i++) {
			AccountCategory ac = (AccountCategory) siblings.get(i);
			if (ac.getCategoryId().equals(a.getCategoryId())) {
				idx = i;
				break;
			}
		}
		int[] index = new int[1];
		index[0] = idx;

		ancestors.remove(ancestors.size() - 1);
		TreeModelEvent ev = new TreeModelEvent(this, new TreePath(
				ancestors.toArray()), index, new Object[] { a });

		for (TreeModelListener l : listener) {
			l.treeStructureChanged(ev);
		}
	}

	public AccountCategory getAccountCategory(long id) {
		return map.get(id);
	}

	public boolean isAccount(long id) {
		boolean ret = false;
		for (Account a : accountList) {
			if (a.getAccountId() == id) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public Account getAccount(long id) {
		for (Account a : accountList) {
			if (a.getAccountId() == id)
				return a;
		}
		return null;
	}

	public int getNumAccounts() {
		return accountList.size();
	}

	public int getSize() {
		return map.size();
	}

	public long getMajorAccountCategory(AccountCategory a) {
		ArrayList<AccountCategory> ancestors = this.getAncestors(a);
		if (ancestors != null && ancestors.size() > 1) {
			return ancestors.get(1).getCategoryId(); // First element is root.
														// Next is the major
														// account category
		}
		return -1;
	}
}
