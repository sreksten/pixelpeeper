package com.threeamigos.pixelpeeper.implementations.filters.ui;

import com.threeamigos.pixelpeeper.implementations.filters.flavors.SharpnessHeatmapFilterImpl;
import com.threeamigos.pixelpeeper.interfaces.datamodel.DataModel;
import com.threeamigos.pixelpeeper.interfaces.filters.Filter;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SharpnessHeatmapFilterPreferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;

class SharpnessHeatmapFilterPreferencesSelectorDataModel extends FilterPreferencesSelectorDataModel {

    private final SharpnessHeatmapFilterPreferences preferences;
    private final int gridSizeBackup;

    final JSlider gridSizeSlider;
    final JLabel gridSizeText;

    SharpnessHeatmapFilterPreferencesSelectorDataModel(DataModel dataModel,
                                                       FilterPreferences filterPreferences,
                                                       SharpnessHeatmapFilterPreferences preferences,
                                                       Component component) {
        super(dataModel, filterPreferences, component);
        this.preferences = preferences;

        gridSizeBackup = preferences.getGridSize();

        gridSizeSlider = createSlider(
                SharpnessHeatmapFilterPreferences.GRID_SIZE_MIN,
                SharpnessHeatmapFilterPreferences.GRID_SIZE_MAX,
                gridSizeBackup);
        gridSizeSlider.setMajorTickSpacing(SharpnessHeatmapFilterPreferences.GRID_SIZE_STEP);
        gridSizeSlider.setMinorTickSpacing(SharpnessHeatmapFilterPreferences.GRID_SIZE_STEP);
        gridSizeSlider.setSnapToTicks(true);
        gridSizeSlider.setPaintTicks(true);
        gridSizeSlider.setPaintLabels(true);

        gridSizeText = new JLabel(gridSizeBackup + "×" + gridSizeBackup);
    }

    @Override
    void cancelSelection() {
        preferences.setGridSize(gridSizeBackup);
        gridSizeSlider.setValue(gridSizeBackup);
    }

    @Override
    void acceptSelection() {
        preferences.setGridSize(gridSizeSlider.getValue());
    }

    @Override
    void reset() {
        preferences.setGridSize(gridSizeBackup);
        gridSizeSlider.setValue(gridSizeBackup);
    }

    @Override
    void resetToDefault() {
        int def = SharpnessHeatmapFilterPreferences.GRID_SIZE_DEFAULT;
        gridSizeSlider.setValue(def);
        preferences.setGridSize(def);
        updateGridSizeText(def);
    }

    @Override
    public void handleStateChanged(ChangeEvent e) {
        if (e.getSource() == gridSizeSlider) {
            int value = gridSizeSlider.getValue();
            // Snap to nearest odd value in case the slider lands on an even one
            if (value % 2 == 0) {
                value = Math.min(value + 1, SharpnessHeatmapFilterPreferences.GRID_SIZE_MAX);
                gridSizeSlider.setValue(value);
            }
            updateGridSizeText(value);
            preferences.setGridSize(value);
        }
    }

    private void updateGridSizeText(int size) {
        gridSizeText.setText(size + "×" + size);
    }

    @Override
    public FilterFlavor getFilterFlavor() {
        return FilterFlavor.SHARPNESS_HEATMAP;
    }

    @Override
    protected Filter getFilterImplementation() {
        return new SharpnessHeatmapFilterImpl(preferences);
    }

    @Override
    boolean isAnyCalculationParameterModified() {
        return gridSizeSlider.getValue() != gridSizeBackup;
    }
}
