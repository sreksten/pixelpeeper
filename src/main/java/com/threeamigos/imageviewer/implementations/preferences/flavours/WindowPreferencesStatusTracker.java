package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.persister.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;

public class WindowPreferencesStatusTracker implements StatusTracker<WindowPreferences> {

	private int mainWindowWidthAtStart;
	private int mainWindowHeightAtStart;
	private int mainWindowXAtStart;
	private int mainWindowYAtStart;

	private boolean dragAndDropWindowVisibleAtStart;
	private int dragAndDropWindowWidthAtStart;
	private int dragAndDropWindowHeightAtStart;
	private int dragAndDropWindowXAtStart;
	private int dragAndDropWindowYAtStart;

	private boolean autorotationAtStart;
	private boolean movementAppliesToAllImagesAtStart;

	private final WindowPreferences windowPreferences;

	public WindowPreferencesStatusTracker(WindowPreferences windowPreferences) {
		this.windowPreferences = windowPreferences;
	}

	@Override
	public void loadInitialValues() {
		mainWindowWidthAtStart = windowPreferences.getMainWindowWidth();
		mainWindowHeightAtStart = windowPreferences.getMainWindowHeight();
		mainWindowXAtStart = windowPreferences.getMainWindowX();
		mainWindowYAtStart = windowPreferences.getMainWindowY();

		dragAndDropWindowVisibleAtStart = windowPreferences.isDragAndDropWindowVisible();
		dragAndDropWindowWidthAtStart = windowPreferences.getDragAndDropWindowWidth();
		dragAndDropWindowHeightAtStart = windowPreferences.getDragAndDropWindowHeight();
		dragAndDropWindowXAtStart = windowPreferences.getDragAndDropWindowX();
		dragAndDropWindowYAtStart = windowPreferences.getDragAndDropWindowY();

		autorotationAtStart = windowPreferences.isAutorotation();
		movementAppliesToAllImagesAtStart = windowPreferences.isMovementAppliedToAllImages();
	}

	@Override
	public boolean hasChanged() {
		return windowPreferences.getMainWindowWidth() != mainWindowWidthAtStart
				|| windowPreferences.getMainWindowHeight() != mainWindowHeightAtStart
				|| windowPreferences.getMainWindowX() != mainWindowXAtStart
				|| windowPreferences.getMainWindowY() != mainWindowYAtStart
				|| windowPreferences.isDragAndDropWindowVisible() != dragAndDropWindowVisibleAtStart
				|| windowPreferences.getDragAndDropWindowWidth() != dragAndDropWindowWidthAtStart
				|| windowPreferences.getDragAndDropWindowHeight() != dragAndDropWindowHeightAtStart
				|| windowPreferences.getDragAndDropWindowX() != dragAndDropWindowXAtStart
				|| windowPreferences.getDragAndDropWindowY() != dragAndDropWindowYAtStart
				|| windowPreferences.isAutorotation() != autorotationAtStart
				|| windowPreferences.isMovementAppliedToAllImages() != movementAppliesToAllImagesAtStart;
	}

}
