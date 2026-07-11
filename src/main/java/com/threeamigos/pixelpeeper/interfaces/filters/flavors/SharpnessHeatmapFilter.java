package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.Filter;

/**
 * A filter that divides an image into a configurable grid of cells and computes a
 * per-cell sharpness score using Laplacian variance. The result is rendered as a
 * semi-transparent color-coded heatmap overlay (blue = soft, red = sharp).
 *
 * @author Stefano Reksten
 */
public interface SharpnessHeatmapFilter extends Filter {
}
