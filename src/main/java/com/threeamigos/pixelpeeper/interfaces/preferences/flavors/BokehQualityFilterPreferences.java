package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Tuneable parameters for the Bokeh Quality filter.
 *
 * @author Stefano Reksten
 */
public interface BokehQualityFilterPreferences extends Preferences {

    /**
     * Laplacian variance threshold that divides in-focus patches from out-of-focus patches.
     * Patches with variance above this value are classified as in-focus (sharp).
     * Increase for very sharp images; decrease for soft or low-contrast subjects.
     */
    int SHARPNESS_THRESHOLD_DEFAULT = 80;
    int SHARPNESS_THRESHOLD_MIN = 5;
    int SHARPNESS_THRESHOLD_MAX = 500;

    /**
     * Side length (in pixels) of the non-overlapping square patches used for the local
     * sharpness analysis.  Smaller patches capture finer focus transitions; larger patches
     * are more stable on noisy images.
     */
    int PATCH_SIZE_DEFAULT = 16;
    int PATCH_SIZE_MIN = 8;
    int PATCH_SIZE_MAX = 48;

    default String getDescription() {
        return "Bokeh quality filter preferences";
    }

    int getSharpnessThreshold();
    void setSharpnessThreshold(int sharpnessThreshold);

    int getPatchSize();
    void setPatchSize(int patchSize);
}
