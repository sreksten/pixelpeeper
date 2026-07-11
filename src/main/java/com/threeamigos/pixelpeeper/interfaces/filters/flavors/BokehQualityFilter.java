package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.Filter;

/**
 * Marker interface for the Bokeh Quality filter.
 *
 * <p>Segments the image into in-focus and out-of-focus regions using local Laplacian variance,
 * measures out-of-focus region smoothness as a proxy for bokeh quality, detects specular
 * highlight blobs and analyses their circularity (circular vs. cat's-eye) and interior
 * uniformity (smooth fill vs. onion rings), and reports a composite bokeh quality score.</p>
 *
 * @author Stefano Reksten
 */
public interface BokehQualityFilter extends Filter {
}
