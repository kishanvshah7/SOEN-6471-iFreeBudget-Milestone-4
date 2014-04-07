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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.mjrz.fm.entity.beans.Transaction;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.wizards.components.WizardComponent;
import net.mjrz.fm.ui.wizards.components.profileimport.BackupSelectPanel;
import net.mjrz.fm.ui.wizards.components.profileimport.DestinationSelectPanel;
import net.mjrz.fm.utils.ImportProfileUtil;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ImportProfileWizard extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private WizardProgressPanel progressPanel;
	private JButton next, previous, cancel;

	private WizardComponent[] wizardComponents;

	private JPanel centerPanel;

	private HashMap<String, String[][]> values = null;

	private static final String[] PANELS = { "SelectBkupPanel",
			"SelectDestPanel" };

	private JLabel[] progressLabels;

	private int curr = 0;

	private static Logger logger = Logger.getLogger(ImportProfileWizard.class
			.getName());

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public ImportProfileWizard(Window frame) {
		super(frame, "Import profile");

		progressLabels = new JLabel[PANELS.length];
		progressLabels[0] = new JLabel(tr("1. Select backup file"));
		progressLabels[1] = new JLabel(tr("2. Select location"));

		wizardComponents = new WizardComponent[PANELS.length];
		wizardComponents[0] = new BackupSelectPanel();
		wizardComponents[1] = new DestinationSelectPanel();

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

		setTitle(tr(UIDefaults.PRODUCT_TITLE + " - Import profile"));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(650, 350));
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				ImportProfileWizard.class.getClassLoader().getResource(
						"icons/icon_money.png")));
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private JPanel getCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setLayout(new CardLayout());
		centerPanel.add((JPanel) wizardComponents[0], PANELS[0]);
		centerPanel.add((JPanel) wizardComponents[1], PANELS[1]);

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
		progressPanel.setPreferredSize(new Dimension(200, 350));
		return progressPanel;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		next = new JButton(tr("Next") + " >");
		next.setActionCommand("next");
		next.addActionListener(this);
		next.setMnemonic(KeyEvent.VK_N);
		// next.setPreferredSize(new Dimension(85, 25));

		previous = new JButton("< " + tr("Back"));
		previous.setActionCommand("back");
		previous.setMnemonic(KeyEvent.VK_B);
		previous.addActionListener(this);
		// previous.setPreferredSize(new Dimension(85, 25));

		cancel = new JButton(tr("Cancel"));
		cancel.setActionCommand("cancel");
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(this);
		// cancel.setPreferredSize(new Dimension(85, 25));

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
			}
			else {
				fn = new Font(f.getFamily(), Font.PLAIN, f.getSize());
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

	private void createTransaction() {
		try {
			SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				Transaction t = null;

				public Boolean doInBackground() throws Exception {
					printValues();
					try {
						String[][] source = values.get("SelectBkupPanel");
						String[][] dest = values.get("SelectDestPanel");
						File sourceFile = new File(source[0][1]);
						File destFolder = new File(dest[0][1]);

						ImportProfileUtil ipu = new ImportProfileUtil(
								sourceFile, destFolder);
						ipu.importFile();
						return true;
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(ImportProfileWizard.this,
								tr("Error occured") + ": " + e.getMessage(),
								tr("Error"), JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}

				public void done() {
					try {
						boolean result = get();
						if (result) {
							dispose();
							ImportProfileWizard.this.firePropertyChange(
									"importProfile", false, true);
							// parent.reloadProfiles();
						}
					}
					catch (Exception e) {
						logger.info(e);
					}
				}
			};
			worker.execute();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					tr("Unable to save transaction") + e.getMessage(),
					tr("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void finish() {
		createTransaction();
	}

	public String[][] getValue(String panelName) {
		return values.get(panelName);
	}
}
