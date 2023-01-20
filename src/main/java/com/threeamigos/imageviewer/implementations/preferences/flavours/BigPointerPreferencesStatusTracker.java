package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;

public class BigPointerPreferencesStatusTracker implements StatusTracker<BigPointerPreferences> {

	private boolean bigPointerVisibleAtStart;
	private int bigPointerSizeAtStart;
	private float bigPointerRotationAtStart;

	private final BigPointerPreferences pointerPreferences;

	public BigPointerPreferencesStatusTracker(BigPointerPreferences pointerPreferences) {
		this.pointerPreferences = pointerPreferences;
	}

	@Override
	public void loadInitialValues() {
		bigPointerVisibleAtStart = pointerPreferences.isBigPointerVisible();
		bigPointerSizeAtStart = pointerPreferences.getBigPointerSize();
		bigPointerRotationAtStart = pointerPreferences.getBigPointerRotation();
	}

	@Override
	public boolean hasChanged() {
		return pointerPreferences.isBigPointerVisible() != bigPointerVisibleAtStart
				|| pointerPreferences.getBigPointerSize() != bigPointerSizeAtStart
				|| pointerPreferences.getBigPointerRotation() != bigPointerRotationAtStart;
	}

}
