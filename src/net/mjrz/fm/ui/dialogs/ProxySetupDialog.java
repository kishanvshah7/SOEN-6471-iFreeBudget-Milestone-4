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
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import net.mjrz.fm.Main;
import net.mjrz.fm.utils.Messages;
import net.mjrz.fm.utils.ZProperties;

import org.apache.log4j.Logger;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class ProxySetupDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 3524792421496769537L;

	private JRadioButton dc;

	private JRadioButton mc;

	private JTextField httpProxyHost, httpProxyPort;

	private JButton b1, b2;

	private ButtonGroup bc;

	public ProxySetupDialog(JFrame parent) {
		super(parent, Messages.getString("Configure Internet connection"), true); //$NON-NLS-1$
		initialize();
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		cp.add(getSettingsPanel(), BorderLayout.CENTER);
		cp.add(getButtonPanel(), BorderLayout.SOUTH);

		try {
			getCurrentSettings();
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
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

	private JPanel getSettingsPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BorderLayout());

		ret.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("Configure access the Internet"))); //$NON-NLS-1$

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

		dc = new JRadioButton(
				Messages.getString("Direct connection to the Internet")); //$NON-NLS-1$
		dc.setMnemonic(KeyEvent.VK_D);
		dc.setSelected(true);
		dc.addActionListener(this);

		mc = new JRadioButton(Messages.getString("Manual proxy setup")); //$NON-NLS-1$
		mc.setMnemonic(KeyEvent.VK_M);
		mc.addActionListener(this);

		bc = new ButtonGroup();
		bc.add(dc);
		bc.add(mc);

		top.add(dc);
		top.add(mc);

		ret.add(top, BorderLayout.NORTH);

		JPanel in = new JPanel();

		httpProxyHost = new JTextField(20);
		httpProxyHost.setEditable(false);

		httpProxyPort = new JTextField(4);
		httpProxyPort.setEditable(false);

		in.add(new JLabel(Messages.getString("Http Proxy:"))); //$NON-NLS-1$
		in.add(httpProxyHost);
		in.add(new JLabel(Messages.getString("Port:"))); //$NON-NLS-1$
		in.add(httpProxyPort);

		ret.add(in, BorderLayout.CENTER);

		return ret;
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		b1 = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
		b1.addActionListener(this);
		b1.setMinimumSize(new Dimension(80, 20));
		b1.setMnemonic(KeyEvent.VK_O);

		b2 = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
		b2.setMinimumSize(new Dimension(80, 20));
		b2.addActionListener(this);
		b2.setMnemonic(KeyEvent.VK_C);

		ret.add(Box.createHorizontalGlue());
		ret.add(b1);
		ret.add(Box.createHorizontalStrut(10));
		ret.add(b2);
		ret.add(Box.createHorizontalGlue());

		return ret;
	}

	private String getXmlString(String type, String host, String port) {
		StringBuffer ret = new StringBuffer();

		ret.append("<proxy>"); //$NON-NLS-1$
		ret.append("<type>"); //$NON-NLS-1$
		ret.append(type);
		ret.append("</type>"); //$NON-NLS-1$
		ret.append("<host>"); //$NON-NLS-1$
		ret.append(host);
		ret.append("</host>"); //$NON-NLS-1$
		ret.append("<port>"); //$NON-NLS-1$
		ret.append(port);
		ret.append("</port>"); //$NON-NLS-1$
		ret.append("</proxy>"); //$NON-NLS-1$

		return ret.toString();
	}

	private String getString(String type, String host, String port) {
		StringBuffer ret = new StringBuffer();

		ret.append("CONNECTION.TYPE="); //$NON-NLS-1$
		ret.append(type);
		ret.append("\r\n"); //$NON-NLS-1$
		ret.append("PROXY.HOST="); //$NON-NLS-1$
		ret.append(host);
		ret.append("\r\n"); //$NON-NLS-1$
		ret.append("PROXY.PORT="); //$NON-NLS-1$
		ret.append(port);
		ret.append("\r\n"); //$NON-NLS-1$

		return ret.toString();
	}

	private void saveSettings(String type, String host, String port) {
		BufferedWriter out = null;
		try {
			String dir = ZProperties.getProperty("FMHOME") + Main.PATH_SEPARATOR + "conf"; //$NON-NLS-1$ //$NON-NLS-2$
			File f = new File(dir);
			if (!f.exists()) {
				f.mkdir();
			}
			File pfile = new File(f, "proxy.properties"); //$NON-NLS-1$
			out = new BufferedWriter(new FileWriter(pfile));
			String xml = getString(type, host, port);
			out.write(xml);
			ZProperties.replaceRuntimeProperty("CONNECTION.TYPE", type); //$NON-NLS-1$
			ZProperties.replaceRuntimeProperty("PROXY.HOST", host); //$NON-NLS-1$
			ZProperties.replaceRuntimeProperty("PROXY.PORT", port); //$NON-NLS-1$

		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
			return;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (Exception e) { /* Ignore closing error */
				}
			}
		}
	}

	private void getCurrentSettings() {
		try {
			String type, host, port = null;
			type = ZProperties.getProperty("CONNECTION.TYPE"); //$NON-NLS-1$
			host = ZProperties.getProperty("PROXY.HOST"); //$NON-NLS-1$
			port = ZProperties.getProperty("PROXY.PORT"); //$NON-NLS-1$

			if (type != null && type.equals("direct")) //$NON-NLS-1$
				dc.setSelected(true);
			if (type != null && type.equals("proxy")) { //$NON-NLS-1$
				mc.setSelected(true);
				this.httpProxyHost.setText(host);
				this.httpProxyPort.setText(port);
			}

			// String dir = ZProperties.getProperty("FMHOME") +
			// Main.PATH_SEPARATOR + "conf";
			// File f = new File(dir);
			// if(!f.exists() ) {
			// f.mkdir();
			// }
			// File pfile = new File(f, "proxysettings.xml");
			// if(pfile.exists() ) {
			// readXmlFile(pfile);
			// }
		}
		catch (Exception e) {
			Logger.getLogger(getClass()).error(e);
			return;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == dc) {
			httpProxyPort.setText(""); //$NON-NLS-1$
			httpProxyHost.setText(""); //$NON-NLS-1$
			httpProxyPort.setEditable(false);
			httpProxyHost.setEditable(false);
		}
		if (e.getSource() == mc) {
			httpProxyPort.setEditable(true);
			httpProxyHost.setEditable(true);
			httpProxyHost.grabFocus();
		}
		if (e.getSource() == b2) {
			dispose();
		}
		if (e.getSource() == b1) {
			String type = "direct"; //$NON-NLS-1$
			if (mc.isSelected()) {
				type = "proxy"; //$NON-NLS-1$
			}
			saveSettings(type, httpProxyHost.getText().trim(), httpProxyPort
					.getText().trim());
			dispose();
		}
	}
}
