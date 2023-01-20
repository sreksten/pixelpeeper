package com.threeamigos.imageviewer.implementations.helpers;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;

public class ImageDrawHelper {

	public static void drawTransparentImageAtop(Graphics2D graphics, Image backgroundImage, Image foregroundImage,
			int x, int y, int transparency) {

		if (backgroundImage == null) {
			return;
		}

		if (transparency > 0 || foregroundImage == null) {
			graphics.drawImage(backgroundImage, x, y, null);
		}

		if (foregroundImage != null) {

			if (transparency == 0) {
				graphics.drawImage(foregroundImage, x, y, null);
			} else if (transparency < 100) {
				Composite originalAc = graphics.getComposite();

				AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (100 - transparency) / 100.0f);
				graphics.setComposite(ac);

				graphics.drawImage(foregroundImage, x, y, null);

				graphics.setComposite(originalAc);
			}

		}
	}

}
