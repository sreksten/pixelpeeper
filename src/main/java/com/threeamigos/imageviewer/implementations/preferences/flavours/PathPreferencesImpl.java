package com.threeamigos.imageviewer.implementations.preferences.flavours;

import java.io.File;
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

	@Override
	public void validate() {
		if (lastPath == null) {
			throw new IllegalArgumentException("Invalid last path");
		}
		File path = new File(lastPath);
		if (!path.exists()) {
			throw new IllegalArgumentException("Last directory " + lastPath + " does not exist.");
		}
		if (!path.canRead()) {
			throw new IllegalArgumentException("Last directory " + lastPath + " is not readable.");
		}
		for (String filename : lastFilenames) {
			File file = new File(lastPath + File.separator + filename);
			if (!file.exists()) {
				throw new IllegalArgumentException("File " + filename + " does not exist.");
			}
			if (!file.canRead()) {
				throw new IllegalArgumentException("File " + filename + " is not readable.");
			}
		}
	}

}
