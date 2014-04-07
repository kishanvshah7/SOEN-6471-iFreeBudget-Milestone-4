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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.ui.panels.prefs.DisplayPrefsPanel;
import net.mjrz.fm.ui.panels.prefs.PreferencesPanel;
import net.mjrz.fm.ui.utils.MyTabPaneUI;
import net.mjrz.fm.utils.ZProperties;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JButton closeB = null;
	private JButton saveB = null;
	private FinanceManagerUI parent = null;
	private JTabbedPane tabs = null;
	private PreferencesPanel[] panels = null;

	public PreferencesDialog(FinanceManagerUI parent) {
		super(parent, "Preferences");
		this.parent = (FinanceManagerUI) parent;
		initialize();
	}

	public void initialize() {
		panels = new PreferencesPanel[1];
		panels[0] = new DisplayPrefsPanel();

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		tabs = new JTabbedPane();
		tabs.setUI(new MyTabPaneUI());
		tabs.addTab("Display", (Component) panels[0]);

		cp.add(tabs, BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		setPreferredSize(new Dimension(500, 300));
	}

	private JPanel getButtonPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

		closeB = new JButton(tr("Close"));
		closeB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		saveB = new JButton(tr("Save"));
		saveB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveButtonActionPerformed(e);
			}
		});
		p.add(Box.createHorizontalGlue());
		p.add(saveB);
		p.add(Box.createHorizontalStrut(5));
		p.add(closeB);
		p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		return p;
	}

	private void doSaveButtonActionPerformed(ActionEvent e) {
		int sel = tabs.getSelectedIndex();
		if (sel >= 0) {
			Map<String, Object> prefs = panels[sel].getPreferences();
			Set<String> keys = prefs.keySet();
			for (String s : keys) {
				Object o = prefs.get(s);
				if (s.equals(PreferencesPanel.DATE_FORMAT_PROPERTY)) {
					ZProperties
							.replaceRuntimeProperty(
									PreferencesPanel.DATE_FORMAT_PROPERTY,
									o.toString());
					SessionManager
							.setDateFormat(ZProperties
									.getProperty(PreferencesPanel.DATE_FORMAT_PROPERTY));

					PageControlPanel instance = PageControlPanel
							.getInstance(parent);
					instance.makePageRequest(instance.getDefaultFilter());
				}
			}
		}
		dispose();
	}
}
