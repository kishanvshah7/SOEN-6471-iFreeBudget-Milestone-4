package net.mjrz.fm.ui.panels;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.mjrz.fm.actions.ActionResponse;
import net.mjrz.fm.actions.AddAccountAction;
import net.mjrz.fm.constants.AccountTypes;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.services.SessionManager;
import net.mjrz.fm.ui.wizards.components.transaction.FromAccountPanel;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class NewAccountPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField acctNameTf, startingBalTf;
	private JComboBox categoryCb;
	private AccountCategory[] categories;
	private JButton ok, cancel;
	private JDialog parent;
	private ActionResponse result = null;
	private JLabel errMsg = null;

	public NewAccountPanel(JDialog parent, AccountCategory defaultCategory) {
		super();
		this.parent = parent;
		initialize();
		if (defaultCategory != null) {
			categoryCb.setSelectedItem(defaultCategory);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					acctNameTf.requestFocusInWindow();
				}
			});
		}
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		try {
			List<AccountCategory> cList = FManEntityManager.getRootCategoies();
			int sz = cList.size();
			categories = new AccountCategory[sz];
			for (int i = 0; i < sz; i++) {
				categories[i] = cList.get(i);
			}
		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(MiscUtils.stackTrace2String(e));
		}
		acctNameTf = new JTextField();
		acctNameTf.requestFocusInWindow();

		startingBalTf = new JTextField();
		startingBalTf.setText("0.0");

		categoryCb = new JComboBox(categories);

		errMsg = new JLabel();
		errMsg.setForeground(Color.red);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(new JLabel(tr("Category")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(categoryCb, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(new JLabel(tr("Name")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(acctNameTf, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(new JLabel(tr("Starting balance")), gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.insets = new Insets(10, 10, 10, 10);
		add(startingBalTf, gbc);

		// gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		// gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		add(getButtonPanel(), gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridwidth = 2;
		add(errMsg, gbc);
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		ok = new JButton(tr("Save"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AccountCategory cat = (AccountCategory) categoryCb
						.getSelectedItem();
				String name = acctNameTf.getText();
				String sBal = startingBalTf.getText();

				if (name == null || name.trim().length() == 0) {
					return;
				}
				name = name.trim();
				BigDecimal sb = null;
				try {
					sb = new BigDecimal(sBal);
				}
				catch (Exception ex) {
					Logger.getLogger(FromAccountPanel.class).error(ex);
					sb = new BigDecimal(0);
				}
				createNewAccount(cat, name, sb);
			}
		});

		cancel = new JButton(tr("Cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});
		parent.getRootPane().setDefaultButton(ok);

		ret.add(Box.createHorizontalGlue());
		ret.add(ok);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(cancel);
		ret.add(Box.createHorizontalStrut(10));
		return ret;
	}

	private void createNewAccount(AccountCategory cat, String name,
			BigDecimal sb) {
		Account a = new Account();
		a.setAccountName(name);
		a.setAccountType(cat.getCategoryId().intValue());
		a.setAccountNotes("<change me>");
		a.setCategoryId(cat.getCategoryId());
		a.setStartDate(new java.util.Date());
		a.setStartingBalance(sb);
		a.setCurrentBalance(sb);

		a.setStatus(AccountTypes.ACCOUNT_ACTIVE);

		AddAccountAction action = new AddAccountAction();

		User user = new User();
		user.setUid(SessionManager.getSessionUserId());
		try {
			errMsg.setText("");
			result = action.executeAction(user, a);
			if (result.getErrorCode() != ActionResponse.NOERROR) {
				errMsg.setText("<html><p>" + result.getErrorMessage()
						+ "</p></html>");
			}
			else {
				parent.dispose();
			}
			// errMsg.setToolTipText(errMsg.getText());
		}
		catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
		}
	}

	public ActionResponse getResult() {
		return result;
	}
}
