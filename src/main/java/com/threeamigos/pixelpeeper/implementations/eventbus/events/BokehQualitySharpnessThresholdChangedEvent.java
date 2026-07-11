package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class BokehQualitySharpnessThresholdChangedEvent {
    public final int sharpnessThreshold;
    public BokehQualitySharpnessThresholdChangedEvent(int sharpnessThreshold) {
        this.sharpnessThreshold = sharpnessThreshold;
    }
}
