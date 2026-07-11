package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class VignettingProfileRingCountChangedEvent {

    public final int ringCount;

    public VignettingProfileRingCountChangedEvent(int ringCount) {
        this.ringCount = ringCount;
    }
}
