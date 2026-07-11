package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface HistogramClippingDetectorFilterPreferences extends Preferences {

    int HIGHLIGHT_THRESHOLD_DEFAULT = 253;
    int HIGHLIGHT_THRESHOLD_MIN = 240;
    int HIGHLIGHT_THRESHOLD_MAX = 255;

    int SHADOW_THRESHOLD_DEFAULT = 2;
    int SHADOW_THRESHOLD_MIN = 0;
    int SHADOW_THRESHOLD_MAX = 15;

    default String getDescription() {
        return "Histogram with Clipping Detection filter preferences";
    }

    int getHighlightThreshold();

    void setHighlightThreshold(int highlightThreshold);

    int getShadowThreshold();

    void setShadowThreshold(int shadowThreshold);
}
