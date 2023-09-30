package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.common.util.implementations.PropertyChangeAwareImpl;
import com.threeamigos.common.util.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;

public abstract class AbstractWindowPreferencesImpl extends PropertyChangeAwareImpl implements WindowPreferences {

	private int x;
	private int y;
	protected int width;
	protected int height;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		int oldX = this.x;
		this.x = x;
		firePropertyChange(CommunicationMessages.WINDOW_X_POSITION_CHANGED, oldX, x);
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		int oldY = this.y;
		this.y = y;
		firePropertyChange(CommunicationMessages.WINDOW_Y_POSITION_CHANGED, oldY, y);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		int oldWidth = this.width;
		this.width = width;
		firePropertyChange(CommunicationMessages.WINDOW_WIDTH_CHANGED, oldWidth, width);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		int oldHeight = this.height;
		this.height = height;
		firePropertyChange(CommunicationMessages.WINDOW_HEIGHT_CHANGED, oldHeight, height);
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
