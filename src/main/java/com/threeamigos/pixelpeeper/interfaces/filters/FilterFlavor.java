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
    WINDOWS_3_11_PALETTE("Windows 3.11 palette"),
    /**
     * Divides the image into a configurable grid and renders a Laplacian-variance sharpness heatmap
     */
    SHARPNESS_HEATMAP("Sharpness Heatmap"),
    /**
     * Computes per-channel histograms and highlights shadow/highlight clipping as an overlay
     */
    HISTOGRAM_CLIPPING_DETECTOR("Histogram Clipping Detector"),
    /**
     * Estimates image noise by measuring luminance and chroma standard deviation in flat (uniform) regions
     */
    NOISE_ESTIMATOR("Noise Estimator"),
    /**
     * Measures the radial brightness falloff from centre to corners and expresses it as EV loss per ring
     */
    VIGNETTING_PROFILE("Vignetting Profile"),
    /**
     * Computes near/far DoF limits, hyperfocal distance, and diffraction warning from EXIF metadata
     */
    DEPTH_OF_FIELD("Depth of Field"),
    /**
     * Computes full-frame-equivalent exposure settings and a comparable light-gathering index from EXIF metadata
     */
    EQUIVALENT_EXPOSURE("Equivalent Exposure"),
    /**
     * Detects lateral chromatic aberration by measuring R/G and B/G channel misalignment at high-contrast edges
     */
    CHROMATIC_ABERRATION("Chromatic Aberration"),
    /**
     * Measures barrel and pincushion distortion from the curvature of detected straight edges
     */
    DISTORTION_MEASUREMENT("Distortion Measurement"),
    /**
     * Analyses background blur quality: OOF smoothness, bokeh highlight shape (cat's-eye, onion rings), and an overall score
     */
    BOKEH_QUALITY("Bokeh Quality");

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
        list.add(SHARPNESS_HEATMAP);
        list.add(HISTOGRAM_CLIPPING_DETECTOR);
        list.add(NOISE_ESTIMATOR);
        list.add(VIGNETTING_PROFILE);
        list.add(DEPTH_OF_FIELD);
        list.add(EQUIVALENT_EXPOSURE);
        list.add(CHROMATIC_ABERRATION);
        list.add(DISTORTION_MEASUREMENT);
        list.add(BOKEH_QUALITY);
        return list;
    }
}
