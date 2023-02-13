package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;

public interface HintsPreferences extends Preferences {

	public static final boolean HINTS_PREFERENCES_VISIBLE_DEFAULT = true;
	public static final int HINTS_PREFERENCES_INDEX_DEFAULT = 0;

	public void setHintsVisibleAtStartup(boolean hintsVisibleAtStartup);

	public boolean isHintsVisibleAtStartup();

	public void setLastHintIndex(int lastHintIndex);

	public int getLastHintIndex();

}
