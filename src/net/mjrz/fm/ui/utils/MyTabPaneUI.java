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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class MyTabPaneUI extends BasicTabbedPaneUI {
	private static final Insets NO_INSETS = new Insets(2, 0, 0, 0);

	private Font boldFont;

	private FontMetrics boldFontMetrics;

	final static BasicStroke thinStroke = new BasicStroke(1.0f);
	final static BasicStroke mediumStroke = new BasicStroke(1.5f);
	final static BasicStroke wideStroke = new BasicStroke(4.0f);

	public static ComponentUI createUI(JComponent c) {
		return new MyTabPaneUI();
	}

	protected void installDefaults() {
		super.installDefaults();
		tabAreaInsets.left = 4;
		selectedTabPadInsets = new Insets(0, 0, 0, 0);
		tabInsets = selectedTabPadInsets;

		boldFont = tabPane.getFont().deriveFont(Font.BOLD);
		boldFontMetrics = tabPane.getFontMetrics(boldFont);
	}

	public int getTabRunCount(JTabbedPane pane) {
		return 1;
	}

	protected Insets getContentBorderInsets(int tabPlacement) {
		return NO_INSETS;
	}

	protected int calculateTabHeight(int tabPlacement, int tabIndex,
			int fontHeight) {
		int vHeight = fontHeight;
		if (vHeight % 2 > 0) {
			vHeight += 1;
		}
		return vHeight + 5;
	}

	protected int calculateTabWidth(int tabPlacement, int tabIndex,
			FontMetrics metrics) {
		return super.calculateTabWidth(tabPlacement, tabIndex, metrics)
				+ metrics.getHeight() + 20;
	}

	protected void paintTabBackground(Graphics g, int tabPlacement,
			int tabIndex, int x, int y, int w, int h, boolean isSelected) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		GradientPaint gp = new GradientPaint(x, y,
				UIDefaults.DEFAULT_TABLE_HEADER_COLOR, x + w, y,
				UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR);

		int topCurveStart = x + w - 30;

		if (isSelected) {
			g2D.setPaint(gp);
		}
		else {
			g2D.setColor(tabPane.getBackground());
		}

		Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w - 30, h);

		Rectangle2D.Double rect1 = new Rectangle2D.Double(topCurveStart, y,
				(w - topCurveStart), h);

		double sx1 = rect1.getX();
		double sy1 = rect1.getY();

		double ex1 = x + w;
		double ey1 = rect1.getY() + h;

		double cx1 = rect1.getX() + h;
		double cy1 = rect1.getY() / 2;

		QuadCurve2D.Double qc = new QuadCurve2D.Double(sx1, sy1, cx1, cy1, ex1,
				ey1);

		Polygon p = new Polygon();
		p.addPoint((int) sx1, (int) sy1);
		p.addPoint((int) ex1 + 2, (int) ey1);
		p.addPoint((int) rect1.getX(), (int) rect1.getY() + h);

		g2D.fill(rect);
		g2D.fill(qc);
		g2D.fill(p);
	}

	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
			int x, int y, int w, int h, boolean isSelected) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2D.setStroke(thinStroke);
		if (isSelected) {
			g2D.setColor(UIDefaults.DEFAULT_COLOR);
		}
		else {
			g2D.setColor(Color.LIGHT_GRAY);
		}

		// int topCurveStart = x + w - 20;
		int topCurveStart = x + w - 30;
		/* Top */
		g2D.drawLine(x, y, topCurveStart, y);

		/* Left */
		g2D.drawLine(x, y, x, y + h);

		Rectangle2D.Double rect = new Rectangle2D.Double(topCurveStart, y,
				(w - topCurveStart), h);

		double sx1 = rect.getX();
		double sy1 = rect.getY();

		double ex1 = x + w;
		double ey1 = rect.getY() + h;

		double cx1 = rect.getX() + h;
		double cy1 = rect.getY() / 2;

		QuadCurve2D.Double qc = new QuadCurve2D.Double(sx1, sy1, cx1, cy1, ex1,
				ey1);

		g2D.draw(qc);
	}

	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) {
		Graphics2D g2D = (Graphics2D) g;

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2D.setStroke(wideStroke);
		g2D.setColor(UIDefaults.DEFAULT_TABLE_HEADER_COLOR);

		g2D.drawLine(x, y, x + w, y);
	}

	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) {
		// Do nothing
	}

	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) {
		// Do nothing
	}

	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
			int selectedIndex, int x, int y, int w, int h) {
		// Do nothing
	}

	protected void paintFocusIndicator(Graphics g, int tabPlacement,
			Rectangle[] rects, int tabIndex, Rectangle iconRect,
			Rectangle textRect, boolean isSelected) {
		// Do nothing
	}

	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int tw = tabPane.getBounds().width;

		g2D.setColor(tabPane.getBackground());
		g2D.fillRect(0, 0, tw, rects[0].height + 3);

		super.paintTabArea(g2D, tabPlacement, selectedIndex);
	}

	protected void paintText(Graphics g, int tabPlacement, Font font,
			FontMetrics metrics, int tabIndex, String title,
			Rectangle textRect, boolean isSelected) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (tabIndex < rects.length) {
			Rectangle tabRect = rects[tabIndex];
			textRect.x = tabRect.x + 5;
		}
		if (isSelected) {
			super.paintText(g2D, tabPlacement, boldFont, boldFontMetrics,
					tabIndex, title, textRect, isSelected);
		}
		else {
			super.paintText(g2D, tabPlacement, font, metrics, tabIndex, title,
					textRect, isSelected);
		}
	}

	protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
			boolean isSelected) {
		return 0;
	}
}
