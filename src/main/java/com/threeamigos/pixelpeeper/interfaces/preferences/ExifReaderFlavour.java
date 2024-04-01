package com.threeamigos.pixelpeeper.interfaces.preferences;

public enum ExifReaderFlavour {

    DREW_NOAKES("Drew Noakes' library");

    private final String description;

    ExifReaderFlavour(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
