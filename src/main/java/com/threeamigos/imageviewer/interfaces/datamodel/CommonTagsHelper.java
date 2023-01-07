package com.threeamigos.imageviewer.interfaces.datamodel;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;

public interface CommonTagsHelper {

	public void updateCommonTags(Collection<PictureData> pictureData);

	public boolean isCommonTag(ExifTag exifTag);

	public Collection<ExifTag> getCommonTags();

	public Map<ExifTag, Collection<String>> getUncommonTagsToValues();

}
