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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mjrz.fm.ui.FinanceManagerUI;

import org.apache.log4j.Logger;

public class ExitPromptDialog extends JDialog {
	private static Logger logger = Logger.getLogger(ExitPromptDialog.class
			.getName());
	public static final int EXIT = 1;
	public static final int LOGOUT = 2;
	public static final int YES = 3;
	public static final int NO = 4;

	private static final long serialVersionUID = 1L;
	private JButton ok, cancel;
	private JCheckBox cb;
	private int type = 0;
	private String userName = null;
	private FinanceManagerUI parent;
	private int response = NO;

	public ExitPromptDialog(int type, String username, FinanceManagerUI parent) {
		super(parent, "Confirm", true);
		this.type = type;
		this.userName = username;
		this.parent = parent;
		initialize();
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(5, 5));
		JLabel l = null;
		if (type == EXIT) {
			l = new JLabel(tr("Exit iFreeBudget?"),
					new net.mjrz.fm.ui.utils.MyImageIcon("icons/question.png"),
					JLabel.LEADING);
		}
		else {
			l = new JLabel(tr("Logout user ") + userName,
					new net.mjrz.fm.ui.utils.MyImageIcon("icons/question.png"),
					JLabel.LEADING);
		}

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(l, BorderLayout.CENTER);

		cp.add(p, BorderLayout.NORTH);

		ok = new JButton("Ok");
		ok.setPreferredSize(new Dimension(75, 25));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean checked = !cb.isSelected();
				String prop = "MAIN.EXITWITHPROMPT";
				String value = Boolean.toString(checked);
				try {
					net.mjrz.fm.utils.PropertiesUtils.saveSettings(
							"settings.properties", prop, value);
					response = YES;
					ExitPromptDialog.this.dispose();
				}
				catch (Exception ex) {
					logger.error(net.mjrz.fm.utils.MiscUtils
							.stackTrace2String(ex));
				}
			}
		});

		cancel = new JButton("Cancel");
		cancel.setPreferredSize(new Dimension(75, 25));
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				response = NO;
				ExitPromptDialog.this.dispose();
			}
		});

		cb = new JCheckBox("Always exit without prompt");

		JPanel s = new JPanel();
		s.setLayout(new BoxLayout(s, BoxLayout.LINE_AXIS));
		s.add(cb);
		s.add(Box.createHorizontalGlue());
		s.add(ok);
		s.add(Box.createHorizontalStrut(5));
		s.add(cancel);
		s.add(Box.createHorizontalStrut(5));

		cp.add(s, BorderLayout.SOUTH);
		ok.requestFocusInWindow();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 200));
	}

	public void setDialogFocus() {
		if (ok != null)
			ok.requestFocusInWindow();
	}

	public int getResponse() {
		return response;
	}
}
