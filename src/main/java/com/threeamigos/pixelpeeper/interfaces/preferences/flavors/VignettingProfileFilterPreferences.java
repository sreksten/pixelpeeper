package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface VignettingProfileFilterPreferences extends Preferences {

    int RING_COUNT_DEFAULT = 10;
    int RING_COUNT_MIN = 5;
    int RING_COUNT_MAX = 30;

    default String getDescription() {
        return "Vignetting Profile filter preferences";
    }

    int getRingCount();

    void setRingCount(int ringCount);
}
