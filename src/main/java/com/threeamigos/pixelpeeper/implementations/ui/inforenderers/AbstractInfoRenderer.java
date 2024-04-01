package com.threeamigos.pixelpeeper.implementations.ui.inforenderers;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.InfoRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagsPreferences;

import java.awt.*;

abstract class AbstractInfoRenderer implements InfoRenderer {

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

    protected final ExifTagsPreferences exifTagsPreferences;
    protected final ExifTagsClassifier exifTagsClassifier;
    protected final FontService fontService;
    protected final PictureData pictureData;

    protected AbstractInfoRenderer(ExifTagsPreferences tagPreferences, ExifTagsClassifier tagsClassifier,
                                   FontService fontService, PictureData pictureData) {
        this.exifTagsPreferences = tagPreferences;
        this.exifTagsClassifier = tagsClassifier;
        this.fontService = fontService;
        this.pictureData = pictureData;
    }

    protected boolean isVisible(ExifTag exifTag) {
        boolean exifTagVisible = exifTagsPreferences.isTagsVisible();
        if (exifTagVisible && !exifTagsPreferences.isOverridingTagsVisibility()) {
            ExifTagVisibility visibility = exifTagsPreferences.getTagVisibility(exifTag);
            exifTagVisible = visibility == ExifTagVisibility.YES ||
                    visibility == ExifTagVisibility.ONLY_IF_DIFFERENT &&
                            (exifTagsClassifier.getTotalMappedPictures() == 1 || !exifTagsClassifier.isCommonTag(exifTag));
        }
        return exifTagVisible;
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

    protected Font getExifTagFont() {
        return fontService.getFont("Arial", Font.BOLD, TAG_FONT_HEIGHT);
    }

    protected Color getExifTagColor(ExifTag exifTag) {
        return exifTagsClassifier.getTotalMappedPictures() == 1 || exifTagsClassifier.isCommonTag(exifTag)
                ? Color.LIGHT_GRAY
                : Color.YELLOW;
    }
}
