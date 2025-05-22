package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Preferences for the mass-renaming capabilities
 *
 * @author Stefano Reksten
 */
public interface NamePatternPreferences extends Preferences {

    String NAME_PATTERN_PREFERENCES_DEFAULT = "{FILENAME}";

    default String getDescription() {
        return "Name pattern preferences";
    }

    void setNamePattern(String namePattern);

    String getNamePattern();

}
