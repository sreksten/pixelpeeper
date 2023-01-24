package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;

public class DragAndDropWindowPreferencesImpl extends AbstractSecondaryWindowPreferencesImpl
		implements DragAndDropWindowPreferences {

	@Override
	public void validate() {
		checkBoundaries("drag and drop");
	}
}
