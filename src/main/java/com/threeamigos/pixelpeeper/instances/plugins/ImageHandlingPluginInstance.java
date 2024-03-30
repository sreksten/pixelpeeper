package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.ImageHandlingPlugin;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class ImageHandlingPluginInstance {

    private static final ImageHandlingPlugin instance = new ImageHandlingPlugin(Preferences.IMAGE_HANDLING);

    public static ImageHandlingPlugin get() {
        return instance;
    }

    private ImageHandlingPluginInstance() {
    }
}
