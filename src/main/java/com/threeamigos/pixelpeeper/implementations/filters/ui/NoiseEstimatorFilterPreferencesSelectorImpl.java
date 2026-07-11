package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NoiseEstimatorFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class NoiseEstimatorFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 100);

    private final NoiseEstimatorFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public NoiseEstimatorFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            NoiseEstimatorFilterPreferences noiseEstimatorFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new NoiseEstimatorFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, noiseEstimatorFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public NoiseEstimatorFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Noise Estimator Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties patchSizeLabelTable = new Properties();
        patchSizeLabelTable.put(NoiseEstimatorFilterPreferences.PATCH_SIZE_MIN,
                new JLabel(String.valueOf(NoiseEstimatorFilterPreferences.PATCH_SIZE_MIN)));
        patchSizeLabelTable.put(NoiseEstimatorFilterPreferences.PATCH_SIZE_MAX,
                new JLabel(String.valueOf(NoiseEstimatorFilterPreferences.PATCH_SIZE_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Patch size (px)",
                filterPreferencesSelectorDataModel.patchSizeSlider, patchSizeLabelTable,
                filterPreferencesSelectorDataModel.patchSizeText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        Properties thresholdLabelTable = new Properties();
        thresholdLabelTable.put(NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MIN,
                new JLabel(String.valueOf(NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MIN)));
        thresholdLabelTable.put(NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MAX,
                new JLabel(String.valueOf(NoiseEstimatorFilterPreferences.FLAT_VARIANCE_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Flat variance threshold",
                filterPreferencesSelectorDataModel.flatVarianceThresholdSlider, thresholdLabelTable,
                filterPreferencesSelectorDataModel.flatVarianceThresholdText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
