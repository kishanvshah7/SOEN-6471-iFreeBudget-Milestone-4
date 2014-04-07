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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class DateChooser extends JPanel implements CalendarWidgetListener {

	private static final long serialVersionUID = 1L;

	private SimpleDateFormat dateFormat = null;

	private JTextField dateField;

	private boolean showHours = false;

	private boolean calendarShown = false;

	private static Logger logger = Logger.getLogger(DateChooser.class);

	public DateChooser(final boolean showHours, SimpleDateFormat dateFormat) {
		super();
		this.dateFormat = dateFormat;
		this.showHours = showHours;

		try {
			dateField = new DateField();
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			dateField = new JTextField();
		}

		setLayout(new GridBagLayout());

		GridBagConstraints g1 = new GridBagConstraints();
		g1.gridx = 0;
		g1.gridy = 0;
		g1.weightx = 1;
		g1.fill = GridBagConstraints.HORIZONTAL;
		g1.anchor = GridBagConstraints.LINE_START;
		g1.insets = new Insets(0, 0, 0, 0);
		add(dateField, g1);

		// Border tfBorder = dateField.getBorder();
		// dateField.setBorder(BorderFactory.createEmptyBorder());
		// calButton.setBorder(BorderFactory.createEmptyBorder());
		// setBorder(tfBorder);
	}

	private void showCal(final Point p) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (calendarShown) {
					return;
				}
				CalendarWidget calendar = null;

				Window win = SwingUtilities.getWindowAncestor(DateChooser.this);

				if (showHours) {
					calendar = new CalendarWidget(win, DateChooser.this, true);
				}
				else {
					calendar = new CalendarWidget(win, DateChooser.this, false);
				}
				calendar.setLocation(p);
				calendar.pack();

				// int w = (int) dateField.getSize().getWidth();
				// w += (int) calButton.getSize().getWidth();
				//
				// if(w < 210) {
				// w = 210;
				// }
				int w = 210;
				calendar.setSize(new Dimension(w, 200));
				calendar.setVisible(true);
				calendarShown = true;
			}
		});
	}

	@Override
	public void setDate(Date date) {
		calendarShown = false;
		if (date != null) {
			String obj = dateFormat.format(date);
			dateField.setText(obj);
		}
	}

	public Date getDate() {
		try {
			String txt = dateField.getText();
			return dateFormat.parse(txt);
		}
		catch (Exception e) {
			Logger.getLogger(DateChooser.class).error(
					MiscUtils.stackTrace2String(e));
			return null;
		}
	}

	public void setText(String text) {
		Date date;
		try {
			date = dateFormat.parse(text);
			setDate(date);
		}
		catch (ParseException e) {
			e.printStackTrace();
			setDate(new Date());
		}
	}

	public String getText() {
		return dateField.getText();
	}

	public boolean isShowHours() {
		return showHours;
	}

	public void setShowHours(boolean showHours) {
		this.showHours = showHours;
	}

	class DateField extends JTextField {

		private static final long serialVersionUID = 1L;

		private BufferedImage img = null;

		private int iconX = -1;

		private int iconY = -1;

		DateField() throws Exception {
			super();
			this.img = ImageIO.read(getClass().getClassLoader().getResource(
					"icons/calendar.png"));
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (checkLocation(e)) {
						showCal(getCalendarLoc());
					}
				}
			});
			this.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int kc = e.getKeyCode();
					if (kc == KeyEvent.VK_DOWN) {
						showCal(getCalendarLoc());
					}
				}
			});
			this.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					if (checkLocation(e)) {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
					else {
						setCursor(Cursor
								.getPredefinedCursor(Cursor.TEXT_CURSOR));
					}
				}
			});
			super.setOpaque(false);
		}

		private Point getCalendarLoc() {
			Point p1 = dateField.getLocationOnScreen();
			double y = p1.getY();
			y += dateField.getSize().getHeight();
			y += 2;
			p1.setLocation(p1.getX(), y);
			return p1;
		}

		private boolean checkLocation(MouseEvent e) {
			if (e.getX() > iconX && e.getX() < getWidth()) {
				return true;
			}
			return false;
		}

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			iconX = getWidth() - (img.getWidth()) - 2;
			iconY = (getHeight() - img.getHeight()) / 2;

			g2D.setColor(Color.WHITE);
			g2D.fillRect(0, 0, getWidth(), getHeight());
			g2D.drawImage(img, iconX, iconY, null);
			super.paintComponent(g);
		}
	}
}
