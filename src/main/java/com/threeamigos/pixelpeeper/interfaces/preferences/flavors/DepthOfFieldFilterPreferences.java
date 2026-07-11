package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface DepthOfFieldFilterPreferences extends Preferences {

    /**
     * Denominator used in the Circle of Confusion formula: CoC = sensor_diagonal / denominator.
     * The conventional value of 1500 is a widely accepted standard that targets a 25 cm
     * viewing distance with a 8×10 inch print.
     */
    int COC_DENOMINATOR_DEFAULT = 1500;
    int COC_DENOMINATOR_MIN = 1000;
    int COC_DENOMINATOR_MAX = 2000;

    default String getDescription() {
        return "Depth of Field filter preferences";
    }

    int getCocDenominator();

    void setCocDenominator(int cocDenominator);
}
