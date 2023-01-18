package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.util.Collections;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.PathPreferences;

public class PathPreferencesImpl implements PathPreferences {

	private String lastPath;
	private List<String> lastFilenames;

	@Override
	public void setLastPath(String path) {
		this.lastPath = path;
	}

	@Override
	public String getLastPath() {
		return lastPath;
	}

	@Override
	public void setLastFilenames(List<String> lastFilenames) {
		this.lastFilenames = lastFilenames;
	}

	@Override
	public List<String> getLastFilenames() {
		return lastFilenames;
	}

	@Override
	public void loadDefaultValues() {
		lastPath = System.getProperty("user.home");
		lastFilenames = Collections.emptyList();
	}

}
