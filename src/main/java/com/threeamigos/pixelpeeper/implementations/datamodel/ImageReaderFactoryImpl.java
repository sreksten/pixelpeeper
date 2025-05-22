package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.ApacheCommonsImagingImageReader;
import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.JavaImageIOImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;

public class ImageReaderFactoryImpl implements ImageReaderFactory {

    private final ImageHandlingPreferences imageHandlingPreferences;

    public ImageReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public ImageReader getImageReader() {
        ImageReaderFlavor imageReaderFlavor = imageHandlingPreferences.getImageReaderFlavor();
        if (imageReaderFlavor == ImageReaderFlavor.JAVA) {
            return new JavaImageIOImageReader();
        } else if (imageReaderFlavor == ImageReaderFlavor.APACHE_COMMONS_IMAGING) {
            return new ApacheCommonsImagingImageReader();
        } else {
            throw new IllegalArgumentException("No image reader was defined for flavor "
                    + imageReaderFlavor.getDescription());
        }
    }

}
