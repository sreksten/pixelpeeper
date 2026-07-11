package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.ChromaticAberrationEdgeThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.ChromaticAberrationSensitivityChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ChromaticAberrationFilterPreferences;

public class ChromaticAberrationFilterPreferencesImpl extends BasicPropertyChangeAware
        implements ChromaticAberrationFilterPreferences {

    private int edgeThreshold;
    private int sensitivity;

    @Override
    public int getEdgeThreshold() {
        return edgeThreshold;
    }

    @Override
    public void setEdgeThreshold(int edgeThreshold) {
        this.edgeThreshold = edgeThreshold;
        EventBus.get().publish(new ChromaticAberrationEdgeThresholdChangedEvent(edgeThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public int getSensitivity() {
        return sensitivity;
    }

    @Override
    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
        EventBus.get().publish(new ChromaticAberrationSensitivityChangedEvent(sensitivity));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        edgeThreshold = EDGE_THRESHOLD_DEFAULT;
        sensitivity = SENSITIVITY_DEFAULT;
    }

    @Override
    public void validate() {
        if (edgeThreshold < EDGE_THRESHOLD_MIN || edgeThreshold > EDGE_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid edge threshold: " + edgeThreshold);
        }
        if (sensitivity < SENSITIVITY_MIN || sensitivity > SENSITIVITY_MAX) {
            throw new IllegalArgumentException("Invalid sensitivity: " + sensitivity);
        }
    }
}
