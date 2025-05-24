package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.flavours.SecondaryWindowPreferences;

/**
 * Preferences for the drag-and-drop proxy window
 *
 * @author Stefano Reksten
 */
public interface DragAndDropWindowPreferences extends SecondaryWindowPreferences {

    boolean OPEN_IMMEDIATELY_DEFAULT = true;

    boolean isOpenImmediately();

    void setOpenImmediately(boolean openImmediately);

    default String getDescription() {
        return "Drag-and-drop Window preferences";
    }

}
