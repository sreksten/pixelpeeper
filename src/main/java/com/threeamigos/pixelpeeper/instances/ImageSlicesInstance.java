package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.ImageSlicesImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlices;

public class ImageSlicesInstance {

    private static final ImageSlices instance = new ImageSlicesImpl(ExifTagsClassifierInstance.get(),
            InfoRendererFactoryInstance.get(), Preferences.IMAGE_HANDLING,
            Preferences.DOODLING, Preferences.FILTER, FontServiceInstance.get());

    public static ImageSlices get() {
        return instance;
    }

    private ImageSlicesInstance() {
    }
}
