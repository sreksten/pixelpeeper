package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;

public class DragAndDropWindowPreferencesImpl extends AbstractSecondaryWindowPreferencesImpl
		implements DragAndDropWindowPreferences {

	private boolean openImmediately;

	@Override
	public boolean isOpenImmediately() {
		return openImmediately;
	}

	@Override
	public void setOpenImmediately(boolean openImmediately) {
		this.openImmediately = openImmediately;
	}

	@Override
	public void validate() {
		checkBoundaries("drag and drop");
	}

	@Override
	public void loadDefaultValues() {
		super.loadDefaultValues();
		this.openImmediately = DragAndDropWindowPreferences.OPEN_IMMEDIATELY_DEFAULT;
	}

}
