package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SessionPreferences;

public class SessionPreferencesImpl extends BasicPropertyChangeAware implements SessionPreferences {

	private String lastPath;
	private List<String> lastFilenames;
	private ExifTag tagToGroupBy;
	private int groupIndex;
	private int tolerance;
	private ExifTag tagToOrderBy;

	@Override
	public void setLastPath(String path) {
		String oldLastPath = this.lastPath;
		this.lastPath = path;
	}

	@Override
	public String getLastPath() {
		return lastPath;
	}

	@Override
	public void setLastFilenames(List<String> lastFilenames) {
		List<String> oldLastFilenames = this.lastFilenames;
		this.lastFilenames = lastFilenames;
	}

	@Override
	public List<String> getLastFilenames() {
		return lastFilenames;
	}

	@Override
	public void setTagToGroupBy(ExifTag exifTag) {
		ExifTag oldTagToGroupBy = this.tagToGroupBy;
		this.tagToGroupBy = exifTag;
	}

	@Override
	public ExifTag getTagToGroupBy() {
		return tagToGroupBy;
	}

	@Override
	public void setGroupIndex(int groupIndex) {
		int oldGroupIndex = this.groupIndex;
		this.groupIndex = groupIndex;
	}

	@Override
	public int getGroupIndex() {
		return groupIndex;
	}

	@Override
	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public int getTolerance() {
		return tolerance;
	}

	@Override
	public void setTagToOrderBy(ExifTag exifTag) {
		ExifTag oldTagToOrderBy = this.tagToOrderBy;
		this.tagToOrderBy = exifTag;
	}

	@Override
	public ExifTag getTagToOrderBy() {
		return tagToOrderBy;
	}

	@Override
	public void loadDefaultValues() {
		lastPath = System.getProperty("user.home");
		lastFilenames = Collections.emptyList();
		tagToGroupBy = null;
		tolerance = 0;
		tagToOrderBy = null;
		groupIndex = 0;
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
			File file = new File(filename);
			if (!file.exists()) {
				throw new IllegalArgumentException("File " + filename + " does not exist.");
			}
			if (!file.canRead()) {
				throw new IllegalArgumentException("File " + filename + " is not readable.");
			}
		}
	}

}
