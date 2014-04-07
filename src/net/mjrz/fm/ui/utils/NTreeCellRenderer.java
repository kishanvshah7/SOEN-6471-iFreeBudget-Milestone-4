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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class NTreeCellRenderer extends DefaultTreeCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Color curr = Color.DARK_GRAY;

	public NTreeCellRenderer() {
		setOpaque(true);
		this.setDoubleBuffered(true);
		this.setPreferredSize(new Dimension(150, 25));
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setBackground(Color.WHITE);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (!sel)
			curr = Color.RED;
		else
			curr = Color.DARK_GRAY;
		this.setText(value.toString());
		return this;
	}

	public void paintComponent(Graphics g) {

		int w = getWidth();
		int h = getHeight();

		Graphics2D g2D = (Graphics2D) g.create();

		RoundRectangle2D.Float sh = new RoundRectangle2D.Float(0, 0, w - 2,
				h - 2, h - 1, h - 1);
		super.paintComponent(g);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2D.setColor(curr);
		g2D.draw(sh);
		g2D.dispose();
	}
}
