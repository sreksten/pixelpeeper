package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author Stefano Reksten
 */
public class CompositeImageReader implements ImageReader {

    private final ImageHandlingPreferences imageHandlingPreferences;

    public CompositeImageReader(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public BufferedImage readImage(File file) throws Exception {

        String filename = file.getName();
        ImageReader imageReader;

        if (filename.toUpperCase().endsWith(".RW2")) {
            imageReader = new PanasonicRawImageReader();
        } else if  (filename.toUpperCase().endsWith(".CR3")) {
            imageReader = new CanonCr3RawImageReader();
        } else {
            ImageReaderFlavor imageReaderFlavor = imageHandlingPreferences.getImageReaderFlavor();
            if (imageReaderFlavor == ImageReaderFlavor.JAVA) {
                imageReader = new JavaImageIOImageReader();
            } else if (imageReaderFlavor == ImageReaderFlavor.APACHE_COMMONS_IMAGING) {
                imageReader = new ApacheCommonsImagingImageReader();
            } else {
                throw new IllegalArgumentException("No image reader was defined for flavor "
                        + imageReaderFlavor.getDescription());
            }
        }

        return imageReader.readImage(file);
    }

}
