package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.NoiseEstimatorFlatVarianceThresholdChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.NoiseEstimatorPatchSizeChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NoiseEstimatorFilterPreferences;

public class NoiseEstimatorFilterPreferencesImpl extends BasicPropertyChangeAware
        implements NoiseEstimatorFilterPreferences {

    private int patchSize;
    private int flatVarianceThreshold;

    @Override
    public int getPatchSize() {
        return patchSize;
    }

    @Override
    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
        EventBus.get().publish(new NoiseEstimatorPatchSizeChangedEvent(patchSize));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public int getFlatVarianceThreshold() {
        return flatVarianceThreshold;
    }

    @Override
    public void setFlatVarianceThreshold(int flatVarianceThreshold) {
        this.flatVarianceThreshold = flatVarianceThreshold;
        EventBus.get().publish(new NoiseEstimatorFlatVarianceThresholdChangedEvent(flatVarianceThreshold));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        patchSize = PATCH_SIZE_DEFAULT;
        flatVarianceThreshold = FLAT_VARIANCE_THRESHOLD_DEFAULT;
    }

    @Override
    public void validate() {
        if (patchSize < PATCH_SIZE_MIN || patchSize > PATCH_SIZE_MAX) {
            throw new IllegalArgumentException("Invalid patch size: " + patchSize);
        }
        if (flatVarianceThreshold < FLAT_VARIANCE_THRESHOLD_MIN || flatVarianceThreshold > FLAT_VARIANCE_THRESHOLD_MAX) {
            throw new IllegalArgumentException("Invalid flat variance threshold: " + flatVarianceThreshold);
        }
    }
}
