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
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.ifreebudget.web.service.response.UpdateCheckResponse;

import net.mjrz.fm.Version;
import net.mjrz.fm.ui.utils.MyTabPaneUI;
import net.mjrz.fm.ui.utils.SpringUtilities;
import net.mjrz.fm.ui.utils.UIDefaults;
import net.mjrz.fm.utils.UpdateCheck;
import net.mjrz.fm.utils.ZProperties;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AboutDialog extends JDialog implements ActionListener,
		MouseListener {
	static final long serialVersionUID = 1L;

	private JLabel l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12, banner;
	private JButton u, b;

	public AboutDialog(JFrame parent) {
		super(parent, "About", true);
		try {
			initialize();
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private void initialize() throws Exception {
		Version v = Version.getVersion();

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setUI(new MyTabPaneUI());

		JPanel center = new JPanel();
		center.setLayout(new SpringLayout());

		File f = new File(".");

		l1 = new JLabel("Version", JLabel.LEADING);
		l2 = new JLabel(v.toString());
		l3 = new JLabel("Java Version", JLabel.LEADING);
		l4 = new JLabel(System.getProperty("java.version"));
		l5 = new JLabel("Installation directory", JLabel.LEADING);
		l6 = new JLabel(f.getCanonicalPath());
		l7 = new JLabel("Data directory", JLabel.LEADING);
		l8 = new JLabel(ZProperties.getProperty("FMHOME"));
		l9 = new JLabel("Copyright(c) iFreeBudget.com All rights reserved.");
		l10 = new JLabel("Apache Software License Version 2.0");

		l11 = new JLabel("Web", JLabel.LEADING);
		l12 = new JLabel(
				"<html><a href=\"http://www.ifreebudget.net/\">http://www.ifreebudget.com/</a></html>");
		l12.addMouseListener(this);

		center.add(l1);
		center.add(l2);
		center.add(l3);
		center.add(l4);
		center.add(l5);
		center.add(l6);
		center.add(l7);
		center.add(l8);
		center.add(l11);
		center.add(l12);
		// center.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		SpringUtilities.makeCompactGrid(center, 5, 2, 5, 5, 10, 10);
		JPanel north = new JPanel();
		north.add(Box.createHorizontalGlue());
		banner = new JLabel("<html><font size=\"+1\" color=\"black\"><b>"
				+ UIDefaults.PRODUCT_TITLE + "</b></font></html>",
				JLabel.CENTER);
		north.add(banner);
		north.add(Box.createHorizontalGlue());
		cp.add(north, BorderLayout.NORTH);

		tabPane.addTab("General", center);
		tabPane.addTab("Translations", getTransPanel());
		tabPane.addTab("Credits", getCreditsPanel());
		tabPane.addTab("License", getLicensePanel());

		cp.add(tabPane, BorderLayout.CENTER);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		p.add(Box.createHorizontalGlue());

		u = new JButton("Check updates");
		u.setPreferredSize(new Dimension(150, 25));
		u.addActionListener(this);
		p.add(u);
		p.add(Box.createHorizontalGlue());

		b = new JButton("Close");
		b.setPreferredSize(new Dimension(150, 25));
		b.addActionListener(this);
		p.add(b);
		p.add(Box.createHorizontalGlue());

		JPanel c = new JPanel();
		p.setLayout(new FlowLayout());
		c.add(Box.createHorizontalGlue());
		c.add(l9);
		c.add(Box.createHorizontalGlue());

		JPanel s = new JPanel();
		s.setLayout(new SpringLayout());
		s.add(c);
		s.add(p);
		SpringUtilities.makeCompactGrid(s, 2, 1, 0, 0, 0, 0);
		cp.add(s, BorderLayout.SOUTH);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(600, 500));

		ActionMap am = getRootPane().getActionMap();
		InputMap im = getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		Object windowCloseKey = new Object();
		KeyStroke windowCloseStroke = KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0);
		Action windowCloseAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
		im.put(windowCloseStroke, windowCloseKey);
		am.put(windowCloseKey, windowCloseAction);
	}

	public JPanel getTransPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		String[] header = { "Translation", "Provided by" };
		String[][] rows = { { "English", "Mjrz (contact@mjrz.net)" },
				{ "French", "Meriadec (meriadec.michel@yahoo.fr)" },
				{ "German", "Tobias Jakobs (tobias.jakobs@googlemail.com)" },
				{ "Italian", "Massimo de Lucia (massimo@tiscali.it)" },
				{ "Italian", "Revised by Luca (luca@sogea.net)" },
				{ "Dutch", "TigZ (TigZ@tigz.com)" },
				{ "Catalan", "Josep Roca (jeproca@hotmail.com)" }, };

		JTable table = new JTable(rows, header);

		ret.add(new JScrollPane(table), BorderLayout.CENTER);
		return ret;
	}

	public JPanel getCreditsPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		String[] creditsList = {
				"Hibernate (http://www.hibernate.org/)",
				"HSQLDB (http://www.hsqldb.org/)",
				"log4j, Apache commons (http://www.apache.org)",
				"Icons (http://www.famfamfam.com/)",
				"Icons (www.MouseRunner.com)",
				"Icons (http://gnome-look.org/content/show.php?content=117931)",
				"Testing (Claire Nguyen, ctn@users.sourceforge.net)",
				"Documentation (Anindita Basu, ab.techwriter@gmail.com)",
				"Bug fixes - (Julie Sparrow, juliesparrow22@gmail.com)" };

		JList list = new JList(creditsList);

		ret.add(new JScrollPane(list), BorderLayout.CENTER);
		return ret;
	}

	public JPanel getLicensePanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());
		JTextArea txt = new JTextArea(30, 80);
		txt.setEditable(false);
		StringBuffer license = new StringBuffer("");
		BufferedReader in = null;
		try {
			File f = new File("LICENSE.txt");
			in = new BufferedReader(new FileReader(f));
			while (true) {
				String s = in.readLine();
				if (s == null)
					break;
				license.append(s);
				license.append("\r\n");
			}
		}
		catch (FileNotFoundException e) {
			license.append("Apache software license v2.0\r\nCopyright 2007 Mjrz.net");
		}
		catch (IOException e) {
			license.append("Apache software license v2.0\r\nCopyright 2007 Mjrz.net");
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
				}
			}
		}
		txt.setText(license.toString());
		txt.setCaretPosition(0);
		ret.add(new JScrollPane(txt));
		return ret;
	}

	private void checkForUpdates() throws Exception {
		try {
			boolean updtReq = UpdateCheck.updateCheck();
			if (updtReq) {
				int n = JOptionPane
						.showConfirmDialog(
								this,
								"A updated version is available\nDo you want to download the latest version?",
								"Message", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);

				if (n == JOptionPane.YES_OPTION) {
					java.awt.Desktop d = Desktop.getDesktop();
					if (Desktop.isDesktopSupported()) {
						d.browse(new URI("http://www.ifreebudget.com/dl.html"));
					}
				}
			}
			else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(AboutDialog.this,
								"There are no new updates");
					}
				});
			}
		}
		catch (Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane
							.showMessageDialog(
									AboutDialog.this,
									"Unable to get latest version, Please check network connection",
									"Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("Close")) {
			dispose();
		}
		if (cmd.equals("Check updates")) {
			u.setEnabled(false);
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					try {
						checkForUpdates();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

				protected void done() {
					u.setEnabled(true);
				}
			};
			worker.execute();
		}
	}

	public void mouseClicked(MouseEvent e) {
		try {
			java.awt.Desktop d = Desktop.getDesktop();
			if (Desktop.isDesktopSupported()) {
				d.browse(new URI(UIDefaults.PRODUCT_URL));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
