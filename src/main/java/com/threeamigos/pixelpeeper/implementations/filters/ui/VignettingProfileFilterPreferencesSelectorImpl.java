package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.VignettingProfileFilterPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class VignettingProfileFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 55);

    private final VignettingProfileFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public VignettingProfileFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            VignettingProfileFilterPreferences vignettingProfileFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new VignettingProfileFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, vignettingProfileFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public VignettingProfileFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Vignetting Profile Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        Properties ringCountLabelTable = new Properties();
        ringCountLabelTable.put(VignettingProfileFilterPreferences.RING_COUNT_MIN,
                new JLabel(String.valueOf(VignettingProfileFilterPreferences.RING_COUNT_MIN)));
        ringCountLabelTable.put(VignettingProfileFilterPreferences.RING_COUNT_MAX,
                new JLabel(String.valueOf(VignettingProfileFilterPreferences.RING_COUNT_MAX)));

        createSliderPanel(flavorPanel, FLAVOR_DIMENSION, "Ring count",
                filterPreferencesSelectorDataModel.ringCountSlider, ringCountLabelTable,
                filterPreferencesSelectorDataModel.ringCountText);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
