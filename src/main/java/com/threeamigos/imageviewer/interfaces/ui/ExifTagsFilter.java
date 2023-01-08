package com.threeamigos.imageviewer.interfaces.ui;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;

public interface ExifTagsFilter {

	/**
	 * @param map
	 * @return null if operation was canceled, a map of filtered tags with their possible values
	 */
	public Map<ExifTag, Collection<String>> filterTags(Map<ExifTag, Collection<String>> map);

}
