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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.mjrz.fm.utils.ZProperties;

public class TipBrowser extends JDialog {
	private static final long serialVersionUID = 1L;

	String[] msgs = {
			tr("You can open New account dialog by using CTRL+SHIFT+A key combination"),
			tr("You can open New transaction dialog by using CTRL+SHIFT+T key combination"),
			tr("You can edit a transaction by right clicking on the transaction in the list"
					+ "and selecting edit in the popup menu"),
			tr("You can disable this dialog by un-checking Show tips on startup checkbox"),
			tr("You can import Quicken or Money files in OFX format. Select File->Import OFX/QFX and select your file"),
			tr("You can view your daily/weekly/monthly net worth chart by clicking Graphs->Net worth and "
					+ "and selecting the appropriate menu"),
			tr("You can view a pie chart distribution of your Assets by clicking Graphs->Assets"),
			tr("You can view a pie chart distribution of your Liability accounts by clicking Graphs->Liabilities"),
			tr("You can delete multiple transactions at once by selecting Edit->Select all and Edit->Cancel"),
			// tr("You can create and save filters to search for transactions based on Account, Date and Amounts"),
			tr("You can enter transactions scheduled for a future date. On the scheduled date, the transaction "
					+ "will be committed"),
			tr("You can use address book to store your contacts. Click on Tools->Address book"),
			tr("You can import Outlook, Yahoo, Gmail contacts in CSV file to the address book"),
			tr("You can print the transaction list. Click on File->Print"),
			tr("You can save the transaction list to a file in HTML format. Click on File->Save report"),
			tr("You can view stock information using the Quote viewer dialog. Click on Tools->Get quotes. "
					+ "Enter the stock symbol and click Get quotes."),
			tr("The quote viewer displays a graph tracking the stock price for the past one year"),
			tr("iFreeBudget is licensed under Apache Open source license"),
			tr("You can contribute to this project by filing bug reports. Visit this url for filing a bug report:\n"
					+ " http://sourceforge.net/tracker/?func=add&group_id=204184&atid=988595"),
			tr("You can contribute to this project by providing suggestions or criticisms. Visit the home page "
					+ "for contact information:\nhttp://www.ifreebudget.com"),
			tr("Many people have helped in translating this program to various languages "
					+ "You can view the list of contributors to this "
					+ "project by clicking Help->About and click on the "
					+ "transalations tab"),
			tr("You can contribute to this project by helping translate to different languages. No programming "
					+ "experience is necessary"),
			// tr("You can open the search tool by typing CTRL+F")
			tr("You can follow iFreeBudget for updates and news on Twitter at http://twitter.com/ifreebudget") };

	int curr = 0;
	java.util.List<String> msgList;

	public TipBrowser(JFrame parent) {
		super(parent, "Tip of the day", true);
		msgList = Arrays.asList(msgs);
		Collections.shuffle(msgList);
		initialize();
	}

	private void initialize() {
		Container container = getContentPane();
		container.setLayout(new GridLayout(1, 1, 50, 50));

		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());

		JPanel top = new JPanel();
		top.setLayout(new GridLayout(1, 1));
		JLabel l = new JLabel("<html><font size=\"+1\"><b>"
				+ tr("Did you know...") + "</b></font></html>",
				new net.mjrz.fm.ui.utils.MyImageIcon("icons/excl.png"),
				JLabel.CENTER);
		// l.setPreferredSize(new Dimension(100, 75));
		top.add(l);
		cp.add(top, BorderLayout.NORTH);

		final JTextArea disp = new JTextArea(10, 35);
		disp.setText(msgs[curr]);
		disp.setCaretPosition(0);
		disp.setEditable(false);
		disp.setWrapStyleWord(true);
		disp.setLineWrap(true);
		JPanel center = new JPanel(new GridLayout(1, 1, 5, 5));

		center.add(new JScrollPane(disp));

		cp.add(center, BorderLayout.CENTER);

		JPanel s = new JPanel();
		s.setLayout(new BoxLayout(s, BoxLayout.LINE_AXIS));
		final JCheckBox cb = new JCheckBox(tr("Show tips on startup"));
		String checked = ZProperties.getProperty("TIPBROWSER.SHOWONSTART");
		if (checked == null || checked.equals("true")) {
			cb.setSelected(true);
		}

		s.add(cb);

		JButton next = new JButton(tr("Next tip"));
		next.setMinimumSize(new Dimension(75, 20));
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (++curr >= msgs.length)
					curr = 0;

				disp.setText(msgList.get(curr));
				disp.setCaretPosition(0);
			}
		});

		JButton close = new JButton(tr("Close"));
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean checked = cb.isSelected();
				String prop = "TIPBROWSER.SHOWONSTART";
				String value = Boolean.toString(checked);
				try {
					net.mjrz.fm.utils.PropertiesUtils.saveSettings(
							"settings.properties", prop, value);
				}
				catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
				dispose();
			}
		});
		close.setMinimumSize(new Dimension(75, 20));

		s.add(Box.createHorizontalGlue());
		s.add(next);
		s.add(Box.createHorizontalStrut(5));
		s.add(close);

		cp.add(s, BorderLayout.SOUTH);

		container.add(cp);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		net.mjrz.fm.ui.utils.GuiUtilities.addWindowClosingActionMap(this);
	}
}
