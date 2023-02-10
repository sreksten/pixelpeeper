package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.awt.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.threeamigos.imageviewer.implementations.persister.GsonColorAdapter;
import com.threeamigos.imageviewer.interfaces.StatusTracker;

public class JsonStatusTracker<T> implements StatusTracker<T> {

	private final T preferences;
	private final Gson gson;

	private String initialEntityRepresentationAsString;

	public JsonStatusTracker(T preferences) {
		this.preferences = preferences;

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Color.class, new GsonColorAdapter());
		gson = builder.create();
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
		return gson.toJson(preferences);
	}

}
