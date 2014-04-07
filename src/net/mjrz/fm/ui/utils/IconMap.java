package net.mjrz.fm.ui.utils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.CategoryIconMap;
import net.mjrz.fm.utils.MiscUtils;

import org.apache.log4j.Logger;

public class IconMap {
	private static IconMap instance = new IconMap();

	public static IconMap getInstance() {
		return instance;
	}

	private Map<Long, String> catToLocMap = null;

	private Map<String, ImageIcon> map = null;
	private FManEntityManager em = null;
	private Logger logger = Logger.getLogger(getClass());

	private IconMap() {
		em = new FManEntityManager();

		map = new HashMap<String, ImageIcon>();
		catToLocMap = new HashMap<Long, String>();
		try {
			List<?> objs = em.getObjects("CategoryIconMap", null);
			for (Object o : objs) {
				CategoryIconMap m = (CategoryIconMap) o;
				catToLocMap.put(m.getCategoryId(), m.getIconPath());
			}
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
		}
	}

	public void setIcon(Long categoryId, String loc, ImageIcon icon) {
		catToLocMap.put(categoryId, loc);
		map.put(loc, icon);
	}

	public ImageIcon getIcon(Long categoryId) {
		try {
			String file = catToLocMap.get(categoryId);
			if (file == null) {
				return null;
			}

			ImageIcon ic = map.get(file);

			if (ic == null) {
				File f = new File(file);
				if (f.exists()) {
					URL url = f.toURI().toURL();
					ic = new net.mjrz.fm.ui.utils.MyImageIcon(url);
					map.put(file, ic);
				}
			}
			return ic;
		}
		catch (Exception e) {
			logger.error(MiscUtils.stackTrace2String(e));
			return null;
		}
	}
}
