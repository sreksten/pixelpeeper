package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Preferences for the ZX Spectrum filter
 *
 * @author Stefano Reksten
 */
public interface ZXSpectrumPaletteFilterPreferences extends Preferences {

    boolean COLOR_CLASH_ENABLED_DEFAULT = true;
    int SATURATION_THRESHOLD_DEFAULT = 15;
    int LIGHTNESS_MIN_THRESHOLD_DEFAULT = 10;
    int LIGHTNESS_MAX_THRESHOLD_DEFAULT = 90;
    double HUE_WEIGHT_DEFAULT = 0.8;
    double SATURATION_WEIGHT_DEFAULT = 0.1;
    double LIGHTNESS_WEIGHT_DEFAULT = 0.1;
    boolean SKIN_TONES_MAPPING_ENABLED_DEFAULT = true;

    default String getDescription() {
        return "ZX Spectrum palette filter preferences";
    }

    boolean isColorClashEnabled();

    void setColorClashEnabled(boolean colorClashEnabled);

    int getSaturationThreshold();

    void setSaturationThreshold(int saturationThreshold);

    int getLightnessMinThreshold();

    void setLightnessMinThreshold(int lightnessMinThreshold);

    int getLightnessMaxThreshold();

    void setLightnessMaxThreshold(int lightnessMaxThreshold);

    double getHueWeight();

    void setHueWeight(double hueWeight);

    double getSaturationWeight();

    void setSaturationWeight(double saturationWeight);

    double getLightnessWeight();

    void setLightnessWeight(double lightnessWeight);

    boolean isSkinTonesMappingEnabled();

    void setSkinTonesMappingEnabled(boolean skinTonesMappingEnabled);

}
