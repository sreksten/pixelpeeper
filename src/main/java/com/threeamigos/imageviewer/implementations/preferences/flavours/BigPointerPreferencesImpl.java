package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;

public class BigPointerPreferencesImpl implements BigPointerPreferences {

	private boolean bigPointerVisible;
	private int bigPointerSize;
	private float rotation;

	@Override
	public void setBigPointerVisible(boolean bigPointerVisible) {
		this.bigPointerVisible = bigPointerVisible;
	}

	@Override
	public boolean isBigPointerVisible() {
		return bigPointerVisible;
	}

	@Override
	public void setBigPointerSize(int bigPointerSize) {
		this.bigPointerSize = bigPointerSize;
	}

	@Override
	public int getBigPointerSize() {
		return bigPointerSize;
	}

	@Override
	public void setBigPointerRotation(float radians) {
		this.rotation = radians;
	}

	@Override
	public float getBigPointerRotation() {
		return rotation;
	}

	@Override
	public void loadDefaultValues() {
		bigPointerVisible = BigPointerPreferences.BIG_POINTER_VISIBLE_DEFAULT;
		bigPointerSize = BigPointerPreferences.BIG_POINTER_SIZE_DEFAULT;
		rotation = BigPointerPreferences.BIG_POINTER_ROTATION_DEFAULT;
	}

}
