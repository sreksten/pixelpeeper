package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.BokehQualityFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class BokehQualityFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 100);

    private final BokehQualityFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public BokehQualityFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            BokehQualityFilterPreferences bokehQualityFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new BokehQualityFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, bokehQualityFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public BokehQualityFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Bokeh Quality Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties sharpnessLabelTable = new Properties();
        sharpnessLabelTable.put(BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MIN,
                new JLabel(String.valueOf(BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MIN)));
        sharpnessLabelTable.put(BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MAX,
                new JLabel(String.valueOf(BokehQualityFilterPreferences.SHARPNESS_THRESHOLD_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Sharpness threshold",
                filterPreferencesSelectorDataModel.sharpnessThresholdSlider, sharpnessLabelTable,
                filterPreferencesSelectorDataModel.sharpnessThresholdText);

        flavorPanel.add(Box.createVerticalStrut(SPACING));

        Properties patchLabelTable = new Properties();
        patchLabelTable.put(BokehQualityFilterPreferences.PATCH_SIZE_MIN,
                new JLabel(String.valueOf(BokehQualityFilterPreferences.PATCH_SIZE_MIN)));
        patchLabelTable.put(BokehQualityFilterPreferences.PATCH_SIZE_MAX,
                new JLabel(String.valueOf(BokehQualityFilterPreferences.PATCH_SIZE_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Patch size",
                filterPreferencesSelectorDataModel.patchSizeSlider, patchLabelTable,
                filterPreferencesSelectorDataModel.patchSizeText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
