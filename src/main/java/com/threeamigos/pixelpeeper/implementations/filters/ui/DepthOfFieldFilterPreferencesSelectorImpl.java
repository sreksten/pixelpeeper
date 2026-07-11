package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DepthOfFieldFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class DepthOfFieldFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 55);

    private final DepthOfFieldFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public DepthOfFieldFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            DepthOfFieldFilterPreferences depthOfFieldFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new DepthOfFieldFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, depthOfFieldFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public DepthOfFieldFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Depth of Field Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties labelTable = new Properties();
        labelTable.put(DepthOfFieldFilterPreferences.COC_DENOMINATOR_MIN,
                new JLabel(String.valueOf(DepthOfFieldFilterPreferences.COC_DENOMINATOR_MIN)));
        labelTable.put(DepthOfFieldFilterPreferences.COC_DENOMINATOR_MAX,
                new JLabel(String.valueOf(DepthOfFieldFilterPreferences.COC_DENOMINATOR_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "CoC denominator",
                filterPreferencesSelectorDataModel.cocDenominatorSlider, labelTable,
                filterPreferencesSelectorDataModel.cocDenominatorText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
