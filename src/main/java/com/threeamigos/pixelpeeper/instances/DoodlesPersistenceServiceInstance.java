package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.datamodel.DoodlesPersistenceService;

public class DoodlesPersistenceServiceInstance {

    private static final DoodlesPersistenceService instance = new DoodlesPersistenceService();

    public static DoodlesPersistenceService get() {
        return instance;
    }

    private DoodlesPersistenceServiceInstance() {
    }
}
