package com.threeamigos.imageviewer.interfaces.preferences.flavours;

public interface DragAndDropWindowPreferences extends SecondaryWindowPreferences {

	default String getDescription() {
		return "Drag and drop window preferences";
	}

}
