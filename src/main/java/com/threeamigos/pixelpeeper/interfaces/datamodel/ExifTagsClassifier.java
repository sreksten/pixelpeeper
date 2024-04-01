package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

import java.util.Collection;
import java.util.Map;

public interface ExifTagsClassifier {

    /**
     * Splits tags in common tags (present in all images with the same value) and
     * uncommon tags (tags not present in every image or with different values).
     * Uncommon tags are associated to a collection of their possible values.
     *
     * @param pictureData a collection of ExifMaps
     */
    void classifyTags(Collection<ExifMap> pictureData);

    int getTotalMappedPictures();

    boolean isCommonTag(ExifTag exifTag);

    Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues();

    Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues(Collection<ExifTag> tagsToFilter);

}
