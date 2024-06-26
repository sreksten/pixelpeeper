package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * An interface able to produce BufferedImages from image files.
 *
 * @author Stefano Reksten
 */
@FunctionalInterface
public interface ImageReader {

    /**
     * All the actual implementations throw a generic Exception
     */
    BufferedImage readImage(File file) throws Exception; //NOSONAR

}
