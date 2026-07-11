package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class BokehQualityPatchSizeChangedEvent {
    public final int patchSize;
    public BokehQualityPatchSizeChangedEvent(int patchSize) {
        this.patchSize = patchSize;
    }
}
