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

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import net.mjrz.fm.entity.beans.types.EString;
import net.mjrz.fm.search.newfilter.Filter;
import net.mjrz.fm.search.newfilter.OperatorType;
import net.mjrz.fm.search.newfilter.Predicate;
import net.mjrz.fm.search.newfilter.RelationType;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.panels.PageControlPanel;
import net.mjrz.fm.utils.Messages;

import org.apache.log4j.Logger;

public class SearchComponent extends JToolBar {
	private static final long serialVersionUID = 1L;

	private JButton ok, close;

	private JFormattedTextField param;

	private FinanceManagerUI parent;

	private DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();
	private MaskFormatter dateFormatter = null;
	private MaskFormatter textFormatter = null;

	private static Logger logger = Logger.getLogger(SearchComponent.class
			.getName());

	public SearchComponent(FinanceManagerUI p) {
		this.parent = p;
		initialize();
		this.setMaximumSize(new Dimension(500, 25));
	}

	private void initialize() {
		add(Box.createHorizontalStrut(5));

		add(new JLabel("Find "));

		add(Box.createHorizontalStrut(5));

		param = new JFormattedTextField();
		param.setPreferredSize(new Dimension(250, 25));
		GuiUtilities.removeCustomMouseListener(param);
		param.setFormatterFactory(formatterFactory);
		param.addKeyListener(new KeyEventHandler());
		add(param);

		add(Box.createHorizontalStrut(5));

		ok = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon("icons/view.png"));
		ok.setActionCommand("ok");
		ok.addActionListener(new ButtonHandler());

		close = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/exit.png"));
		close.setActionCommand("close");
		close.addActionListener(new ButtonHandler());

		setFloatable(false);
		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		add(ok);
		add(close);

		add(Box.createHorizontalGlue());
	}

	private Filter buildFilter(String aname) {
		aname = aname.toLowerCase();
		Filter f = new Filter("TT", null);

		List<String> sel = new ArrayList<String>();
		sel.add("accountId");

		Predicate p1 = Predicate.create("lower(fromName)", aname, RelationType.LIKE,
				String.class.getName());

		Predicate p2 = Predicate.create("lower(toName)", aname, RelationType.LIKE,
				String.class.getName());

		Predicate p3 = Predicate.create("lower(txNotes)", aname, RelationType.LIKE,
				String.class.getName());
		
		f.addPredicate(p1, OperatorType.OR);
		f.addPredicate(p2, OperatorType.OR);
		f.addPredicate(p3, OperatorType.AND);

		return f;
	}

	public void undisplay() {
		if (param.isFocusOwner())
			setVisible(false);
	}

	public void display() {
		setVisible(true);
		param.setText("");
		param.requestFocusInWindow();
	}

	class KeyEventHandler implements KeyListener {
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ENTER) {
				String s = param.getText();
				if (s == null || s.trim().length() == 0)
					return;
				String valid = validateString(s);
				if (valid == null)
					executeFilter(s.trim());
				else
					JOptionPane.showMessageDialog(parent, valid,
							Messages.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

	}

	private void executeFilter(String param) {
		Filter f = buildFilter(param);
		if (f == null) {
			JOptionPane op = FinanceManagerUI.getNarrowOptionPane(80);
			op.setMessageType(JOptionPane.ERROR_MESSAGE);
			
			op.setMessage(tr("Failed to search for parameters : " + param));

			JDialog dialog = op.createDialog(parent,
					Messages.getString("Error")); //$NON-NLS-1$
			dialog.pack();
			dialog.setVisible(true);
			return;
		}
		else {
			try {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				PageControlPanel.getInstance(parent).makePageRequest(f);
				// parent.executeFilter(f);
			}
			catch (Exception ex) {
				logger.error(ex.getMessage());
			}
			finally {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private String validateString(String val) {
		return null;
	}

	class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("ok")) {
				String s = param.getText();
				if (s == null || s.trim().length() == 0)
					return;
				String valid = validateString(s);
				if (valid == null)
					executeFilter(s.trim());
				else
					JOptionPane.showMessageDialog(parent, valid,
							Messages.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
			}
			if (cmd.equals("close")) {
				setVisible(false);
			}
		}
	}
}
