package com.threeamigos.pixelpeeper.interfaces;

public interface StatusTracker<T> {

	public void loadInitialValues();

	public boolean hasChanged();

}
