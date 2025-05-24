package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.ExifReaderFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.ImageReaderFlavor;

/**
 * Preferences for the image handling capabilities. These track:
 * <ul>
 *     <li>Autorotation: should the image be automatically rotated to show it properly or not (a picture may be
 *     taken with the camera tilted to one side)</li>
 *     <li>Zoom level from 10% to 100%</li>
 *     <li>Normalization for crop factor (images will be zoomed relative to the camera crop factor)</li>
 *     <li>Normalization for focal length (images will be zoomed relative to the focal length)</li>
 *     <li>Movement in percentage when images are of different size</li>
 *     <li>Movement applied to all images or only to the active image</li>
 *     <li>Visibility of the position miniature</li>
 *     <li>Disposition of the images if more than one is loaded (vertically, horizontally, grid)</li>
 *     <li>Library used to load the images</li>
 *     <li>Library used to read the EXIF tags</li>
 * </ul>
 *
 * @author Stefano Reksten
 */
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
    ImageReaderFlavor IMAGE_READER_FLAVOR_DEFAULT = ImageReaderFlavor.APACHE_COMMONS_IMAGING;
    ExifReaderFlavor METADATA_READER_FLAVOR_DEFAULT = ExifReaderFlavor.DREW_NOAKES;

    default String getDescription() {
        return "Image Handling preferences";
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

    void setImageReaderFlavor(ImageReaderFlavor imageReaderFlavor);

    ImageReaderFlavor getImageReaderFlavor();

    void setExifReaderFlavor(ExifReaderFlavor exifReaderFlavor);

    ExifReaderFlavor getExifReaderFlavor();

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
