package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.SessionPreferences;

public class SessionPreferencesImpl extends PropertyChangeAwareImpl implements SessionPreferences {

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
		firePropertyChange(CommunicationMessages.LAST_PATH_CHANGED, oldLastPath, lastPath);
	}

	@Override
	public String getLastPath() {
		return lastPath;
	}

	@Override
	public void setLastFilenames(List<String> lastFilenames) {
		List<String> oldLastFilenames = this.lastFilenames;
		this.lastFilenames = lastFilenames;
		firePropertyChange(CommunicationMessages.LAST_FILES_CHANGED, oldLastFilenames, lastFilenames);
	}

	@Override
	public List<String> getLastFilenames() {
		return lastFilenames;
	}

	@Override
	public void setTagToGroupBy(ExifTag exifTag) {
		ExifTag oldTagToGroupBy = this.tagToGroupBy;
		this.tagToGroupBy = exifTag;
		firePropertyChange(CommunicationMessages.TAG_TO_GROUP_BY_CHANGED, oldTagToGroupBy, tagToGroupBy);
	}

	@Override
	public ExifTag getTagToGroupBy() {
		return tagToGroupBy;
	}

	@Override
	public void setGroupIndex(int groupIndex) {
		int oldGroupIndex = this.groupIndex;
		this.groupIndex = groupIndex;
		firePropertyChange(CommunicationMessages.GROUP_INDEX_CHANGED, oldGroupIndex, groupIndex);
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
		firePropertyChange(CommunicationMessages.TAG_TO_ORDER_BY_CHANGED, oldTagToOrderBy, tagToOrderBy);
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
