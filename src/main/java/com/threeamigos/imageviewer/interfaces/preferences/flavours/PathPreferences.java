package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import java.util.List;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

/**
 * Keeps track of the last directory browsed and the files that were loaded last
 *
 * @author Stefano Reksten
 *
 */
public interface PathPreferences extends Preferences {

	default String getDescription() {
		return "Path and files preferences";
	}

	public void setLastPath(String path);

	public String getLastPath();

	public void setLastFilenames(List<String> lastFilenames);

	public List<String> getLastFilenames();

	public void setTagToGroupBy(ExifTag exifTag);

	public ExifTag getTagToGroupBy();

	public void setGroupIndex(int lastGroup);

	public int getLastGroup();

}
