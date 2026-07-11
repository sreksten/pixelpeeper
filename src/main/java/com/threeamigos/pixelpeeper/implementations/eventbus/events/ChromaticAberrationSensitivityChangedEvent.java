package com.threeamigos.pixelpeeper.implementations.eventbus.events;

public class ChromaticAberrationSensitivityChangedEvent {
    public final int sensitivity;
    public ChromaticAberrationSensitivityChangedEvent(int sensitivity) {
        this.sensitivity = sensitivity;
    }
}
