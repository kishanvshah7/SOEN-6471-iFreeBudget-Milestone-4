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
package net.mjrz.fm.ui.wizards;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.Budget;
import net.mjrz.fm.entity.beans.BudgetedAccount;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.wizards.components.WizardComponent;
import net.mjrz.fm.ui.wizards.components.budget.AccountSelectorPanel;
import net.mjrz.fm.ui.wizards.components.budget.BudgetAmountsPanel;
import net.mjrz.fm.ui.wizards.components.budget.BudgetTypePanel;
import net.mjrz.fm.ui.wizards.components.budget.ReviewPanel;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.crypto.CHelper;

import org.apache.log4j.Logger;

public class BudgetCreatorWizard extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private WizardProgressPanel progressPanel;
	private JButton next, previous, cancel;

	private WizardComponent[] wizardComponents;

	private JPanel centerPanel;

	private HashMap<String, String[][]> values = null;

	private static final String[] PANELS = { "Select type", "Select accounts",
			"Set amounts", "Finish" };

	private JLabel[] progressLabels;

	private int curr = 0;

	private static Logger logger = Logger.getLogger(BudgetCreatorWizard.class
			.getName());

	private FManEntityManager em;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private FinanceManagerUI parent;

	private final int width = 860;
	private final int height = 500;

	public BudgetCreatorWizard(JFrame parent) {
		super(parent, "Budget", true);
		this.parent = (FinanceManagerUI) parent;

		em = new FManEntityManager();
		progressLabels = new JLabel[PANELS.length];
		progressLabels[0] = new JLabel(tr("1. Select type"));
		progressLabels[1] = new JLabel(tr("2. Select accounts"));
		progressLabels[2] = new JLabel(tr("3. Set amounts"));
		progressLabels[3] = new JLabel(tr("4. Save"));

		wizardComponents = new WizardComponent[PANELS.length];
		wizardComponents[0] = new BudgetTypePanel();
		wizardComponents[1] = new AccountSelectorPanel();
		wizardComponents[2] = new BudgetAmountsPanel();
		wizardComponents[3] = new ReviewPanel();

		values = new HashMap<String, String[][]>();

		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		add(getProgressPanel(), BorderLayout.WEST);
		add(getCenterPanel(), BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
		boldenLabel(curr);
		manageButtonState();

		setTitle(tr("New transaction"));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(width, height));
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				BudgetCreatorWizard.class.getClassLoader().getResource(
						"icons/icon_money.png")));
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel getCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setLayout(new CardLayout());
		centerPanel.add((JPanel) wizardComponents[0], PANELS[0]);
		centerPanel.add((JPanel) wizardComponents[1], PANELS[1]);
		centerPanel.add((JPanel) wizardComponents[2], PANELS[2]);
		centerPanel.add((JPanel) wizardComponents[3], PANELS[3]);

		return centerPanel;
	}

	private JPanel getProgressPanel() {
		progressPanel = new WizardProgressPanel();
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

		for (int i = 0; i < progressLabels.length; i++) {
			progressPanel.add(progressLabels[i]);
			progressPanel.add(Box.createVerticalStrut(10));
		}
		progressPanel.add(Box.createVerticalGlue());

		progressPanel.setBorder(BorderFactory.createTitledBorder(tr("Progress")
				+ ": "));
		progressPanel.setBackground(UIDefaults.DEFAULT_COLOR);
		progressPanel.setPreferredSize(new Dimension(225, height));
		return progressPanel;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		next = new JButton(tr("Next") + " >");
		next.setActionCommand("next");
		next.addActionListener(this);
		next.setMnemonic(KeyEvent.VK_N);
		getRootPane().setDefaultButton(next);

		previous = new JButton("< " + tr("Back"));
		previous.setActionCommand("back");
		previous.setMnemonic(KeyEvent.VK_B);
		previous.addActionListener(this);

		cancel = new JButton(tr("Cancel"));
		cancel.setActionCommand("cancel");
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(this);

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(previous);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(next);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(cancel);
		buttonPanel.add(Box.createHorizontalStrut(10));

		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		return buttonPanel;
	}

	private void manageButtonState() {
		if (curr > 0)
			previous.setEnabled(true);
		else
			previous.setEnabled(false);

		if (curr < PANELS.length - 1) {
			next.setEnabled(true);
		}
		else
			next.setEnabled(false);

		if (curr == PANELS.length - 1) {
			cancel.setActionCommand("finish");
			cancel.setText(tr("Finish"));
			cancel.setMnemonic(KeyEvent.VK_F);
		}
		else {
			cancel.setActionCommand("cancel");
			cancel.setText(tr("Cancel"));
			cancel.setMnemonic(KeyEvent.VK_C);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("next")) {
			boolean valid = wizardComponents[curr].isComponentValid();
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						wizardComponents[curr].getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			String[][] v = wizardComponents[curr].getValues();
			for (int i = 0; i < v.length; i++) {
				values.put(PANELS[curr], v);
			}
			curr++;
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			wizardComponents[curr].updateComponentUI(values);
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
			return;
		}

		if (cmd.equals("back")) {
			if (curr >= PANELS.length)
				curr = PANELS.length - 1;

			values.remove(PANELS[curr]);
			curr--;
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			wizardComponents[curr].updateComponentUI(values);
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
		}
		if (cmd.equals("cancel")) {
			dispose();
		}
		if (cmd.equals("finish")) {
			boolean valid = wizardComponents[curr].isComponentValid();
			if (!valid) {
				JOptionPane.showMessageDialog(this,
						wizardComponents[curr].getMessage(), tr("Error"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String[][] v = wizardComponents[curr].getValues();
			for (int i = 0; i < v.length; i++) {
				values.put(PANELS[curr], v);
			}
			curr++;
			finish();
		}
	}

	private void boldenLabel(int idx) {
		for (int i = 0; i < progressLabels.length; i++) {
			Font f = progressLabels[i].getFont();
			Font fn = null;
			if (i == idx && f.getStyle() == Font.PLAIN) {
				fn = new Font(f.getFamily(), Font.BOLD, f.getSize());
				progressLabels[i].setForeground(Color.WHITE);
			}
			else {
				fn = new Font(f.getFamily(), Font.PLAIN, f.getSize());
				progressLabels[i].setForeground(Color.BLACK);
			}
			progressLabels[i].setFont(fn);
		}
	}

	private void printValues() {
		Set<String> keys = values.keySet();
		for (String k : keys) {
			String[][] vals = values.get(k);
			System.out.println("Key: " + k);
			for (int i = 0; i < vals.length; i++) {
				System.out.println("\t" + vals[i][0] + " - " + vals[i][1]);
			}
		}
	}

	public void finish() {
		String[] type = getValue("Select type")[0];
		String[] name = getValue("Select accounts")[0];
		String[][] amounts = getValue("Set amounts");

		Budget b = new Budget();
		b.setType(Budget.getTypeFromString(type[0]));
		b.setName(name[0]);
		b.setUid(SessionManager.getSessionUserId());
		Set<BudgetedAccount> baSet = new HashSet<BudgetedAccount>();

		for (int i = 0; i < amounts.length; i++) {
			String[] row = amounts[i];
			try {
				Account a = em
						.getAccountFromName(SessionManager.getSessionUserId(),
								AccountTypes.ACCT_TYPE_EXPENSE,
								CHelper.encrypt(row[0]));

				BudgetedAccount ba = new BudgetedAccount();
				ba.setAccountId(a.getAccountId());
				ba.setAllocatedAmount(new BigDecimal(row[1]));
				baSet.add(ba);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
				continue;
			}
		}
		b.setAccounts(baSet);
		try {
			em.addBudget(b);
			dispose();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public String[][] getValue(String panelName) {
		return values.get(panelName);
	}
}
