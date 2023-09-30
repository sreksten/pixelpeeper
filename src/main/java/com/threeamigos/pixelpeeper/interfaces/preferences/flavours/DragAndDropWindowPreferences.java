package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;

public interface DragAndDropWindowPreferences extends SecondaryWindowPreferences {

	public static final boolean OPEN_IMMEDIATELY_DEFAULT = true;

	public boolean isOpenImmediately();

	public void setOpenImmediately(boolean openImmediately);

	default String getDescription() {
		return "Drag and drop window preferences";
	}

}
