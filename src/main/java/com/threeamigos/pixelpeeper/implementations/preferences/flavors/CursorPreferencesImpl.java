package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BigPointerRotationChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BigPointerSizeChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BigPointerVisibilityChangedEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CursorPreferences;

public class CursorPreferencesImpl extends BasicPropertyChangeAware implements CursorPreferences {

    private boolean bigPointerVisible;
    private int bigPointerSize;
    private Rotation bigPointerRotation;

    @Override
    public void setBigPointerVisible(boolean bigPointerVisible) {
        boolean oldBigPointerVisible = this.bigPointerVisible;
        this.bigPointerVisible = bigPointerVisible;
        EventBus.get().publish(new BigPointerVisibilityChangedEvent());
    }

    @Override
    public boolean isBigPointerVisible() {
        return bigPointerVisible;
    }

    @Override
    public void setBigPointerSize(int bigPointerSize) {
        int oldBigPointerSize = this.bigPointerSize;
        this.bigPointerSize = bigPointerSize;
        EventBus.get().publish(new BigPointerSizeChangedEvent());
    }

    @Override
    public int getBigPointerSize() {
        return bigPointerSize;
    }

    @Override
    public void setBigPointerRotation(Rotation bigPointerRotation) {
        Rotation oldBigPointerRotation = this.bigPointerRotation;
        this.bigPointerRotation = bigPointerRotation;
        EventBus.get().publish(new BigPointerRotationChangedEvent());
    }

    @Override
    public Rotation getBigPointerRotation() {
        return bigPointerRotation;
    }

    @Override
    public void loadDefaultValues() {
        bigPointerVisible = CursorPreferences.BIG_POINTER_VISIBLE_DEFAULT;
        bigPointerSize = CursorPreferences.BIG_POINTER_SIZE_DEFAULT;
        bigPointerRotation = CursorPreferences.POINTER_ROTATION_DEFAULT;
    }
}
