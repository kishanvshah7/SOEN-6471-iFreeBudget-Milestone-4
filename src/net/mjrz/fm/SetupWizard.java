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
package net.mjrz.fm;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.CreateInitialAccounts;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.ui.panels.LoginPanel;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.wizards.WizardProgressPanel;
import net.mjrz.fm.ui.wizards.components.CurrPanel;
import net.mjrz.fm.ui.wizards.components.LangPanel;
import net.mjrz.fm.ui.wizards.components.LocationPanel;
import net.mjrz.fm.ui.wizards.components.PasswordPanel;
import net.mjrz.fm.ui.wizards.components.UsernamePanel;
import net.mjrz.fm.ui.wizards.components.WizardComponent;
import net.mjrz.fm.utils.LoginUtils;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class SetupWizard extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel buttonPanel;
	private WizardProgressPanel progressPanel;
	private JButton next, previous, cancel;

	private WizardComponent[] wizardComponents;

	private JPanel centerPanel;

	private HashMap<String, String[][]> values = null;

	private static final String[] PANELS = { "UsernamePanel", "PasswordPanel",
			"LocationPanel", "LangPanel", "CurrPanel" };

	private JLabel[] progressLabels;

	private int curr = 0;

	private static Logger logger = Logger
			.getLogger(SetupWizard.class.getName());

	private int width = 550;
	private int height = 400;

	public SetupWizard() {
		progressLabels = new JLabel[PANELS.length];
		progressLabels[0] = new JLabel(tr("1. Choose username"));
		progressLabels[1] = new JLabel(tr("2. Choose password"));
		progressLabels[2] = new JLabel(tr("3. Select location"));
		progressLabels[3] = new JLabel(tr("4. Select language"));
		progressLabels[4] = new JLabel(tr("5. Select currency"));

		wizardComponents = new WizardComponent[PANELS.length];
		wizardComponents[0] = new UsernamePanel();
		wizardComponents[1] = new PasswordPanel();
		wizardComponents[2] = new LocationPanel();
		wizardComponents[3] = new LangPanel();
		wizardComponents[4] = new CurrPanel();

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

		setTitle(UIDefaults.PRODUCT_TITLE + " - " + tr("Setup"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(width, height));
		setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				SetupWizard.class.getClassLoader().getResource(
						"icons/icon_money.png")));
		pack();
		center();
		setVisible(true);
	}

	private void center() {
		GraphicsConfiguration gc = this.getGraphicsConfiguration();
		Rectangle bounds = gc.getBounds();
		int x = (int) (bounds.getWidth() - this.getWidth()) / 2;
		int y = (int) (bounds.getHeight() - this.getHeight()) / 2;
		this.setLocation(x, y);
	}

	private JPanel getCenterPanel() {
		centerPanel = new JPanel();
		centerPanel.setLayout(new CardLayout());
		centerPanel.add((JPanel) wizardComponents[0], PANELS[0]);
		centerPanel.add((JPanel) wizardComponents[1], PANELS[1]);
		centerPanel.add((JPanel) wizardComponents[2], PANELS[2]);
		centerPanel.add((JPanel) wizardComponents[3], PANELS[3]);
		centerPanel.add((JPanel) wizardComponents[4], PANELS[4]);

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
		progressPanel.setPreferredSize(new Dimension(200, height));
		return progressPanel;
	}

	private JPanel getButtonPanel() {
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		next = new JButton(tr("Next") + " >");
		next.setActionCommand("next");
		next.addActionListener(this);
		next.setMnemonic(KeyEvent.VK_N);
		next.setPreferredSize(new Dimension(85, 25));

		previous = new JButton("< " + tr("Back"));
		previous.setActionCommand("back");
		previous.setMnemonic(KeyEvent.VK_B);
		previous.addActionListener(this);
		previous.setPreferredSize(new Dimension(85, 25));

		cancel = new JButton(tr("Cancel"));
		cancel.setActionCommand("cancel");
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(this);
		cancel.setPreferredSize(new Dimension(85, 25));

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
			cancel.setText("Finish");
			cancel.setMnemonic(KeyEvent.VK_F);
		}
		else {
			cancel.setActionCommand("cancel");
			cancel.setText("Cancel");
			cancel.setMnemonic(KeyEvent.VK_C);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("next")) {
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
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
			return;
		}

		if (cmd.equals("back")) {
			values.remove(PANELS[curr]);
			curr--;
			manageButtonState();

			CardLayout cl = (CardLayout) centerPanel.getLayout();
			cl.show(centerPanel, PANELS[curr]);
			wizardComponents[curr].setComponentFocus();

			boldenLabel(curr);
		}
		if (cmd.equals("cancel")) {
			logger.info("Shutting down HSQLDB");
			net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
			System.exit(0);
		}
		if (cmd.equals("finish")) {
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
		cancel.setEnabled(false);
		previous.setEnabled(false);
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			User user;
			Locale langSelected, currSelected;

			public Boolean doInBackground() throws Exception {
				try {
					SetupWizard.this.setCursor(Cursor
							.getPredefinedCursor(Cursor.WAIT_CURSOR));

					String uname = values.get("UsernamePanel")[0][1];

					String[] vals = values.get("PasswordPanel")[0];
					char[] pass = vals[1].toCharArray();

					String location = values.get("LocationPanel")[0][1];
					logger.info(location);

					boolean checkOk = Main.checkDataFiles(location, uname);
					if (!checkOk)
						return false;
					else {
						net.mjrz.fm.utils.crypto.CHelper.init(pass);
						user = Main.createInitialUser(location, uname, pass);

						vals = values.get("LangPanel")[0];
						String lang = vals[1];

						vals = values.get("CurrPanel")[0];
						String curr = vals[1];

						// langSelected = getLocaleFromCode(lang);
						// currSelected = getLocaleFromCode(curr);

						saveLocale("LANGUAGE.LOCALE", lang, false);
						saveLocale("CURRENCY.LOCALE", curr, true);

						return true;
					}
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
					return false;
				}

			}

			public void done() {
				try {
					boolean success = get();
					if (success) {
						String profile = values.get("UsernamePanel")[0][1];
						createInitialAccounts(user);
						// LoginUtils.initialize(user, profile, langSelected,
						// currSelected);
						LoginUtils.initialize(user, profile);
						SetupWizard.this.dispose();
					}
					else {
						logger.error("Failed to initialize");
						net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
						System.exit(0);
					}
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
					net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
					System.exit(0);
				}
			}
		};
		worker.execute();
	}

	private void createInitialAccounts(User user) {
		try {
			ActionRequest req = new ActionRequest();
			req.setActionName("createBasicAccounts");
			req.setUser(user);
			new CreateInitialAccounts().executeAction(req);
		}
		catch (Exception e) {
			System.out
					.println(Messages.getString("Exception: ") + e.getMessage()); //$NON-NLS-1$
		}
	}

	private void saveLocale(String propName, String locale, boolean append) {
		BufferedWriter out = null;
		try {
			String dir = ZProperties.getProperty("FMHOME") + Main.PATH_SEPARATOR + "conf"; //$NON-NLS-1$ //$NON-NLS-2$
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			File pfile = new File(f, "localesettings.properties"); //$NON-NLS-1$
			out = new BufferedWriter(new FileWriter(pfile, append));
			out.write(propName + "=" + locale);
			out.write("\r\n");
			ZProperties.replaceRuntimeProperty(propName, locale); //$NON-NLS-1$

		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception ignore) {
					ignore.printStackTrace();
				}
			}
		}
	}

	private Locale getLocaleFromCode(String code) {
		String language = "en";
		String country = "US";
		int pos = code.indexOf('_');
		if (pos >= 0) {
			language = code.substring(0, pos);
			country = code.substring(pos + 1);
		}

		Locale selected = new Locale(language, country);
		return selected;
	}
}
