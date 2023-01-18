package com.threeamigos.imageviewer.interfaces.persister;

public interface StatusTracker<T> {

	public void loadInitialValues();

	public boolean hasChanged();

}
