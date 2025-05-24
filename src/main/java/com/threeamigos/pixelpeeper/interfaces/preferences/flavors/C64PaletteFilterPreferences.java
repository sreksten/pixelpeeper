package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

/**
 * Preferences for the C64 filter
 *
 * @author Stefano Reksten
 */
public interface C64PaletteFilterPreferences extends ColorClashPaletteFilterPreferences {

    default String getDescription() {
        return "C64 Palette Filter preferences";
    }
}
