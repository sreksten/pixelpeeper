package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;
import org.apache.commons.imaging.Imaging;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * An implementation of {@link ImageReader} that uses the Apache Commons Imaging suite.
 *
 * @author Stefano Reksten
 */
public class ApacheCommonsImagingImageReader implements ImageReader {

    @Override
    public BufferedImage readImage(File file) throws Exception {
        return Imaging.getBufferedImage(file);
    }

}
