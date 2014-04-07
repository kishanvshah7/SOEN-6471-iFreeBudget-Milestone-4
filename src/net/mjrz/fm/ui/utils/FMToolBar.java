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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;

import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.dialogs.ExitPromptDialog;

public class FMToolBar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private JButton newAcct, newTrans, newTransWiz, addrBook, portfolio,
			logout, exit;

	private FinanceManagerUI parent;

	public FMToolBar(FinanceManagerUI p) {
		parent = p;
		newAcct = new JButton(new MyImageIcon("icons/tb_newacct.png"));
		newAcct.setToolTipText("New account");
		newAcct.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showNewAccountDialog(null);
			}
		});

		newTrans = new JButton(new MyImageIcon("icons/tb_newtrans.png"));
		newTrans.setToolTipText("New transaction");
		newTrans.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showNewTransactionDialog();
			}
		});

		newTransWiz = new JButton(new MyImageIcon("icons/wizard.png"));
		newTransWiz.setToolTipText("New transaction wizard");
		newTransWiz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showNewTransactionWizard();
			}
		});

		addrBook = new JButton(new MyImageIcon("icons/tb_addrbook.png"));
		addrBook.setToolTipText("Address book");
		addrBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showContactsDialog();
			}
		});

		portfolio = new JButton(new MyImageIcon("icons/portfolio.png"));
		portfolio.setToolTipText("Portfolio manager");
		portfolio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showPortfolioDialog();
			}
		});

		logout = new JButton(new MyImageIcon("icons/logout_tb.png"));
		logout.setToolTipText("Logout");
		logout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showExitDialog(ExitPromptDialog.LOGOUT);
			}
		});

		exit = new JButton(new MyImageIcon("icons/tb_exit.png"));
		exit.setToolTipText("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showExitDialog(ExitPromptDialog.EXIT);
			}
		});

		add(newAcct);
		add(newTrans);
		add(newTransWiz);
		add(addrBook);
		add(portfolio);
		add(logout);
		add(exit);
		addSeparator();
	}
}
