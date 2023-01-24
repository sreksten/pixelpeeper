package com.threeamigos.imageviewer.interfaces.preferences.flavours;

/**
 * Keeps track of windows preferences
 *
 * @author Stefano Reksten
 *
 */
public interface MainWindowPreferences extends WindowPreferences {

	default String getDescription() {
		return "Main window preferences";
	}

}
