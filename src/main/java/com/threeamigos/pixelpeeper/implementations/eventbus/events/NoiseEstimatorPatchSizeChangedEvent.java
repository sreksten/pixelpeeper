package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class NoiseEstimatorPatchSizeChangedEvent {

    public final int patchSize;

    public NoiseEstimatorPatchSizeChangedEvent(int patchSize) {
        this.patchSize = patchSize;
    }
}
