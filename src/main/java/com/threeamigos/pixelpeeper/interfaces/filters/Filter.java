package com.threeamigos.pixelpeeper.interfaces.filters;

import java.awt.image.BufferedImage;

/**
 * An interface that implements a filter on the image
 *
 * @author Stefano Reksten
 */
public interface Filter {

    /**
     * Source image to apply the filter unto
     */
    void setSourceImage(BufferedImage sourceImage);

    /**
     * Starts the filter algorithm in a background process.
     * When completed, it should notify the host application.
     */
    void process();

    /**
     * Asks the filter algorithm to stop processing the image.
     * It may be e.g., because the user loads another image or changes the zoom level.
     */
    void abort();

    /**
     * Returns an image containing the filtered image only. This image can be superimposed
     * on the original (or not).
     */
    BufferedImage getResultingImage();

}
