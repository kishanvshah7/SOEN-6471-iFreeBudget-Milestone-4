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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.JTextComponent;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Prefs;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.panels.prefs.PreferencesPanel;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

public class GuiUtilities {
	private static Logger logger = Logger.getLogger(GuiUtilities.class
			.getName());

	public static void removeCustomMouseListener(JTextComponent tf) {
		MouseListener[] mlist = tf.getMouseListeners();
		for (int i = 0; i < mlist.length; i++) {
			if (mlist[i].getClass().getName()
					.equals("com.pagosoft.plaf.TextComponentPopupHandler")) {
				tf.removeMouseListener(mlist[i]);
				break;
			}
		}
	}

	public static void setupTextComponent(final JTextComponent textComponent) {
		removeCustomMouseListener(textComponent);
		final TextComponentPopup popup = new TextComponentPopup(textComponent);

		textComponent.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				mouseReleased(e);

			}

			public void mouseReleased(MouseEvent e) {
				if (!e.isPopupTrigger())
					return;
				popup.show(textComponent, e.getX(), e.getY());
			}
		});
		// textComponent.setBorder(BorderFactory.createLineBorder(UIDefaults.DEFAULT_COLOR));
	}

	@SuppressWarnings("serial")
	public static void addWindowClosingActionMap(final JDialog window) {
		ActionMap am = window.getRootPane().getActionMap();
		InputMap im = window.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				window.setVisible(false);
				window.dispose();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
	}

	public static boolean hasNimbusLAF() {
		LookAndFeelInfo[] laf = UIManager.getInstalledLookAndFeels();
		boolean hasNimbusLaf = false;
		for (int i = 0; i < laf.length; i++) {
			if (laf[i].getClassName().equals(
					"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
				hasNimbusLaf = true;
				break;
			}
		}
		return hasNimbusLaf;
	}

	public static void preloadUIPrefs() {
		try {
			FManEntityManager entityManager = new FManEntityManager();
			java.util.List<Object> prefs = entityManager.getObjects("Prefs",
					" propName like 'UIPrefs.%'");

			java.util.List<Prefs> columnPrefs = new ArrayList<Prefs>();
			if (prefs != null) {
				for (Object o : prefs) {
					Prefs p = (Prefs) o;
					if (p.getPropName().indexOf("UIPrefs.TxTableColumn.") >= 0) {
						columnPrefs.add(p);
					}
					else {
						ZProperties.replaceRuntimeProperty(p.getPropName(),
								p.getPropValue());
					}
				}
			}
			loadTableColPrefs(columnPrefs);
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private static void loadTableColPrefs(java.util.List<Prefs> prefs)
			throws Exception {
		if (prefs != null && prefs.size() == 5) {
			int sz = prefs.size();
			for (int i = 0; i < sz; i++) {
				Prefs p = (Prefs) prefs.get(i);
				String propName = p.getPropName();
				String propVal = p.getPropValue();
				if (propName != null && propVal != null) {
					ZProperties.replaceRuntimeProperty(propName, propVal);
				}
			}
		}
		else {
			FManEntityManager entityManager = new FManEntityManager();
			entityManager.deleteObject("Prefs",
					" propName like 'UIPrefs.TxTableColumn.%'");
			for (int i = 0; i < 5; i++) {
				int val = 16;
				if (i == 0) {
					val = 4;
				}
				else if (i == 1 || i == 2) {
					val = 45;
				}
				else if (i == 3 || i == 4) {
					val = 3;
				}

				Prefs p = new Prefs();
				p.setPropName("UIPrefs.TxTableColumn." + i);
				p.setPropValue(String.valueOf(val));
				entityManager.addObject("Prefs", p);
			}
		}
	}

	public static void saveUIPrefs() {
		try {
			FManEntityManager entityManager = new FManEntityManager();
			/* persist table widths */
			for (int i = 0; i < 5; i++) {
				String propName = "UIPrefs.TxTableColumn." + i;
				String propVal = ZProperties.getProperty(propName);
				if (propVal != null) {
					Prefs p = (Prefs) entityManager.getObject("Prefs",
							" propName = '" + propName + "'");
					if (p != null) {
						p.setPropValue(propVal);
						entityManager.updateObject("Prefs", p);
					}
				}
			}
			/* persist date format if set */
			String propVal = ZProperties
					.getProperty(PreferencesPanel.DATE_FORMAT_PROPERTY);
			if (propVal != null) {
				saveOrUpdate(entityManager,
						PreferencesPanel.DATE_FORMAT_PROPERTY, propVal);
			}
			/* persist split panel location if set */
			propVal = ZProperties.getProperty(PreferencesPanel.TX_SPLIT_LOC);
			if (propVal != null) {
				saveOrUpdate(entityManager, PreferencesPanel.TX_SPLIT_LOC,
						propVal);
			}
			/* persist last dir path if set */
			propVal = ZProperties.getProperty(PreferencesPanel.LAST_DIR_PATH);
			if (propVal != null) {
				saveOrUpdate(entityManager, PreferencesPanel.LAST_DIR_PATH,
						propVal);
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private static void saveOrUpdate(FManEntityManager entityManager,
			String propName, String propVal) throws Exception {
		if (propVal != null) {
			Prefs p = (Prefs) entityManager.getObject("Prefs", " propName = '"
					+ propName + "'");
			if (p != null) {
				p.setPropValue(propVal);
				entityManager.updateObject("Prefs", p);
			}
			else {
				Prefs pref = new Prefs();
				pref.setUid(SessionManager.getSessionUserId());
				pref.setPropName(propName);
				pref.setPropValue(propVal);
				entityManager.addObject("Prefs", pref);
			}
		}
	}
}
