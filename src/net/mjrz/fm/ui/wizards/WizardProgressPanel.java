package net.mjrz.fm.ui.wizards;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import net.mjrz.fm.ui.utils.UIDefaults;

public class WizardProgressPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public WizardProgressPanel() {
		super();
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
