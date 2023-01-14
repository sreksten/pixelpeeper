package com.threeamigos.imageviewer.implementations.helpers;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;

public class ImageDrawHelper {

	public static void drawTransparentImageAtop(Graphics2D graphics, Image backgroundImage, Image foregroundImage,
			int x, int y, int transparency) {
		if (backgroundImage != null) {
			graphics.drawImage(backgroundImage, x, y, null);

			if (foregroundImage != null) {
				Composite originalAc = graphics.getComposite();

				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparency / 100.0f);
				graphics.setComposite(ac);

				graphics.drawImage(foregroundImage, x, y, null);

				graphics.setComposite(originalAc);
			}
		}
	}

}
