package com.threeamigos.imageviewer.interfaces.ui;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;

public interface ExifTagsFilter {

	public Map<ExifTag, Collection<String>> filterTags(Map<ExifTag, Collection<String>> map);

}
