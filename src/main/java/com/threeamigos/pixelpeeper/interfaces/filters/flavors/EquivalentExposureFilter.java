package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ExifAwareFilter;

/**
 * Marker interface for the Equivalent Exposure Display filter.
 *
 * <p>Computes the full-frame-equivalent exposure settings from EXIF metadata and displays
 * them as a viewport overlay.  No pixel data is modified; all output is produced via
 * {@link com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter}.</p>
 *
 * @author Stefano Reksten
 */
public interface EquivalentExposureFilter extends ExifAwareFilter {
}
