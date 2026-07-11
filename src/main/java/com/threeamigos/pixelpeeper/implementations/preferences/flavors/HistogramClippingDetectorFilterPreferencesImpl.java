package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.HistogramHighlightThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.HistogramShadowThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.HistogramClippingDetectorFilterPreferences;

public class HistogramClippingDetectorFilterPreferencesImpl extends BasicPropertyChangeAware
        implements HistogramClippingDetectorFilterPreferences {

    private int highlightThreshold;
    private int shadowThreshold;

    @Override
    public int getHighlightThreshold() {
        return highlightThreshold;
    }

    @Override
    public void setHighlightThreshold(int highlightThreshold) {
        this.highlightThreshold = highlightThreshold;
        EventBus.get().publish(new HistogramHighlightThresholdChangedEvent(highlightThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public int getShadowThreshold() {
        return shadowThreshold;
    }

    @Override
    public void setShadowThreshold(int shadowThreshold) {
        this.shadowThreshold = shadowThreshold;
        EventBus.get().publish(new HistogramShadowThresholdChangedEvent(shadowThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        highlightThreshold = HIGHLIGHT_THRESHOLD_DEFAULT;
        shadowThreshold = SHADOW_THRESHOLD_DEFAULT;
    }

    @Override
    public void validate() {
        if (highlightThreshold < HIGHLIGHT_THRESHOLD_MIN || highlightThreshold > HIGHLIGHT_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid highlight threshold: " + highlightThreshold);
        }
        if (shadowThreshold < SHADOW_THRESHOLD_MIN || shadowThreshold > SHADOW_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid shadow threshold: " + shadowThreshold);
        }
    }
}
