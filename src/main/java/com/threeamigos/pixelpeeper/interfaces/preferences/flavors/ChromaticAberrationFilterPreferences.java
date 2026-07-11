package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Tuneable parameters for the Chromatic Aberration Detection filter.
 *
 * @author Stefano Reksten
 */
public interface ChromaticAberrationFilterPreferences extends Preferences {

    /**
     * Minimum Sobel gradient magnitude (0–255 normalised) for a pixel to be classified as a
     * high-contrast edge.  Only edge pixels are analysed for colour fringing.
     */
    int EDGE_THRESHOLD_DEFAULT = 60;
    int EDGE_THRESHOLD_MIN = 10;
    int EDGE_THRESHOLD_MAX = 150;

    /**
     * Minimum absolute channel difference (|R−G| or |B−G|) at an edge pixel for that pixel
     * to be counted as visibly fringed.  Pixels below this threshold are still painted in the
     * overlay, but at reduced opacity.
     */
    int SENSITIVITY_DEFAULT = 5;
    int SENSITIVITY_MIN = 1;
    int SENSITIVITY_MAX = 40;

    default String getDescription() {
        return "Chromatic aberration filter preferences";
    }

    int getEdgeThreshold();
    void setEdgeThreshold(int edgeThreshold);

    int getSensitivity();
    void setSensitivity(int sensitivity);
}
