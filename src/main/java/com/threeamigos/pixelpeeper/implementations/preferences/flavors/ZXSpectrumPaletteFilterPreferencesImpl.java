package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

public class ZXSpectrumPaletteFilterPreferencesImpl extends BasicPropertyChangeAware
        implements ZXSpectrumPaletteFilterPreferences {

    private boolean colorClashEnabled = COLOR_CLASH_ENABLED_DEFAULT;
    private int saturationThreshold = SATURATION_THRESHOLD_DEFAULT;
    private int lightnessMinThreshold = LIGHTNESS_MIN_THRESHOLD_DEFAULT;
    private int lightnessMaxThreshold = LIGHTNESS_MAX_THRESHOLD_DEFAULT;
    private double hueWeight = HUE_WEIGHT_DEFAULT;
    private double saturationWeight = SATURATION_WEIGHT_DEFAULT;
    private double lightnessWeight = LIGHTNESS_WEIGHT_DEFAULT;
    private boolean skinTonesMappingEnabled = SKIN_TONES_MAPPING_ENABLED_DEFAULT;

    @Override
    public boolean isColorClashEnabled() {
        return colorClashEnabled;
    }

    @Override
    public void setColorClashEnabled(boolean colorClashEnabled) {
        boolean oldColorClashEnabled = this.colorClashEnabled;
        this.colorClashEnabled = colorClashEnabled;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_COLOR_CLASH_CHANGED, oldColorClashEnabled,
                colorClashEnabled);
    }

    @Override
    public int getSaturationThreshold() {
        return saturationThreshold;
    }

    @Override
    public void setSaturationThreshold(int saturationThreshold) {
        int oldSaturationThreshold = this.saturationThreshold;
        this.saturationThreshold = saturationThreshold;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_SATURATION_THRESHOLD_CHANGED, oldSaturationThreshold,
                saturationThreshold);
    }

    @Override
    public int getLightnessMinThreshold() {
        return lightnessMinThreshold;
    }

    @Override
    public void setLightnessMinThreshold(int lightnessMinThreshold) {
        int oldLightnessMinThreshold = this.lightnessMinThreshold;
        this.lightnessMinThreshold = lightnessMinThreshold;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_LIGHTNESS_MIN_THRESHOLD_CHANGED, oldLightnessMinThreshold,
                lightnessMinThreshold);
    }

    @Override
    public int getLightnessMaxThreshold() {
        return lightnessMaxThreshold;
    }

    @Override
    public void setLightnessMaxThreshold(int lightnessMaxThreshold) {
        int oldLightnessMaxThreshold = this.lightnessMaxThreshold;
        this.lightnessMaxThreshold = lightnessMaxThreshold;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_LIGHTNESS_MAX_THRESHOLD_CHANGED, oldLightnessMaxThreshold,
                lightnessMaxThreshold);
    }

    @Override
    public double getHueWeight() {
        return hueWeight;
    }

    @Override
    public void setHueWeight(double hueWeight) {
        double oldHueWeight = this.hueWeight;
        this.hueWeight = hueWeight;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_HUE_WEIGHT_CHANGED, oldHueWeight, hueWeight);
    }

    @Override
    public double getSaturationWeight() {
        return saturationWeight;
    }

    @Override
    public void setSaturationWeight(double saturationWeight) {
        double oldSaturationWeight = this.saturationWeight;
        this.saturationWeight = saturationWeight;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_SATURATION_WEIGHT_CHANGED, oldSaturationWeight,
                saturationWeight);
    }

    @Override
    public double getLightnessWeight() {
        return lightnessWeight;
    }

    @Override
    public void setLightnessWeight(double lightnessWeight) {
        double oldLightnessWeight = this.lightnessWeight;
        this.lightnessWeight = lightnessWeight;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_LIGHTNESS_WEIGHT_CHANGED, oldLightnessWeight,
                lightnessWeight);
    }

    @Override
    public boolean isSkinTonesMappingEnabled() {
        return skinTonesMappingEnabled;
    }

    @Override
    public void setSkinTonesMappingEnabled(boolean skinTonesMappingEnabled) {
        boolean oldSkinTonesMappingEnabled = this.skinTonesMappingEnabled;
        this.skinTonesMappingEnabled = skinTonesMappingEnabled;
        firePropertyChange(CommunicationMessages.ZX_SPECTRUM_SKIN_TONES_MAPPING_CHANGED,
                oldSkinTonesMappingEnabled, skinTonesMappingEnabled);
    }

    @Override
    public void loadDefaultValues() {
        colorClashEnabled = COLOR_CLASH_ENABLED_DEFAULT;
        saturationThreshold = SATURATION_THRESHOLD_DEFAULT;
        lightnessMinThreshold = LIGHTNESS_MIN_THRESHOLD_DEFAULT;
        lightnessMaxThreshold = LIGHTNESS_MAX_THRESHOLD_DEFAULT;
        hueWeight = HUE_WEIGHT_DEFAULT;
        saturationWeight = SATURATION_WEIGHT_DEFAULT;
        lightnessWeight = LIGHTNESS_WEIGHT_DEFAULT;
        skinTonesMappingEnabled = SKIN_TONES_MAPPING_ENABLED_DEFAULT;
    }

    @Override
    public void validate() {
        if (saturationThreshold < 0 || saturationThreshold > 100) {
            throw new IllegalArgumentException("Invalid saturation threshold");
        }
        if (lightnessMinThreshold < 0 || lightnessMinThreshold > 100) {
            throw new IllegalArgumentException("Invalid lightness min threshold");
        }
        if (lightnessMaxThreshold < 0 || lightnessMaxThreshold > 100) {
            throw new IllegalArgumentException("Invalid lightness max threshold");
        }
        if (lightnessMinThreshold > lightnessMaxThreshold) {
            throw new IllegalArgumentException("Lightness min threshold should be less than max threshold");
        }
        if (hueWeight < 0.0d || hueWeight > 1.0d) {
            throw new IllegalArgumentException("Invalid hue weight");
        }
        if (saturationWeight < 0.0d || saturationWeight > 1.0d) {
            throw new IllegalArgumentException("Invalid saturation weight");
        }
        if (lightnessWeight < 0.0d || lightnessWeight > 1.0d) {
            throw new IllegalArgumentException("Invalid lightness weight");
        }
//        if (hueWeight + saturationWeight + lightnessWeight != 1.0d) {
//            throw new IllegalArgumentException("Hue, Saturation and Lightness Weights do not sum to 1.0");
//        }
    }
}
