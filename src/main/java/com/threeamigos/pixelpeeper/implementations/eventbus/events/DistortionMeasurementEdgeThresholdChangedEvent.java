package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class DistortionMeasurementEdgeThresholdChangedEvent {
    public final int edgeThreshold;
    public DistortionMeasurementEdgeThresholdChangedEvent(int edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
    }
}
