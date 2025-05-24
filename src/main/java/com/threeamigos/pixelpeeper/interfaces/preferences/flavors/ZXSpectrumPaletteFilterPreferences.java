package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

/**
 * Preferences for the ZX Spectrum filter
 *
 * @author Stefano Reksten
 */
public interface ZXSpectrumPaletteFilterPreferences extends ColorClashPaletteFilterPreferences {

    default String getDescription() {
        return "ZX Spectrum Palette Filter preferences";
    }
}
