package com.threeamigos.pixelpeeper.data;

import com.threeamigos.pixelpeeper.implementations.helpers.ExifOrientationHelper;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class ExifMap {

    private final Map<ExifTag, ExifValue> map = new EnumMap<>(ExifTag.class);

    // This tag is used more often than others, so we will just avoid getting it
    // from the map
    private int pictureOrientation = ExifOrientationHelper.AS_IS;

    public void setIfAbsent(ExifTag exifTag, String value, Object object) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        map.put(exifTag, new ExifValue(exifTag, value, object));
    }

    public Collection<ExifTag> getKeys() {
        return map.keySet();
    }

    public ExifValue getExifValue(ExifTag exifTag) {
        return map.get(exifTag);
    }

    public String getTagDescriptive(ExifTag exifTag) {
        ExifValue exifValue = map.get(exifTag);
        if (exifValue == null) {
            return "N/A";
        } else {
            return exifValue.getDescription();
        }
    }

    public Float getAsFloat(ExifTag exifTag) {
        ExifValue exifValue = map.get(exifTag);
        if (exifValue == null) {
            return null;
        }
        return exifValue.asFloat();
    }

    public void setPictureOrientation(int pictureOrientation) {
        this.pictureOrientation = pictureOrientation;
    }

    public int getPictureOrientation() {
        return pictureOrientation;
    }

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
