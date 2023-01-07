package com.threeamigos.imageviewer.implementations.preferences;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.WindowPreferences;

public class WindowPreferencesImpl extends AbstractPreferencesImpl<WindowPreferences> implements WindowPreferences {

	private int width;
	private int height;
	private int x;
	private int y;
	private boolean autorotation;
	private boolean tagsVisible;
	private boolean tagsVisibleOnlyIfDifferent;
	private boolean movementAppliesToAllImages;

	@Override
	protected String getEntityDescription() {
		return "window";
	}

	public WindowPreferencesImpl(Persister<WindowPreferences> persister) {
		super(persister);

		loadPostConstruct();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
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
	public void setTagsVisible(boolean tagsVisible) {
		this.tagsVisible = tagsVisible;
	}

	@Override
	public boolean isTagsVisible() {
		return tagsVisible;
	}

	@Override
	public void setTagsVisibleOnlyIfDifferent(boolean tagsVisibleOnlyIfDifferent) {
		this.tagsVisibleOnlyIfDifferent = tagsVisibleOnlyIfDifferent;
	}

	@Override
	public boolean isTagsVisibleOnlyIfDifferent() {
		return tagsVisibleOnlyIfDifferent;
	}

	@Override
	protected void loadDefaultValues() {
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		width = screenDimension.width * 2 / 3;
		height = screenDimension.height * 2 / 3;
		x = (screenDimension.width - width) / 2;
		y = (screenDimension.height - height) / 2;
		autorotation = AUTOROTATION_DEFAULT;
		tagsVisible = TAGS_VISIBLE_DEFAULT;
		tagsVisibleOnlyIfDifferent = TAGS_VISIBLE_ONLY_IF_DIFFERENT_DEFAULT;
		movementAppliesToAllImages = MOVEMENT_APPLIES_TO_ALL_IMAGES_DEFAULT;
	}

	@Override
	public void setMovementAppliedToAllImages(boolean movementAppliesToAllImages) {
		this.movementAppliesToAllImages = movementAppliesToAllImages;
	}

	@Override
	public boolean isMovementAppliedToAllImages() {
		return movementAppliesToAllImages;
	}

}
