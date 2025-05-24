package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

/**
 * Preferences for the Windows 3.11 filter
 *
 * @author Stefano Reksten
 */
public interface Windows311PaletteFilterPreferences extends PaletteFilterPreferences {

    default String getDescription() {
        return "Windows 3.11 Palette Filter preferences";
    }
}
