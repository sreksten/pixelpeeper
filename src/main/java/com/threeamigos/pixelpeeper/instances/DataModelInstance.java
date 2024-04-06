package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.DataModelBuilderImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;

public class DataModelInstance {

    private static final DataModel instance = DataModelBuilderImpl.builder()
            .withMessageHandler(MessageHandlerInstance.get())
            .withSessionPreferences(Preferences.SESSION)
            .withImageSlices(ImageSlicesInstance.get())
            .withImageHandlingPreferences(Preferences.IMAGE_HANDLING)
            .withExifCache(ExifCacheInstance.get())
            .withExifImageReader(ExifImageReaderInstance.get())
            .withExifTagsClassifier(ExifTagsClassifierInstance.get())
            .withExifTagsFilter(ExifTagsFilterInstance.get())
            .build();

    public static DataModel get() {
        return instance;
    }

    private DataModelInstance() {
    }
}
