package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.DataModelImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;

public class DataModelInstance {

    private static final DataModel instance = new DataModelImpl(TagsClassifierInstance.get(), ImageSlicesInstance.get(),
            Preferences.IMAGE_HANDLING, Preferences.SESSION, Preferences.EDGES_DETECTOR, ExifCacheInstance.get(),
            ExifImageReaderInstance.get(), ExifTagsFilterInstance.get(), MessageHandlerInstance.get());

    public static DataModel get() {
        return instance;
    }

    private DataModelInstance() {
    }
}
