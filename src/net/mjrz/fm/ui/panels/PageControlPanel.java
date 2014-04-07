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
package net.mjrz.fm.ui.panels;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.ExecuteFilterAction;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.search.newfilter.Filter;
import net.mjrz.fm.search.newfilter.Filterable;
import net.mjrz.fm.search.newfilter.OperatorType;
import net.mjrz.fm.search.newfilter.Order;
import net.mjrz.fm.search.newfilter.Predicate;
import net.mjrz.fm.search.newfilter.PredicateValue;
import net.mjrz.fm.search.newfilter.RelationType;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class PageControlPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton next, previous, first, last;
	private JLabel status;
	private JLabel sigma;

	private Long offset;
	private Long count;
	private Long numPages;
	private Long currPageNo;

	public static final int PAGE_SIZE = 20;

	private FinanceManagerUI parent;

	private static Logger logger = Logger.getLogger(PageControlPanel.class
			.getName());

	private NumberFormat numFormat = NumberFormat
			.getCurrencyInstance(SessionManager.getCurrencyLocale());

	private User user;

	// public Filter defaultFilter = null;

	private static PageControlPanel instance = null;

	private PageControlPanel() {
	}

	private PageControlPanel(FinanceManagerUI p) {
		parent = p;
		offset = 0l;
		currPageNo = 0l;
		numPages = 0l;
		count = 0l;
		initialize();
	}

	public static synchronized PageControlPanel getInstance(
			FinanceManagerUI parent) {
		if (instance == null) {
			instance = new PageControlPanel(parent);
		}
		return instance;
	}

	public synchronized static void disposeInstance() {
		if (instance == null)
			return;
		instance = null;
	}

	public Filter getDefaultFilter() {
		Filter defaultFilter = new Filter("TT", null);

		List<Filterable> l2 = new ArrayList<Filterable>();
		l2.add(new PredicateValue(RelationType.EQUALS, String
				.valueOf(TT.IsParent.NO.getVal())));

		Predicate p3 = Predicate.create("parentTxId", l2, RelationType.EQUALS,
				Long.class.getName());

		defaultFilter.addPredicate(p3, OperatorType.AND);

		defaultFilter.addOrder(new Order("Date", Order.DESC));
		defaultFilter.addOrder(new Order("createDate", Order.DESC));

		defaultFilter.setName("all transactions");

		return defaultFilter;
	}

	public Long getOffset() {
		return offset;
	}

	public void reset() {
		offset = 0l;
		count = 0l;
		currPageNo = 0l;
		numPages = 0l;
	}

	public void initializePaging() {
		offset = 0l;
		makePageRequest(getDefaultFilter());
	}

	private String getStatusString() {
		StringBuilder ret = new StringBuilder();
		if (count == 0) {
			ret.append(tr("Nothing to display"));
			return ret.toString();
		}
		ret.append(offset + 1);
		ret.append(" to ");

		long end = offset + PAGE_SIZE;
		if (end > count)
			end = count;

		ret.append(end);

		ret.append(" of ");
		ret.append(count);

		return ret.toString();
	}

	private Long getCount(Filter f) throws Exception {
		ActionRequest req = new ActionRequest();
		req.setActionName("executeFilter");
		req.setProperty("FILTER", f);
		req.setProperty("COUNT", true);
		req.setUser(user);

		ActionResponse resp = new ExecuteFilterAction().executeAction(req);
		Long ct = (Long) resp.getResult("COUNT");
		return ct;
	}

	private Long getNumPages() {
		if (count == 0)
			return count;

		long div = count / PAGE_SIZE;
		if (count >= PAGE_SIZE) {
			if (count % PAGE_SIZE != 0)
				div += 1;
		}
		return div;
	}

	private void makePageRequest() {
		try {
			Filter f = parent.getCurrentFilter();
			if (f == null)
				return;

			Long ct = getCount(f);
			if (ct != null) {
				this.count = ct;
				numPages = getNumPages();
				status.setText(getStatusString());
			}
			else {
				status.setText("Error making page request");
				return;
			}
			parent.executeFilter(f, offset, PAGE_SIZE);
			parent.updateStatusPane(ct);
			setButtonState();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void makePageRequest(Filter filter) {
		try {
			reset();
			Long ct = getCount(filter);
			if (ct != null) {
				this.count = ct;
				numPages = getNumPages();
				status.setText(getStatusString());
			}
			else {
				status.setText("Error making page request");
				return;
			}
			parent.executeFilter(filter, offset, PAGE_SIZE);
			parent.updateStatusPane(ct);
			setButtonState();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void initialize() {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		next = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/next.png"));
		next.setActionCommand("next");
		next.addActionListener(this);
		next.setPreferredSize(new Dimension(24, 24));
		next.setToolTipText(tr("Next"));

		previous = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/previous.png"));
		previous.setActionCommand("previous");
		previous.addActionListener(this);
		previous.setPreferredSize(new Dimension(24, 24));
		previous.setToolTipText(tr("Previous"));

		first = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/first.png"));
		first.setActionCommand("first");
		first.addActionListener(this);
		first.setPreferredSize(new Dimension(24, 24));
		first.setToolTipText(tr("First"));

		last = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/last.png"));
		last.setActionCommand("last");
		last.addActionListener(this);
		last.setPreferredSize(new Dimension(24, 24));
		last.setToolTipText(tr("Last"));

		status = new JLabel();

		sigma = new JLabel();
		sigma.setVisible(false);

		add(sigma);
		add(Box.createHorizontalGlue());
		add(status);
		add(Box.createHorizontalStrut(10));
		add(first);
		add(Box.createHorizontalStrut(10));
		add(previous);
		add(Box.createHorizontalStrut(10));
		add(next);
		add(Box.createHorizontalStrut(10));
		add(last);
		add(Box.createHorizontalStrut(10));
		setButtonState();
	}

	private void setButtonState() {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (numPages <= 0) {
						first.setEnabled(false);
						previous.setEnabled(false);
						next.setEnabled(false);
						last.setEnabled(false);
						return;
					}
					if (currPageNo <= 1) {
						first.setEnabled(false);
						previous.setEnabled(false);
						next.setEnabled(true);
						last.setEnabled(true);
						return;
					}
					if (currPageNo >= numPages) {
						next.setEnabled(false);
						last.setEnabled(false);
						first.setEnabled(true);
						previous.setEnabled(true);
						return;
					}
					if (currPageNo > 1 && currPageNo < numPages) {
						next.setEnabled(true);
						last.setEnabled(true);
						first.setEnabled(true);
						previous.setEnabled(true);
					}
				}
			});
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void hideSigma() {
		sigma.setVisible(false);
	}

	public void setSigma(BigDecimal value) {
		sigma.setText("Total = " + numFormat.format(value));
		sigma.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("next")) {
			if (currPageNo == 0)
				currPageNo += 1;
			currPageNo++;
		}
		if (cmd.equals("previous")) {
			if (currPageNo == 1)
				currPageNo -= 1;
			currPageNo--;
		}
		if (cmd.equals("first")) {
			currPageNo = 0l;
		}
		if (cmd.equals("last")) {
			currPageNo = numPages;
		}
		offset = (currPageNo * PAGE_SIZE) - PAGE_SIZE;
		if (offset >= count)
			offset = offset - count;
		if (offset < 0)
			offset = 0l;

		setButtonState();
		hideSigma();
		makePageRequest();
	}
}
