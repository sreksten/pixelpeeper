package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;

public class MainWindowPreferencesImpl implements MainWindowPreferences {

	private int mainWindowWidth;
	private int mainWindowHeight;
	private int mainWindowX;
	private int mainWindowY;

	private boolean dragAndDropWindowVisible;
	private int dragAndDropWindowWidth;
	private int dragAndDropWindowHeight;
	private int dragAndDropWindowX;
	private int dragAndDropWindowY;

	private boolean autorotation;
	private boolean movementAppliesToAllImages;

	@Override
	public int getMainWindowWidth() {
		return mainWindowWidth;
	}

	@Override
	public void setMainWindowWidth(int width) {
		this.mainWindowWidth = width;
	}

	@Override
	public int getMainWindowHeight() {
		return mainWindowHeight;
	}

	@Override
	public void setMainWindowHeight(int height) {
		this.mainWindowHeight = height;
	}

	@Override
	public int getMainWindowX() {
		return mainWindowX;
	}

	@Override
	public void setMainWindowX(int x) {
		this.mainWindowX = x;
	}

	@Override
	public int getMainWindowY() {
		return mainWindowY;
	}

	@Override
	public void setMainWindowY(int y) {
		this.mainWindowY = y;
	}

	@Override
	public void setDragAndDropWindowVisible(boolean visible) {
		this.dragAndDropWindowVisible = visible;
	}

	@Override
	public boolean isDragAndDropWindowVisible() {
		return dragAndDropWindowVisible;
	}

	@Override
	public void setDragAndDropWindowWidth(int width) {
		this.dragAndDropWindowWidth = width;
	}

	@Override
	public int getDragAndDropWindowWidth() {
		return dragAndDropWindowWidth;
	}

	@Override
	public void setDragAndDropWindowHeight(int height) {
		this.dragAndDropWindowHeight = height;
	}

	@Override
	public int getDragAndDropWindowHeight() {
		return dragAndDropWindowHeight;
	}

	@Override
	public void setDragAndDropWindowX(int x) {
		this.dragAndDropWindowX = x;
	}

	@Override
	public int getDragAndDropWindowX() {
		return dragAndDropWindowX;
	}

	@Override
	public void setDragAndDropWindowY(int y) {
		this.dragAndDropWindowY = y;
	}

	@Override
	public int getDragAndDropWindowY() {
		return dragAndDropWindowY;
	}

	@Override
	public void setAutorotation(boolean autorotation) {
		this.autorotation = autorotation;
	}

	@Override
	public boolean isAutorotation() {
		return autorotation;
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages) {
		this.movementAppliesToAllImages = movementAppliesToAllImages;
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return movementAppliesToAllImages;
	}

	@Override
	public void loadDefaultValues() {
		loadMainWindowDefaultValues();
		loadDragAndDropWindowDefaultValues();
		loadOtherValues();
	}

	private void loadMainWindowDefaultValues() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		int mainWindowWidth = screenDimension.width * 2 / 3;
		setMainWindowWidth(mainWindowWidth);
		int mainWindowHeight = screenDimension.height * 2 / 3;
		setMainWindowHeight(mainWindowHeight);
		setMainWindowX((screenDimension.width - mainWindowWidth) / 2);
		setMainWindowY((screenDimension.height - mainWindowHeight) / 2);
	}

	private void loadDragAndDropWindowDefaultValues() {
		setDragAndDropWindowVisible(false);
		setDragAndDropWindowWidth(300);
		setDragAndDropWindowHeight(300);
		setDragAndDropWindowX(0);
		setDragAndDropWindowY(0);
	}

	private void loadOtherValues() {
		setAutorotation(AUTOROTATION_DEFAULT);
		setMovementAppliedToAllImages(MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT);
	}

	@Override
	public void validate() {

		checkBoundaries("main", mainWindowWidth, mainWindowHeight, mainWindowX, mainWindowY);

		checkBoundaries("drag and drop", dragAndDropWindowWidth, dragAndDropWindowHeight, dragAndDropWindowX,
				dragAndDropWindowY);
	}

	private void checkBoundaries(String windowName, int width, int height, int x, int y)
			throws IllegalArgumentException {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		if (width <= 0) {
			throw new IllegalArgumentException(
					String.format("Invalid %s window preferences: width must be greater than 0", windowName));
		}
		if (height <= 0) {
			throw new IllegalArgumentException(
					String.format("Invalid %s window preferences: height must be greater than 0", windowName));
		}
		if (x < 0) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: x position must be equal or greater than 0", windowName));
		}
		if (x >= dimension.width) {
			throw new IllegalArgumentException(String.format(
					"Invalid %s window preferences: x position must be less than the screen width", windowName));
		}
		if (y < 0) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: y position must be equal or greater than 0", windowName));
		}
		if (y >= dimension.height) {
			throw new IllegalArgumentException(String.format(
					"Invalid %s window preferences: y position must be less than the screen height", windowName));
		}
	}
}
