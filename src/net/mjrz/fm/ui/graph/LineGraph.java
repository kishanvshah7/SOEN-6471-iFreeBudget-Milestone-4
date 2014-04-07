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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import net.mjrz.fm.services.SessionManager;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class LineGraph extends JPanel {

	static final long serialVersionUID = 0L;
	java.util.List<Double> yPoints = null;
	java.util.List<String> xPoints = null;

	ArrayList<Point2D> scaledPoints = null;

	int gWidth = 540;

	boolean hasNeg = false;

	static NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
			.getCurrencyLocale());

	String xLegend = "";

	int xPad = 20;
	int yPad = 20;

	private boolean drawLineToOrigin = true;

	public LineGraph(java.util.List<String> xp, java.util.List<Double> yp,
			int gw) throws IllegalArgumentException {
		if (xp == null || xp.size() == 0 || xp == null || yp.size() == 0
				|| xp.size() != yp.size()) {
			throw new IllegalArgumentException(
					"Illegal Arguments to the constructor");
		}

		updatePoints(xp, yp, gw);
		setBackground(Color.WHITE);
	}

	public void updatePoints(java.util.List<String> xp,
			java.util.List<Double> yp, int gw) {
		this.yPoints = yp;
		this.xPoints = xp;

		double min = 0;
		double max = 0;
		double mFac = 1;

		gWidth = gw;

		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < yPoints.size(); i++) {
			double x = yPoints.get(i);
			x = roundDouble(x, 2);
			if (x < 0)
				temp.add(x * (-1));
			else
				temp.add(x);
			if (x < min)
				min = x;
			if (x > max)
				max = x;
			double tmp = getM(x);
			if ((x > -1.0 && x < 1.0 && x != 0) && tmp > mFac) {
				mFac = tmp;
			}
		}
		xLegend = nf.format(min) + " to " + nf.format(max);

		scaledPoints = net.mjrz.fm.ui.utils.NScale.scale(temp, gWidth, mFac);

		for (int i = 0; i < scaledPoints.size(); i++) {
			Point2D x = scaledPoints.get(i);
			if (yPoints.get(i) < 0) {
				Point2D.Double tmp = new Point2D.Double(x.getX(), (-1)
						* x.getY());
				scaledPoints.remove(i);
				scaledPoints.add(i, tmp);
				hasNeg = true;
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	protected void draw(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setBackground(Color.WHITE);

		g2D.setColor(Color.GRAY);

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);

		int xPad = 20;
		int yPad = 20;

		xPad = (getWidth() - gWidth) / 2;
		yPad = (getHeight() - gWidth) / 2;

		int pvgW = gWidth / 2;

		Line2D.Double yaxis = new Line2D.Double((xPad + 0), (yPad + 0),
				(xPad + 0), (yPad + gWidth));

		Line2D.Double xaxis = new Line2D.Double((xPad + 0), (yPad + pvgW),
				(xPad + gWidth), (yPad + pvgW));

		Line2D.Double xaxisMj = new Line2D.Double((xPad + 0), (yPad + gWidth),
				(xPad + gWidth), (yPad + gWidth));

		g2D.setColor(Color.DARK_GRAY);
		g2D.draw(yaxis);
		g2D.draw(xaxisMj);
		g2D.setColor(Color.LIGHT_GRAY);
		g2D.draw(xaxis);
		g2D.setColor(Color.DARK_GRAY);

		int sz = scaledPoints.size();
		Point2D curr = scaledPoints.get(0);
		for (int i = 1; i < sz; i++) {
			boolean drawLine = true;
			if (!drawLineToOrigin && i == 1)
				drawLine = false;

			Point2D p = scaledPoints.get(i);
			if (hasNeg) {
				drawShapes(g2D, xPad, yPad, p.getX(), p.getY() / 2,
						curr.getX(), curr.getY() / 2, yPoints.get(i),
						xPoints.get(i), gWidth / 2, drawLine);
			}
			else {
				drawShapes(g2D, xPad, yPad, p.getX(), p.getY(), curr.getX(),
						curr.getY(), yPoints.get(i), xPoints.get(i), gWidth,
						drawLine);
			}

			curr = p;
		}
	}

	protected void drawShapes(Graphics2D g2D, int xPad, int yPad, double x1,
			double y1, double x2, double y2, double value, String xvalue,
			int gWidth, boolean drawLine) {
		Line2D.Double line = new Line2D.Double(xPad + x1, (gWidth + yPad) - y1,
				xPad + x2, (gWidth + yPad) - y2);
		g2D.draw(line);

		String val = nf.format(value);
		// System.out.println(val);
		g2D.drawString(val, (xPad + (int) x1) + 3, (gWidth + yPad)
				- ((int) y1 + 3));

		if (hasNeg)
			gWidth *= 2;

		Line2D.Double xtick = new Line2D.Double(xPad + x1, (yPad + gWidth),
				xPad + x1, (yPad + gWidth + 5));
		Line2D.Double ytick = new Line2D.Double(xPad - 3, (yPad + gWidth) - x1,
				xPad, (yPad + gWidth) - x1);
		g2D.draw(xtick);
		g2D.draw(ytick);

		g2D.drawString(xvalue, (int) (xPad + x1), (int) (yPad + gWidth + 20));
	}

	public static final double roundDouble(double d, int places) {
		return Math.round(d * Math.pow(10, (double) places))
				/ Math.pow(10, (double) places);
	}

	public static double getM(double d) {
		double x = 10;
		while (true) {
			double tmp = d * x;
			if (Math.ceil(tmp) == tmp)
				break;
			else
				x = x * 10;
		}
		return x;
	}

	public void drawLineToOrigin(boolean draw) {
		drawLineToOrigin = draw;
	}

	public boolean getDrawLineToOrigin() {
		return drawLineToOrigin;
	}

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
		}

		ArrayList<Double> dlist = new ArrayList<Double>();
		ArrayList<String> slist = new ArrayList<String>();

		dlist.add(new Double(0));
		dlist.add(new Double(9950.0));
		dlist.add(new Double(9802.0));
		dlist.add(new Double(4950.0));
		dlist.add(new Double(9905.0));
		dlist.add(new Double(5000.0));
		dlist.add(new Double(9854.0));
		dlist.add(new Double(12302.0));
		dlist.add(new Double(9302.0));
		for (int i = 0; i < dlist.size(); i++) {
			slist.add(String.valueOf(i));
		}
		// Random r = new Random();
		// for(int i = 0; i < 10; i++) {
		// double x = r.nextDouble() * 10000 + 1;
		// if(i % 2 == 0) {
		// //x *= -1;
		// }
		// dlist.add(x);
		// slist.add("" + i);
		// System.out.println(x + "," + i);
		// }
		// System.out.println(dlist);

		LineGraph f = new LineGraph(slist, dlist, 540);
		// ButtonPanel bp = f. new ButtonPanel();
		//
		JFrame dc = new JFrame();
		dc.add(new JScrollPane(f), java.awt.BorderLayout.CENTER);
		// dc.add(bp, BorderLayout.SOUTH);

		dc.pack();
		dc.setSize(700, 700);
		System.out.println(dlist.size() * 20);
		dc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dc.setVisible(true);
	}
}
