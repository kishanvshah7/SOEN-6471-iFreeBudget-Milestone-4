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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class AnimatingSheet extends JPanel {
	private static final long serialVersionUID = 1L;
	Dimension animatingSize = new Dimension(0, 1);
	JComponent source;
	BufferedImage offscreenImage;

	public AnimatingSheet() {
		super();
		setOpaque(true);
	}

	public void setSource(JComponent source) {
		this.source = source;
		animatingSize.width = source.getWidth();
		makeOffscreenImage(source);
		source.grabFocus();
	}

	public void setAnimatingHeight(int height) {
		animatingSize.height = height;
		setSize(animatingSize);
	}

	private void makeOffscreenImage(JComponent source) {
		GraphicsConfiguration gfxConfig = GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		offscreenImage = gfxConfig.createCompatibleImage(source.getWidth(),
				source.getHeight());
		Graphics2D offscreenGraphics = (Graphics2D) offscreenImage
				.getGraphics();
		source.paint(offscreenGraphics);
	}

	public Dimension getPreferredSize() {
		return animatingSize;
	}

	public Dimension getMinimumSize() {
		return animatingSize;
	}

	public Dimension getMaximumSize() {
		return animatingSize;
	}

	public void paint(Graphics g) {
		BufferedImage fragment = offscreenImage.getSubimage(0,
				offscreenImage.getHeight() - animatingSize.height,
				source.getWidth(), animatingSize.height);
		g.drawImage(fragment, 0, 0, this);
	}
}
