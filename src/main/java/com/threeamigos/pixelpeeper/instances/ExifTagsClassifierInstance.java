package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.ExifTagsClassifierImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;

public class ExifTagsClassifierInstance {

    private static final ExifTagsClassifier instance = new ExifTagsClassifierImpl();

    public static ExifTagsClassifier get() {
        return instance;
    }

    private ExifTagsClassifierInstance() {
    }
}
