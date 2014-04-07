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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ZDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JButton ok, cancel;
	public static final int EXIT = 1;
	public static final int LOGOUT = 2;

	private JFrame parent;
	private String message;

	public static final int MESSAGE = 1;
	public static final int ERROR = 2;

	public static final int YES = 1;
	public static final int NO = 2;

	private int type = 1;

	private int answer = 0;

	public static void showDialog(int type, String title, String msg,
			JFrame parent) {
		ZDialog d = new ZDialog(type, title, msg, parent);
		d.pack();
		d.setDialogFocus();
		d.setLocationRelativeTo(parent);
		d.setVisible(true);
	}

	private ZDialog(int type, String title, String msg, JFrame parent) {
		super(parent, title, true);
		this.type = type;
		this.parent = parent;
		this.message = msg;
		initialize();
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(5, 5));
		JLabel l = null;

		if (type == MESSAGE) {
			l = new JLabel(message, new net.mjrz.fm.ui.utils.MyImageIcon(
					"icons/dialog-information.png"), JLabel.LEADING);
		}
		else {
			l = new JLabel(message, new net.mjrz.fm.ui.utils.MyImageIcon(
					"icons/dialog-error.png"), JLabel.LEADING);
		}
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(5, 5));
		p.add(l, BorderLayout.CENTER);

		cp.add(p, BorderLayout.NORTH);

		ok = new JButton("Ok");
		ok.setPreferredSize(new Dimension(75, 20));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		JPanel s = new JPanel();
		s.setLayout(new BoxLayout(s, BoxLayout.LINE_AXIS));
		s.add(Box.createHorizontalGlue());
		s.add(ok);
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
}
