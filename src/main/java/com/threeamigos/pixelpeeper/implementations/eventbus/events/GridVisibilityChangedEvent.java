package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class GridVisibilityChangedEvent {
    public final boolean visible;

    public GridVisibilityChangedEvent(boolean visible) {
        this.visible = visible;
    }
}
