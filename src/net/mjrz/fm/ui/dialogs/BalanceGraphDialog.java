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
package net.mjrz.fm.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.GetBalanceHistoryAction;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.TT;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.graph.AccountBalanceLineGraph;
import net.mjrz.fm.ui.graph.PageableLineGraph;
import net.mjrz.fm.utils.Messages;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class BalanceGraphDialog extends JDialog {
	private User user;
	private JFrame parent;
	static final long serialVersionUID = 0L;
	private PageableLineGraph lineGraph = null;
	private JLabel statusLbl = null;
	private Account account = null;
	private static final int PAGE_SIZE = 3000;
	private int index = 0;
	private static Logger logger = Logger.getLogger(BalanceGraphDialog.class
			.getName());
	private final int GRAPH_WIDTH = 340;
	private JButton b1, b2, b3;

	public BalanceGraphDialog(JFrame p, User u, Account account)
			throws Exception {
		super(p, Messages.getString("Balance history"), true); //$NON-NLS-1$
		this.account = account;
		parent = p;
		user = u;

		try {
			initialize();
			this.setPreferredSize(new Dimension(540, 400));
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void initialize() throws Exception {
		try {
			ActionResponse resp = getResults();
			java.util.List<TT> txList = (java.util.List<TT>) resp
					.getResult("TXLIST");
			if (txList == null || txList.size() == 0) {
				throw new Exception("No values to display");
			}

			Container cp = getContentPane();
			cp.setLayout(new BorderLayout());

			this.setMinimumSize(new Dimension(640, 540));

			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			cp.add(getGraphPane(account, txList), BorderLayout.CENTER);
			cp.add(getButtonPane(), BorderLayout.SOUTH);
			cp.add(getNorthPanel(), BorderLayout.NORTH);

			net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
		}
		catch (Exception e) {
			throw e;
		}
	}

	private ActionResponse getResults() throws Exception {
		ActionRequest req = new ActionRequest();
		req.setActionName("getBalanceHistory");
		req.setProperty("ACCOUNT", account);
		req.setProperty("INDEX", index);
		req.setProperty("PAGE_SIZE", PAGE_SIZE);

		GetBalanceHistoryAction action = new GetBalanceHistoryAction();
		ActionResponse resp = action.executeAction(req);
		return resp;
	}

	private JPanel getGraphPane(Account account, java.util.List<TT> txList)
			throws Exception {
		lineGraph = new AccountBalanceLineGraph(account, txList);
		return lineGraph;
	}

	private JPanel getButtonPane() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.LINE_AXIS));

		b1 = new JButton(Messages.getString("Close"));
		b1.setActionCommand("Close");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		b2 = new JButton(Messages.getString("Next"));
		b2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraphN();
			}
		});

		b3 = new JButton(Messages.getString("Previous"));
		b3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateGraphP();
			}
		});

		ret.add(Box.createHorizontalGlue());
		ret.add(b3);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(b2);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(b1);

		ret.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		setButtonState();
		return ret;
	}

	private JPanel getNorthPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new FlowLayout());
		statusLbl = new JLabel("");
		ret.add(statusLbl);
		Font f = statusLbl.getFont();
		statusLbl.setFont(f.deriveFont(Font.BOLD));
		ret.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return ret;
	}

	private void updateGraphN() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				b2.setEnabled(false);
				b3.setEnabled(false);
				if (lineGraph.hasNext()) {
					lineGraph.nextPage();
				}
				return null;
			}

			protected void done() {
				setButtonState();
			}
		};
		worker.execute();
	}

	private void updateGraphP() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				b2.setEnabled(false);
				b3.setEnabled(false);
				if (lineGraph.hasPrevious()) {
					lineGraph.previousPage();
				}
				return null;
			}

			protected void done() {
				setButtonState();
			}
		};
		worker.execute();
	}

	private void setButtonState() {
		if (!lineGraph.hasNext()) {
			b2.setEnabled(false);
		}
		else {
			b2.setEnabled(true);
		}

		if (!lineGraph.hasPrevious()) {
			b3.setEnabled(false);
		}
		else {
			b3.setEnabled(true);
		}
	}
}
