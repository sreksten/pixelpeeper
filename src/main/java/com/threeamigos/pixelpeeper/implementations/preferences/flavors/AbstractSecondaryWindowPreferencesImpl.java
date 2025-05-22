package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;

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
		firePropertyChange(CommunicationMessages.WINDOW_VISIBILITY_CHANGED, oldVisible, visible);
	}

	@Override
	public void loadDefaultValues() {
		super.loadDefaultValues();
		this.visible = SecondaryWindowPreferences.VISIBLE_DEFAULT;
	}

}
