package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.ExifCacheImpl;
import com.threeamigos.pixelpeeper.implementations.datamodel.ExifReaderFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;

public class ExifCacheInstance {

    private static final ExifCache instance = new ExifCacheImpl(new ExifReaderFactoryImpl(Preferences.IMAGE_HANDLING),
            CropFactorProviderInstance.get());

    public static ExifCache get() {
        return instance;
    }

    private ExifCacheInstance() {
    }
}
