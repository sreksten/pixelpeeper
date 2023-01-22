package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.google.gson.Gson;
import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public class JsonStatusTracker<T extends Preferences> implements StatusTracker<T> {

	private T preferences;
	private String initialEntityRepresentationAsString;

	public JsonStatusTracker(T preferences) {
		this.preferences = preferences;
	}

	@Override
	public void loadInitialValues() {
		initialEntityRepresentationAsString = getEntityRepresentationAsString();
	}

	@Override
	public boolean hasChanged() {
		return !getEntityRepresentationAsString().equals(initialEntityRepresentationAsString);
	}

	private String getEntityRepresentationAsString() {
		return new Gson().toJson(preferences);
	}

}
