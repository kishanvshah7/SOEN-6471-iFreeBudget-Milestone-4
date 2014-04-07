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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CompoundIcon implements Icon {
	private Icon left, right;
	private int gap;

	public CompoundIcon(Icon left, Icon right, int gap) {
		if (left == null || right == null)
			throw new NullPointerException();
		this.left = left;
		this.right = right;
		this.gap = gap;
	}

	public int getIconHeight() {
		return Math.max(left.getIconHeight(), right.getIconHeight());
	}

	public int getIconWidth() {
		return left.getIconWidth() + gap + right.getIconWidth();
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		int h = getIconHeight();
		left.paintIcon(c, g, x, y + (h - left.getIconHeight()) / 2);
		right.paintIcon(c, g, x + left.getIconWidth() + gap,
				y + (h - right.getIconHeight()) / 2);
	}

	public static void main(String[] args) throws MalformedURLException {
		JPanel p = new JPanel(new GridLayout(0, 1));

		String img1 = "icons/about.png";
		String img2 = "icons/account.png";
		String img3 = "icons/accept.png";

		add(img1, p);
		add(img2, p);
		add(img3, p);

		JFrame f = new JFrame("Example");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(p);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	static void add(String file, JPanel p) {
		JLabel l = new JLabel("test", new CompoundIcon(
				new net.mjrz.fm.ui.utils.MyImageIcon(file),
				new net.mjrz.fm.ui.utils.MyImageIcon(file), 3),
				JLabel.HORIZONTAL);
		p.add(l);
	}
}
