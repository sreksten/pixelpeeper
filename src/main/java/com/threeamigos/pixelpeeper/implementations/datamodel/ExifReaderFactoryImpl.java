package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.implementations.datamodel.metadatareaders.DrewNoakesExifReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ImageHandlingPreferences;

import java.util.Objects;

public class ExifReaderFactoryImpl implements ExifReaderFactory {

    private final ImageHandlingPreferences imageHandlingPreferences;

    public ExifReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public ExifReader getExifReader() {
        if (Objects.requireNonNull(imageHandlingPreferences.getExifReaderFlavor()) == ExifReaderFlavor.DREW_NOAKES) {
            return new DrewNoakesExifReader();
        }
        throw new IllegalArgumentException("No Exif reader was defined for flavor "
                + imageHandlingPreferences.getImageReaderFlavor().getDescription());
    }

}
