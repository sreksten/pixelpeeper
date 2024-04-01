package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface NamePatternPreferences extends Preferences {

    String NAME_PATTERN_PREFERENCES_DEFAULT = "{FILENAME}";

    default String getDescription() {
        return "Name pattern preferences";
    }

    void setNamePattern(String namePattern);

    String getNamePattern();

}
