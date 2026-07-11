package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface NoiseEstimatorFilterPreferences extends Preferences {

    int PATCH_SIZE_DEFAULT = 16;
    int PATCH_SIZE_MIN = 8;
    int PATCH_SIZE_MAX = 64;

    int FLAT_VARIANCE_THRESHOLD_DEFAULT = 30;
    int FLAT_VARIANCE_THRESHOLD_MIN = 5;
    int FLAT_VARIANCE_THRESHOLD_MAX = 200;

    default String getDescription() {
        return "Noise Estimator filter preferences";
    }

    int getPatchSize();

    void setPatchSize(int patchSize);

    int getFlatVarianceThreshold();

    void setFlatVarianceThreshold(int flatVarianceThreshold);
}
