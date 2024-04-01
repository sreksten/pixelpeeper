package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

import java.awt.*;

abstract class AbstractTagsRenderer implements TagsRenderer {

    protected static final ExifTag[] tagsToCheck = new ExifTag[]{
            ExifTag.CAMERA_MANUFACTURER,
            ExifTag.CAMERA_MODEL,
            ExifTag.CAMERA_FIRMWARE,
            ExifTag.LENS_MANUFACTURER,
            ExifTag.LENS_MODEL,
            ExifTag.LENS_MAXIMUM_APERTURE,
            ExifTag.LENS_FIRMWARE,
            ExifTag.IMAGE_ORIENTATION,
            ExifTag.IMAGE_DIMENSIONS,
            ExifTag.PICTURE_DATE,
            ExifTag.FOCAL_LENGTH,
            ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT,
            ExifTag.APERTURE,
            ExifTag.ISO,
            ExifTag.EXPOSURE_TIME,
            ExifTag.EXPOSURE_PROGRAM,
            ExifTag.EXPOSURE_MODE,
            ExifTag.DISTANCE_FROM_SUBJECT,
            ExifTag.METERING_MODE,
            ExifTag.WHITE_BALANCE,
            ExifTag.WHITE_BALANCE_MODE,
            ExifTag.COLOR_TEMPERATURE,
            ExifTag.FOCUS_MODE,
            ExifTag.FLASH,
            ExifTag.COLOR_SPACE,
            ExifTag.DIGITAL_ZOOM_RATIO,
            ExifTag.GAIN_CONTROL,
            ExifTag.CONTRAST,
            ExifTag.SATURATION,
            ExifTag.SHARPNESS,
            ExifTag.HDR
    };

    protected final ExifTagPreferences tagPreferences;
    protected final TagsClassifier tagsClassifier;
    protected final FontService fontService;
    protected final PictureData pictureData;

    protected AbstractTagsRenderer(ExifTagPreferences tagPreferences, TagsClassifier tagsClassifier,
                                   FontService fontService, PictureData pictureData) {
        this.tagPreferences = tagPreferences;
        this.tagsClassifier = tagsClassifier;
        this.fontService = fontService;
        this.pictureData = pictureData;
    }

    protected boolean isVisible(ExifTag exifTag) {
        boolean tagVisible = tagPreferences.isTagsVisible();
        if (tagVisible && !tagPreferences.isOverridingTagsVisibility()) {
            ExifTagVisibility visibility = tagPreferences.getTagVisibility(exifTag);
            tagVisible = visibility == ExifTagVisibility.YES ||
                    visibility == ExifTagVisibility.ONLY_IF_DIFFERENT &&
                            (tagsClassifier.getTotalMappedPictures() == 1 || !tagsClassifier.isCommonTag(exifTag));
        }
        return tagVisible;
    }

    protected String getCompleteTag(ExifTag exifTag) {
        String tagDescription = exifTag.getDescription();
        String tagValue = pictureData.getTagDescriptive(exifTag);
        return String.format("%s: %s", tagDescription, tagValue);
    }

    protected Font getFilenameFont() {
        return fontService.getFont("Arial", Font.BOLD, FILENAME_FONT_HEIGHT);
    }
    
    protected Color getFilenameColor() {
        return Color.WHITE;
    }

    protected Font getTagFont() {
        return fontService.getFont("Arial", Font.BOLD, TAG_FONT_HEIGHT);
    }

    protected Color getTagColor(ExifTag exifTag) {
        return tagsClassifier.getTotalMappedPictures() == 1 || tagsClassifier.isCommonTag(exifTag)
                ? Color.LIGHT_GRAY
                : Color.YELLOW;
    }
}
