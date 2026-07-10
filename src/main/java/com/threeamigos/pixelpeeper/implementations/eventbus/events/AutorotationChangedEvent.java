package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class AutorotationChangedEvent {
    public final boolean autorotation;

    public AutorotationChangedEvent(boolean autorotation) {
        this.autorotation = autorotation;
    }
}
