package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;

public class MainWindowPreferencesImpl implements MainWindowPreferences {

	private int mainWindowWidth;
	private int mainWindowHeight;
	private int mainWindowX;
	private int mainWindowY;

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
	public void loadDefaultValues() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainWindowWidth = screenDimension.width * 2 / 3;
		mainWindowHeight = screenDimension.height * 2 / 3;
		mainWindowX = (screenDimension.width - mainWindowWidth) / 2;
		mainWindowY = (screenDimension.height - mainWindowHeight) / 2;
	}

	@Override
	public void validate() {
		checkBoundaries("main", mainWindowWidth, mainWindowHeight, mainWindowX, mainWindowY);
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
