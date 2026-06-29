package com.threeamigos.pixelpeeper.implementations.helpers;

import java.awt.*;

public class ImageDrawHelper {

    private ImageDrawHelper() {
    }

    public static void drawTransparentImageAtop(Graphics2D graphics, Image backgroundImage, Image foregroundImage,
                                                int x, int y, int transparency) {
        if (backgroundImage == null) {
            return;
        }
        drawTransparentImageAtop(graphics, backgroundImage, foregroundImage,
                x, y, backgroundImage.getWidth(null), backgroundImage.getHeight(null), transparency);
    }

    public static void drawTransparentImageAtop(Graphics2D graphics, Image backgroundImage, Image foregroundImage,
                                                int x, int y, int width, int height, int transparency) {

        if (backgroundImage == null) {
            return;
        }

        if (transparency > 0 || foregroundImage == null) {
            graphics.drawImage(backgroundImage, x, y, width, height, null);
        }

        if (foregroundImage != null) {
            if (transparency == 0) {
                graphics.drawImage(foregroundImage, x, y, width, height, null);
            } else if (transparency < 100) {
                Composite originalAc = graphics.getComposite();
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, (100 - transparency) / 100.0f);
                graphics.setComposite(ac);
                graphics.drawImage(foregroundImage, x, y, width, height, null);
                graphics.setComposite(originalAc);
            }
        }
    }
}
