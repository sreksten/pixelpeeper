package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class SharpnessHeatmapGridSizeChangedEvent {

    public final int gridSize;

    public SharpnessHeatmapGridSizeChangedEvent(int gridSize) {
        this.gridSize = gridSize;
    }
}
