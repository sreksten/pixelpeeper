package com.threeamigos.imageviewer.interfaces;

public interface StatusTracker<T> {

	public void loadInitialValues();

	public boolean hasChanged();

}
