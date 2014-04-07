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
import java.awt.Dimension;
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
import net.mjrz.fm.ui.utils.notifications.types.UINotification;

public class AlertNotificationFrame extends JFrame implements
		NotificationDisplay {
	private static final long serialVersionUID = 1L;
	private JLabel message = null;
	private FinanceManagerUI frame = null;

	public AlertNotificationFrame(UINotification notification,
			FinanceManagerUI frame) {
		this.frame = frame;
		initialize();
	}

	private void initialize() {
		getContentPane().setLayout(new BorderLayout());
		message = new JLabel("");

		getContentPane().add(Box.createVerticalStrut(5), BorderLayout.NORTH);
		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.EAST);
		getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.WEST);

		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.LINE_AXIS));
		south.add(Box.createHorizontalGlue());
		JButton close = new JButton(new net.mjrz.fm.ui.utils.MyImageIcon(
				"icons/exit.png"));
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.hideSheet();
			}
		});
		close.setPreferredSize(new Dimension(18, 18));
		south.add(close);
		south.setBackground(NotificationHandler.NOTIF_COLOR);

		getContentPane().add(message, BorderLayout.CENTER);
		getContentPane().add(south, BorderLayout.SOUTH);
		getContentPane().setBackground(NotificationHandler.NOTIF_COLOR);

		((JComponent) getContentPane())
				.setBorder(new LineBorder(Color.BLACK, 1));

		pack();
		setPreferredSize(new Dimension(250, 200));
	}

	@Override
	public void setMessageText(String text) {
		message.setText(text);
	}
}
