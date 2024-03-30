package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.NamePatternSelectorImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.NamePatternSelector;

public class NamePatternSelectorInstance {

    private static final NamePatternSelector instance = new NamePatternSelectorImpl(Preferences.NAME_PATTERN);

    public static NamePatternSelector get() {
        return instance;
    }

    private NamePatternSelectorInstance() {
    }
}
