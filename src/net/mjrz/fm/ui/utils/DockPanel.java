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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings("serial")
public class DockPanel extends JPanel {
	private BufferedImage icons[] = null;
	private String[] labels = null;

	private final int GAP = 16;
	private int selected = -1;
	private int DOCK_WIDTH = -1;
	private final int DOCK_HEIGHT = 72;
	private final int SF = 8;
	private int PAD = 8;
	private String selectedLabel;

	public DockPanel(String[] iconPaths, String iconLabels[])
			throws IOException {
		icons = new BufferedImage[iconPaths.length];
		labels = new String[iconPaths.length];

		for (int i = 0; i < iconPaths.length; i++) {
			File f = new File(iconPaths[i]);
			icons[i] = ImageIO.read(f.getAbsoluteFile());
			if (iconLabels != null && i < iconLabels.length) {
				labels[i] = iconLabels[i];
			}
			else {
				labels[i] = f.getName();
			}
		}

		DOCK_WIDTH = icons.length * (32 + GAP);
		this.setPreferredSize(new Dimension(DOCK_WIDTH, DOCK_HEIGHT + GAP));
		PAD = (int) (getSize().getWidth() - DOCK_WIDTH) / 2;

		MyMouseInputListener mml = new MyMouseInputListener();
		addMouseMotionListener(mml);
		addMouseListener(mml);
		setOpaque(true);
		setBackground(Color.WHITE);
	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		setRenderingHints(g2D);

		Dimension sz = getSize();
		PAD = (int) (getSize().getWidth() - DOCK_WIDTH) / 2;

		g2D.clearRect(0, 0, (int) sz.getWidth(), (int) sz.getHeight());
		for (int i = 0; i < icons.length; i++) {
			BufferedImage img = icons[i];
			int x = PAD + i * (32 + GAP);
			int y = DOCK_HEIGHT - img.getHeight();

			if (i == selected) {
				String lbl = labels[i];
				drawScaledImage(g2D, img, lbl, x, y);
				continue;
			}
			g2D.drawImage(img, x, y, this);
		}
	}

	private void setRenderingHints(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void drawScaledImage(Graphics2D g2D, BufferedImage img,
			String label, int x, int y) {
		int w = img.getWidth();
		int h = img.getHeight();

		int ypad = 4;
		RoundRectangle2D rect = new RoundRectangle2D.Double(x - 2, y
				- (ypad + 2), w + 4, h + 4, 8, 8);
		Color old = g2D.getColor();
		g2D.setColor(Color.GRAY);
		g2D.fill(rect);
		g2D.setColor(old);
		BufferedImage tmp = img;
		g2D.drawImage(tmp, x, y - ypad, null);
		drawXString(g2D, label);
	}

	// private void drawScaledImage(Graphics2D g2D, BufferedImage img, String
	// label, int x, int y) {
	// // BufferedImage tmp = scale(img, img.getWidth() + SF, img.getHeight() +
	// SF);
	// BufferedImage tmp = img;
	// g2D.drawImage(tmp, x - 5, y - 5, null);
	// drawXString(g2D, label);
	// }

	private BufferedImage scale(BufferedImage image, int width, int height) {
		int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image
				.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();

		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	private void eraseLastText(Graphics2D g2D) {
		if (lastXRect != null) {
			g2D.clearRect((int) lastXRect.getX(), (int) lastXRect.getY()
					- (int) lastXRect.getHeight(), (int) lastXRect.getWidth(),
					2 * (int) lastXRect.getHeight());
		}
	}

	private Rectangle2D lastXRect = null;

	private void drawXString(Graphics2D g2D, String val) {
		Font f = g2D.getFont().deriveFont(Font.BOLD);
		g2D.setFont(f);
		eraseLastText(g2D);

		Rectangle2D bounds = g2D.getFontMetrics().getStringBounds(val, g2D);
		int width = (int) bounds.getWidth();

		Dimension d = getSize();
		int xloc = (int) ((d.getWidth() / 2) - (width / 2));
		int yloc = 15;
		g2D.drawString(val, (int) xloc, (int) yloc);
		lastXRect = new Rectangle2D.Double(xloc, yloc, bounds.getWidth(),
				bounds.getHeight());
	}

	public String getSelectedLabel() {
		return selectedLabel;
	}

	class MyMouseInputListener extends MouseInputAdapter {
		@Override
		public void mouseExited(MouseEvent e) {
			selected = -1;
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int ic = getIconUnderPoint(e.getPoint());
			selected = ic;
			repaint();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int ic = getIconUnderPoint(e.getPoint());
			if (ic < 0 || ic >= labels.length)
				return;

			selectedLabel = labels[ic];
			DockPanel.this.firePropertyChange("getSelectedLabel", null,
					selectedLabel);
			selected = -1;
			repaint();
		}

		private int getIconUnderPoint(Point p) {
			double x = p.getX() - PAD;
			double y = p.getY();

			if (y < (DOCK_HEIGHT - 32)) {
				return -1;
			}
			else {
				int mod = (int) (x / (32 + GAP));
				if (mod >= 0 && mod < icons.length) {
					return mod;
				}
			}
			return -1;
		}
	}

	// public static void main(String args[]) throws Exception {
	// try {
	// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// SwingUtilities.invokeLater(new Runnable() {
	// public void run() {
	// JFrame f = new JFrame();
	// f.setUndecorated(false);
	// f.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
	//
	// f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// f.setLayout(new BorderLayout());
	//
	// JPanel p = new JPanel();
	// p.setLayout(new SpringLayout());
	//
	// p.add(new JLabel("Username", JLabel.CENTER));
	// p.add(new JTextField(20));
	//
	// p.add(new JLabel("Password", JLabel.CENTER));
	// p.add(new JPasswordField(20));
	//
	// SpringUtilities.makeCompactGrid(p, 4, 1, 0, 0, 2, 2);
	//
	// // f.add(p, BorderLayout.CENTER);
	// try {
	// String path = "icons/dock/32x32";
	// f.add(new DockPanel(path, null), BorderLayout.SOUTH);
	// f.pack();
	// f.setVisible(true);
	// }
	// catch (IOException e) {
	// e.printStackTrace();
	// System.exit(-1);
	// }
	// }
	// });
	// }
}
