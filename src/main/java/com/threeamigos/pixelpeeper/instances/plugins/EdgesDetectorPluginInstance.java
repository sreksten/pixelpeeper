package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.EdgesDetectorPlugin;
import com.threeamigos.pixelpeeper.instances.EdgesDetectorPreferencesSelectorFactoryInstance;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class EdgesDetectorPluginInstance {

    private static final EdgesDetectorPlugin instance = new EdgesDetectorPlugin(Preferences.EDGES_DETECTOR,
            EdgesDetectorPreferencesSelectorFactoryInstance.get());

    public static EdgesDetectorPlugin get() {
        return instance;
    }

    private EdgesDetectorPluginInstance() {
    }
}
