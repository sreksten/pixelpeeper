package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;

public class BigPointerPreferencesImpl implements BigPointerPreferences {

	private boolean bigPointerVisible;
	private int bigPointerSize;
	private Rotation bigPointerRotation;

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
	public void setBigPointerRotation(Rotation rotation) {
		this.bigPointerRotation = rotation;
	}

	@Override
	public Rotation getBigPointerRotation() {
		return bigPointerRotation;
	}

	@Override
	public void loadDefaultValues() {
		bigPointerVisible = BigPointerPreferences.BIG_POINTER_VISIBLE_DEFAULT;
		bigPointerSize = BigPointerPreferences.BIG_POINTER_SIZE_DEFAULT;
		bigPointerRotation = BigPointerPreferences.POINTER_ROTATION_DEFAULT;
	}

	@Override
	public void validate() {
	}

}
