package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Tuneable parameters for the Distortion Measurement filter.
 *
 * @author Stefano Reksten
 */
public interface DistortionMeasurementFilterPreferences extends Preferences {

    /**
     * Minimum normalised Sobel gradient magnitude (0–255) for a pixel to be classified as a
     * high-contrast edge.  Only edge pixels contribute to the Hough line accumulator.
     */
    int EDGE_THRESHOLD_DEFAULT = 40;
    int EDGE_THRESHOLD_MIN = 10;
    int EDGE_THRESHOLD_MAX = 120;

    /**
     * Number of grid lines drawn in each axis of the deformed-grid overlay.
     * Must be odd so that a centre line is always present.  Range 3–11, step 2.
     */
    int GRID_SIZE_DEFAULT = 7;
    int GRID_SIZE_MIN = 3;
    int GRID_SIZE_MAX = 11;

    default String getDescription() {
        return "Distortion measurement filter preferences";
    }

    int getEdgeThreshold();
    void setEdgeThreshold(int edgeThreshold);

    int getGridSize();
    void setGridSize(int gridSize);
}
