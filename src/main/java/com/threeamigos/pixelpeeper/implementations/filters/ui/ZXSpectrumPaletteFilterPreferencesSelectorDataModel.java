package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.filters.flavors.ZXSpectrumPaletteFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class ZXSpectrumPaletteFilterPreferencesSelectorDataModel extends AbstractFilterPreferencesSelectorDataModel
implements ZXSpectrumPaletteFilterPreferences {

    private static final float NORMALIZATION_VALUE = 100;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences;

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

    ZXSpectrumPaletteFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                        FilterPreferences filterPreferences,
                                                        ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                        Component component) {
        super(dataModel, filterPreferences, component);
        this.zxSpectrumPaletteFilterPreferences = zxSpectrumPaletteFilterPreferences;

        isColorClashEnabledBackup = zxSpectrumPaletteFilterPreferences.isColorClashEnabled();
        saturationThresholdBackup = zxSpectrumPaletteFilterPreferences.getSaturationThreshold();
        lightnessMinThresholdBackup = zxSpectrumPaletteFilterPreferences.getLightnessMinThreshold();
        lightnessMaxThresholdBackup = zxSpectrumPaletteFilterPreferences.getLightnessMaxThreshold();
        hueWeightBackup = normalize(zxSpectrumPaletteFilterPreferences.getHueWeight());
        saturationWeightBackup = normalize(zxSpectrumPaletteFilterPreferences.getSaturationWeight());
        lightnessWeightBackup = normalize(zxSpectrumPaletteFilterPreferences.getLightnessWeight());
        skinTonesMappingEnabledBackup = zxSpectrumPaletteFilterPreferences.isSkinTonesMappingEnabled();

        saturationThresholdText = new JLabel(String.valueOf(zxSpectrumPaletteFilterPreferences.getSaturationThreshold()));
        lightnessMinThresholdText = new JLabel(String.valueOf(zxSpectrumPaletteFilterPreferences.getLightnessMinThreshold()));
        lightnessMaxThresholdText = new JLabel(String.valueOf(zxSpectrumPaletteFilterPreferences.getLightnessMaxThreshold()));
        hueWeightText = new JLabel(String.format("%.2f", zxSpectrumPaletteFilterPreferences.getHueWeight()));
        saturationWeightText = new JLabel(String.format("%.2f", zxSpectrumPaletteFilterPreferences.getSaturationWeight()));
        lightnessWeightText = new JLabel(String.format("%.2f", zxSpectrumPaletteFilterPreferences.getLightnessWeight()));

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
        zxSpectrumPaletteFilterPreferences.setColorClashEnabled(isColorClashEnabledBackup);
        zxSpectrumPaletteFilterPreferences.setSaturationThreshold(saturationThresholdBackup);
        zxSpectrumPaletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdBackup);
        zxSpectrumPaletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdBackup);
        zxSpectrumPaletteFilterPreferences.setHueWeight(denormalize(hueWeightBackup));
        zxSpectrumPaletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightBackup));
        zxSpectrumPaletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightBackup));
        zxSpectrumPaletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledBackup);
    }

    void acceptSelection() {
        zxSpectrumPaletteFilterPreferences.setColorClashEnabled(colorClashEnabledCheckbox.isSelected());
        zxSpectrumPaletteFilterPreferences.setSaturationThreshold(saturationThresholdSlider.getValue());
        zxSpectrumPaletteFilterPreferences.setLightnessMinThreshold(lightnessMinThresholdSlider.getValue());
        zxSpectrumPaletteFilterPreferences.setLightnessMaxThreshold(lightnessMaxThresholdSlider.getValue());
        zxSpectrumPaletteFilterPreferences.setHueWeight(denormalize(hueWeightSlider.getValue()));
        zxSpectrumPaletteFilterPreferences.setSaturationWeight(denormalize(saturationWeightSlider.getValue()));
        zxSpectrumPaletteFilterPreferences.setLightnessWeight(denormalize(lightnessWeightSlider.getValue()));
        zxSpectrumPaletteFilterPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledCheckbox.isSelected());
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
        colorClashEnabledCheckbox.setSelected(ZXSpectrumPaletteFilterPreferences.COLOR_CLASH_ENABLED_DEFAULT);
        saturationThresholdSlider.setValue(ZXSpectrumPaletteFilterPreferences.SATURATION_THRESHOLD_DEFAULT);
        lightnessMinThresholdSlider.setValue(ZXSpectrumPaletteFilterPreferences.LIGHTNESS_MIN_THRESHOLD_DEFAULT);
        lightnessMaxThresholdSlider.setValue(ZXSpectrumPaletteFilterPreferences.LIGHTNESS_MAX_THRESHOLD_DEFAULT);
        hueWeightSlider.setValue(normalize(ZXSpectrumPaletteFilterPreferences.HUE_WEIGHT_DEFAULT));
        saturationWeightSlider.setValue(normalize(ZXSpectrumPaletteFilterPreferences.SATURATION_WEIGHT_DEFAULT));
        lightnessWeightSlider.setValue(normalize(ZXSpectrumPaletteFilterPreferences.LIGHTNESS_WEIGHT_DEFAULT));
        skinTonesMappingEnabledCheckbox.setSelected(ZXSpectrumPaletteFilterPreferences.SKIN_TONES_MAPPING_ENABLED_DEFAULT);
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
        return FilterFlavor.ZX_SPECTRUM_PALETTE;
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
        return new ZXSpectrumPaletteFilterImpl(this, new SwingMessageHandler());
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return zxSpectrumPaletteFilterPreferences.isColorClashEnabled() != colorClashEnabledCheckbox.isSelected()
                || zxSpectrumPaletteFilterPreferences.getSaturationThreshold() != saturationThresholdSlider.getValue()
                || zxSpectrumPaletteFilterPreferences.getLightnessMinThreshold() != lightnessMinThresholdSlider.getValue()
                || zxSpectrumPaletteFilterPreferences.getLightnessMaxThreshold() != lightnessMaxThresholdSlider.getValue()
                || zxSpectrumPaletteFilterPreferences.getHueWeight() != denormalize(hueWeightSlider.getValue())
                || zxSpectrumPaletteFilterPreferences.getSaturationWeight() != denormalize(saturationWeightSlider.getValue())
                || zxSpectrumPaletteFilterPreferences.getLightnessWeight() != denormalize(lightnessWeightSlider.getValue())
                || zxSpectrumPaletteFilterPreferences.isSkinTonesMappingEnabled() != skinTonesMappingEnabledCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return ZXSpectrumPaletteFilterPreferences.super.getDescription();
    }
}
