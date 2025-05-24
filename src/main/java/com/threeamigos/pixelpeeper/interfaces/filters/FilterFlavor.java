package com.threeamigos.pixelpeeper.interfaces.filters;

import com.threeamigos.pixelpeeper.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An enumeration of the various filters available to the application
 *
 * @author Stefano Reksten
 */
public enum FilterFlavor {

    /**
     * Implements the Canny edge-detection algorithm
     */
    CANNY_EDGES_DETECTOR("Canny Edges Detector"),
    /**
     * A fake edge detector just to check the hosting capabilities of the preferences' host window
     */
    ROMY_JONA("Romy Jona"),
    /**
     * Implements the Sobel edge-detection algorithm
     */
    SOBEL_EDGES_DETECTOR("Sobel Edges Detector"),
    /**
     * A quick hack to render an image using Sinclair ZX Spectrum colors
     */
    ZX_SPECTRUM_PALETTE("ZX Spectrum palette"),
    /**
     * A quick hack to render an image using Commodore 64 colors
     */
    C64_PALETTE("C64 palette"),
    /**
     * A quick hack to render an image using Windows 3.11 colors
     */
    WINDOWS_3_11_PALETTE("Windows 3.11 palette");

    private final String description;

    FilterFlavor(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Collection<FilterFlavor> getActiveValues() {
        List<FilterFlavor> list = new ArrayList<>();
        list.add(CANNY_EDGES_DETECTOR);
        if (Environment.isDev()) {
            list.add(ROMY_JONA);
        }
        list.add(SOBEL_EDGES_DETECTOR);
        list.add(ZX_SPECTRUM_PALETTE);
        list.add(C64_PALETTE);
        list.add(WINDOWS_3_11_PALETTE);
        return list;
    }
}
