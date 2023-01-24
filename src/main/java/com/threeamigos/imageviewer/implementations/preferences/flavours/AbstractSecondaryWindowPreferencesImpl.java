package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.SecondaryWindowPreferences;

public abstract class AbstractSecondaryWindowPreferencesImpl extends AbstractWindowPreferencesImpl
		implements SecondaryWindowPreferences {

	private boolean visible;

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

}
