package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.ExifImageReaderImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;

public class ExifImageReaderInstance {

    private static final ExifImageReader instance = new ExifImageReaderImpl(Preferences.IMAGE_HANDLING,
            ImageReaderFactoryInstance.get(), ExifCacheInstance.get(), Preferences.EDGES_DETECTOR,
            EdgesDetectorFactoryInstance.get(), MessageHandlerInstance.get());

    public static ExifImageReader get() {
        return instance;
    }

    private ExifImageReaderInstance() {
    }
}
