package com.threeamigos.pixelpeeper.data;

/**
 * An EXIF tag is a tag associated to an image that brings along information like camera manufacturer,
 * lens model, ISO, aperture etc. An image file contains a set of EXIF tags within. This class lists some of
 * the tags we are interested in when comparing two images.
 *
 * @author Stefano Reksten
 */
public enum ExifTag {

    CAMERA_MANUFACTURER("Camera maker"),
    CAMERA_MODEL("Camera model"),
    CAMERA_FIRMWARE("Camera firmware"),

    LENS_MANUFACTURER("Lens maker"),
    LENS_MODEL("Lens model"),
    LENS_MAXIMUM_APERTURE("Lens maximum aperture"),
    LENS_FIRMWARE("Lens firmware"),

    IMAGE_ORIENTATION("Image orientation"),
    IMAGE_DIMENSIONS("Image dimensions"),
    PICTURE_DATE("Picture date"),
    FOCAL_LENGTH("Focal length"),
    FOCAL_LENGTH_35MM_EQUIVALENT("Focal length (35mm equiv)"),
    APERTURE("Aperture"),
    ISO("ISO speed"),
    EXPOSURE_TIME("Exposure time"),
    EXPOSURE_PROGRAM("Exposure program"),
    EXPOSURE_MODE("Exposure mode"),
    DISTANCE_FROM_SUBJECT("Distance from subject"),
    METERING_MODE("Metering mode"),
    WHITE_BALANCE("White balance"),
    WHITE_BALANCE_MODE("White balance mode"),
    COLOR_TEMPERATURE("Color temperature (°K)"),
    FOCUS_MODE("Focus mode"),
    FLASH("Flash"),
    COLOR_SPACE("Color space"),
    DIGITAL_ZOOM_RATIO("Digital zoom ratio"),
    GAIN_CONTROL("Gain control"),
    CONTRAST("Contrast"),
    SATURATION("Saturation"),
    SHARPNESS("Sharpness"),
    HDR("HDR");

    private final String description;

    private ExifTag(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

}
