package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ColorClashPaletteFilterPreferences;

abstract class ColorClashPaletteFilterPreferencesImpl extends PaletteFilterPreferencesImpl
        implements ColorClashPaletteFilterPreferences {

    private boolean colorClashEnabled = COLOR_CLASH_ENABLED_DEFAULT;

    @Override
    public boolean isColorClashEnabled() {
        return colorClashEnabled;
    }

    @Override
    public void setColorClashEnabled(boolean colorClashEnabled) {
        boolean oldColorClashEnabled = this.colorClashEnabled;
        this.colorClashEnabled = colorClashEnabled;
    }

    @Override
    public void loadDefaultValues() {
        colorClashEnabled = COLOR_CLASH_ENABLED_DEFAULT;
        super.loadDefaultValues();
    }
}
