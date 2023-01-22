package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

/**
 * Keeps track of windows preferences
 *
 * @author Stefano Reksten
 *
 */
public interface MainWindowPreferences extends Preferences {

	default String getDescription() {
		return "Main window preferences";
	}

	public void setMainWindowWidth(int width);

	public int getMainWindowWidth();

	public void setMainWindowHeight(int height);

	public int getMainWindowHeight();

	public void setMainWindowX(int x);

	public int getMainWindowX();

	public void setMainWindowY(int y);

	public int getMainWindowY();

}
