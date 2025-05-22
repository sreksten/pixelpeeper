package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.FilterFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFactory;

public class FilterFactoryInstance {

    private static final FilterFactory instance =
            new FilterFactoryImpl(
                    Preferences.FILTER,
                    Preferences.CANNY_EDGES_DETECTOR_FILTER,
                    Preferences.ROMY_JONA_FILTER,
                    Preferences.ZX_SPECTRUM_PALETTE_FILTER,
                    Preferences.C64_PALETTE_FILTER,
                    new SwingMessageHandler());

    public static FilterFactory get() {
        return instance;
    }

    private FilterFactoryInstance() {
    }
}
