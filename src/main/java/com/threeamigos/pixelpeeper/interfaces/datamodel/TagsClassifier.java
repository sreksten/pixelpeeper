package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.util.Collection;
import java.util.Map;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

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

	public Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues();

	public Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues(Collection<ExifTag> tagsToFilter);

}
