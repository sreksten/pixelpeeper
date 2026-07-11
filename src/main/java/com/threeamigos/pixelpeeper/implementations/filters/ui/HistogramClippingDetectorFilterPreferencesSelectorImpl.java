package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.HistogramClippingDetectorFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class HistogramClippingDetectorFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 100);

    private final HistogramClippingDetectorFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public HistogramClippingDetectorFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            HistogramClippingDetectorFilterPreferences histogramClippingDetectorFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new HistogramClippingDetectorFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, histogramClippingDetectorFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public HistogramClippingDetectorFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Histogram Clipping Detector Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties shadowLabelTable = new Properties();
        shadowLabelTable.put(HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MIN,
                new JLabel(String.valueOf(HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MIN)));
        shadowLabelTable.put(HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MAX,
                new JLabel(String.valueOf(HistogramClippingDetectorFilterPreferences.SHADOW_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Shadow threshold",
                filterPreferencesSelectorDataModel.shadowThresholdSlider, shadowLabelTable,
                filterPreferencesSelectorDataModel.shadowThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        Properties highlightLabelTable = new Properties();
        highlightLabelTable.put(HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MIN,
                new JLabel(String.valueOf(HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MIN)));
        highlightLabelTable.put(HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MAX,
                new JLabel(String.valueOf(HistogramClippingDetectorFilterPreferences.HIGHLIGHT_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Highlight threshold",
                filterPreferencesSelectorDataModel.highlightThresholdSlider, highlightLabelTable,
                filterPreferencesSelectorDataModel.highlightThresholdText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
