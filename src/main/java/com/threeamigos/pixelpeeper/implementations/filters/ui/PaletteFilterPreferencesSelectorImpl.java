package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

abstract class PaletteFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final String SATURATION_THRESHOLD = "Min saturation before grayscale mapping";
    private static final String LIGHTNESS_MIN_THRESHOLD = "Min lightness before black mapping";
    private static final String LIGHTNESS_MAX_THRESHOLD = "Max lightness before white mapping";
    private static final String COLOR_MAPPING = "Color mapping";
    private static final String HUE_WEIGHT = "Hue weight";
    private static final String SATURATION_WEIGHT = "Saturation weight";
    private static final String LIGHTNESS_WEIGHT = "Lightness weight";
    private static final String SKIN_TONES_MAPPING_ENABLED = "Skin tones mapping";

    private Dimension flavorDimension;

    public PaletteFilterPreferencesSelectorImpl(FilterPreferences filterPreferences,
                                                          DataModel dataModel, ExifImageReader exifImageReader, ExceptionHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);
    }

    protected abstract PaletteFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel();

    JPanel createFlavorPanel(Component component) {

        PaletteFilterPreferencesSelectorDataModel paletteFilterPreferencesDataModel = getFilterPreferencesSelectorDataModel();

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

        flavorDimension = getMaxDimension(component.getGraphics(), getAllLabels());

        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        addPreComponents(flavorPanel);

        createSliderPanel(flavorPanel, flavorDimension, SATURATION_THRESHOLD,
                paletteFilterPreferencesDataModel.saturationThresholdSlider,
                saturationThresholdSliderLabelTable,
                paletteFilterPreferencesDataModel.saturationThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_MIN_THRESHOLD,
                paletteFilterPreferencesDataModel.lightnessMinThresholdSlider,
                lightnessMinThresholdSliderLabelTable,
                paletteFilterPreferencesDataModel.lightnessMinThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_MAX_THRESHOLD,
                paletteFilterPreferencesDataModel.lightnessMaxThresholdSlider,
                lightnessMaxThresholdSliderLabelTable,
                paletteFilterPreferencesDataModel.lightnessMaxThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        flavorPanel.add(new JLabel(COLOR_MAPPING));

        createSliderPanel(flavorPanel, flavorDimension, HUE_WEIGHT,
                paletteFilterPreferencesDataModel.hueWeightSlider,
                hueWeightSliderLabelTable,
                paletteFilterPreferencesDataModel.hueWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, SATURATION_WEIGHT,
                paletteFilterPreferencesDataModel.saturationWeightSlider,
                saturationWeightSliderLabelTable,
                paletteFilterPreferencesDataModel.saturationWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createSliderPanel(flavorPanel, flavorDimension, LIGHTNESS_WEIGHT,
                paletteFilterPreferencesDataModel.lightnessWeightSlider,
                lightnessWeightSliderLabelTable,
                paletteFilterPreferencesDataModel.lightnessWeightText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        createCheckboxPanel(flavorPanel, SKIN_TONES_MAPPING_ENABLED, paletteFilterPreferencesDataModel.skinTonesMappingEnabledCheckbox);

        addPostComponents(flavorPanel);

        return flavorPanel;
    }

    protected java.util.List<String> getAllLabels() {
        java.util.List<String> allLabels = new ArrayList<>();
        allLabels.add(SATURATION_THRESHOLD);
        allLabels.add(LIGHTNESS_MIN_THRESHOLD);
        allLabels.add(LIGHTNESS_MAX_THRESHOLD);
        allLabels.add(HUE_WEIGHT);
        allLabels.add(SATURATION_WEIGHT);
        allLabels.add(LIGHTNESS_WEIGHT);
        allLabels.add(SKIN_TONES_MAPPING_ENABLED);
        return allLabels;
    }

    Dimension getFlavorDimension() {
        return flavorDimension;
    }

    protected void addPreComponents(JPanel panel) {
    }

    protected void addPostComponents(JPanel panel) {
    }

}
