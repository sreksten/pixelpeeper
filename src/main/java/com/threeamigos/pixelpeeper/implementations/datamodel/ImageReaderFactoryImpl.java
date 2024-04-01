package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.ApacheCommonsImagingImageReader;
import com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders.JavaImageIOImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

public class ImageReaderFactoryImpl implements ImageReaderFactory {

    private final ImageHandlingPreferences imageHandlingPreferences;

    public ImageReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public ImageReader getImageReader() {
        ImageReaderFlavour imageReaderFlavour = imageHandlingPreferences.getImageReaderFlavour();
        if (imageReaderFlavour == ImageReaderFlavour.JAVA) {
            return new JavaImageIOImageReader();
        } else if (imageReaderFlavour == ImageReaderFlavour.APACHE_COMMONS_IMAGING) {
            return new ApacheCommonsImagingImageReader();
        } else {
            throw new IllegalArgumentException("No image reader was defined for flavour "
                    + imageReaderFlavour.getDescription());
        }
    }

}
