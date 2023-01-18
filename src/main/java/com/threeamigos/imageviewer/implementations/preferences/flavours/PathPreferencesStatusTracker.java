package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;

public class PathPreferencesStatusTracker implements StatusTracker<PathPreferences> {

	private String lastPathAtStart;
	private List<String> lastFilenamesAtStart;

	private final PathPreferences pathPreferences;

	public PathPreferencesStatusTracker(PathPreferences pathPreferences) {
		this.pathPreferences = pathPreferences;
	}

	@Override
	public void loadInitialValues() {
		lastPathAtStart = pathPreferences.getLastPath();
		lastFilenamesAtStart = new ArrayList<>();
		lastFilenamesAtStart.addAll(pathPreferences.getLastFilenames());
	}

	@Override
	public boolean hasChanged() {
		return !pathPreferences.getLastPath().equals(lastPathAtStart)
				|| !pathPreferences.getLastFilenames().equals(lastFilenamesAtStart);
	}

}
