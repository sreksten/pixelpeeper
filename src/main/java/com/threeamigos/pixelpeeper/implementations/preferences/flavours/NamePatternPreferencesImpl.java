package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.NamePatternPreferences;

public class NamePatternPreferencesImpl extends PropertyChangeAwareImpl implements NamePatternPreferences {

	private String namePattern;
	
	@Override
	public void setNamePattern(String namePattern) {
		this.namePattern = namePattern;
	}
	
	@Override
	public String getNamePattern() {
		return namePattern;
	}

	@Override
	public void validate() {
	}

	@Override
	public void loadDefaultValues() {
		namePattern = NamePatternPreferences.NAME_PATTERN_PREFERENCES_DEFAULT;
	}

}
