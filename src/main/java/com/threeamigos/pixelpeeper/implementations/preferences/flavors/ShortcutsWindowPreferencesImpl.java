package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ShortcutsWindowPreferences;

public class ShortcutsWindowPreferencesImpl extends AbstractSecondaryWindowPreferencesImpl
        implements ShortcutsWindowPreferences {

    @Override
    public void validate() {
        checkBoundaries("shortcuts");
    }

}
