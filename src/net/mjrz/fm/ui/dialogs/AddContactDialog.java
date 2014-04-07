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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import net.mjrz.fm.actions.ActionRequest;
import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddContactAction;
import net.mjrz.fm.entity.beans.Contact;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.utils.SpringUtilities;
import net.mjrz.fm.utils.Messages;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddContactDialog extends JDialog {

	private static final long serialVersionUID = 1500565957108252341L;

	private JTextField[] tflist;
	private int curr = 0;
	private JButton save, cancel;
	private User user;
	private JTextArea notes, street;
	private Contact contact = null;
	private Component parent;

	public AddContactDialog(Window parent, User user) {
		super(parent, Messages.getString("Add contact")); //$NON-NLS-1$
		this.user = user;
		this.parent = parent;
		tflist = new JTextField[15];
		for (int i = 0; i < tflist.length; i++) {
			tflist[i] = new JTextField(20);
			net.mjrz.fm.ui.utils.GuiUtilities.setupTextComponent(tflist[i]);
			tflist[i].setMinimumSize(new Dimension(100, 25));
		}
		init();
	}

	public AddContactDialog(Window parent, User user, Contact c) {
		super(parent, Messages.getString("Edit contact: ") + c.getFullName()); //$NON-NLS-1$
		this.user = user;
		this.contact = c;

		tflist = new JTextField[15];
		for (int i = 0; i < tflist.length; i++) {
			tflist[i] = new JTextField(20);
			tflist[i].setPreferredSize(new Dimension(100, 20));
		}
		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1, 2, 5, 5));
		center.add(getLeftPanel());
		center.add(getRightPanel());
		center.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (contact != null)
			populateFields();
	}

	private void populateFields() {
		contact.setCreateDate(new Date());
		tflist[12].setText(contact.getAltEmail());
		tflist[5].setText(contact.getBusFax());
		tflist[3].setText(contact.getBusPhone());
		tflist[7].setText(contact.getCity());
		tflist[2].setText(contact.getCompany());
		tflist[10].setText(contact.getCountry());
		tflist[11].setText(contact.getEmail());
		tflist[0].setText(contact.getFullName());
		tflist[4].setText(contact.getHomePhone());
		tflist[14].setText(contact.getImAddr());
		tflist[1].setText(contact.getJobTitle());
		tflist[6].setText(contact.getMobPhone());
		notes.setText(contact.getNotes());
		tflist[9].setText(contact.getState());
		street.setText(contact.getStreet());
		tflist[13].setText(contact.getWebAddr());
		tflist[8].setText(contact.getZip());
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		save = new JButton(Messages.getString("Save")); //$NON-NLS-1$
		save.setMinimumSize(new Dimension(80, 20));
		save.setMnemonic(KeyEvent.VK_S);
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addContact();
				dispose();
			}
		});

		cancel = new JButton(Messages.getString("Close")); //$NON-NLS-1$
		cancel.setMinimumSize(new Dimension(80, 20));
		cancel.setMnemonic(KeyEvent.VK_C);
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		ret.add(Box.createHorizontalGlue());
		ret.add(save);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancel);
		ret.add(Box.createHorizontalGlue());

		return ret;
	}

	private JPanel getLeftPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		ret.add(getGeneralInfoPanel());
		ret.add(new JPanel());
		return ret;
	}

	private JPanel getRightPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));

		ret.add(getEmailInfoPanel());

		JPanel np = new JPanel();
		np.setLayout(new GridLayout(1, 1));
		np.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("Notes:"))); //$NON-NLS-1$

		notes = new JTextArea(10, 20);

		np.add(new JScrollPane(notes));

		ret.add(np);
		ret.add(new JPanel());
		ret.add(Box.createVerticalStrut(30));

		return ret;
	}

	private JPanel getGeneralInfoPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new SpringLayout());

		JLabel x = new JLabel("<html><font color=\"#000000\"><b>"
				+ Messages.getString("General") + ":</b></font></html>");

		ret.add(x);
		ret.add(new JLabel());

		ret.add(new JLabel(Messages.getString("Full name"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Job title"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Company"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		x = new JLabel("<html><font color=\"#000000\"><b>"
				+ Messages.getString("Phone numbers") + ":</b></font></html>");
		ret.add(x);
		ret.add(new JLabel());

		ret.add(new JLabel(Messages.getString("Business"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Home"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Business Fax"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Mobile"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		// 9 till now

		x = new JLabel("<html><font color=\"#000000\"><b>"
				+ Messages.getString("Address") + ":</b></font></html>");
		ret.add(x);
		ret.add(new JLabel());

		street = new JTextArea(3, 20);
		ret.add(new JLabel(Messages.getString("Street"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(new JScrollPane(street));

		ret.add(new JLabel(Messages.getString("City"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Zip"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("State"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Country"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		SpringUtilities.makeCompactGrid(ret, 15, 2, 2, 2, 2, 2);

		return ret;
	}

	private JPanel getEmailInfoPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new SpringLayout());

		JLabel x = new JLabel("<html><font color=\"#000000\"><b>"
				+ Messages.getString("Email") + ":</b></font></html>");

		ret.add(x);
		ret.add(new JLabel());

		ret.add(new JLabel(Messages.getString("Email"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(
				Messages.getString("Alternate Email"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("Web address"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		ret.add(new JLabel(Messages.getString("IM Address"), JLabel.LEADING)); //$NON-NLS-1$
		ret.add(tflist[curr++]);

		SpringUtilities.makeCompactGrid(ret, 5, 2, 2, 2, 2, 2);

		return ret;
	}

	private void addContact() {
		try {
			Contact c = new Contact();
			c.setCreateDate(new Date());
			c.setFullName(tflist[0].getText());
			c.setJobTitle(tflist[1].getText());
			c.setCompany(tflist[2].getText());
			c.setBusPhone(tflist[3].getText());
			c.setHomePhone(tflist[4].getText());
			c.setBusFax(tflist[5].getText());
			c.setMobPhone(tflist[6].getText());
			c.setCity(tflist[7].getText());
			c.setZip(tflist[8].getText());
			c.setState(tflist[9].getText());
			c.setCountry(tflist[10].getText());
			c.setEmail(tflist[11].getText());
			c.setAltEmail(tflist[12].getText());
			c.setWebAddr(tflist[13].getText());
			c.setImAddr(tflist[14].getText());
			c.setNotes(notes.getText());
			c.setStreet(street.getText());
			c.setUserId(SessionManager.getSessionUserId());

			ActionRequest req = new ActionRequest();
			req.setActionName("addContact"); //$NON-NLS-1$
			req.setProperty("CONTACT", c); //$NON-NLS-1$

			if (contact == null) {
				req.setProperty("EXISTING", false); //$NON-NLS-1$
			}
			else {
				req.setProperty("EXISTING", true); //$NON-NLS-1$
				c.setId(contact.getId());
			}

			AddContactAction action = new AddContactAction();
			ActionResponse resp = action.executeAction(req);
			if (resp.getErrorCode() == ActionResponse.NOERROR) {
				if (ViewContactsDialog.isInited())
					ViewContactsDialog.getInstance(parent, user).addToView(c,
							contact);
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					Messages.getString("Unable to save: ") + e.getMessage(),
					Messages.getString("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}
}
