package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class GridSpacingChangedEvent {
    public final int spacing;

    public GridSpacingChangedEvent(int spacing) {
        this.spacing = spacing;
    }
}
