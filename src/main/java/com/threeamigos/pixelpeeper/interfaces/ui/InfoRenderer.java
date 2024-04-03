package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.*;

/**
 * An interface that, given an image, draws related information (e.g. filename, dimensions, EXIF tags)
 * atop of another Graphics.
 *
 * @author Stefano Reksten
 */
public interface InfoRenderer {

    /**
     * Horizontal spacing from left
     */
    int HSPACING = 5;
    /**
     * Vertical spacing between different lines of text
     */
    int VSPACING = 5;

    int FILENAME_FONT_HEIGHT = 32;
    int TAG_FONT_HEIGHT = 16;

    /**
     * Clears the InfoRenderer. Useful if, for example, instead of simply calling the drawText library function,
     * it pre-renders an image, that should be recalculated if the information contained within must change.
     */
    void reset();

    /**
     * Draws information about the image
     *
     * @param graphics Graphics to render unto
     * @param x        initial x coordinate
     * @param y        initial y coordinate
     */
    void render(Graphics graphics, int x, int y);

}
