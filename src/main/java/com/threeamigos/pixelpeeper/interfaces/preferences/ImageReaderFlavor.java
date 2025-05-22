package com.threeamigos.pixelpeeper.interfaces.preferences;

/**
 * An enumeration of the various image-reader libraries available to the application
 *
 * @author Stefano Reksten
 */
public enum ImageReaderFlavor {

    // Standard java libraries
    JAVA("Java ImageIO"),
    // Extern java libraries
    APACHE_COMMONS_IMAGING("Apache Commons Imaging");

    private final String description;

    ImageReaderFlavor(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
