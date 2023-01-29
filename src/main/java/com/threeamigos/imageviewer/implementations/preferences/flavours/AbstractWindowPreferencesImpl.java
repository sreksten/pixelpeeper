package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;

public abstract class AbstractWindowPreferencesImpl implements WindowPreferences {

	protected int width;
	protected int height;
	private int x;
	private int y;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void loadDefaultValues() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenDimension.width * 2 / 3;
		height = screenDimension.height * 2 / 3;
		x = (screenDimension.width - width) / 2;
		y = (screenDimension.height - height) / 2;
	}

	protected void checkBoundaries(String windowName) throws IllegalArgumentException {
		checkMinWidth(windowName, 0);
		checkMinHeight(windowName, 0);
	}

	protected void checkMinWidth(String windowName, int minDimension) {
		if (width <= minDimension) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: width must be greater than %d", windowName, minDimension));
		}
	}

	protected void checkMinHeight(String windowName, int minDimension) {
		if (height <= minDimension) {
			throw new IllegalArgumentException(String
					.format("Invalid %s window preferences: height must be greater than %d", windowName, minDimension));
		}
	}

}
