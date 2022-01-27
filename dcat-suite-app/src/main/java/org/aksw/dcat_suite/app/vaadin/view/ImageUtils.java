package org.aksw.dcat_suite.app.vaadin.view;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageUtils {
	
	public static BufferedImage scaleImage(BufferedImage img, Dimension requestedSize) {
		Dimension oldSize = new Dimension(img.getWidth(), img.getHeight());
		Dimension finalSize = getScaledDimension(oldSize, requestedSize);
		Image scaled = img.getScaledInstance((int)finalSize.getWidth(), (int)finalSize.getHeight(), Image.SCALE_SMOOTH);
		
		BufferedImage result = new BufferedImage((int)finalSize.getWidth(), (int)finalSize.getHeight(), img.getType());
		result.getGraphics().drawImage(scaled, 0, 0 , null);
		
		return result;
	}
	
	/**
	 * Scale (grow/shrink) image dimensions to a given target dimension while retaining aspect ratio.
	 * 
	 * https://stackoverflow.com/questions/10245220/java-image-resize-maintain-aspect-ratio
	 * 
	 * @param imgSize
	 * @param boundary
	 * @return
	 */
	public static Dimension getScaledDimension(Dimension imageSize, Dimension boundary) {

		double w = imageSize.getWidth();
		double h = imageSize.getHeight();

	    double widthRatio = w == 0.0 ? 0.0 : boundary.getWidth() / w;
	    double heightRatio = h == 0.0 ? 0.0 : boundary.getHeight() / h;
	    double ratio = Math.min(widthRatio, heightRatio);

	    return new Dimension((int) (imageSize.width  * ratio),
	                         (int) (imageSize.height * ratio));
	}
}
