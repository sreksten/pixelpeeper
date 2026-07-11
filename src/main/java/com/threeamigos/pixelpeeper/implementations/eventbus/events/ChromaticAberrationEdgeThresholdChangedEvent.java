package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class ChromaticAberrationEdgeThresholdChangedEvent {
    public final int edgeThreshold;
    public ChromaticAberrationEdgeThresholdChangedEvent(int edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
    }
}
