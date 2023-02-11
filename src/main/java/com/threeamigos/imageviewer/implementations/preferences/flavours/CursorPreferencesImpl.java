package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.CursorPreferences;

public class CursorPreferencesImpl extends PropertyChangeAwareImpl implements CursorPreferences {

	private boolean bigPointerVisible;
	private int bigPointerSize;
	private Rotation bigPointerRotation;

	@Override
	public void setBigPointerVisible(boolean bigPointerVisible) {
		boolean oldBigPointerVisible = this.bigPointerVisible;
		this.bigPointerVisible = bigPointerVisible;
		firePropertyChange(CommunicationMessages.BIG_POINTER_VISIBILITY_CHANGED, oldBigPointerVisible,
				bigPointerVisible);
	}

	@Override
	public boolean isBigPointerVisible() {
		return bigPointerVisible;
	}

	@Override
	public void setBigPointerSize(int bigPointerSize) {
		int oldBigPointerSize = this.bigPointerSize;
		this.bigPointerSize = bigPointerSize;
		firePropertyChange(CommunicationMessages.BIG_POINTER_SIZE_CHANGED, oldBigPointerSize, bigPointerSize);
	}

	@Override
	public int getBigPointerSize() {
		return bigPointerSize;
	}

	@Override
	public void setBigPointerRotation(Rotation bigPointerRotation) {
		Rotation oldBigPointerRotation = this.bigPointerRotation;
		this.bigPointerRotation = bigPointerRotation;
		firePropertyChange(CommunicationMessages.BIG_POINTER_ROTATION_CHANGED, oldBigPointerRotation,
				bigPointerRotation);
	}

	@Override
	public Rotation getBigPointerRotation() {
		return bigPointerRotation;
	}

	@Override
	public void loadDefaultValues() {
		bigPointerVisible = CursorPreferences.BIG_POINTER_VISIBLE_DEFAULT;
		bigPointerSize = CursorPreferences.BIG_POINTER_SIZE_DEFAULT;
		bigPointerRotation = CursorPreferences.POINTER_ROTATION_DEFAULT;
	}

	@Override
	public void validate() {
	}

}
