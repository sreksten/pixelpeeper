package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class NoiseEstimatorFlatVarianceThresholdChangedEvent {

    public final int flatVarianceThreshold;

    public NoiseEstimatorFlatVarianceThresholdChangedEvent(int flatVarianceThreshold) {
        this.flatVarianceThreshold = flatVarianceThreshold;
    }
}
