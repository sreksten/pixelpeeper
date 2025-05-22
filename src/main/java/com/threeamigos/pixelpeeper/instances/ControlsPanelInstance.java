package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.ControlsPanel;

public class ControlsPanelInstance {

    private static final ControlsPanel instance = new ControlsPanel(Preferences.IMAGE_HANDLING, Preferences.DOODLING,
            DataModelInstance.get());

    public static ControlsPanel get() {
        return instance;
    }

    private ControlsPanelInstance() {
    }
}
