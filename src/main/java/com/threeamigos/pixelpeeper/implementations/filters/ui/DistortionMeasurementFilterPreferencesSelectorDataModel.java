package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.DistortionMeasurementFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DistortionMeasurementFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class DistortionMeasurementFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final DistortionMeasurementFilterPreferences preferences;
    private final int edgeThresholdBackup;
    private final int gridSizeBackup;

    final JSlider edgeThresholdSlider;
    final JLabel edgeThresholdText;

    final JSlider gridSizeSlider;
    final JLabel gridSizeText;

    DistortionMeasurementFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                            FilterPreferences filterPreferences,
                                                            DistortionMeasurementFilterPreferences preferences,
                                                            Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        edgeThresholdBackup = preferences.getEdgeThreshold();
        gridSizeBackup = preferences.getGridSize();

        edgeThresholdSlider = createSlider(
                DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MIN,
                DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_MAX,
                edgeThresholdBackup);
        edgeThresholdText = new JLabel(String.valueOf(edgeThresholdBackup));

        gridSizeSlider = createSlider(
                DistortionMeasurementFilterPreferences.GRID_SIZE_MIN,
                DistortionMeasurementFilterPreferences.GRID_SIZE_MAX,
                gridSizeBackup);
        gridSizeText = new JLabel(String.valueOf(gridSizeBackup));
    }

    @Override
    void cancelSelection() {
        preferences.setEdgeThreshold(edgeThresholdBackup);
        edgeThresholdSlider.setValue(edgeThresholdBackup);
        preferences.setGridSize(gridSizeBackup);
        gridSizeSlider.setValue(gridSizeBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setEdgeThreshold(edgeThresholdSlider.getValue());
        preferences.setGridSize(gridSizeSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setEdgeThreshold(edgeThresholdBackup);
        edgeThresholdSlider.setValue(edgeThresholdBackup);
        preferences.setGridSize(gridSizeBackup);
        gridSizeSlider.setValue(gridSizeBackup);
    }

    @Override
    void resetToDefault() {
        int defEdge = DistortionMeasurementFilterPreferences.EDGE_THRESHOLD_DEFAULT;
        int defGrid = DistortionMeasurementFilterPreferences.GRID_SIZE_DEFAULT;

        edgeThresholdSlider.setValue(defEdge);
        preferences.setEdgeThreshold(defEdge);
        edgeThresholdText.setText(String.valueOf(defEdge));

        gridSizeSlider.setValue(defGrid);
        preferences.setGridSize(defGrid);
        gridSizeText.setText(String.valueOf(defGrid));
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == edgeThresholdSlider) {
            int value = edgeThresholdSlider.getValue();
            edgeThresholdText.setText(String.valueOf(value));
            preferences.setEdgeThreshold(value);
        } else if (e.getSource() == gridSizeSlider) {
            int value = gridSizeSlider.getValue();
            // Snap to odd
            if (value % 2 == 0) value = value + 1;
            gridSizeText.setText(String.valueOf(value));
            preferences.setGridSize(value);
        }
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.DISTORTION_MEASUREMENT;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new DistortionMeasurementFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return edgeThresholdSlider.getValue() != edgeThresholdBackup
                || gridSizeSlider.getValue() != gridSizeBackup;
    }
}
