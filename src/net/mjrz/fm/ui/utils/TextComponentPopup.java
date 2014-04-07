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

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

public class TextComponentPopup extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private JTextComponent textComponent;
	private JMenuItem cut, copy, paste, selectAll;
	private Insets menuInset = null;

	public TextComponentPopup(JTextComponent component) {
		LookAndFeel laf = UIManager.getLookAndFeel();
		if (laf.getClass().getName()
				.equals("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel")) {
			menuInset = new Insets(1, -10, 1, 1);
		}
		else {
			menuInset = new Insets(1, 1, 1, 1);
		}
		textComponent = component;
		initialize();
	}

	private boolean hasSelectedText() {
		String txt = textComponent.getSelectedText();
		if (txt == null || txt.length() == 0) {
			return false;
		}
		return true;
	}

	private void initialize() {
		cut = new JMenuItem("Cut", new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/cut.png"));
		cut.setMargin(menuInset);
		cut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textComponent.cut();
			}
		});
		add(cut);

		copy = new JMenuItem("Copy", new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/copy.png"));
		copy.setMargin(menuInset);
		copy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textComponent.copy();
			}
		});
		add(copy);

		paste = new JMenuItem("Paste", new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/paste.png"));
		paste.setMargin(menuInset);
		paste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textComponent.paste();
			}
		});
		add(paste);

		add(new JSeparator(JSeparator.HORIZONTAL));

		selectAll = new JMenuItem("Select All");
		selectAll.setMargin(menuInset);
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textComponent.requestFocusInWindow();
				textComponent.selectAll();
			}
		});
		add(selectAll);
		pack();
	}

	public void show(Component invoker, int x, int y) {
		super.show(invoker, x, y);
		if (!hasSelectedText()) {
			cut.setEnabled(false);
			copy.setEnabled(false);
		}
		else {
			cut.setEnabled(true);
			copy.setEnabled(true);
		}

		if (!textComponent.isEditable()) {
			cut.setEnabled(false);
			paste.setEnabled(false);
		}
	}
}
