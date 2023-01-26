package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.HintsPreferences;

public class HintsPreferencesImpl implements HintsPreferences {

	private boolean hintsVisibleAtStartup;
	private int lastHintIndex;

	@Override
	public String getDescription() {
		return "hints preferences";
	}

	@Override
	public void validate() {
	}

	@Override
	public void loadDefaultValues() {
		hintsVisibleAtStartup = HintsPreferences.HINTS_PREFERENCES_VISIBLE_DEFAULT;
		lastHintIndex = 0;
	}

	@Override
	public void setHintsVisibleAtStartup(boolean hintsVisibleAtStartup) {
		this.hintsVisibleAtStartup = hintsVisibleAtStartup;
	}

	@Override
	public boolean isHintsVisibleAtStartup() {
		return hintsVisibleAtStartup;
	}

	@Override
	public void setLastHintIndex(int lastHintIndex) {
		this.lastHintIndex = lastHintIndex;
	}

	@Override
	public int getLastHintIndex() {
		return lastHintIndex;
	}

}
