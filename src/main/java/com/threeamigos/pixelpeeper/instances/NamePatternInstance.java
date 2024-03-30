package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.NamePatternImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.NamePattern;

public class NamePatternInstance {

    private static final NamePattern instance = new NamePatternImpl(Preferences.NAME_PATTERN, ExifCacheInstance.get());

    public static NamePattern get() {
        return instance;
    }

    private NamePatternInstance() {
    }
}
