package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.awt.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.threeamigos.pixelpeeper.implementations.persister.GsonColorAdapter;
import com.threeamigos.pixelpeeper.interfaces.StatusTracker;

public class JsonStatusTracker<T> implements StatusTracker<T> {

	private final T entity;
	private final Gson gson;

	private String initialEntityRepresentationAsString;

	public JsonStatusTracker(T entity) {
		this.entity = entity;

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
		return gson.toJson(entity);
	}

}
