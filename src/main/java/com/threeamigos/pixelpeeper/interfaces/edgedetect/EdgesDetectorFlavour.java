package com.threeamigos.pixelpeeper.interfaces.edgedetect;

import com.threeamigos.pixelpeeper.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    SOBEL_EDGES_DETECTOR("Sobel Edges Detector"),
    /**
     * A quick hack to render an image using Sinclair ZX Spectrum colors
     */
    ZX_SPECTRUM_EDGES_DETECTOR("ZX Spectrum Edges Detector"),
    /**
     * A quick hack to render an image using Commodore 64 colors
     */
    C64_EDGES_DETECTOR("C64 Edges Detector");

    private final String description;

    EdgesDetectorFlavour(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Collection<EdgesDetectorFlavour> getActiveValues() {
        List<EdgesDetectorFlavour> list = new ArrayList<>();
        list.add(CANNY_EDGES_DETECTOR);
        if (Environment.isDev()) {
            list.add(ROMY_JONA_EDGES_DETECTOR);
        }
        list.add(SOBEL_EDGES_DETECTOR);
        list.add(ZX_SPECTRUM_EDGES_DETECTOR);
        list.add(C64_EDGES_DETECTOR);
        return list;
    }
}
