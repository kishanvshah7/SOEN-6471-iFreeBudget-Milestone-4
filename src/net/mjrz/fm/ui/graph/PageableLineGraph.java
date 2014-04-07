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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.mjrz.fm.ui.utils.NScale;

public class PageableLineGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	protected int gWidth = 0;
	protected List<String> xPoints = null;
	protected List<BigDecimal> yPoints = null;
	private List<GraphPoint> scaledPoints = null;
	private List<GraphPoint> currentPage = null;

	protected int PAGE_SIZE = 30;
	boolean hasNeg = false;
	public static NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
	private int currIndex = 0;
	protected int xPad = 20;
	protected int yPad = 20;
	private RenderingHints renderHints = null;
	private int pageNo = 0;
	private int numPages = 0;
	private final int NUM_STEPS = 30;

	protected PageableLineGraph() {

	}

	public void initializeGraph(java.util.List<String> xp,
			java.util.List<BigDecimal> yp, int gw)
			throws IllegalArgumentException {
		if (xp == null || xp.size() == 0 || xp == null || yp.size() == 0
				|| xp.size() != yp.size()) {
			throw new IllegalArgumentException(
					"Illegal Arguments to the constructor");
		}
		this.xPoints = xp;
		this.yPoints = yp;
		this.gWidth = gw;

		if (xp.size() < PAGE_SIZE)
			PAGE_SIZE = xp.size();

		getNumPages();
		pageNo = numPages - 1;
		currIndex = pageNo * PAGE_SIZE;

		currentPage = new ArrayList<GraphPoint>();
		scaledPoints = new ArrayList<GraphPoint>();

		scalePoints(yp);
		updatePoints(currIndex, PAGE_SIZE);
		setBackground(Color.WHITE);
		addMouseMotionListener(new MouseMotionHandler());
	}

	private void getNumPages() {
		int sz = xPoints.size();
		if (sz <= PAGE_SIZE)
			numPages = 1;

		int mod = sz % PAGE_SIZE;
		int div = sz / PAGE_SIZE;
		if (mod != 0) {
			numPages = div + 1;
		}
		else {
			numPages = div;
		}
	}

	public void nextPage() {
		if (pageNo >= numPages)
			return;

		pageNo++;
		currIndex = pageNo * PAGE_SIZE;

		updatePointsS(currIndex, PAGE_SIZE);
	}

	public void previousPage() {
		if (pageNo < 0)
			return;

		pageNo -= 1;
		currIndex = pageNo * PAGE_SIZE;

		updatePointsS(currIndex, PAGE_SIZE);
	}

	public boolean hasNext() {
		return pageNo + 1 < numPages;
	}

	public boolean hasPrevious() {
		return pageNo > 0;
	}

	public int getCurrentIndex() {
		return currIndex;
	}

	private void loadCurrentPage(int start, int size) {
		currentPage.clear();
		int sz = scaledPoints.size();
		for (int i = start; i < start + size && i < sz; i++) {
			GraphPoint x = scaledPoints.get(i);
			if (yPoints.get(i).doubleValue() < 0) {
				Point2D.Double tmp = new Point2D.Double(x.getPoint().getX(),
						(-1) * x.getPoint().getY());
				currentPage.add(new GraphPoint(tmp, x.getXValue(), x
						.getYValue()));
			}
			else {
				currentPage.add(x);
			}
		}
	}

	public BufferedImage getCurrentPage(int start, int size) {
		BufferedImage img = new BufferedImage(this.getWidth(),
				this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g2D = (Graphics2D) img.getGraphics();
		g2D.setBackground(Color.WHITE);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);

		draw(g2D);

		return img;
	}

	public void updatePoints(int start, int size) {
		// System.out.println("Update points " + start + " , " + size);
		loadCurrentPage(start, size);
		updateUI();
	}

	public void updatePointsS(int start, int size) {
		loadCurrentPage(start, size);
		BufferedImage img = getCurrentPage(start, size);
		int width = img.getWidth();
		if (width == 0)
			return;

		Graphics2D g2D = (Graphics2D) getGraphics();
		g2D.setBackground(Color.WHITE);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);

		g2D.clearRect(xPad, yPad, gWidth + 4, gWidth + 4);
		int step = (int) width / NUM_STEPS;
		for (int i = 0; i < NUM_STEPS; i++) {
			try {
				BufferedImage subImg = img.getSubimage(i * step, 0, step,
						img.getHeight());
				getGraphics().drawImage(subImg, (i * step), 0, step,
						img.getHeight(), null);
				// Thread.sleep(25);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setBackground(Color.WHITE);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		draw(g2D);
	}

	protected void draw(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setBackground(Color.WHITE);

		g2D.setColor(Color.GRAY);

		xPad = (getWidth() - gWidth) / 2;
		yPad = (getHeight() - gWidth) / 2;

		int pvgW = gWidth / 2;

		Line2D.Double xaxisMj = new Line2D.Double((xPad + 0), (yPad + gWidth),
				(xPad + gWidth), (yPad + gWidth));
		Line2D.Double xaxis = new Line2D.Double((xPad + 0), (yPad + pvgW),
				(xPad + gWidth), (yPad + pvgW));
		Line2D.Double yaxis = new Line2D.Double((xPad + 0), (yPad), (xPad + 0),
				(yPad + gWidth));

		g2D.draw(xaxisMj);
		g2D.draw(yaxis);
		if (hasNeg) {
			g2D.setColor(Color.LIGHT_GRAY);
			g2D.draw(xaxis);
			g2D.setColor(Color.GRAY);
		}

		int sz = currentPage.size();
		GraphPoint curr = currentPage.get(0);
		for (int i = 0; i < sz; i++) {
			boolean drawLine = true;
			GraphPoint p = currentPage.get(i);
			if (hasNeg) {
				drawShapes(g2D, xPad, yPad, p.getPoint().getX(), p.getPoint()
						.getY() / 2, curr.getPoint().getX(), curr.getPoint()
						.getY() / 2, p.getYValue(), p.getXValue(), gWidth / 2,
						drawLine);
			}
			else {
				drawShapes(g2D, xPad, yPad, p.getPoint().getX(), p.getPoint()
						.getY(), curr.getPoint().getX(),
						curr.getPoint().getY(), p.getYValue(), p.getXValue(),
						gWidth, drawLine);
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
	}

	private int lastXIndex = -1;

	private void highlightNearestPoint(double x) {
		x -= xPad;
		int sz = currentPage.size();
		x = Math.round(x);
		for (int i = 0; i < sz; i++) {
			Point2D p = currentPage.get(i).getPoint();
			if (Math.round(p.getX()) >= x) {
				if (lastXIndex == i) {
					return;
				}
				else {
					lastXIndex = i;
				}
				if (hasNeg) {
					highlightGraph(gWidth, p.getX(), p.getY() / 2, i);
				}
				else {
					highlightGraph(gWidth, p.getX(), p.getY(), i);
				}
				break;
			}
		}
	}

	private boolean trackXValue = true;
	private boolean trackYValue = true;
	private Rectangle2D lastXRect = null;
	private Rectangle2D lastYRect = null;
	private Shape lastHighlight = null;

	protected void highlightGraph(int gWidth, double x, double y, int index) {
		Graphics2D g2D = (Graphics2D) getGraphics();
		g2D.setBackground(Color.WHITE);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g2D.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);

		g2D.setColor(Color.GRAY);

		xPad = (getWidth() - gWidth) / 2;
		yPad = (getHeight() - gWidth) / 2;
		final Rectangle2D xrect = new Rectangle2D.Double(xPad + 10, yPad + 10,
				gWidth, 40);
		final Rectangle2D yrect = new Rectangle2D.Double(xPad + 10, yPad + 30,
				gWidth, 40);

		Ellipse2D.Double rect = new Ellipse2D.Double(((xPad + (int) x) - 4),
				((gWidth + yPad) - y) - 4, 8, 8);
		if (hasNeg) {
			rect = new Ellipse2D.Double(((xPad + (int) x) - 4),
					((gWidth / 2 + yPad) - y) - 4, 8, 8);
		}

		if (lastHighlight != null && !lastHighlight.equals(rect)) {
			repaint(lastHighlight.getBounds());
		}
		lastHighlight = rect;
		g2D.fill(lastHighlight);

		if (trackXValue) {
			if (lastXRect != null) {
				g2D.clearRect((int) lastXRect.getX(), (int) lastXRect.getY()
						- (int) lastXRect.getHeight(),
						(int) lastXRect.getWidth(),
						2 * (int) lastXRect.getHeight());
			}
		}
		if (trackYValue) {
			if (lastYRect != null) {
				g2D.clearRect((int) lastYRect.getX(), (int) lastYRect.getY()
						- (int) lastYRect.getHeight(),
						(int) lastYRect.getWidth(),
						2 * (int) lastYRect.getHeight());
			}
		}

		String xStr = getXLegendString(index);
		String yStr = getYLegendString(index);

		double xloc = xrect.getX();
		double yloc = xrect.getY();
		drawXString(g2D, xStr, (int) xloc, (int) yloc);

		xloc = yrect.getX();
		yloc = yrect.getY();
		drawYString(g2D, yStr, (int) xloc, (int) yloc);
	}

	private void drawXString(Graphics2D g2D, String val, int xloc, int yloc) {
		g2D.drawString(val, (int) xloc, (int) yloc);
		Rectangle2D bounds = g2D.getFontMetrics().getStringBounds(val, g2D);
		lastXRect = new Rectangle2D.Double(xloc, yloc, bounds.getWidth(),
				bounds.getHeight());
	}

	private void drawYString(Graphics2D g2D, String val, int xloc, int yloc) {
		g2D.drawString(val, (int) xloc, (int) yloc);
		Rectangle2D bounds = g2D.getFontMetrics().getStringBounds(val, g2D);
		lastYRect = new Rectangle2D.Double(xloc, yloc, bounds.getWidth(),
				bounds.getHeight());
	}

	protected int convertPageIndexToModel(int index) {
		int modelIndex = (pageNo * PAGE_SIZE) + index;
		return modelIndex;
	}

	protected String getXLegendString(int index) {
		int modelIndex = convertPageIndexToModel(index);
		return xPoints.get(modelIndex);
	}

	protected String getYLegendString(int index) {
		int modelIndex = convertPageIndexToModel(index);
		return xPoints.get(modelIndex);
	}

	private void scalePoints(List<BigDecimal> yPoints) {
		double min = 0;
		double max = 0;
		double mFac = 1;
		int sz = yPoints.size();
		ArrayList<Double> temp = new ArrayList<Double>();
		for (int i = 0; i < sz; i++) {
			double x = yPoints.get(i).doubleValue();
			if (x < 0)
				hasNeg = true;

			x = GraphUtils.roundDouble(x, 2);
			if (x < 0)
				temp.add(x * (-1));
			else
				temp.add(x);
			if (x < min)
				min = x;
			if (x > max)
				max = x;
			double tmp = GraphUtils.getM(x);
			if ((x > -1.0 && x < 1.0 && x != 0) && tmp > mFac) {
				mFac = tmp;
			}
		}
		ArrayList<Point2D> points = NScale.scale(temp, gWidth, mFac, PAGE_SIZE);
		int psz = points.size();
		for (int i = 0; i < psz; i++) {
			GraphPoint gp = new GraphPoint(points.get(i), xPoints.get(i),
					yPoints.get(i).doubleValue());
			scaledPoints.add(gp);
		}
	}

	class MouseMotionHandler implements MouseMotionListener {
		public void mouseDragged(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {
			highlightNearestPoint(e.getX());
		}
	}

	public static void main(String args[]) {
		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName ());
		// }
		// catch(Exception e) { }
		//
		// ArrayList<Double> dlist = new ArrayList<Double>();
		// ArrayList<String> slist = new ArrayList<String>();
		//
		// Random r = new Random();
		// dlist.add(new Double(0));
		// slist.add("0");
		// for(int i = 1; i <= 3000; i++) {
		// double x = r.nextDouble() * 1000000 + 1;
		// // if(i % 5 == 0) {
		// // x *= -1;
		// // }
		// dlist.add(x);
		// slist.add("" + i);
		// System.out.println(i + " - " + x);
		// }
		//
		// PageableLineGraph f = new PageableLineGraph();
		// f.initializeGraph(slist, dlist, 340);
		// ButtonPanel bp = f.new ButtonPanel(f);
		//
		// JFrame dc = new JFrame();
		// dc.add(new JScrollPane(f), java.awt.BorderLayout.CENTER);
		// dc.add(bp, BorderLayout.SOUTH);
		//
		// dc.pack();
		// dc.setSize(800, 440);
		// dc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// dc.setVisible(true);
	}

	static class ButtonPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private PageableLineGraph graph;
		private JButton b1, b2;

		public ButtonPanel(PageableLineGraph graph) {
			this.graph = graph;
			setLayout(new FlowLayout());

			b1 = new JButton("Next");
			b1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doNext();
				}
			});

			b2 = new JButton("Previous");
			b2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doPrevious();
				}
			});
			add(b1);
			add(b2);
		}

		private void doPrevious() {
			if (!graph.hasPrevious()) {
				b1.setEnabled(true);
				b2.setEnabled(false);
			}
			else {
				graph.previousPage();
			}
		}

		private void doNext() {
			if (!graph.hasNext()) {
				b1.setEnabled(false);
				b2.setEnabled(true);
			}
			else {
				graph.nextPage();
			}
		}
	}
}
