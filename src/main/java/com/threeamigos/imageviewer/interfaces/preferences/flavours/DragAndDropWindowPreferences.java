package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface DragAndDropWindowPreferences extends Preferences {

	default String getDescription() {
		return "Drag and drop window preferences";
	}

	public void setDragAndDropWindowVisible(boolean visible);

	public boolean isDragAndDropWindowVisible();

	public void setDragAndDropWindowWidth(int width);

	public int getDragAndDropWindowWidth();

	public void setDragAndDropWindowHeight(int height);

	public int getDragAndDropWindowHeight();

	public void setDragAndDropWindowX(int x);

	public int getDragAndDropWindowX();

	public void setDragAndDropWindowY(int y);

	public int getDragAndDropWindowY();

}
