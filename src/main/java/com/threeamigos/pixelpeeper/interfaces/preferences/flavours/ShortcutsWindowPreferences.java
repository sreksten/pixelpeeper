package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;

public interface ShortcutsWindowPreferences extends SecondaryWindowPreferences {

    default String getDescription() {
        return "Shortcuts window preferences";
    }

}
