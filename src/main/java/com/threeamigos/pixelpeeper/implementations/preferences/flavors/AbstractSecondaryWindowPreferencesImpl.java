package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;

public abstract class AbstractSecondaryWindowPreferencesImpl extends AbstractWindowPreferencesImpl
		implements SecondaryWindowPreferences {

	private boolean visible;

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		boolean oldVisible = this.visible;
		this.visible = visible;
	}

	@Override
	public void loadDefaultValues() {
		super.loadDefaultValues();
		this.visible = SecondaryWindowPreferences.VISIBLE_DEFAULT;
	}

}
