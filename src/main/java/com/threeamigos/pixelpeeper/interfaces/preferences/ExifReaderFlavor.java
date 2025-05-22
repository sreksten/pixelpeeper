package com.threeamigos.pixelpeeper.interfaces.preferences;

/**
 * An enumeration of the various EXIF tag readers available to the application
 *
 * @author Stefano Reksten
 */
public enum ExifReaderFlavor {

    DREW_NOAKES("Drew Noakes' library");

    private final String description;

    ExifReaderFlavor(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
