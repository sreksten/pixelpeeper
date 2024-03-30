package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.ExifTagsPlugin;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class ExifTagsPluginInstance {

    private static final ExifTagsPlugin instance = new ExifTagsPlugin(Preferences.EXIF_TAG);

    public static ExifTagsPlugin get() {
        return instance;
    }

    private ExifTagsPluginInstance() {
    }
}
