package net.mjrz.fm.ui.panels;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import net.mjrz.fm.SetupWizard;
import net.mjrz.fm.entity.beans.User;
import net.mjrz.fm.entity.utils.HibernateUtils;
import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.dialogs.LangChangeDialog;
import net.mjrz.fm.ui.dialogs.PwdChangeDialog;
import net.mjrz.fm.ui.utils.MyImageIcon;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.ui.widgets.GradientLabel;
import net.mjrz.fm.ui.wizards.ImportProfileWizard;
import net.mjrz.fm.utils.BackupProfileUtil;
import net.mjrz.fm.utils.LoginUtils;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.Profiles;
import net.mjrz.fm.utils.ZProperties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.exception.JDBCConnectionException;

public class LoginPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JComboBox profileField = null;
	private JButton loginButton, cancelButton;
	private JPasswordField pwdField = null;
	private JLabel userLbl = null;
	private JPanel optionsPanel = null;
	private JLabel optionsLbl = null;
	private JPanel glass = null;
	private JProgressBar pbar = null;
	private JComboBox langCb = null;
	private JLabel messageLbl;
	private final int W = 450;
	private final int H = 300;
	private static final Logger logger = Logger.getLogger(LoginPanel.class);

	public LoginPanel() {
		super();
		super.setPreferredSize(new Dimension(W, H));
		initialize();
	}

	private void initialize() {
		super.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weighty = 0.5;
		gbc1.weightx = 1;
		add(getTitlePanel(), gbc1);

		gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.NORTH;
		gbc1.weighty = 0.5;
		add(getUserPanel(), gbc1);
	}

	private JPanel getTitlePanel() {
		JPanel ret = new JPanel(new GridBagLayout());

		JLabel l = new GradientLabel(" iFreeBudget", JLabel.LEADING);
		l.setOpaque(true);
		l.setBackground(UIDefaults.DEFAULT_COLOR);
		l.setForeground(Color.white);
		Font f = l.getFont().deriveFont(Font.BOLD, 24);
		l.setFont(f);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 1;
		gbc1.weighty = 1;
		gbc1.insets = new Insets(1, 1, 1, 1);
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(l, gbc1);

		// ret.setBackground(net.mjrz.fm.ui.utils.UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);

		// ret.setBorder(BorderFactory.createLineBorder(Color.gray));
		return ret;
	}

	private JPanel getUserPanel() {
		JPanel ret = new JPanel(new GridBagLayout());

		String[] profiles = this.getProfiles();
		Collections.sort(Arrays.asList(profiles));
		profileField = new JComboBox(profiles);
		// profileField.setEditable(true);

		userLbl = new JLabel("Enter profile name and password");
		boldenFont(userLbl);

		pwdField = new JPasswordField();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				pwdField.requestFocusInWindow();
			}
		});
		pwdField.requestFocusInWindow();

		optionsPanel = getOptionsPanel();
		
		JButton openProfileButton = new JButton(new MyImageIcon("icons/folder_explore.png"));
		System.out.println(openProfileButton.getMinimumSize());
		openProfileButton.setPreferredSize(new Dimension(25, 25));
		openProfileButton.setToolTipText(tr("Select profile folder"));
		openProfileButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				openProfileButtonActionPerformed(e);
			}
		});

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.gridwidth = 2;
		ret.add(userLbl, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(1, 15, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.gridwidth = 1;
		ret.add(new JLabel("Name"), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(1, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.weightx = 1;
		ret.add(profileField, gbc1);

//
		gbc1.gridx = 2;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(1, 0, 10, 10);
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.weightx = 0;
		ret.add(openProfileButton, gbc1);
//		
		
		gbc1.gridx = 0;
		gbc1.gridy = 2;
		gbc1.insets = new Insets(1, 15, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.weightx = 0;
		ret.add(new JLabel("Password"), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 2;
		gbc1.insets = new Insets(1, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.WEST;
		ret.add(pwdField, gbc1);

		optionsLbl = new JLabel("+  Options");
		optionsLbl.setOpaque(true);
		optionsLbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				LoginPanel.this.boldenFont(optionsLbl);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				LoginPanel.this.unBoldenFont(optionsLbl);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setOptionsPanelVisibility();
			}
		});
		optionsLbl.setHorizontalAlignment(JLabel.LEADING);

		gbc1.gridx = 0;
		gbc1.gridy = 3;
		gbc1.insets = new Insets(1, 20, 10, 10);
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.EAST;
		gbc1.gridwidth = 2;
		ret.add(getButtonPanel(), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 4;
		gbc1.insets = new Insets(10, 20, 10, 10);
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.gridwidth = 2;
		ret.add(optionsLbl, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 5;
		gbc1.insets = new Insets(1, 20, 10, 20);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.gridwidth = 2;
		ret.add(optionsPanel, gbc1);

		return ret;
	}

	private JPanel getOptionsPanel() {
		JPanel ret = new JPanel(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();

		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.insets = new Insets(0, 10, 10, 10);
		gbc1.anchor = GridBagConstraints.WEST;
		gbc1.weightx = 1;
		ret.add(new HyperLinkLabel("Language",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/bell.png")), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.insets = new Insets(0, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new HyperLinkLabel("Change password",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/lock.png")), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 2;
		gbc1.insets = new Insets(0, 0, 0, 0);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new JSeparator(), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 3;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new HyperLinkLabel("New profile",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/application.png")),
				gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 4;
		gbc1.insets = new Insets(0, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new HyperLinkLabel(
				"Import profile",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/import_profile.png")),
				gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 5;
		gbc1.insets = new Insets(0, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new HyperLinkLabel("Backup profile",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/save.png")), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 6;
		gbc1.insets = new Insets(0, 0, 0, 0);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new JSeparator(), gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 7;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.PAGE_START;
		ret.add(new HyperLinkLabel("Delete profile",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/sell.png")), gbc1);

		Dimension d = this.getPreferredSize();
		d.setSize(d.getWidth(), 185);
		ret.setPreferredSize(d);
		ret.setVisible(false);

		return ret;
	}

	private void openProfileButtonActionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			logger.info("Adding profile : " + f.getName() + " ; path : "
					+ f.getAbsolutePath());
			boolean isValid = validateProfileDirectory(f);
			if(isValid) {
				Profiles.getInstance().addProfile(f.getName(), f.getAbsolutePath());
				reloadProfiles();
			}
		}
	}
	
	private boolean validateProfileDirectory(File dir) {
		if(dir == null || !dir.isDirectory()) {
			return false;
		}
		String[] toCheck = { "data.data", "data.properties", "data.script"};
		int checkCount = toCheck.length;
		File[] flist = dir.listFiles();
		for(File f : flist) {
			if(f.isDirectory()) {
				continue;
			}
			for(String s : toCheck) {
				if(s.equalsIgnoreCase(f.getName())) {
					checkCount--;
				}
			}
		}
		return checkCount == 0;
	}

	private void animateResize(Window w, int width, int curr, int dest) {
		// int steps = (dest - curr) / 10;
		// for (int i = 0; i < Math.abs(steps); i++) {
		// if (steps > 0) {
		// curr += 10;
		// }
		// else {
		// curr -= 10;
		// }
		// Dimension d = new Dimension(width, curr);
		// this.setPreferredSize(d);
		// this.revalidate();
		// w.pack();
		// repaint();
		// }

		Dimension d = new Dimension(width, dest);
		this.setPreferredSize(d);
		this.revalidate();
		w.pack();
		repaint();
	}

	boolean toggle = false;

	private void setOptionsPanelVisibility() {
		if (optionsPanel == null)
			return;
		toggle = !toggle;

		Dimension d = getPreferredSize();

		int oldH = (int) d.getHeight();

		if (toggle) {
			optionsLbl.setText("-  Options");
			d.setSize(d.getWidth(), d.getHeight()
					+ optionsPanel.getPreferredSize().getHeight());
		}
		else {
			optionsLbl.setText("+  Options");
			d.setSize(d.getWidth(), d.getHeight()
					- optionsPanel.getPreferredSize().getHeight());
		}

		optionsPanel.setVisible(toggle);
		// Window w = SwingUtilities.getWindowAncestor(this);

		Window w = (Window) getTopLevelAncestor();
		animateResize(w, (int) d.getWidth(), (int) oldH, (int) d.getHeight());
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				login();
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Shutting down...");
				shutdown();
			}
		});

		messageLbl = new JLabel();

		ret.add(messageLbl);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(loginButton);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancelButton);

		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		return ret;
	}

	private void boldenFont(Component l) {
		Font f = l.getFont().deriveFont(Font.BOLD);
		l.setFont(f);
	}

	private void unBoldenFont(Component l) {
		Font f = l.getFont().deriveFont(Font.PLAIN);
		l.setFont(f);
	}

	private void handleCommand(String cmd) {
		if (cmd.equalsIgnoreCase("Language")) {
			String sel = (String) profileField.getSelectedItem();
			try {
				JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(
						JFrame.class, this);
				LangChangeDialog d = new LangChangeDialog(f, sel);
				d.pack();
				d.setSize(new Dimension(300, 150));
				d.setLocationRelativeTo(this);
				d.setVisible(true);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
		if (cmd.equalsIgnoreCase("New profile")) {
			SwingUtilities.getWindowAncestor(this).dispose();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					new SetupWizard();
				}
			});
		}
		else if (cmd.equalsIgnoreCase("Change password")) {
			String sel = (String) profileField.getSelectedItem();
			try {
				JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(
						JFrame.class, this);
				PwdChangeDialog d = new PwdChangeDialog(f, sel);
				d.pack();
				d.setSize(new Dimension(400, 250));
				d.setLocationRelativeTo(this);
				d.setVisible(true);
			}
			catch (Exception e) {
				logger.error(MiscUtils.stackTrace2String(e));
			}
		}
		else if (cmd.equals("Backup profile")) {
			String sel = (String) profileField.getSelectedItem();
			if (sel != null) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int val = fc.showSaveDialog(this);
				if (val == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();
					try {
						BackupProfileUtil bpu = new BackupProfileUtil(sel, f);
						bpu.createBackup();
					}
					catch (Exception ex) {
						logger.error(MiscUtils.stackTrace2String(ex));
						JOptionPane
								.showMessageDialog(
										this,
										tr("Creating backup failed. Please see log file for details"),
										tr("Error"), JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else {
				JOptionPane.showMessageDialog(this,
						tr("Please select a profile"), tr("Error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (cmd.equals("Import profile")) {
			ImportProfileWizard d = new ImportProfileWizard(
					SwingUtilities.getWindowAncestor(this));
			d.pack();
			d.setLocationRelativeTo(this);
			d.setVisible(true);
			d.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					LoginPanel.this.reloadProfiles();
				}
			});
		}
		else if (cmd.equals("Delete profile")) {
			String sel = (String) profileField.getSelectedItem();
			if (sel != null) {
				int n = JOptionPane
						.showConfirmDialog(
								this,
								"You are about to delete all data and settings for this user.\nAre you sure?",
								"Delete user", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION)
					deleteProfile(sel);
			}
			else {
				JOptionPane.showMessageDialog(this,
						tr("Please select a profile"), tr("Error"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deleteProfile(String name) {
		try {
			String homedir = Profiles.getInstance().getPathForProfile(name);
			File f = new File(homedir.toString());
			FileUtils.deleteDirectory(f);
			Profiles.getInstance().deleteProfile(name);
			reloadProfiles();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	private void reloadProfiles() {
		String[] profiles = getProfiles();
		profileField.removeAllItems();
		for (String p : profiles) {
			profileField.addItem(p);
		}
	}

	class HyperLinkLabel extends JLabel {
		private static final long serialVersionUID = 1L;
		private String labelText = null;

		HyperLinkLabel(String text, ImageIcon icon) {
			super("<html><body><a href=#>" + text + "</a></body></html>");
			super.setIcon(icon);
			this.labelText = text;
			super.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					handleCommand(labelText);
				}
			});
		}
	}

	private String[] getProfiles() {
		return Profiles.getInstance().getProfileNames();
	}

	public static void center(Window w) {
		GraphicsConfiguration gc = w.getGraphicsConfiguration();
		Rectangle bounds = gc.getBounds();
		int x = (int) (bounds.getWidth() - w.getWidth()) / 2;
		int y = (int) (bounds.getHeight() - w.getHeight()) / 2 - (185);
		w.setLocation(x, y);
	}

	private void initializeGlassPane() {
		if (glass != null)
			return;

		JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class,
				this);
		Container o = this.getParent();
		while (true) {
			o = o.getParent();
			if (o == null)
				break;
		}
		if (f != null) {
			glass = (JPanel) f.getGlassPane();
			glass.addMouseListener(new MouseAdapter() {
			});
			this.configureGlassPane(glass);
		}
	}

	int numTries = 0;

	private void setStatusFields(final boolean status) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				glass.setVisible(status);
				pbar.setIndeterminate(status);
			}
		});
	}

	private void shutdown() {
		net.mjrz.fm.entity.utils.HibernateUtils.shutdownHsql();
		System.exit(0);
	}

	private void login() {
		initializeGlassPane();
		if (++numTries >= 5) {
			return;
		}
		if (pwdField.getPassword() == null
				|| pwdField.getPassword().length == 0) {
			return;
		}
		SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
			public User doInBackground() throws JDBCConnectionException,
					Exception {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					setStatusFields(true);

					String user = (String) profileField.getSelectedItem();
					char[] pass = pwdField.getPassword();

					logger.info("User login : " + user + " ; num attempts : " + numTries);
					User u = LoginUtils.login(user, pass);
					if (u == null) {
						ZProperties.removeRuntimeProperty("FMUSER");
					}
					return u;
				}
				catch (JDBCConnectionException e) {
					throw e;
				}
				catch (Exception e) {
					throw e;
				}
				finally {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}

			public void done() {
				try {
					User user = get();
					String prof = (String) profileField.getSelectedItem();
					if (user != null) {
						try {
							LoginUtils.initialize(user, prof);
							SwingUtilities.getWindowAncestor(LoginPanel.this)
									.dispose();
						}
						catch (Exception e) {
							logger.error(MiscUtils.stackTrace2String(e));
							shutdown();
						}
					}
					else {
						Toolkit.getDefaultToolkit().beep();
						HibernateUtils.shutdownHsql();
					}
				}
				catch (JDBCConnectionException e) {
					updateStatusOnException(e);
				}
				catch (Exception e) {
					updateStatusOnException(e);
				}
				finally {
					pwdField.setText("");
					setStatusFields(false);
				}
			}
		};
		worker.execute();
	}

	private void updateStatusOnException(Exception e) {
		Throwable t = e.getCause();
		try {
			if (t != null && t.getMessage() != null) {
				if (t instanceof JDBCConnectionException) {
					t = (JDBCConnectionException) t;
					SQLException sqle = ((JDBCConnectionException) t)
							.getSQLException();
					if (sqle.getSQLState().equals("08001")) {
						messageLbl.setText("Cannot login, database in use");
						messageLbl.setForeground(Color.red);
					}
				}
				else {
					messageLbl.setText("Login failed");
					messageLbl.setForeground(Color.red);
					Toolkit.getDefaultToolkit().beep();
				}
			}
			else {
				Toolkit.getDefaultToolkit().beep();
			}
		}
		finally {
			HibernateUtils.shutdownHsql();
		}
	}

	private void configureGlassPane(JPanel panel) {
		pbar = new JProgressBar(0, 100);
		panel.setLayout(new GridBagLayout());
		// panel.add(pbar, BorderLayout.CENTER);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.anchor = GridBagConstraints.CENTER;
		panel.add(new JLabel("Please wait..."), gbc1);

		gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.fill = GridBagConstraints.NONE;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.anchor = GridBagConstraints.CENTER;
		panel.add(pbar, gbc1);

		panel.setOpaque(true);
	}

	public static void getLoginFrame() {
		final JFrame f = new JFrame(UIDefaults.PRODUCT_TITLE + " - "
				+ Messages.getString("Login"));
		final LoginPanel p = new LoginPanel();
		f.add(p);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getRootPane().setDefaultButton(p.loginButton);
		center(f);

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
				0, false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				logger.info("Shutting down...");
				p.shutdown();
			}
		};

		f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(escapeKeyStroke, "ESCAPE");
		f.getRootPane().getActionMap().put("ESCAPE", escapeAction);
		f.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(
				LoginPanel.class.getClassLoader().getResource(
						"icons/icon_money.png")));
	}

	// public static void main(String args[]) {
	// try {
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	// }
	// catch (Exception e) {
	// }
	//
	// SwingUtilities.invokeLater(new Runnable() {
	// public void run() {
	// JFrame f = new JFrame();
	// LoginPanel p = new LoginPanel();
	// f.add(p);
	// f.pack();
	// f.setVisible(true);
	// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// center(f);
	// }
	// });
	// }
}
