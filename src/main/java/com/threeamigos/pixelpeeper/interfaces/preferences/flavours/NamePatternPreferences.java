package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;

public interface NamePatternPreferences extends Preferences {

	public static final String NAME_PATTERN_PREFERENCES_DEFAULT = "{FILENAME}";

	default String getDescription() {
		return "Name pattern preferences";
	}

	public void setNamePattern(String namePattern);

	public String getNamePattern();

}
