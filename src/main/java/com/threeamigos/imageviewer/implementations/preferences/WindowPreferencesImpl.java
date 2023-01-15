package com.threeamigos.imageviewer.implementations.preferences;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class WindowPreferencesImpl extends AbstractPreferencesImpl<WindowPreferences> implements WindowPreferences {

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
	protected String getEntityDescription() {
		return "window";
	}

	public WindowPreferencesImpl(Persister<WindowPreferences> persister, ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		loadPostConstruct();
	}

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
	protected void loadDefaultValues() {
		loadMainWindowDefaultValues();
		loadDragAndDropWindowDefaultValues();
		loadOtherValues();
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
	public void loadMainWindowDefaultValues() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		mainWindowWidth = screenDimension.width * 2 / 3;
		mainWindowHeight = screenDimension.height * 2 / 3;
		mainWindowX = (screenDimension.width - mainWindowWidth) / 2;
		mainWindowY = (screenDimension.height - mainWindowHeight) / 2;
	}

	@Override
	public void loadDragAndDropWindowDefaultValues() {
		dragAndDropWindowVisible = false;
		dragAndDropWindowWidth = 300;
		dragAndDropWindowHeight = 300;
		dragAndDropWindowX = 0;
		dragAndDropWindowY = 0;
	}

	@Override
	public void loadOtherValues() {
		autorotation = AUTOROTATION_DEFAULT;
		movementAppliesToAllImages = MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT;
	}

}
