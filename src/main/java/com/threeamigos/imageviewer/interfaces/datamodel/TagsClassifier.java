package com.threeamigos.imageviewer.interfaces.datamodel;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;

public interface TagsClassifier {

	/**
	 * Splits tags in common tags (present in all images with the same value) and
	 * uncommon tags (tags not present in every image or with different values).
	 * Uncommon tags are associated to a collection of their possible values.
	 * 
	 * @param pictureData a collection of ExifMaps
	 */
	public void classifyTags(Collection<ExifMap> pictureData);

	public int getTotalMappedPictures();

	public boolean isCommonTag(ExifTag exifTag);

	public Collection<ExifTag> getCommonTags();

	public Map<ExifTag, Collection<String>> getUncommonTagsToValues();

}
