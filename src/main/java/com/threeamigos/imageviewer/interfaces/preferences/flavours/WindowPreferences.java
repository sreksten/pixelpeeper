package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

/**
 * Keeps track of windows preferences
 *
 * @author Stefano Reksten
 *
 */
public interface WindowPreferences extends Preferences {

	public static final boolean AUTOROTATION_DEFAULT = true;
	public static final boolean MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT = true;

	public void setMainWindowWidth(int width);

	public int getMainWindowWidth();

	public void setMainWindowHeight(int height);

	public int getMainWindowHeight();

	public void setMainWindowX(int x);

	public int getMainWindowX();

	public void setMainWindowY(int y);

	public int getMainWindowY();

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

	public void setAutorotation(boolean autorotation);

	public boolean isAutorotation();

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages);

	public boolean isMovementAppliedToAllImages();

}
