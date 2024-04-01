package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.InfoRendererFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRendererFactory;

public class InfoRendererFactoryInstance {

    private static final InfoRendererFactory instance = new InfoRendererFactoryImpl(FontServiceInstance.get(),
            Preferences.EXIF_TAG);

    private InfoRendererFactoryInstance() {
    }

    public static InfoRendererFactory get() {
        return instance;
    }
}
