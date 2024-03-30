package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.persistence.file.rootpathprovider.RootPathProviderImpl;
import com.threeamigos.common.util.interfaces.persistence.file.RootPathProvider;

public class RootPathProviderInstance {

    private static final RootPathProvider instance;

    static {
        instance = new RootPathProviderImpl(com.threeamigos.pixelpeeper.Main.class, MessageHandlerInstance.get());
        if (instance.hasUnrecoverableErrors()) {
            System.exit(0);
        }
    }

    public static RootPathProvider get() {
        return instance;
    }

    private RootPathProviderInstance() {
    }
}
