package com.threeamigos.pixelpeeper.implementations.edgedetect.ui;

import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.SobelEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.implementations.edgedetect.flavours.ZXSpectrumEdgesDetectorImpl;
import com.threeamigos.pixelpeeper.instances.EdgesDetectorFactoryInstance;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ZXSpectrumEdgesDetectorPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

public class ZXSpectrumEdgesDetectorPreferencesSelectorDataModel extends AbstractEdgesDetectorPreferencesSelectorDataModel
implements ZXSpectrumEdgesDetectorPreferences{

    private static final float NORMALIZATION_VALUE = 100;

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 100;

    private final ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences;

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

    ZXSpectrumEdgesDetectorPreferencesSelectorDataModel(DataModel dataModel,
                                                        EdgesDetectorPreferences edgesDetectorPreferences,
                                                        ZXSpectrumEdgesDetectorPreferences zxSpectrumEdgesDetectorPreferences,
                                                        Component component) {
        super(dataModel, edgesDetectorPreferences, component);
        this.zxSpectrumEdgesDetectorPreferences = zxSpectrumEdgesDetectorPreferences;

        isColorClashEnabledBackup = zxSpectrumEdgesDetectorPreferences.isColorClashEnabled();
        saturationThresholdBackup = zxSpectrumEdgesDetectorPreferences.getSaturationThreshold();
        lightnessMinThresholdBackup = zxSpectrumEdgesDetectorPreferences.getLightnessMinThreshold();
        lightnessMaxThresholdBackup = zxSpectrumEdgesDetectorPreferences.getLightnessMaxThreshold();
        hueWeightBackup = normalize(zxSpectrumEdgesDetectorPreferences.getHueWeight());
        saturationWeightBackup = normalize(zxSpectrumEdgesDetectorPreferences.getSaturationWeight());
        lightnessWeightBackup = normalize(zxSpectrumEdgesDetectorPreferences.getLightnessWeight());
        skinTonesMappingEnabledBackup = zxSpectrumEdgesDetectorPreferences.isSkinTonesMappingEnabled();

        saturationThresholdText = new JLabel(String.valueOf(zxSpectrumEdgesDetectorPreferences.getSaturationThreshold()));
        lightnessMinThresholdText = new JLabel(String.valueOf(zxSpectrumEdgesDetectorPreferences.getLightnessMinThreshold()));
        lightnessMaxThresholdText = new JLabel(String.valueOf(zxSpectrumEdgesDetectorPreferences.getLightnessMaxThreshold()));
        hueWeightText = new JLabel(String.format("%.2f", zxSpectrumEdgesDetectorPreferences.getHueWeight()));
        saturationWeightText = new JLabel(String.format("%.2f", zxSpectrumEdgesDetectorPreferences.getSaturationWeight()));
        lightnessWeightText = new JLabel(String.format("%.2f", zxSpectrumEdgesDetectorPreferences.getLightnessWeight()));

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
        zxSpectrumEdgesDetectorPreferences.setColorClashEnabled(isColorClashEnabledBackup);
        zxSpectrumEdgesDetectorPreferences.setSaturationThreshold(saturationThresholdBackup);
        zxSpectrumEdgesDetectorPreferences.setLightnessMinThreshold(lightnessMinThresholdBackup);
        zxSpectrumEdgesDetectorPreferences.setLightnessMaxThreshold(lightnessMaxThresholdBackup);
        zxSpectrumEdgesDetectorPreferences.setHueWeight(denormalize(hueWeightBackup));
        zxSpectrumEdgesDetectorPreferences.setSaturationWeight(denormalize(saturationWeightBackup));
        zxSpectrumEdgesDetectorPreferences.setLightnessWeight(denormalize(lightnessWeightBackup));
        zxSpectrumEdgesDetectorPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledBackup);
    }

    void acceptSelection() {
        zxSpectrumEdgesDetectorPreferences.setColorClashEnabled(colorClashEnabledCheckbox.isSelected());
        zxSpectrumEdgesDetectorPreferences.setSaturationThreshold(saturationThresholdSlider.getValue());
        zxSpectrumEdgesDetectorPreferences.setLightnessMinThreshold(lightnessMinThresholdSlider.getValue());
        zxSpectrumEdgesDetectorPreferences.setLightnessMaxThreshold(lightnessMaxThresholdSlider.getValue());
        zxSpectrumEdgesDetectorPreferences.setHueWeight(denormalize(hueWeightSlider.getValue()));
        zxSpectrumEdgesDetectorPreferences.setSaturationWeight(denormalize(saturationWeightSlider.getValue()));
        zxSpectrumEdgesDetectorPreferences.setLightnessWeight(denormalize(lightnessWeightSlider.getValue()));
        zxSpectrumEdgesDetectorPreferences.setSkinTonesMappingEnabled(skinTonesMappingEnabledCheckbox.isSelected());
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
        colorClashEnabledCheckbox.setSelected(ZXSpectrumEdgesDetectorPreferences.COLOR_CLASH_ENABLED_DEFAULT);
        saturationThresholdSlider.setValue(ZXSpectrumEdgesDetectorPreferences.SATURATION_THRESHOLD_DEFAULT);
        lightnessMinThresholdSlider.setValue(ZXSpectrumEdgesDetectorPreferences.LIGHTNESS_MIN_THRESHOLD_DEFAULT);
        lightnessMaxThresholdSlider.setValue(ZXSpectrumEdgesDetectorPreferences.LIGHTNESS_MAX_THRESHOLD_DEFAULT);
        hueWeightSlider.setValue(normalize(ZXSpectrumEdgesDetectorPreferences.HUE_WEIGHT_DEFAULT));
        saturationWeightSlider.setValue(normalize(ZXSpectrumEdgesDetectorPreferences.SATURATION_WEIGHT_DEFAULT));
        lightnessWeightSlider.setValue(normalize(ZXSpectrumEdgesDetectorPreferences.LIGHTNESS_WEIGHT_DEFAULT));
        skinTonesMappingEnabledCheckbox.setSelected(ZXSpectrumEdgesDetectorPreferences.SKIN_TONES_MAPPING_ENABLED_DEFAULT);
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
        return EdgesDetectorFlavour.ZX_SPECTRUM_EDGES_DETECTOR;
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
        return new ZXSpectrumEdgesDetectorImpl(this, new SwingMessageHandler());
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return zxSpectrumEdgesDetectorPreferences.isColorClashEnabled() != colorClashEnabledCheckbox.isSelected()
                || zxSpectrumEdgesDetectorPreferences.getSaturationThreshold() != saturationThresholdSlider.getValue()
                || zxSpectrumEdgesDetectorPreferences.getLightnessMinThreshold() != lightnessMinThresholdSlider.getValue()
                || zxSpectrumEdgesDetectorPreferences.getLightnessMaxThreshold() != lightnessMaxThresholdSlider.getValue()
                || zxSpectrumEdgesDetectorPreferences.getHueWeight() != denormalize(hueWeightSlider.getValue())
                || zxSpectrumEdgesDetectorPreferences.getSaturationWeight() != denormalize(saturationWeightSlider.getValue())
                || zxSpectrumEdgesDetectorPreferences.getLightnessWeight() != denormalize(lightnessWeightSlider.getValue())
                || zxSpectrumEdgesDetectorPreferences.isSkinTonesMappingEnabled() != skinTonesMappingEnabledCheckbox.isSelected();
    }

    @Override
    public String getDescription() {
        return ZXSpectrumEdgesDetectorPreferences.super.getDescription();
    }
}
