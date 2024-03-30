package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.ExifTagsFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;

public class ExifTagsFilterInstance {

    private static final ExifTagsFilter instance = new ExifTagsFilterImpl(ExifCacheInstance.get(),
            MessageHandlerInstance.get());

    public static ExifTagsFilter get() {
        return instance;
    }

    private ExifTagsFilterInstance() {
    }
}
