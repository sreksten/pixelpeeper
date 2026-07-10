package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class FilterVisibilityChangedEvent {
    public final boolean showResults;

    public FilterVisibilityChangedEvent(boolean showResults) {
        this.showResults = showResults;
    }
}
