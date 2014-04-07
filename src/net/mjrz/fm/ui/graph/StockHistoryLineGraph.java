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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

public class StockHistoryLineGraph extends PageableLineGraph {
	private static final long serialVersionUID = 1L;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy");
	private final int WIDTH = 440;
	private List<String> dates;
	private List<BigDecimal> amounts;

	public StockHistoryLineGraph(java.util.List<String> xp,
			java.util.List<BigDecimal> yp, int width) {
		super.initializeGraph(xp, yp, width);
		dates = xp;
		amounts = yp;
		addMouseMotionListener(new MyMouseMotionListener());
		addMouseListener(new MyMouseListener());
	}

	protected void highlightGraph(int gWidth, double x, double y, int index) {
		super.highlightGraph(gWidth, x, y, index);
	}

	protected String getXLegendString(int index) {
		int modelIndex = convertPageIndexToModel(index);
		String date = dates.get(modelIndex);
		try {
			Date d = sdf.parse(dates.get(modelIndex));
			date = displayFormat.format(d);
		}
		catch (Exception e) {
			date = dates.get(modelIndex);
		}

		StringBuilder ret = new StringBuilder();
		ret.append(date);
		ret.append(" Price:");
		ret.append(amounts.get(modelIndex));
		return ret.toString();
	}

	protected String getYLegendString(int index) {
		return "";
	}

	protected void draw(Graphics g) {
		super.draw(g);
	}

	private void updateGraph(final int direction) {
		if (direction == LEFT && !hasPrevious()) {
			return;
		}
		if (direction == RIGHT && !hasNext()) {
			return;
		}
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (direction == RIGHT)
						StockHistoryLineGraph.this.nextPage();
					else
						StockHistoryLineGraph.this.previousPage();
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double dragStart = -1;
	private double dragCurr = -1;
	boolean onDrag = false;
	final int LEFT = 1;
	final int RIGHT = 2;

	class MyMouseMotionListener extends MouseAdapter {

		@Override
		public void mouseDragged(MouseEvent e) {
			onDrag = true;
			Point p = e.getPoint();
			double x = p.getX();
			if (dragStart <= 0) {
				dragStart = x;
			}
			dragCurr = x;
		}
	}

	class MyMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			StockHistoryLineGraph.this.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			StockHistoryLineGraph.this.setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (onDrag) {
				if (dragCurr >= dragStart) {
					updateGraph(LEFT);
				}
				else {
					updateGraph(RIGHT);
				}
			}
			onDrag = false;
			dragStart = -1;
			dragCurr = -1;
		}
	}
}
