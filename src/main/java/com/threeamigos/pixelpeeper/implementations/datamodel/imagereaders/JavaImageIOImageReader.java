package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * An implementation of {@link ImageReader} that uses the standard Java capabilities.
 */
public class JavaImageIOImageReader implements ImageReader {

    @Override
    public BufferedImage readImage(File file) throws Exception {
        return ImageIO.read(file);
    }

}
