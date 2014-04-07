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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

public class Splash extends JWindow {
	private static final long serialVersionUID = 1L;
	private AnimatedLabel status;
	private static Splash instance = new Splash();

	public synchronized static Splash getInstance() {
		if (instance == null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						instance = new Splash();
					}
				});
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	private Splash() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JLabel l1 = new JLabel(new net.mjrz.fm.ui.utils.MyImageIcon(
				"splash.jpg"));
		cp.add(l1, BorderLayout.CENTER);

		JPanel s = new JPanel();
		s.setLayout(new GridLayout(1, 1));

		status = new AnimatedLabel(AnimatedLabel.ANIM_LOCATION_TRAILING,
				AnimatedLabel.CENTER);
		status.setText("Initializing... Please wait");
		status.setOpaque(true);
		status.setBackground(UIDefaults.DEFAULT_COLOR);
		status.setForeground(Color.BLACK);
		status.setPreferredSize(new Dimension(getWidth(), 32));

		java.awt.Font f = status.getFont();
		status.setFont(new java.awt.Font(f.getName(), java.awt.Font.BOLD, f
				.getSize()));

		s.add(status);

		status.startAnim();

		cp.add(s, BorderLayout.SOUTH);

		pack();
		int WIDTH = 400;
		int HEIGHT = 330;
		setSize(WIDTH, HEIGHT);
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenDim.width - WIDTH) / 2,
				(screenDim.height - HEIGHT) / 2);
		setVisible(true);
	}

	public void updateStatus(String s) {
		status.setText(s);
	}

	public void dispose() {
		status.stopAnim();
		super.dispose();
	}
}
