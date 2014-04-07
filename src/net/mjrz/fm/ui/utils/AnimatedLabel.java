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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

public class AnimatedLabel extends JLabel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private javax.swing.Timer timer;
	private final int W = 16;
	private final int H = 16;
	private int curr = 0;

	public static final int ANIM_LOCATION_LEADING = 1;
	public static final int ANIM_LOCATION_TRAILING = 2;
	public static final int ANIM_LOCATION_CENTER = 3;

	private int location = 1;

	private String text = "";

	private BufferedImage idle = null;

	private BufferedImage[] images = null;

	private final int DELAY = 50;

	@SuppressWarnings("unused")
	private AnimatedLabel() {
	}

	public AnimatedLabel(int animationLocation, int textAlignment) {
		location = animationLocation;
		setHorizontalAlignment(textAlignment);
		timer = new javax.swing.Timer(DELAY, this);
		timer.setInitialDelay(10);
		readImg();
	}

	public void startAnim() {
		timer.start();
	}

	public void stopAnim() {
		timer.stop();
		repaint();
	}

	private void readImg() {
		try {
			images = getImages();
			idle = ImageIO.read(getClass().getClassLoader().getResource(
					"icons/busy/idle-icon.png"));
		}
		catch (IOException e) {
			Logger.getLogger(getClass()).error(e);
		}
	}

	private BufferedImage[] getImages() throws IOException {
		BufferedImage images[] = new BufferedImage[15];
		for (int i = 0; i < images.length; i++) {
			String name = "busy-icon" + i + ".png";
			images[i] = ImageIO.read(getClass().getClassLoader().getResource(
					"icons/busy/" + name));
		}
		return images;
	}

	private BufferedImage getCurrClip() {
		BufferedImage currClip = images[curr++];
		if (curr >= 15) {
			curr = 0;
		}
		return currClip;
	}

	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		if (timer.isRunning()) {
			BufferedImage img = getCurrClip();
			if (img == null)
				return;

			int y = (getHeight() - H) / 2;
			if (location == ANIM_LOCATION_TRAILING)
				g2D.drawImage(img, getWidth() - (W + 2), y, W, H, null);
			if (location == ANIM_LOCATION_LEADING)
				g2D.drawImage(img, 2, y, W, H, null);
			if (location == ANIM_LOCATION_CENTER)
				g2D.drawImage(img, getWidth() / 2, y, W, H, null);
		}
		else {
			if (idle == null)
				return;

			int y = (getHeight() - H) / 2;
			if (location == ANIM_LOCATION_TRAILING)
				g2D.drawImage(idle, getWidth() - (W + 2), y, W, H, null);
			if (location == ANIM_LOCATION_LEADING)
				g2D.drawImage(idle, 2, y, W, H, null);
			if (location == ANIM_LOCATION_CENTER)
				g2D.drawImage(idle, getWidth() / 2, y, W, H, null);
		}
	}
}
