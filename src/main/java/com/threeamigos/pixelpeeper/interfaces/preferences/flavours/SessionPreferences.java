package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import java.util.List;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.data.ExifTag;

/**
 * Keeps track of the last directory browsed and the files that were loaded last
 *
 * @author Stefano Reksten
 *
 */
public interface SessionPreferences extends Preferences {

	default String getDescription() {
		return "Session preferences";
	}

	public void setLastPath(String path);

	public String getLastPath();

	public void setLastFilenames(List<String> lastFilenames);

	public List<String> getLastFilenames();

	public void setTagToGroupBy(ExifTag exifTag);

	public ExifTag getTagToGroupBy();

	public void setTolerance(int tolerance);

	public int getTolerance();

	public void setTagToOrderBy(ExifTag exifTag);

	public ExifTag getTagToOrderBy();

	public void setGroupIndex(int lastGroup);

	public int getGroupIndex();

}
