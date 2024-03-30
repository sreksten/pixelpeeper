package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.TagsClassifierImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;

public class TagsClassifierInstance {

    private static final TagsClassifier instance = new TagsClassifierImpl();

    public static TagsClassifier get() {
        return instance;
    }

    private TagsClassifierInstance() {
    }
}
