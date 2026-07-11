package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class HistogramShadowThresholdChangedEvent {

    public final int shadowThreshold;

    public HistogramShadowThresholdChangedEvent(int shadowThreshold) {
        this.shadowThreshold = shadowThreshold;
    }
}
