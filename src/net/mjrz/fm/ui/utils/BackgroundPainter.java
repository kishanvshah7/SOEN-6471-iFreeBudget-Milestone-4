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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;

public class BackgroundPainter extends SynthPainter {

	public void paintPanelBackground(SynthContext context, Graphics g, int x,
			int y, int w, int h) {
		Color start = UIManager.getColor("Panel.startBackground");
		Color end = UIManager.getColor("Panel.endBackground");
		Graphics2D g2 = (Graphics2D) g;
		GradientPaint grPaint = new GradientPaint((float) x, (float) y, start,
				(float) w, (float) h, end);
		g2.setPaint(grPaint);
		g2.fillRect(x, y, w, h);
		g2.setPaint(null);
		g2.setColor(new Color(255, 255, 255, 120));

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// CubicCurve2D.Double arc2d = new CubicCurve2D.Double(0, h/4, w/3,
		// h/10, .66 * w, 1.5 * h, w, h/8);
		// g2.draw(arc2d);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void paintListBackground(SynthContext context, Graphics g, int x,
			int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}

	public void paintScrollPaneBackground(SynthContext context, Graphics g,
			int x, int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}

	public void paintTableBackground(SynthContext context, Graphics g, int x,
			int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}

	public void paintTableBorder(SynthContext context, Graphics g, int x,
			int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}

	public void paintViewportBackground(SynthContext context, Graphics g,
			int x, int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}

	public void paintTableHeaderBackground(SynthContext context, Graphics g,
			int x, int y, int w, int h) {
		paintPanelBackground(context, g, x, y, w, h);
	}
}
