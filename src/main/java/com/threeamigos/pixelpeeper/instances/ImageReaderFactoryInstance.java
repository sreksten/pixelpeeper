package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.ImageReaderFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReaderFactory;

public class ImageReaderFactoryInstance {

    private static final ImageReaderFactory instance = new ImageReaderFactoryImpl(Preferences.IMAGE_HANDLING);

    public static ImageReaderFactory get() {
        return instance;
    }

    private ImageReaderFactoryInstance() {
    }
}
