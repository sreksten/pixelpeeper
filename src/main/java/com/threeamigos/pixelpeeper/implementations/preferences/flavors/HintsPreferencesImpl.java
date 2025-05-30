package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.common.util.interfaces.preferences.flavours.HintsPreferences;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;

public class HintsPreferencesImpl extends BasicPropertyChangeAware implements HintsPreferences {

    private boolean hintsVisibleAtStartup;
    private int lastHintIndex;

    @Override
    public String getDescription() {
        return "hints preferences";
    }

    @Override
    public void setHintsVisibleAtStartup(boolean hintsVisibleAtStartup) {
        boolean oldHintsVisibleAtStartup = this.hintsVisibleAtStartup;
        this.hintsVisibleAtStartup = hintsVisibleAtStartup;
        firePropertyChange(CommunicationMessages.HINTS_VISIBILITY_AT_STARTUP_CHANGED, oldHintsVisibleAtStartup,
                hintsVisibleAtStartup);
    }

    @Override
    public boolean isHintsVisibleAtStartup() {
        return hintsVisibleAtStartup;
    }

    @Override
    public void setLastHintIndex(int lastHintIndex) {
        int oldLastHintIndex = this.lastHintIndex;
        this.lastHintIndex = lastHintIndex;
        firePropertyChange(CommunicationMessages.HINTS_INDEX_CHANGED, oldLastHintIndex, lastHintIndex);
    }

    @Override
    public int getLastHintIndex() {
        return lastHintIndex;
    }

    @Override
    public void loadDefaultValues() {
        hintsVisibleAtStartup = HINTS_PREFERENCES_VISIBLE_DEFAULT;
        lastHintIndex = HINTS_PREFERENCES_INDEX_DEFAULT;
    }
}
