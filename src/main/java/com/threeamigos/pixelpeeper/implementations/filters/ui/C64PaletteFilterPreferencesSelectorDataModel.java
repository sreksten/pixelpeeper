package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.C64PaletteFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class C64PaletteFilterPreferencesSelectorDataModel extends AbstractFilterPreferencesSelectorDataModel
        implements C64PaletteFilterPreferences {

    private static final float NORMALIZATION_VALUE = 100;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final C64PaletteFilterPreferences c64PaletteFilterPreferences;

    private final boolean isColorClashEnabledBackup;
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

    JCheckBox colorClashEnabledCheckbox;
    JSlider saturationThresholdSlider;
    JSlider lightnessMinThresholdSlider;
    JSlider lightnessMaxThresholdSlider;
    JSlider hueWeightSlider;
    JSlider saturationWeightSlider;
    JSlider lightnessWeightSlider;
    JCheckBox skinTonesMappingEnabledCheckbox;

    C64PaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                 FilterPreferences filterPreferences,
                                                 C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                                 Component component) {
        super(dataModel, filterPreferences, component);
        this.c64PaletteFilterPreferences = c64PaletteFilterPreferences;

        isColorClashEnabledBackup = this.c64PaletteFilterPreferences.isColorClashEnabled();
        saturationThresholdBackup = this.c64PaletteFilterPreferences.getSaturationThreshold();
        lightnessMinThresholdBackup = this.c64PaletteFilterPreferences.getLightnessMinThreshold();
        lightnessMaxThresholdBackup = this.c64PaletteFilterPreferences.getLightnessMaxThreshold();
        hueWeightBackup = normalize(this.c64PaletteFilterPreferences.getHueWeight());
        saturationWeightBackup = normalize(this.c64PaletteFilterPreferences.getSaturationWeight());
        lightnessWeightBackup = normalize(this.c64PaletteFilterPreferences.getLightnessWeight());
        skinTonesMappingEnabledBackup = this.c64PaletteFilterPreferences.isSkinTonesMappingEnabled();

        saturationThresholdText = new JLabel(String.valueOf(this.c64PaletteFilterPreferences.getSaturationThreshold()));
        lightnessMinThresholdText = new JLabel(String.valueOf(this.c64PaletteFilterPreferences.getLightnessMinThreshold()));
        lightnessMaxThresholdText = new JLabel(String.valueOf(this.c64PaletteFilterPreferences.getLightnessMaxThreshold()));
        hueWeightText = new JLabel(String.format("%.2f", this.c64PaletteFilterPreferences.getHueWeight()));
        saturationWeightText = new JLabel(String.format("%.2f", this.c64PaletteFilterPreferences.getSaturationWeight()));
        lightnessWeightText = new JLabel(String.format("%.2f", this.c64PaletteFilterPreferences.getLightnessWeight()));

        colorClashEnabledCheckbox = createCheckbox(isColorClashEnabledBackup);
        saturationThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, saturationThresholdBackup);
        lightnessMinThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lightnessMinThresholdBackup);
        lightnessMaxThresholdSlider = createSlider(MIN_THRESHOLD, MAX_THRESHOLD, lightnessMaxThresholdBackup);
        hueWeightSlider = createSlider(0, 100, hueWeightBackup);
        saturationWeightSlider = createSlider(0, 100, saturationWeightBackup);
        lightnessWeightSlider = createSlider(0, 100, lightnessWeightBackup);
        skinTonesMappingEnabledCheckbox = createCheckbox(skinTonesMappingEnabledBackup);
    }

    void cancelSelection() {
        c64PaletteFilterPreferences.setColorClashEnabled(isColorClashEnabledBackup);
        c64PaletteFilterPreferences.setSaturationThreshold(saturationThresholdBackup);
        c64PaletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdBackup);
        c64PaletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdBackup);
        c64PaletteFilterPreferences.setHueWeight(denormalize(hueWeightBackup));
        c64PaletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightBackup));
        c64PaletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightBackup));
        c64PaletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledBackup);
    }

    void acceptSelection() {
        c64PaletteFilterPreferences.setColorClashEnabled(colorClashEnabledCheckbox.isSelected());
        c64PaletteFilterPreferences.setSaturationThreshold(saturationThresholdSlider.getValue());
        c64PaletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdSlider.getValue());
        c64PaletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdSlider.getValue());
        c64PaletteFilterPreferences.setHueWeight(denormalize(hueWeightSlider.getValue()));
        c64PaletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightSlider.getValue()));
        c64PaletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightSlider.getValue()));
        c64PaletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledCheckbox.isSelected());
    }

    void reset() {
        colorClashEnabledCheckbox.setSelected(isColorClashEnabledBackup);
        saturationThresholdSlider.setValue(saturationThresholdBackup);
        lightnessMinThresholdSlider.setValue(lightnessMinThresholdBackup);
        lightnessMaxThresholdSlider.setValue(lightnessMaxThresholdBackup);
        hueWeightSlider.setValue(normalize(hueWeightBackup));
        saturationWeightSlider.setValue(normalize(saturationWeightBackup));
        lightnessWeightSlider.setValue(normalize(lightnessWeightBackup));
        skinTonesMappingEnabledCheckbox.setSelected(skinTonesMappingEnabledBackup);
    }

    void resetToDefault() {
        colorClashEnabledCheckbox.setSelected(C64PaletteFilterPreferences.COLOR_CLASH_ENABLED_DEFAULT);
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
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.C64_PALETTE;
    }

    @Override
    public boolean isColorClashEnabled() {
        return colorClashEnabledCheckbox.isSelected();
    }

    @Override
    public void setColorClashEnabled(boolean colorClashEnabled) {
        colorClashEnabledCheckbox.setSelected(colorClashEnabled);
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
    protected Filter getFilterImplementation() {
        return new C64PaletteFilterImpl(this, new SwingMessageHandler());
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return c64PaletteFilterPreferences.isColorClashEnabled() != colorClashEnabledCheckbox.isSelected()
                || c64PaletteFilterPreferences.getSaturationThreshold() != saturationThresholdSlider.getValue()
                || c64PaletteFilterPreferences.getLightnessMinThreshold() != lightnessMinThresholdSlider.getValue()
                || c64PaletteFilterPreferences.getLightnessMaxThreshold() != lightnessMaxThresholdSlider.getValue()
                || c64PaletteFilterPreferences.getHueWeight() != denormalize(hueWeightSlider.getValue())
                || c64PaletteFilterPreferences.getSaturationWeight() != denormalize(saturationWeightSlider.getValue())
                || c64PaletteFilterPreferences.getLightnessWeight() != denormalize(lightnessWeightSlider.getValue())
                || c64PaletteFilterPreferences.isSkinTonesMappingEnabled() != skinTonesMappingEnabledCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return C64PaletteFilterPreferences.super.getDescription();
    }
}
