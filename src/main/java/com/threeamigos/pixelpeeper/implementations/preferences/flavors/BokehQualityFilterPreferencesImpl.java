package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BokehQualityPatchSizeChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BokehQualitySharpnessThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.BokehQualityFilterPreferences;

public class BokehQualityFilterPreferencesImpl extends BasicPropertyChangeAware
        implements BokehQualityFilterPreferences {

    private int sharpnessThreshold;
    private int patchSize;

    @Override
    public int getSharpnessThreshold() {
        return sharpnessThreshold;
    }

    @Override
    public void setSharpnessThreshold(int sharpnessThreshold) {
        this.sharpnessThreshold = sharpnessThreshold;
        EventBus.get().publish(new BokehQualitySharpnessThresholdChangedEvent(sharpnessThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public int getPatchSize() {
        return patchSize;
    }

    @Override
    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
        EventBus.get().publish(new BokehQualityPatchSizeChangedEvent(patchSize));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        sharpnessThreshold = SHARPNESS_THRESHOLD_DEFAULT;
        patchSize = PATCH_SIZE_DEFAULT;
    }

    @Override
    public void validate() {
        if (sharpnessThreshold < SHARPNESS_THRESHOLD_MIN || sharpnessThreshold > SHARPNESS_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid sharpness threshold: " + sharpnessThreshold);
        }
        if (patchSize < PATCH_SIZE_MIN || patchSize > PATCH_SIZE_MAX) {
            throw new IllegalArgumentException("Invalid patch size: " + patchSize);
        }
    }
}
