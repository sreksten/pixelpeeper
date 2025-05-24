package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.PaletteFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

abstract class PaletteFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel
        implements PaletteFilterPreferences {

    private static final float NORMALIZATION_VALUE = 100;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final PaletteFilterPreferences paletteFilterPreferences;

    private final int saturationThresholdBackup;
    private final int lightnessMinThresholdBackup;
    private final int lightnessMaxThresholdBackup;
    private final int hueWeightBackup;
    private final int saturationWeightBackup;
    private final int lightnessWeightBackup;
    private final boolean skinTonesMappingEnabledBackup;

    JLabel saturationThresholdText;
    JLabel lightnessMinThresholdText;
    JLabel lightnessMaxThresholdText;
    JLabel hueWeightText;
    JLabel saturationWeightText;
    JLabel lightnessWeightText;

    JSlider saturationThresholdSlider;
    JSlider lightnessMinThresholdSlider;
    JSlider lightnessMaxThresholdSlider;
    JSlider hueWeightSlider;
    JSlider saturationWeightSlider;
    JSlider lightnessWeightSlider;
    JCheckBox skinTonesMappingEnabledCheckbox;

    PaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                              FilterPreferences filterPreferences,
                                              PaletteFilterPreferences paletteFilterPreferences,
                                              Component component) {
        super(dataModel, filterPreferences, component);
        this.paletteFilterPreferences = paletteFilterPreferences;

        saturationThresholdBackup = this.paletteFilterPreferences.getSaturationThreshold();
        lightnessMinThresholdBackup = this.paletteFilterPreferences.getLightnessMinThreshold();
        lightnessMaxThresholdBackup = this.paletteFilterPreferences.getLightnessMaxThreshold();
        hueWeightBackup = normalize(this.paletteFilterPreferences.getHueWeight());
        saturationWeightBackup = normalize(this.paletteFilterPreferences.getSaturationWeight());
        lightnessWeightBackup = normalize(this.paletteFilterPreferences.getLightnessWeight());
        skinTonesMappingEnabledBackup = this.paletteFilterPreferences.isSkinTonesMappingEnabled();

        saturationThresholdText = new JLabel(String.valueOf(this.paletteFilterPreferences.getSaturationThreshold()));
        lightnessMinThresholdText = new JLabel(String.valueOf(this.paletteFilterPreferences.getLightnessMinThreshold()));
        lightnessMaxThresholdText = new JLabel(String.valueOf(this.paletteFilterPreferences.getLightnessMaxThreshold()));
        hueWeightText = new JLabel(String.format("%.2f", this.paletteFilterPreferences.getHueWeight()));
        saturationWeightText = new JLabel(String.format("%.2f", this.paletteFilterPreferences.getSaturationWeight()));
        lightnessWeightText = new JLabel(String.format("%.2f", this.paletteFilterPreferences.getLightnessWeight()));

        saturationThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, saturationThresholdBackup);
        lightnessMinThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lightnessMinThresholdBackup);
        lightnessMaxThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lightnessMaxThresholdBackup);
        hueWeightSlider = createSlider(0, 100, hueWeightBackup);
        saturationWeightSlider = createSlider(0, 100, saturationWeightBackup);
        lightnessWeightSlider = createSlider(0, 100, lightnessWeightBackup);
        skinTonesMappingEnabledCheckbox = createCheckbox(skinTonesMappingEnabledBackup);
    }

    void cancelSelection() {
        paletteFilterPreferences.setSaturationThreshold(saturationThresholdBackup);
        paletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdBackup);
        paletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdBackup);
        paletteFilterPreferences.setHueWeight(denormalize(hueWeightBackup));
        paletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightBackup));
        paletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightBackup));
        paletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledBackup);
    }

    void acceptSelection() {
        paletteFilterPreferences.setSaturationThreshold(saturationThresholdSlider.getValue());
        paletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdSlider.getValue());
        paletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdSlider.getValue());
        paletteFilterPreferences.setHueWeight(denormalize(hueWeightSlider.getValue()));
        paletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightSlider.getValue()));
        paletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightSlider.getValue()));
        paletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledCheckbox.isSelected());
    }

    void reset() {
        saturationThresholdSlider.setValue(saturationThresholdBackup);
        lightnessMinThresholdSlider.setValue(lightnessMinThresholdBackup);
        lightnessMaxThresholdSlider.setValue(lightnessMaxThresholdBackup);
        hueWeightSlider.setValue(normalize(hueWeightBackup));
        saturationWeightSlider.setValue(normalize(saturationWeightBackup));
        lightnessWeightSlider.setValue(normalize(lightnessWeightBackup));
        skinTonesMappingEnabledCheckbox.setSelected(skinTonesMappingEnabledBackup);
    }

    void resetToDefault() {
        saturationThresholdSlider.setValue(C64PaletteFilterPreferences.SATURATION_THRESHOLD_DEFAULT);
        lightnessMinThresholdSlider.setValue(C64PaletteFilterPreferences.LIGHTNESS_MIN_THRESHOLD_DEFAULT);
        lightnessMaxThresholdSlider.setValue(C64PaletteFilterPreferences.LIGHTNESS_MAX_THRESHOLD_DEFAULT);
        hueWeightSlider.setValue(normalize(C64PaletteFilterPreferences.HUE_WEIGHT_DEFAULT));
        saturationWeightSlider.setValue(normalize(C64PaletteFilterPreferences.SATURATION_WEIGHT_DEFAULT));
        lightnessWeightSlider.setValue(normalize(C64PaletteFilterPreferences.LIGHTNESS_WEIGHT_DEFAULT));
        skinTonesMappingEnabledCheckbox.setSelected(C64PaletteFilterPreferences.SKIN_TONES_MAPPING_ENABLED_DEFAULT);
    }

    private int normalize(double value) {
        return (int) (value * NORMALIZATION_VALUE);
    }

    private double denormalize(int value) {
        return value / NORMALIZATION_VALUE;
    }

    public void handleStateChanged(ChangeEvent e) {
        Object object = e.getSource();

        if (object == saturationThresholdSlider) {
            saturationThresholdText.setText(String.valueOf(saturationThresholdSlider.getValue()));
        } else if (object == lightnessMinThresholdSlider) {
            lightnessMinThresholdText.setText(String.valueOf(lightnessMinThresholdSlider.getValue()));
            if (lightnessMinThresholdSlider.getValue() > lightnessMaxThresholdSlider.getValue()) {
                lightnessMaxThresholdSlider.setValue(lightnessMinThresholdSlider.getValue());
                lightnessMaxThresholdText.setText(String.valueOf(lightnessMaxThresholdSlider.getValue()));
            }
        } else if (object == lightnessMaxThresholdSlider) {
            lightnessMaxThresholdText.setText(String.valueOf(lightnessMaxThresholdSlider.getValue()));
            if (lightnessMaxThresholdSlider.getValue() < lightnessMinThresholdSlider.getValue()) {
                lightnessMinThresholdSlider.setValue(lightnessMaxThresholdSlider.getValue());
                lightnessMinThresholdText.setText(String.valueOf(lightnessMinThresholdSlider.getValue()));
            }
        } else if (object == hueWeightSlider) {
            hueWeightText.setText(String.valueOf(hueWeightSlider.getValue() / 100.0f));
        } else if (object == saturationWeightSlider) {
            saturationWeightText.setText(String.valueOf(saturationWeightSlider.getValue() / 100.0f));
        } else if (object == lightnessWeightSlider) {
            lightnessWeightText.setText(String.valueOf(lightnessWeightSlider.getValue() / 100.0f));
        }
    }

    @Override
    public int getSaturationThreshold() {
        return saturationThresholdSlider.getValue();
    }

    @Override
    public void setSaturationThreshold(int saturationThreshold) {
        saturationThresholdSlider.setValue(saturationThreshold);
    }

    @Override
    public int getLightnessMinThreshold() {
        return lightnessMinThresholdSlider.getValue();
    }

    @Override
    public void setLightnessMinThreshold(int lightnessMinThreshold) {
        lightnessMinThresholdSlider.setValue(lightnessMinThreshold);
    }

    @Override
    public int getLightnessMaxThreshold() {
        return lightnessMaxThresholdSlider.getValue();
    }

    @Override
    public void setLightnessMaxThreshold(int lightnessMaxThreshold) {
        lightnessMaxThresholdSlider.setValue(lightnessMaxThreshold);
    }

    @Override
    public double getHueWeight() {
        return denormalize(hueWeightSlider.getValue());
    }

    @Override
    public void setHueWeight(double hueWeight) {
        hueWeightSlider.setValue(normalize(hueWeight));
    }

    @Override
    public double getSaturationWeight() {
        return denormalize(saturationWeightSlider.getValue());
    }

    @Override
    public void setSaturationWeight(double saturationWeight) {
        saturationWeightSlider.setValue(normalize(saturationWeight));
    }

    @Override
    public double getLightnessWeight() {
        return denormalize(lightnessWeightSlider.getValue());
    }

    @Override
    public void setLightnessWeight(double lightnessWeight) {
        lightnessWeightSlider.setValue(normalize(lightnessWeight));
    }

    @Override
    public boolean isSkinTonesMappingEnabled() {
        return skinTonesMappingEnabledCheckbox.isSelected();
    }

    @Override
    public void setSkinTonesMappingEnabled(boolean skinTonesMappingEnabled) {
        skinTonesMappingEnabledCheckbox.setSelected(skinTonesMappingEnabled);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return paletteFilterPreferences.getSaturationThreshold() != saturationThresholdSlider.getValue()
                || paletteFilterPreferences.getLightnessMinThreshold() != lightnessMinThresholdSlider.getValue()
                || paletteFilterPreferences.getLightnessMaxThreshold() != lightnessMaxThresholdSlider.getValue()
                || paletteFilterPreferences.getHueWeight() != denormalize(hueWeightSlider.getValue())
                || paletteFilterPreferences.getSaturationWeight() != denormalize(saturationWeightSlider.getValue())
                || paletteFilterPreferences.getLightnessWeight() != denormalize(lightnessWeightSlider.getValue())
                || paletteFilterPreferences.isSkinTonesMappingEnabled() != skinTonesMappingEnabledCheckbox.isSelected();
    }

}
