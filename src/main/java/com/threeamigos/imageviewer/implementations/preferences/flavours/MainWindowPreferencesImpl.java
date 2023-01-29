package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;

public class MainWindowPreferencesImpl extends AbstractWindowPreferencesImpl implements MainWindowPreferences {

	@Override
	public void validate() {
		checkMinWidth("main", MIN_WIDTH);
		checkMinHeight("main", MIN_HEIGHT);
	}

}
