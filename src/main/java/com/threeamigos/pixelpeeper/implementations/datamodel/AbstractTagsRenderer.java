package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

abstract class AbstractTagsRenderer {

    protected static final ExifTag[] tagsToCheck = new ExifTag[]{
            ExifTag.HDR,
            ExifTag.SHARPNESS,
            ExifTag.SATURATION,
            ExifTag.CONTRAST,
            ExifTag.GAIN_CONTROL,
            ExifTag.DIGITAL_ZOOM_RATIO,
            ExifTag.COLOR_SPACE,
            ExifTag.FLASH,
            ExifTag.FOCUS_MODE,
            ExifTag.COLOR_TEMPERATURE,
            ExifTag.WHITE_BALANCE_MODE,
            ExifTag.WHITE_BALANCE,
            ExifTag.METERING_MODE,
            ExifTag.DISTANCE_FROM_SUBJECT,
            ExifTag.EXPOSURE_MODE,
            ExifTag.EXPOSURE_PROGRAM,
            ExifTag.EXPOSURE_TIME,
            ExifTag.ISO,
            ExifTag.APERTURE,
            ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT,
            ExifTag.FOCAL_LENGTH,
            ExifTag.PICTURE_DATE,
            ExifTag.IMAGE_DIMENSIONS,
            ExifTag.IMAGE_ORIENTATION,
            ExifTag.LENS_FIRMWARE,
            ExifTag.LENS_MAXIMUM_APERTURE,
            ExifTag.LENS_MODEL,
            ExifTag.LENS_MANUFACTURER,
            ExifTag.CAMERA_FIRMWARE,
            ExifTag.CAMERA_MODEL,
            ExifTag.CAMERA_MANUFACTURER
    };

    protected final ExifTagPreferences tagPreferences;
    protected final TagsClassifier commonTagsHelper;

    protected AbstractTagsRenderer(ExifTagPreferences tagPreferences, TagsClassifier commonTagsHelper) {
        this.tagPreferences = tagPreferences;
        this.commonTagsHelper = commonTagsHelper;
    }

    protected boolean isVisible(ExifTag exifTag) {
        boolean tagVisible = tagPreferences.isTagsVisible();
        if (tagVisible && !tagPreferences.isOverridingTagsVisibility()) {
            ExifTagVisibility visibility = tagPreferences.getTagVisibility(exifTag);
            tagVisible = visibility == ExifTagVisibility.YES ||
                    visibility == ExifTagVisibility.ONLY_IF_DIFFERENT &&
                            (commonTagsHelper.getTotalMappedPictures() == 1 || !commonTagsHelper.isCommonTag(exifTag));
        }
        return tagVisible;
    }
}
