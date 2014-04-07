package net.mjrz.fm.ui.dialogs;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;

import net.mjrz.fm.entity.beans.types.EncryptionException;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.PwdUtils;

import org.apache.log4j.Logger;

public class PwdChangeDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPasswordField currPwd, newPwd, verifyNewPwd;
	private static Logger logger = Logger.getLogger(PwdChangeDialog.class
			.getName());
	private String user = null;
	private JButton ok, cancelButton;

	public PwdChangeDialog(JFrame parent, String user) {
		super(parent, tr("Change password - " + user), true);
		super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.user = user;
		initialize();
		GuiUtilities.addWindowClosingActionMap(this);
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());

		currPwd = new JPasswordField();
		newPwd = new JPasswordField();
		verifyNewPwd = new JPasswordField();

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.insets = new Insets(20, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weightx = 0.3;
		add(new JLabel(tr("Current password")), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 0;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weightx = 0.7;
		add(currPwd, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.weightx = 0;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		add(new JLabel(tr("New password")), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		add(newPwd, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 2;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		add(new JLabel(tr("Verify password")), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 2;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		add(verifyNewPwd, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 3;
		gbc1.gridwidth = 2;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		add(getButtonPanel(), gbc1);
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();

		ok = new JButton(tr("Ok"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Changed password for user: " + user);
				changePwd();
			}
		});
		super.getRootPane().setDefaultButton(ok);

		cancelButton = new JButton(tr("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PwdChangeDialog.this.dispose();
			}
		});
		ret.add(Box.createHorizontalGlue());
		ret.add(ok);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancelButton);

		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		ret.setBorder(BorderFactory.createTitledBorder(""));
		return ret;
	}

	private void changePwd() {
		final char[] curr = currPwd.getPassword();
		final char[] p1 = newPwd.getPassword();
		final char[] p2 = verifyNewPwd.getPassword();

		String valid = PwdUtils.validatePwdPolicy(p1, p2);
		if (valid != null) {
			JOptionPane.showMessageDialog(this, valid, tr("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		ok.setEnabled(false);
		cancelButton.setEnabled(false);

		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				boolean success = PwdUtils.changePassword(user, curr, p1);
				return success;
			}

			@Override
			public void done() {
				try {
					boolean success = get();
					if (!success) {
						JOptionPane.showMessageDialog(PwdChangeDialog.this,
								tr("Password change failed"), tr("Error"),
								JOptionPane.ERROR_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(PwdChangeDialog.this,
								tr("Password changed successfully"),
								tr("Success"), JOptionPane.INFORMATION_MESSAGE);
						PwdChangeDialog.this.dispose();
					}
				}
				catch (Exception e) {
					logger.error(MiscUtils.stackTrace2String(e));
					Throwable t = e.getCause();
					if (t instanceof EncryptionException) {
						JOptionPane
								.showMessageDialog(
										PwdChangeDialog.this,
										tr("Password change failed, please check your password"),
										tr("Error"), JOptionPane.ERROR_MESSAGE);
					}
					else {
						JOptionPane.showMessageDialog(PwdChangeDialog.this,
								tr("Password change failed"), tr("Error"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
				finally {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					ok.setEnabled(true);
					cancelButton.setEnabled(true);
				}
			}

		};
		worker.execute();
	}
}
