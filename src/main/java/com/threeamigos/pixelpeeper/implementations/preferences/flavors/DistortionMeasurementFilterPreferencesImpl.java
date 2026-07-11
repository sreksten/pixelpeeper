package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.DistortionMeasurementEdgeThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.DistortionMeasurementGridSizeChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DistortionMeasurementFilterPreferences;

public class DistortionMeasurementFilterPreferencesImpl extends BasicPropertyChangeAware
        implements DistortionMeasurementFilterPreferences {

    private int edgeThreshold;
    private int gridSize;

    @Override
    public int getEdgeThreshold() {
        return edgeThreshold;
    }

    @Override
    public void setEdgeThreshold(int edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
        EventBus.get().publish(new DistortionMeasurementEdgeThresholdChangedEvent(edgeThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public int getGridSize() {
        return gridSize;
    }

    @Override
    public void setGridSize(int gridSize) {
        // Snap to nearest odd value
        this.gridSize = (gridSize % 2 == 0) ? gridSize + 1 : gridSize;
        EventBus.get().publish(new DistortionMeasurementGridSizeChangedEvent(this.gridSize));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        edgeThreshold = EDGE_THRESHOLD_DEFAULT;
        gridSize = GRID_SIZE_DEFAULT;
    }

    @Override
    public void validate() {
        if (edgeThreshold < EDGE_THRESHOLD_MIN || edgeThreshold > EDGE_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid edge threshold: " + edgeThreshold);
        }
        if (gridSize < GRID_SIZE_MIN || gridSize > GRID_SIZE_MAX) {
            throw new IllegalArgumentException("Invalid grid size: " + gridSize);
        }
    }
}
