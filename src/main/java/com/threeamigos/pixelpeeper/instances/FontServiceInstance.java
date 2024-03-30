package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.ui.FontServiceImpl;
import com.threeamigos.common.util.interfaces.ui.FontService;

public class FontServiceInstance {

    private static final FontService instance = new FontServiceImpl();

    public static FontService get() {
        return instance;
    }

    private FontServiceInstance() {
    }
}
