package com.threeamigos.pixelpeeper.data;

/**
 * When comparing two images we can decide which {@link ExifTag}s we want to show.
 * As images have plenty of tags, for each tag we can choose to always show it,
 * always hide it or show it only if the images have different values for this tag.
 *
 * @author Stefano Reksten
 */
public enum ExifTagVisibility {

    /**
     * Always show this tag
     */
    YES("Yes"),
    /**
     * Only if two or more images have this tag but their values differ.
     */
    ONLY_IF_DIFFERENT("Only if different"),
    /**
     * Don't show this tag.
     */
    NO("No");

    private final String description;

    private ExifTagVisibility(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
