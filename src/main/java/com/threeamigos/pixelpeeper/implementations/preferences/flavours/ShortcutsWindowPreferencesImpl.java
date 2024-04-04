package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ShortcutsWindowPreferences;

public class ShortcutsWindowPreferencesImpl extends AbstractSecondaryWindowPreferencesImpl
        implements ShortcutsWindowPreferences {

    @Override
    public void validate() {
        checkBoundaries("shortcuts");
    }

}
