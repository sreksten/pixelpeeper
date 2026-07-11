package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DistortionMeasurementFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class DistortionMeasurementFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 100);

    private final DistortionMeasurementFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public DistortionMeasurementFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            DistortionMeasurementFilterPreferences distortionMeasurementFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new DistortionMeasurementFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, distortionMeasurementFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public DistortionMeasurementFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Distortion Measurement Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties edgeLabelTable = new Properties();
        edgeLabelTable.put(DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MIN,
                new JLabel(String.valueOf(DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MIN)));
        edgeLabelTable.put(DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MAX,
                new JLabel(String.valueOf(DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Edge threshold",
                filterPreferencesSelectorDataModel.edgeThresholdSlider, edgeLabelTable,
                filterPreferencesSelectorDataModel.edgeThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        Properties gridLabelTable = new Properties();
        gridLabelTable.put(DistortionMeasurementFilterPreferences.GRID_SIZE_MIN,
                new JLabel(String.valueOf(DistortionMeasurementFilterPreferences.GRID_SIZE_MIN)));
        gridLabelTable.put(DistortionMeasurementFilterPreferences.GRID_SIZE_MAX,
                new JLabel(String.valueOf(DistortionMeasurementFilterPreferences.GRID_SIZE_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Grid size (lines)",
                filterPreferencesSelectorDataModel.gridSizeSlider, gridLabelTable,
                filterPreferencesSelectorDataModel.gridSizeText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
