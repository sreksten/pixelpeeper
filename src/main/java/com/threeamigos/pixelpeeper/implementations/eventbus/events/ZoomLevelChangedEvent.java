package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class ZoomLevelChangedEvent {
    public final int zoomLevel;

    public ZoomLevelChangedEvent(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}
