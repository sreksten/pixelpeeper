package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface CannyEdgesDetectorPreferences extends Preferences {

    float LOW_THRESHOLD_PREFERENCES_DEFAULT = 2.5f;
    float HIGH_THRESHOLD_PREFERENCES_DEFAULT = 7.5f;
    float GAUSSIAN_KERNEL_RADIUS_DEFAULT = 2f;
    int GAUSSIAN_KERNEL_WIDTH_DEFAULT = 16;
    boolean CONTRAST_NORMALIZED_DEFAULT = false;

    default String getDescription() {
        return "Canny Edges Detector preferences";
    }

    float getLowThreshold();

    void setLowThreshold(float lowThreshold);

    float getHighThreshold();

    void setHighThreshold(float highThreshold);

    float getGaussianKernelRadius();

    void setGaussianKernelRadius(float gaussianKernelRadius);

    int getGaussianKernelWidth();

    void setGaussianKernelWidth(int gaussianKernelWidth);

    boolean isContrastNormalized();

    void setContrastNormalized(boolean contrastNormalized);

}
