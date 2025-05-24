package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

public interface ColorClashPaletteFilterPreferences extends PaletteFilterPreferences {

    boolean COLOR_CLASH_ENABLED_DEFAULT = true;

    boolean isColorClashEnabled();

    void setColorClashEnabled(boolean colorClashEnabled);

}
