package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.Filter;

/**
 * Marker interface for the Distortion Measurement filter.
 *
 * <p>Detects barrel and pincushion distortion by finding near-horizontal and near-vertical
 * high-contrast edges via a row/column Hough accumulator, measuring their sagitta (midpoint
 * deviation from a straight chord), and deriving a radial distortion coefficient k₁.  Results
 * are rendered as a deformed grid overlay and a numeric viewport panel.</p>
 *
 * @author Stefano Reksten
 */
public interface DistortionMeasurementFilter extends Filter {
}
