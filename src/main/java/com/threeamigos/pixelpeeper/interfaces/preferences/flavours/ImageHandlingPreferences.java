package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavour;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavour;

public interface ImageHandlingPreferences extends Preferences {

    boolean AUTOROTATION_DEFAULT = true;
    Disposition DISPOSITION_DEFAULT = Disposition.VERTICAL;
    float MIN_ZOOM_LEVEL = 10.0f;
    float ZOOM_LEVEL_DEFAULT = 100.0f;
    float MAX_ZOOM_LEVEL = 100.0f;
    float ZOOM_LEVEL_STEP = 10.0f;
    boolean NORMALIZED_FOR_CROP_DEFAULT = false;
    boolean NORMALIZED_FOR_FOCAL_LENGTH_DEFAULT = false;
    boolean MOVEMENT_IN_PERCENTAGE_DEFAULT = true;
    boolean MOVEMENT_APPLIED_TO_ALL_IMAGES_DEFAULT = true;
    boolean POSITION_MINIATURE_VISIBLE_DEFAULT = false;
    ImageReaderFlavour IMAGE_READER_FLAVOUR_DEFAULT = ImageReaderFlavour.APACHE_COMMONS_IMAGING;
    ExifReaderFlavour METADATA_READER_FLAVOUR_DEFAULT = ExifReaderFlavour.DREW_NOAKES;

    default String getDescription() {
        return "Image handling preferences";
    }

    void setAutorotation(boolean autorotation);

    boolean isAutorotation();

    void setDisposition(Disposition disposition);

    Disposition getDisposition();

    void setZoomLevel(int zoomLevel);

    int getZoomLevel();

    void setNormalizedForCrop(boolean adaptToCrop);

    boolean isNormalizedForCrop();

    void setNormalizedForFocalLength(boolean adaptToFocalLength);

    boolean isNormalizedForFocalLength();

    void setMovementAppliedToAllImages(boolean movementAppliesToAllImages);

    boolean isMovementAppliedToAllImages();

    void setRelativeMovement(boolean movementInPercentage);

    boolean isRelativeMovement();

    void setPositionMiniatureVisible(boolean positionMiniatureVisible);

    boolean isPositionMiniatureVisible();

    void setImageReaderFlavour(ImageReaderFlavour imageReaderFlavour);

    ImageReaderFlavour getImageReaderFlavour();

    void setExifReaderFlavour(ExifReaderFlavour exifReaderFlavour);

    ExifReaderFlavour getExifReaderFlavour();

    enum Disposition {

        VERTICAL("Vertical"), HORIZONTAL("Horizontal"), GRID("Grid");

        private final String description;

        Disposition(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}
