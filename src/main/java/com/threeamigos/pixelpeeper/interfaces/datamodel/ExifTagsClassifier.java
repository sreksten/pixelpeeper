package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;

import java.util.Collection;
import java.util.Map;

/**
 * An interface that splits a set of {@link ExifTag}s in
 * <ul>
 *     <li>common tags (present in all images with exactly same value)</li>
 *     <li>uncommon tags (tags not present in every image or with different values)</li>
 * </ul>
 * Each uncommon tag is associated to a collection of its values (these values are actually
 * present in one or more files).
 *
 * @author Stefano Reksten
 */
public interface ExifTagsClassifier {

    /**
     * Classifies the tags in common and uncommon
     *
     * @param pictureData a collection of ExifMaps
     */
    void classifyTags(Collection<ExifMap> pictureData);

    /**
     * Number of pictures in which some EXIF tags were found
     */
    int getTotalMappedPictures();

    /**
     * @param exifTag the tag to examine
     * @return true if this tag is present in all pictures with the same value
     */
    boolean isCommonTag(ExifTag exifTag);

    /**
     * @return a map of tags whose value differ from image to image, with the
     * collection of its possible values
     */
    Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues();

    /**
     * @param tagsToFilter the tags of interest
     * @return a map of filtered tags whose value differ from image to image, with the
     * collection of its possible values
     */
    Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues(Collection<ExifTag> tagsToFilter);

}
