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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.mjrz.fm.ui.utils.SpringUtilities;
import net.mjrz.fm.utils.Credentials;
import net.mjrz.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AuthenticationDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -417923614522094916L;

	JTextField uname;
	JPasswordField passwd;
	JButton ok, cancel;

	private static AuthenticationDialog instance = null;

	Credentials credentials = null;

	ViewQuoteDialog parent = null;

	public synchronized static void getCredentials(JDialog parent,
			Credentials credentials) {
		instance = new AuthenticationDialog(parent, credentials);
		instance.pack();
		instance.setVisible(true);
		instance.setLocationRelativeTo(parent);
	}

	private AuthenticationDialog(JDialog parent, Credentials credentials) {
		super(parent);
		this.parent = (ViewQuoteDialog) parent;
		initialize();
		this.credentials = credentials;
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new SpringLayout());

		uname = new JTextField(20);
		passwd = new JPasswordField(20);

		top.add(new JLabel(
				Messages.getString("Proxy account username"), JLabel.TRAILING)); //$NON-NLS-1$
		top.add(uname);
		top.add(new JLabel(
				Messages.getString("Proxy account password"), JLabel.TRAILING)); //$NON-NLS-1$
		top.add(passwd);

		SpringUtilities.makeCompactGrid(top, 2, 2, 5, 5, 2, 2);

		cp.add(top, BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private JPanel getButtonPanel() {
		ok = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
		ok.setActionCommand("Ok");
		ok.setPreferredSize(new Dimension(100, 20));
		ok.addActionListener(this);

		cancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
		cancel.setActionCommand("Cancel");
		cancel.setPreferredSize(new Dimension(100, 20));
		cancel.addActionListener(this);

		JPanel r2 = new JPanel();
		r2.setLayout(new BoxLayout(r2, BoxLayout.X_AXIS));

		r2.add(Box.createHorizontalGlue());
		r2.add(ok);
		r2.add(Box.createHorizontalGlue());
		r2.add(cancel);
		r2.add(Box.createHorizontalGlue());

		r2.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		return r2;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Ok")) { //$NON-NLS-1$
			credentials.setId(uname.getText());
			credentials.setPassword(new String(passwd.getPassword()));
			dispose();
			parent.getQuotes();
		}
		else {
			dispose();
		}
	}
}
