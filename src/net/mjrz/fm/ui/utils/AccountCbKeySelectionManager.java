package net.mjrz.fm.ui.utils;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class AccountCbKeySelectionManager implements
		JComboBox.KeySelectionManager {
	private long last = -1;
	private StringBuilder toMatch = new StringBuilder();
	private int lastSel = 0;

	@Override
	public int selectionForKey(char aKey, ComboBoxModel aModel) {
		long curr = System.currentTimeMillis();

		long diff = curr - last;
		System.out.println(diff);
		last = curr;
		if (diff < 0 || diff > 500) {
			toMatch.delete(0, toMatch.length());
			toMatch.append(aKey);
		}
		else {
			toMatch.append(aKey);
		}
		return match(toMatch.toString(), aModel);
	}

	private int match(String toMatch, ComboBoxModel model) {
		// System.out.println("Searching for " + toMatch + " from idx = " +
		// lastSel);
		int sz = model.getSize();
		if (lastSel >= sz || lastSel == -1) {
			lastSel = 0;
		}
		for (int i = lastSel; i < sz; i++) {
			AccountCbEntry ae = (AccountCbEntry) model.getElementAt(i);
			if (ae.getAccount().getAccountName().toLowerCase()
					.startsWith(toMatch)) {
				lastSel = i;
				return i;
			}
		}
		lastSel = 0;
		return 0;
	}
}
