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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JPanel;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.entity.beans.FManEntity;
import net.mjrz.fm.services.SessionManager;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class PieChart extends JPanel {

	static final long serialVersionUID = 0L;
	java.util.List<FManEntity> list = null;
	java.util.List<Color> colorList = null;

	NumberFormat numberFormat = NumberFormat.getCurrencyInstance(SessionManager
			.getCurrencyLocale());

	public static final int PIE_CHART_WIDTH = 250;
	public static final int PIE_CHART_HEIGHT = 250;

	int[] pVals = null;
	double total = 1;

	private PieChart() {
	}

	public PieChart(java.util.List<FManEntity> a) {
		super(true);
		this.list = a;

		colorList = getColors(list.size());
		setBackground(Color.WHITE);

		/*
		 * Try to guess the minimum height required Rect height is 150 Min
		 * height for each entry in legend is 30 So min height is 150 + (30 *
		 * size of list)
		 */
		int minHt = 2 * PieChart.PIE_CHART_HEIGHT + (30 * list.size());

		this.setPreferredSize(new Dimension(600, minHt));

		setBackground(Color.WHITE);

		pVals = getPercentValues();
		total = getNetValue();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public int[] getPercentValues() {
		double total = getNetValue();

		int[] percentValues = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Account a = (Account) list.get(i);
			int val = (int) Math.ceil(((100 * a.getCurrentBalance()
					.doubleValue()) / total));
			if (val > 100)
				val = 100;
			if (val < 0)
				val = 0;

			percentValues[i] = val;
		}
		return percentValues;
	}

	private double getNetValue() {
		double ret = 0;

		for (int i = 0; i < list.size(); i++) {
			Account a = (Account) list.get(i);
			ret += a.getCurrentBalance().doubleValue();
		}
		// System.out.println("Total net value = " + ret);
		return ret;
	}

	public void draw(Graphics g) {

		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		/* Width and height of pie chart bounding rectangle */
		int rh = PIE_CHART_HEIGHT;
		int rw = PIE_CHART_WIDTH;

		double radius = rw / 2;

		/* center the chart in the panel. always 20 pixels from top */
		int rSt = (getWidth() - rh) / 2;
		int rEnd = 20;

		g2D.draw(new Ellipse2D.Double(rSt, rEnd, rw, rh));

		double radians = 0;
		double lY = (int) (rh + 40);

		for (int i = 0; i < pVals.length; i++) {
			Color c = colorList.get(i);
			radians = drawPie(g2D, (int) rSt, (int) rEnd, (int) radians,
					(int) pVals[i], (int) rw, (int) rh, c);
			lY = drawLegend(i, g2D, (int) rSt, (int) lY, (int) (2 * radius),
					(int) (2 * radius), (int) pVals[i], (int) total, c);
		}
	}

	private ArrayList<Color> getColors(int sz) {
		ColorSequencer cs = new ColorSequencer();
		ArrayList<Color> ret = new ArrayList<Color>();
		while (true) {
			if (ret.size() >= sz)
				break;
			Color c = cs.next();
			if (ret.contains(c)) {
				continue;
			}
			ret.add(c);
		}
		return ret;
	}

	private double drawPie(Graphics2D g, int x, int y, int arc, int val, int w,
			int h, Color c) {
		val = (360 * val) / 100;
		g.setComposite(AlphaComposite.Src);
		g.setColor(c);

		Arc2D.Double arc2D = new Arc2D.Double(x, y, w, h, arc, val, Arc2D.PIE);
		g.fill(arc2D);
		arc += val;
		return arc;
	}

	private int drawLegend(int acctIdx, Graphics2D g, int x, int y, int w,
			int h, int percentageValue, int total, Color c) {
		Account a = (Account) list.get(acctIdx);
		String aname = a.getAccountName();
		if (aname.length() > 15) {
			aname = aname.substring(0, 15) + "...";
		}
		String sval = aname + " - "
				+ numberFormat.format(a.getCurrentBalance()) + " ( "
				+ percentageValue + "% )";

		g.setComposite(AlphaComposite.Src);
		g.setColor(c);
		Font font = new Font("lucida sans unicode", Font.PLAIN, 12);
		g.setFont(font);
		FontRenderContext frc = g.getFontRenderContext();

		int sh = (int) font.getLineMetrics(sval, frc).getAscent();

		Rectangle2D.Double rect = new Rectangle2D.Double(x, y, 20, 20);
		g.fill(rect);

		g.setColor(Color.black);
		g.drawString(sval, x + (w / 4), y + sh);

		y += (sh + 20);
		return y;
	}

	static class ColorSequencer {
		public Color current() {
			return curColor;
		}

		public Color next() {
			int r = curColor.getRed();
			if (r == 0)
				r = 1;
			int g = curColor.getGreen();
			if (g == 0)
				g = 1;
			int b = curColor.getBlue();
			if (b == 0)
				b = 1;
			// System.out.println("Old color: r=" + r + " g=" + g + " b=" + b);
			int newr = (int) (Math.random() * 43 * b) % 256;
			int newg = r;
			int newb = g;
			if (newr < 100 && newg < 100 && newb < 100) {
				if (Math.random() < 0.5)
					newg += 100;
				else
					newb += 100;
			}
			// System.out.println("New color: r=" + newr + " g=" + newg + " b="
			// + newb);
			return (curColor = new Color(newr, newg, newb));
		}

		private Color curColor = new Color(130, 251, 23);
	}
}
