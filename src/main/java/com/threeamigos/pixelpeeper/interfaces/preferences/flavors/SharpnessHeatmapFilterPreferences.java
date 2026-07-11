package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Preferences for the sharpness heatmap filter.
 *
 * @author Stefano Reksten
 */
public interface SharpnessHeatmapFilterPreferences extends Preferences {

    int GRID_SIZE_DEFAULT = 5;
    int GRID_SIZE_MIN = 3;
    int GRID_SIZE_MAX = 9;
    int GRID_SIZE_STEP = 2;

    default String getDescription() {
        return "Sharpness heatmap filter preferences";
    }

    int getGridSize();

    void setGridSize(int gridSize);
}
