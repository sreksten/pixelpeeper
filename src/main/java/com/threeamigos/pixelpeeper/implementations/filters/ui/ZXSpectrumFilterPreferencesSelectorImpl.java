package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ZXSpectrumFilterPreferencesSelectorImpl extends AbstractFilterPreferencesSelectorImpl {

    private static final String COLOR_CLASH_ENABLED = "Color Clash";
    private static final String SATURATION_THRESHOLD = "Min saturation before grayscale mapping";
    private static final String LIGHTNESS_MIN_THRESHOLD = "Min lightness before black mapping";
    private static final String LIGHTNESS_MAX_THRESHOLD = "Max lightness before white mapping";
    private static final String COLOR_MAPPING = "Color mapping";
    private static final String HUE_WEIGHT = "Hue weight";
    private static final String SATURATION_WEIGHT = "Saturation weight";
    private static final String LIGHTNESS_WEIGHT = "Lightness weight";
    private static final String SKIN_TONES_MAPPING_ENABLED = "Skin tones mapping";

    private Dimension flavorDimension;

    public ZXSpectrumFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                   ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                                   DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        preferencesSelectorDataModel = new ZXSpectrumPaletteFilterPreferencesSelectorDataModel(dataModel,
                filterPreferences, zxSpectrumPaletteFilterPreferences, testImageCanvas);
        preferencesSelectorDataModel.setSourceImage(testImage);
        preferencesSelectorDataModel.startFilterCalculation();
    }

    String getPreferencesDescription() {
        return "ZX Spectrum Edge Detector Preferences";
    }

    JPanel createFlavorPanel(Component component) {

        ZXSpectrumPaletteFilterPreferencesSelectorDataModel downcastDatamodel =
                (ZXSpectrumPaletteFilterPreferencesSelectorDataModel) preferencesSelectorDataModel;

        Properties saturationThresholdSliderLabelTable = new Properties();
        saturationThresholdSliderLabelTable.put(1, new JLabel("0"));
        saturationThresholdSliderLabelTable.put(50, new JLabel("50"));
        saturationThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties lightnessMinThresholdSliderLabelTable = new Properties();
        lightnessMinThresholdSliderLabelTable.put(1, new JLabel("0"));
        lightnessMinThresholdSliderLabelTable.put(50, new JLabel("50"));
        lightnessMinThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties lightnessMaxThresholdSliderLabelTable = new Properties();
        lightnessMaxThresholdSliderLabelTable.put(1, new JLabel("0"));
        lightnessMaxThresholdSliderLabelTable.put(50, new JLabel("50"));
        lightnessMaxThresholdSliderLabelTable.put(100, new JLabel("100"));

        Properties hueWeightSliderLabelTable = new Properties();
        hueWeightSliderLabelTable.put(1, new JLabel("0.0"));
        hueWeightSliderLabelTable.put(50, new JLabel("0.5"));
        hueWeightSliderLabelTable.put(100, new JLabel("1.0"));

        Properties saturationWeightSliderLabelTable = new Properties();
        saturationWeightSliderLabelTable.put(1, new JLabel("0.0"));
        saturationWeightSliderLabelTable.put(50, new JLabel("0.5"));
        saturationWeightSliderLabelTable.put(100, new JLabel("1.0"));

        Properties lightnessWeightSliderLabelTable = new Properties();
        lightnessWeightSliderLabelTable.put(1, new JLabel("0.0"));
        lightnessWeightSliderLabelTable.put(50, new JLabel("0.5"));
        lightnessWeightSliderLabelTable.put(100, new JLabel("1.0"));

        flavorDimension = getMaxDimension(component.getGraphics(), COLOR_CLASH_ENABLED,
                SATURATION_THRESHOLD, LIGHTNESS_MIN_THRESHOLD, LIGHTNESS_MAX_THRESHOLD,
                HUE_WEIGHT, SATURATION_WEIGHT, LIGHTNESS_WEIGHT, SKIN_TONES_MAPPING_ENABLED);

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        createCheckboxPanel(flavorPanel, COLOR_CLASH_ENABLED, downcastDatamodel.colorClashEnabledCheckbox);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, SATURATION_THRESHOLD,
                downcastDatamodel.saturationThresholdSlider,
                saturationThresholdSliderLabelTable,
                downcastDatamodel.saturationThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_MIN_THRESHOLD,
                downcastDatamodel.lightnessMinThresholdSlider,
                lightnessMinThresholdSliderLabelTable,
                downcastDatamodel.lightnessMinThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_MAX_THRESHOLD,
                downcastDatamodel.lightnessMaxThresholdSlider,
                lightnessMaxThresholdSliderLabelTable,
                downcastDatamodel.lightnessMaxThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        flavorPanel.add(new JLabel(COLOR_MAPPING));

        createSliderPanel(flavorPanel, flavorDimension, HUE_WEIGHT,
                downcastDatamodel.hueWeightSlider,
                hueWeightSliderLabelTable,
                downcastDatamodel.hueWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, SATURATION_WEIGHT,
                downcastDatamodel.saturationWeightSlider,
                saturationWeightSliderLabelTable,
                downcastDatamodel.saturationWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_WEIGHT,
                downcastDatamodel.lightnessWeightSlider,
                lightnessWeightSliderLabelTable,
                downcastDatamodel.lightnessWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, SKIN_TONES_MAPPING_ENABLED, downcastDatamodel.skinTonesMappingEnabledCheckbox);

        return flavorPanel;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

}
