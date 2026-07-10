package com.threeamigos.pixelpeeper.implementations.eventbus.events;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlice;

public class SliceFilterCalculationCompletedEvent {
    public final ImageSlice imageSlice;

    public SliceFilterCalculationCompletedEvent(ImageSlice imageSlice) {
        this.imageSlice = imageSlice;
    }
}
