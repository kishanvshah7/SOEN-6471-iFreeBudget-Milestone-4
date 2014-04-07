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
package net.mjrz.fm.ui.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class QuoteLineGraph extends LineGraph {

	static final long serialVersionUID = 0L;

	int xPad = 0;
	int yPad = 0;

	public static Color gcolor = new Color(171, 188, 209);
	public static Color lcolor = new Color(190, 65, 171);

	private JLabel updateLbl = null;
	private boolean drawValues = true;

	private Shape lastHighlight = null;

	private String xLegend = "";
	private SimpleDateFormat sdf = new SimpleDateFormat("MMM d ''yy");

	private boolean trackXValue = true;
	private boolean trackYValue = false;

	public QuoteLineGraph(java.util.List<String> xp, java.util.List<Double> yp,
			int gw) throws IllegalArgumentException {
		super(xp, yp, gw);
		this.addMouseMotionListener(new MouseMotionHandler());
		this.addMouseListener(new MouseHandler());
	}

	public QuoteLineGraph(java.util.List<String> xp, java.util.List<Double> yp,
			int gw, boolean drawValues) throws IllegalArgumentException {
		super(xp, yp, gw);
		this.addMouseMotionListener(new MouseMotionHandler());
		this.addMouseListener(new MouseHandler());

		// Calendar start = DateUtils.stringToCalendar(xp.get(0), "/");
		// Calendar end = DateUtils.stringToCalendar(xp.get(xp.size() - 1),
		// "/");
		//
		// xLegend = sdf.format(start.getTime()) + " to " +
		// sdf.format(end.getTime());

		this.drawValues = drawValues;
	}

	public void setUpdateLabel(JLabel l) {
		this.updateLbl = l;
	}

	public void updatePoints(java.util.List<String> xp,
			java.util.List<Double> yp, int gw) {
		super.updatePoints(xp, yp, gw);
	}

	public void pointExists(double x) {
		x -= xPad;
		int sz = scaledPoints.size();
		x = Math.round(x);
		for (int i = 0; i < sz; i++) {
			if (Math.round(scaledPoints.get(i).getX()) == x) {
				highlightGraph(scaledPoints.get(i).getX(), scaledPoints.get(i)
						.getY(), i);
				break;
			}
		}
	}

	private void highlightNearestPoint(double x) {
		x -= xPad;
		int sz = scaledPoints.size();
		x = Math.round(x);
		for (int i = 0; i < sz; i++) {
			if (Math.round(scaledPoints.get(i).getX()) >= x) {
				highlightGraph(scaledPoints.get(i).getX(), scaledPoints.get(i)
						.getY(), i);
				break;
			}
		}
	}

	int[] strHighlightX = { 0, 0 };
	int[] strHighlightY = { 0, 0 };
	String lastXString = "";
	String lastYString = "";

	private void highlightGraph(double x, double y, int index) {
		if (index == 0)
			return;

		Graphics2D g2D = (Graphics2D) getGraphics();

		Color c = g2D.getColor();

		Ellipse2D.Double rect = new Ellipse2D.Double(((xPad + (int) x) - 4),
				((gWidth + yPad) - 4 - (int) y), 8, 8);

		if (lastHighlight != null && !lastHighlight.equals(rect)) {
			repaint(lastHighlight.getBounds());
		}
		if (trackXValue) {
			if (strHighlightY[0] != 0 || strHighlightY[1] != 0) {
				g2D.setColor(Color.WHITE);
				g2D.drawString(lastYString, strHighlightY[0], strHighlightY[1]);
				g2D.setColor(c);
			}
		}
		if (trackYValue) {
			if (strHighlightX[0] != 0 || strHighlightX[1] != 0) {
				g2D.setColor(Color.WHITE);
				g2D.drawString(lastXString, strHighlightX[0], strHighlightX[1]);
				g2D.setColor(c);
			}
		}
		lastHighlight = rect;

		g2D.setColor(Color.RED);
		g2D.fill(lastHighlight);

		String val = nf.format(yPoints.get(index)) + " on "
				+ xPoints.get(index);
		if (updateLbl != null) {
			updateLbl.setText(val);
		}

		String xStr = String.valueOf(xPoints.get(index));
		String yStr = String.valueOf(yPoints.get(index));

		strHighlightY[0] = (int) (xPad + x);
		strHighlightY[1] = yPad + gWidth + 15;

		FontMetrics metrics = g2D.getFontMetrics();
		Rectangle2D bounds = metrics.getStringBounds(yStr, g2D);
		double strBounds = bounds.getWidth();

		strHighlightX[0] = (int) (xPad - (strBounds + 5));
		strHighlightX[1] = (int) (gWidth + yPad - y);

		lastYString = xStr;
		lastXString = yStr;

		if (trackXValue)
			g2D.drawString(xStr, strHighlightY[0], strHighlightY[1]);
		if (trackYValue)
			g2D.drawString(yStr, strHighlightX[0], strHighlightX[1]);

		g2D.setColor(c);
	}

	class MouseMotionHandler implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {
			highlightNearestPoint(e.getX());
		}
	}

	class MouseHandler implements MouseListener {
		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		public void mouseExited(MouseEvent e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	private void setupGraph(Graphics2D g2D) {
		int numlines = scaledPoints.size();
		int spacing = (int) Math.floor(gWidth / numlines);
		g2D.setColor(gcolor);

		for (int i = 0; i <= numlines; i++) {
			Line2D.Double xline = new Line2D.Double((xPad + 0), (yPad + i
					* spacing), (xPad + gWidth), (yPad + i * spacing));

			Line2D.Double yline = new Line2D.Double((xPad + i * spacing),
					(yPad + 0), (xPad + i * spacing), (yPad + gWidth));

			g2D.draw(xline);
			g2D.draw(yline);
		}
	}

	protected void draw(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setBackground(Color.WHITE);

		g2D.setColor(Color.GRAY);

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		xPad = (getWidth() - gWidth) / 2;
		yPad = (getHeight() - gWidth) / 2;

		setupGraph(g2D);

		g2D.setColor(lcolor);

		int sz = scaledPoints.size();
		Point2D curr = scaledPoints.get(0);

		for (int i = 1; i < sz; i++) {
			Point2D p = scaledPoints.get(i);
			boolean drawline = true;
			if (i == 1)
				drawline = false;

			if (hasNeg) {
				drawShapes(g2D, xPad, yPad, p.getX(), p.getY() / 2,
						curr.getX(), curr.getY() / 2, yPoints.get(i),
						xPoints.get(i), gWidth / 2, drawline);
			}
			else {
				drawShapes(g2D, xPad, yPad, p.getX(), p.getY(), curr.getX(),
						curr.getY(), yPoints.get(i), xPoints.get(i), gWidth,
						drawline);
			}
			curr = p;
		}
	}

	protected void drawShapes(Graphics2D g2D, int xPad, int yPad, double x1,
			double y1, double x2, double y2, double value, String xvalue,
			int gWidth, boolean drawline) {

		Line2D.Double line = new Line2D.Double(xPad + x1, (gWidth + yPad) - y1,
				xPad + x2, (gWidth + yPad) - y2);

		if (drawline)
			g2D.draw(line);

		if (drawValues) {
			String val = nf.format(value);

			g2D.drawString(val, (xPad + (int) x1) + 3, (gWidth + yPad)
					- ((int) y1 + 3));
		}

	}

	public void setTrackXValue(boolean trackX) {
		trackXValue = trackX;
	}

	public void setTrackYValue(boolean trackY) {
		trackYValue = trackY;
	}
}
