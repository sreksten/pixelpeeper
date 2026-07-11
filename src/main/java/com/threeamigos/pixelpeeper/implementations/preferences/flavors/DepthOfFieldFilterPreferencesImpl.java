package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.DepthOfFieldCocDenominatorChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.RequestFilterCalculationEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DepthOfFieldFilterPreferences;

public class DepthOfFieldFilterPreferencesImpl extends BasicPropertyChangeAware
        implements DepthOfFieldFilterPreferences {

    private int cocDenominator;

    @Override
    public int getCocDenominator() {
        return cocDenominator;
    }

    @Override
    public void setCocDenominator(int cocDenominator) {
        this.cocDenominator = cocDenominator;
        EventBus.get().publish(new DepthOfFieldCocDenominatorChangedEvent(cocDenominator));
        EventBus.get().publish(new RequestFilterCalculationEvent());
    }

    @Override
    public void loadDefaultValues() {
        cocDenominator = COC_DENOMINATOR_DEFAULT;
    }

    @Override
    public void validate() {
        if (cocDenominator < COC_DENOMINATOR_MIN || cocDenominator > COC_DENOMINATOR_MAX) {
            throw new IllegalArgumentException("Invalid CoC denominator: " + cocDenominator);
        }
    }
}
