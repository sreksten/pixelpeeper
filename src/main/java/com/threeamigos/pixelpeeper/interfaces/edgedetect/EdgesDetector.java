package com.threeamigos.pixelpeeper.interfaces.edgedetect;

import java.awt.image.BufferedImage;

/**
 * An interface that implements a particular flavour of the edge-detection algorithm
 *
 * @author Stefano Reksten
 */
public interface EdgesDetector {

    /**
     * Source image whose edges we want to detect
     *
     * @param sourceImage
     */
    void setSourceImage(BufferedImage sourceImage);

    /**
     * Starts the edge-detection algorithm in a background process.
     * When completed, it should notify the host application.
     */
    void process();

    /**
     * Asks the edge-detection algorithm to stop processing the image.
     * It may be e.g. because the user loads another image or changes the zoom level.
     */
    void abort();

    /**
     * Returns an image containing the detected edges only. This image can be superimposed
     * on the original (or not).
     */
    BufferedImage getEdgesImage();

}
