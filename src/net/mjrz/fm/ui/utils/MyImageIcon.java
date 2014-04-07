package net.mjrz.fm.ui.utils;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class MyImageIcon extends ImageIcon {

	private static final long serialVersionUID = 1L;

	public MyImageIcon(String fileName) {
		super(MyImageIcon.class.getClassLoader().getResource(fileName));
	}

	public MyImageIcon(Image image) {
		super(image);
	}

	public MyImageIcon(URL location) {
		super(location);
	}
}
