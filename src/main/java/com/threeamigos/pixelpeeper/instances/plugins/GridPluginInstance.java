package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.GridPlugin;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class GridPluginInstance {

    private static final GridPlugin instance = new GridPlugin(Preferences.GRID);

    public static GridPlugin get() {
        return instance;
    }

    private GridPluginInstance() {
    }
}
