package net.mjrz.fm.ui.widgets;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JLabel;

import net.mjrz.fm.ui.utils.UIDefaults;

public class GradientLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	public GradientLabel() {
		super();
	}

	public GradientLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public GradientLabel(Icon image) {
		super(image);
	}

	public GradientLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public GradientLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public GradientLabel(String text) {
		super(text);
	}

	protected void paintComponent(Graphics g) {
		if (!isOpaque()) {
			super.paintComponent(g);
			return;
		}
		Graphics2D g2d = (Graphics2D) g;

		int w = getWidth();
		int h = getHeight();

		Color color1 = UIDefaults.DEFAULT_TABLE_HEADER_COLOR;
		Color color2 = UIDefaults.DEFAULT_TABLE_ROW_SEL_COLOR;

		GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);

		setOpaque(false);
		super.paintComponent(g);
		setOpaque(true);
	}
}
