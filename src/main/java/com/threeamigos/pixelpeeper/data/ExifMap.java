package com.threeamigos.pixelpeeper.data;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static com.threeamigos.pixelpeeper.data.ExifValue.PICTURE_ORIENTATION_AS_IS;

/**
 * A collection of {@link ExifTag}s associated to a single image.
 *
 * @author Stefano Reksten
 */
public class ExifMap {

    private final Map<ExifTag, ExifValue> map = new EnumMap<>(ExifTag.class);

    // This tag is used more often than others, so we will just avoid getting it
    // from the map
    private int pictureOrientation = PICTURE_ORIENTATION_AS_IS;

    /**
     * Adds an {@link ExifTag} to this map.
     *
     * @param exifTag     the tag to add
     * @param description its description
     * @param value       its value
     */
    public void add(ExifTag exifTag, String description, Object value) {
        if (description != null && !description.trim().isEmpty()) {
            map.put(exifTag, new ExifValue(exifTag, description, value));
        }
    }

    /**
     * Returns all {@link ExifTag}s found within an image
     */
    public Collection<ExifTag> getKeys() {
        return map.keySet();
    }

    /**
     * Returns the value associated to an {@link ExifTag}
     */
    public ExifValue getExifValue(ExifTag exifTag) {
        return map.get(exifTag);
    }

    /**
     * Returns the value associated to an {@link ExifTag}, if
     * this tag is present in the image, or "N/A" otherwise.
     */
    public String getTagDescriptive(ExifTag exifTag) {
        ExifValue exifValue = map.get(exifTag);
        if (exifValue == null) {
            return "N/A";
        } else {
            return exifValue.getDescription();
        }
    }

    /**
     * Returns the value associated to an {@link ExifTag}, if
     * this tag is present, or null otherwise. The value will
     * be cast to a float, so be sure this is actually
     * applicable. E.g. it is useless to return the
     * {@link ExifTag#CAMERA_MANUFACTURER} as a float, as it
     * is a String.
     */
    public Float getAsFloat(ExifTag exifTag) {
        ExifValue exifValue = map.get(exifTag);
        if (exifValue == null) {
            return null;
        }
        return exifValue.asFloat();
    }

    /**
     * Sets the picture orientation. See {@link ExifValue} for possible values.
     */
    public void setPictureOrientation(int pictureOrientation) {
        this.pictureOrientation = pictureOrientation;
    }

    /**
     * Returns the picture orientation.
     */
    public int getPictureOrientation() {
        return pictureOrientation;
    }

    /**
     * Given a Map of desired {@link ExifTag}s and their desired values, used as a filter,
     * checks if this collection of EXIF tags matches. To match, this map should contain
     * each key of the filter and its corresponding value must match one of the desired values.
     *
     * @return true if this set of values matches.
     */
    public boolean matches(Map<ExifTag, Collection<ExifValue>> selectionMap) {
        for (Map.Entry<ExifTag, Collection<ExifValue>> selectionEntry : selectionMap.entrySet()) {
            ExifTag selectedTag = selectionEntry.getKey();
            Collection<ExifValue> selectedValues = selectionEntry.getValue();
            ExifValue value = getExifValue(selectedTag);
            if (!selectedValues.contains(value)) {
                return false;
            }
        }
        return true;
    }

}
