package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.SharpnessHeatmapGridSizeChangedEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SharpnessHeatmapFilterPreferences;

public class SharpnessHeatmapFilterPreferencesImpl extends BasicPropertyChangeAware
        implements SharpnessHeatmapFilterPreferences {

    private int gridSize;

    @Override
    public int getGridSize() {
        return gridSize;
    }

    @Override
    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        EventBus.get().publish(new SharpnessHeatmapGridSizeChangedEvent(gridSize));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        gridSize = GRID_SIZE_DEFAULT;
    }

    @Override
    public void validate() {
        if (gridSize < GRID_SIZE_MIN || gridSize > GRID_SIZE_MAX) {
            throw new IllegalArgumentException("Invalid sharpness heatmap grid size: " + gridSize);
        }
    }
}
