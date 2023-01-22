package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.DragAndDropWindowPreferences;

public class DragAndDropWindowPreferencesImpl implements DragAndDropWindowPreferences {

	private boolean dragAndDropWindowVisible;
	private int dragAndDropWindowWidth;
	private int dragAndDropWindowHeight;
	private int dragAndDropWindowX;
	private int dragAndDropWindowY;

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
	public void loadDefaultValues() {
		dragAndDropWindowVisible = false;
		dragAndDropWindowWidth = 300;
		dragAndDropWindowHeight = 300;
		dragAndDropWindowX = 0;
		dragAndDropWindowY = 0;
	}

	@Override
	public void validate() {

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
		if (x + dimension.width < 0) {
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
