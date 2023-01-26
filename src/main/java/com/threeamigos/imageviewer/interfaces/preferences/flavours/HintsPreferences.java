package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface HintsPreferences extends Preferences {

	public static final boolean HINTS_PREFERENCES_VISIBLE_DEFAULT = true;

	public void setHintsVisibleAtStartup(boolean hintsVisibleAtStartup);

	public boolean isHintsVisibleAtStartup();

	public void setLastHintIndex(int lastHintIndex);

	public int getLastHintIndex();

}
