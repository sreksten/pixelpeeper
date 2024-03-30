package com.threeamigos.pixelpeeper.instances.plugins;

import com.threeamigos.pixelpeeper.implementations.ui.plugins.CursorPlugin;
import com.threeamigos.pixelpeeper.instances.CursorManagerInstance;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class CursorPluginInstance {

    private static final CursorPlugin instance = new CursorPlugin(Preferences.CURSOR, CursorManagerInstance.get());

    public static CursorPlugin get() {
        return instance;
    }

    private CursorPluginInstance() {
    }
}
