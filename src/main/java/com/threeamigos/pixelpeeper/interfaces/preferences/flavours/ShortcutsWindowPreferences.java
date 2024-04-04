package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;

public interface ShortcutsWindowPreferences extends SecondaryWindowPreferences {

    boolean OPEN_IMMEDIATELY_DEFAULT = true;

    boolean isOpenImmediately();

    void setOpenImmediately(boolean openImmediately);

    default String getDescription() {
        return "Shortcuts window preferences";
    }

}
