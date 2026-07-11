package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.Filter;

/**
 * Marker interface for the Chromatic Aberration Detection filter.
 *
 * <p>Detects lateral (transverse) chromatic aberration by measuring R/G and B/G channel
 * misalignment at high-contrast luminance edges.  Results are rendered as a sparse
 * false-colour fringe map overlay and a numeric score viewport panel.</p>
 *
 * @author Stefano Reksten
 */
public interface ChromaticAberrationFilter extends Filter {
}
