package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.common.util.interfaces.messagehandler.ThrowableHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifImageReader;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SharpnessHeatmapFilterPreferences;

import javax.swing.*;
import java.awt.*;

public class SharpnessHeatmapFilterPreferencesSelectorImpl extends FilterPreferencesSelectorImpl {

    private static final Dimension FLAVOR_DIMENSION = new Dimension(300, 70);

    private final SharpnessHeatmapFilterPreferencesSelectorDataModel filterPreferencesSelectorDataModel;

    public SharpnessHeatmapFilterPreferencesSelectorImpl(
            FilterPreferences filterPreferences,
            SharpnessHeatmapFilterPreferences sharpnessHeatmapFilterPreferences,
            DataModel dataModel, ExifImageReader exifImageReader, ThrowableHandler exceptionHandler) {
        super(filterPreferences, dataModel, exifImageReader, exceptionHandler);

        filterPreferencesSelectorDataModel = new SharpnessHeatmapFilterPreferencesSelectorDataModel(
                dataModel, filterPreferences, sharpnessHeatmapFilterPreferences, testImageCanvas);
        filterPreferencesSelectorDataModel.setSourceImage(testImage);
        filterPreferencesSelectorDataModel.startFilterCalculation();
    }

    @Override
    public SharpnessHeatmapFilterPreferencesSelectorDataModel getFilterPreferencesSelectorDataModel() {
        return filterPreferencesSelectorDataModel;
    }

    @Override
    String getPreferencesDescription() {
        return "Sharpness Heatmap Filter Preferences";
    }

    @Override
    JPanel createFlavorPanel(Component component) {
        JPanel flavorPanel = new JPanel();
        flavorPanel.setLayout(new BoxLayout(flavorPanel, BoxLayout.PAGE_AXIS));

        JPanel gridSizeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, SPACING, SPACING));
        gridSizeRow.add(new JLabel("Grid size:"));
        gridSizeRow.add(filterPreferencesSelectorDataModel.gridSizeSlider);
        gridSizeRow.add(filterPreferencesSelectorDataModel.gridSizeText);
        flavorPanel.add(gridSizeRow);

        return flavorPanel;
    }

    @Override
    Dimension getFlavorDimension() {
        return FLAVOR_DIMENSION;
    }
}
