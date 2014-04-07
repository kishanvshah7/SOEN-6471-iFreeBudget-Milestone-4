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
package net.mjrz.fm.ui.utils.notifications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import net.mjrz.fm.ui.FinanceManagerUI;
import net.mjrz.fm.ui.utils.NotificationHandler;
import net.mjrz.fm.ui.utils.TimerButton;
import net.mjrz.fm.ui.utils.notifications.types.MissedTxNotification;
import net.mjrz.fm.ui.utils.notifications.types.UINotification;

import org.apache.log4j.Logger;

public class MissedTxNotificationFrame extends JFrame implements
		NotificationDisplay {

	private static final long serialVersionUID = 1L;
	private JLabel message = null;
	private FinanceManagerUI frame = null;
	private JButton ok, close;
	private UINotification notif;

	private static Logger logger = Logger
			.getLogger(MissedTxNotificationFrame.class.getName());

	public MissedTxNotificationFrame(UINotification notification,
			FinanceManagerUI frame) {
		this.frame = frame;
		this.notif = notification;
		initialize();
	}

	private void initialize() {
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);

		message = new JLabel("");

		JPanel north = new JPanel();
		north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
		north.add(message);
		north.add(Box.createVerticalStrut(15));
		north.setBackground(NotificationHandler.NOTIF_COLOR);

		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.LINE_AXIS));
		close = new TimerButton("Close", FinanceManagerUI.WAIT_DURATION / 1000);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.hideSheet();
			}
		});
		ok = new JButton("Details");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.hideSheet();
				MissedTxNotification n = (MissedTxNotification) notif;
				frame.showMissedTxDialog(n.getMissed());
			}
		});

		south.add(Box.createHorizontalGlue());
		south.add(ok);
		south.add(close);
		south.setBackground(NotificationHandler.NOTIF_COLOR);

		getContentPane().add(north, BorderLayout.NORTH);
		getContentPane().add(south, BorderLayout.SOUTH);
		getContentPane().setBackground(NotificationHandler.NOTIF_COLOR);

		((JComponent) getContentPane())
				.setBorder(new LineBorder(Color.BLACK, 1));

		pack();
		setSize(400, 150);
	}

	@Override
	public void setMessageText(String text) {
		message.setText(text);
	}
}
