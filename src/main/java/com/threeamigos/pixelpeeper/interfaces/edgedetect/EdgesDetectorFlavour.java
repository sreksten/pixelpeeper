package com.threeamigos.pixelpeeper.interfaces.edgedetect;

/**
 * An enumeration of the various edge-detection algorithms available to the application
 *
 * @author Stefano Reksten
 */
public enum EdgesDetectorFlavour {

    /**
     * Implements the Canny edge-detection algorithm
     */
    CANNY_EDGES_DETECTOR("Canny Edges Detector"),
    /**
     * A fake edge detector just to check the hosting capabilities of the preferences window
     */
    ROMY_JONA_EDGES_DETECTOR("Romy Jona Edges Detector"),
    /**
     * Implements the Sobel edge-detection algorithm
     */
    SOBEL_EDGES_DETECTOR("Sobel Edges Detector");

    private final String description;

    EdgesDetectorFlavour(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
