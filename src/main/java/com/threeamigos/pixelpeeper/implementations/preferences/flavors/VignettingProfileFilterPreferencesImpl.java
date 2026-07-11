package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.VignettingProfileRingCountChangedEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.VignettingProfileFilterPreferences;

public class VignettingProfileFilterPreferencesImpl extends BasicPropertyChangeAware
        implements VignettingProfileFilterPreferences {

    private int ringCount;

    @Override
    public int getRingCount() {
        return ringCount;
    }

    @Override
    public void setRingCount(int ringCount) {
        this.ringCount = ringCount;
        EventBus.get().publish(new VignettingProfileRingCountChangedEvent(ringCount));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        ringCount = RING_COUNT_DEFAULT;
    }

    @Override
    public void validate() {
        if (ringCount < RING_COUNT_MIN || ringCount > RING_COUNT_MAX) {
            throw new IllegalArgumentException("Invalid ring count: " + ringCount);
        }
    }
}
