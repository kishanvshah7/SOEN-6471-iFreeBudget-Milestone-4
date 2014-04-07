package net.mjrz.fm.ui.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.mjrz.fm.Main;
import net.mjrz.fm.entity.FManEntityManager;
import net.mjrz.fm.entity.beans.AccountCategory;
import net.mjrz.fm.entity.beans.CategoryIconMap;
import net.mjrz.fm.utils.MiscUtils;
import net.mjrz.fm.utils.ZProperties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ImportCategoryIcon {
	private String errMsg = null;
	private FManEntityManager em = null;
	private final int LEN = 24;
	private static final Logger logger = Logger
			.getLogger(ImportCategoryIcon.class);

	public ImportCategoryIcon() {
		em = new FManEntityManager();
	}

	public String getErrMsg() {
		return errMsg;
	}

	public boolean importIcon(AccountCategory ac, File f) {
		try {
			BufferedImage orig = ImageIO.read(f);
			return doImport(ac, f.getName(), orig);
		}
		catch (Exception e) {
			errMsg = "Image could not be loaded";
			logger.error(MiscUtils.stackTrace2String(e));
			return false;
		}
	}

	public boolean importIcon(AccountCategory ac, String fileName) {
		try {
			BufferedImage orig = ImageIO.read(getClass().getClassLoader()
					.getResource(fileName));
			return doImport(ac, fileName, orig);
		}
		catch (Exception e) {
			errMsg = "Image could not be loaded";
			logger.error(MiscUtils.stackTrace2String(e));
			return false;
		}
	}

	private boolean doImport(AccountCategory ac, String fileName,
			BufferedImage orig) {
		try {
			if (orig != null) {
				BufferedImage newImg = null;
				if (orig.getWidth() == LEN && orig.getHeight() == 24) {
					newImg = orig;
				}
				else {
					newImg = getScaledInstance(orig, LEN, LEN,
							RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
				}

				ImageIcon ic = new net.mjrz.fm.ui.utils.MyImageIcon(newImg);

				StringBuilder path = new StringBuilder(
						ZProperties.getProperty("FMHOME"));
				path.append(Main.PATH_SEPARATOR);
				path.append("icons/");
				path.append(fileName);

				File outFile = new File(path.toString());

				if (!outFile.exists()) {
					FileUtils.touch(outFile);
				}

				ImageIO.write((RenderedImage) newImg, "png", outFile);

				CategoryIconMap cim = new CategoryIconMap();
				cim.setCategoryId(ac.getCategoryId());
				cim.setIconPath(path.toString());

				em.saveOrUpdateObject("CategoryIconMap", cim);

				IconMap.getInstance().setIcon(ac.getCategoryId(),
						path.toString(), ic);
				return true;
			}
			errMsg = "Image could not be loaded";
		}
		catch (Exception e) {
			errMsg = "Image could not be loaded";
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Convenience method that returns a scaled instance of the provided
	 * {@code BufferedImage}.
	 * 
	 * @param img
	 *            the original image to be scaled
	 * @param targetWidth
	 *            the desired width of the scaled instance, in pixels
	 * @param targetHeight
	 *            the desired height of the scaled instance, in pixels
	 * @param hint
	 *            one of the rendering hints that corresponds to
	 *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality
	 *            if true, this method will use a multi-step scaling technique
	 *            that provides higher quality than the usual one-step technique
	 *            (only useful in downscaling cases, where {@code targetWidth}
	 *            or {@code targetHeight} is smaller than the original
	 *            dimensions, and generally only when the {@code BILINEAR} hint
	 *            is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	public BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
			int targetHeight, Object hint, boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		}
		else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		}
		while (w != targetWidth || h != targetHeight);

		return ret;
	}
}
