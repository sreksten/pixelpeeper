package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class DistortionMeasurementGridSizeChangedEvent {
    public final int gridSize;
    public DistortionMeasurementGridSizeChangedEvent(int gridSize) {
        this.gridSize = gridSize;
    }
}
