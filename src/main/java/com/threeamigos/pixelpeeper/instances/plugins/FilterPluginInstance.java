package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.FilterPlugin;
import com.threeamigos.pixelpeeper.instances.FilterPreferencesSelectorFactoryInstance;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class FilterPluginInstance {

    private static final FilterPlugin instance = new FilterPlugin(Preferences.FILTER,
            FilterPreferencesSelectorFactoryInstance.get());

    public static FilterPlugin get() {
        return instance;
    }

    private FilterPluginInstance() {
    }
}
