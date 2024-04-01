package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.implementations.datamodel.metadatareaders.DrewNoakesExifReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifReader;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifReaderFactory;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;

import java.util.Objects;

public class ExifReaderFactoryImpl implements ExifReaderFactory {

    private final ImageHandlingPreferences imageHandlingPreferences;

    public ExifReaderFactoryImpl(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
    }

    @Override
    public ExifReader getExifReader() {
        if (Objects.requireNonNull(imageHandlingPreferences.getExifReaderFlavour()) == ExifReaderFlavour.DREW_NOAKES) {
            return new DrewNoakesExifReader();
        }
        throw new IllegalArgumentException("No Exif reader was defined for flavour "
                + imageHandlingPreferences.getImageReaderFlavour().getDescription());
    }

}
