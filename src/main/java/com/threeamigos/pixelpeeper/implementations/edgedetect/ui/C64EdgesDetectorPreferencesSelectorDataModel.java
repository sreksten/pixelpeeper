package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.C64EdgesDetectorImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.ZXSpectrumEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.C64EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.C64EdgesDetectorPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class C64EdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel
        implements C64EdgesDetectorPreferences {

    private static final float NORMALIZATION_VALUE = 100;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final C64EdgesDetectorPreferences c64EdgesDetectorPreferences;

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

    C64EdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                        EdgesDetectorPreferences edgesDetectorPreferences,
                                                        C64EdgesDetectorPreferences c64EdgesDetectorPreferences,
                                                        Component component) {
        super(dataModel, edgesDetectorPreferences, component);
        this.c64EdgesDetectorPreferences = c64EdgesDetectorPreferences;

        isColorClashEnabledBackup = this.c64EdgesDetectorPreferences.isColorClashEnabled();
        saturationThresholdBackup = this.c64EdgesDetectorPreferences.getSaturationThreshold();
        lightnessMinThresholdBackup = this.c64EdgesDetectorPreferences.getLightnessMinThreshold();
        lightnessMaxThresholdBackup = this.c64EdgesDetectorPreferences.getLightnessMaxThreshold();
        hueWeightBackup = normalize(this.c64EdgesDetectorPreferences.getHueWeight());
        saturationWeightBackup = normalize(this.c64EdgesDetectorPreferences.getSaturationWeight());
        lightnessWeightBackup = normalize(this.c64EdgesDetectorPreferences.getLightnessWeight());
        skinTonesMappingEnabledBackup = this.c64EdgesDetectorPreferences.isSkinTonesMappingEnabled();

        saturationThresholdText = new JLabel(String.valueOf(this.c64EdgesDetectorPreferences.getSaturationThreshold()));
        lightnessMinThresholdText = new JLabel(String.valueOf(this.c64EdgesDetectorPreferences.getLightnessMinThreshold()));
        lightnessMaxThresholdText = new JLabel(String.valueOf(this.c64EdgesDetectorPreferences.getLightnessMaxThreshold()));
        hueWeightText = new JLabel(String.format("%.2f", this.c64EdgesDetectorPreferences.getHueWeight()));
        saturationWeightText = new JLabel(String.format("%.2f", this.c64EdgesDetectorPreferences.getSaturationWeight()));
        lightnessWeightText = new JLabel(String.format("%.2f", this.c64EdgesDetectorPreferences.getLightnessWeight()));

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
        c64EdgesDetectorPreferences.setColorClashEnabled(isColorClashEnabledBackup);
        c64EdgesDetectorPreferences.setSaturationThreshold(saturationThresholdBackup);
        c64EdgesDetectorPreferences.setLightnessMinThreshold(lightnessMinThresholdBackup);
        c64EdgesDetectorPreferences.setLightnessMaxThreshold(lightnessMaxThresholdBackup);
        c64EdgesDetectorPreferences.setHueWeight(denormalize(hueWeightBackup));
        c64EdgesDetectorPreferences.setSaturationWeight(denormalize(saturationWeightBackup));
        c64EdgesDetectorPreferences.setLightnessWeight(denormalize(lightnessWeightBackup));
        c64EdgesDetectorPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledBackup);
    }

    void acceptSelection() {
        c64EdgesDetectorPreferences.setColorClashEnabled(colorClashEnabledCheckbox.isSelected());
        c64EdgesDetectorPreferences.setSaturationThreshold(saturationThresholdSlider.getValue());
        c64EdgesDetectorPreferences.setLightnessMinThreshold(lightnessMinThresholdSlider.getValue());
        c64EdgesDetectorPreferences.setLightnessMaxThreshold(lightnessMaxThresholdSlider.getValue());
        c64EdgesDetectorPreferences.setHueWeight(denormalize(hueWeightSlider.getValue()));
        c64EdgesDetectorPreferences.setSaturationWeight(denormalize(saturationWeightSlider.getValue()));
        c64EdgesDetectorPreferences.setLightnessWeight(denormalize(lightnessWeightSlider.getValue()));
        c64EdgesDetectorPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledCheckbox.isSelected());
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
        colorClashEnabledCheckbox.setSelected(C64EdgesDetectorPreferences.COLOR_CLASH_ENABLED_DEFAULT);
        saturationThresholdSlider.setValue(C64EdgesDetectorPreferences.SATURATION_THRESHOLD_DEFAULT);
        lightnessMinThresholdSlider.setValue(C64EdgesDetectorPreferences.LIGHTNESS_MIN_THRESHOLD_DEFAULT);
        lightnessMaxThresholdSlider.setValue(C64EdgesDetectorPreferences.LIGHTNESS_MAX_THRESHOLD_DEFAULT);
        hueWeightSlider.setValue(normalize(C64EdgesDetectorPreferences.HUE_WEIGHT_DEFAULT));
        saturationWeightSlider.setValue(normalize(C64EdgesDetectorPreferences.SATURATION_WEIGHT_DEFAULT));
        lightnessWeightSlider.setValue(normalize(C64EdgesDetectorPreferences.LIGHTNESS_WEIGHT_DEFAULT));
        skinTonesMappingEnabledCheckbox.setSelected(C64EdgesDetectorPreferences.SKIN_TONES_MAPPING_ENABLED_DEFAULT);
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
    public EdgesDetectorFlavour getEdgesDetectorFlavour() {
        return EdgesDetectorFlavour.C64_EDGES_DETECTOR;
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
    protected EdgesDetector getEdgesDetectorImplementation() {
        return new C64EdgesDetectorImpl(this, new SwingMessageHandler());
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return c64EdgesDetectorPreferences.isColorClashEnabled() != colorClashEnabledCheckbox.isSelected()
                || c64EdgesDetectorPreferences.getSaturationThreshold() != saturationThresholdSlider.getValue()
                || c64EdgesDetectorPreferences.getLightnessMinThreshold() != lightnessMinThresholdSlider.getValue()
                || c64EdgesDetectorPreferences.getLightnessMaxThreshold() != lightnessMaxThresholdSlider.getValue()
                || c64EdgesDetectorPreferences.getHueWeight() != denormalize(hueWeightSlider.getValue())
                || c64EdgesDetectorPreferences.getSaturationWeight() != denormalize(saturationWeightSlider.getValue())
                || c64EdgesDetectorPreferences.getLightnessWeight() != denormalize(lightnessWeightSlider.getValue())
                || c64EdgesDetectorPreferences.isSkinTonesMappingEnabled() != skinTonesMappingEnabledCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return C64EdgesDetectorPreferences.super.getDescription();
    }
}
