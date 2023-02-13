package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

/**
 * Keeps track of windows preferences
 *
 * @author Stefano Reksten
 *
 */
public interface MainWindowPreferences extends WindowPreferences {

	public static final int MIN_WIDTH = 800;
	public static final int MIN_HEIGHT = 600;

	default String getDescription() {
		return "Main window preferences";
	}

}
