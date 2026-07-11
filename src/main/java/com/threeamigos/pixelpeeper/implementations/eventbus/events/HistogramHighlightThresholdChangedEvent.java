package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class HistogramHighlightThresholdChangedEvent {

    public final int highlightThreshold;

    public HistogramHighlightThresholdChangedEvent(int highlightThreshold) {
        this.highlightThreshold = highlightThreshold;
    }
}
