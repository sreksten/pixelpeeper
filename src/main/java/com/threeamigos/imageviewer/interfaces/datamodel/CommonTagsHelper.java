package com.threeamigos.imageviewer.interfaces.datamodel;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;

public interface CommonTagsHelper {

	public void updateCommonTags(Collection<ExifMap> pictureData);

	public int getMappedPictures();

	public boolean isCommonTag(ExifTag exifTag);

	public Collection<ExifTag> getCommonTags();

	public Map<ExifTag, Collection<String>> getUncommonTagsToValues();

}
