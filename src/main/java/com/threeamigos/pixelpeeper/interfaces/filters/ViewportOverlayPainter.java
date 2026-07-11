package com.threeamigos.pixelpeeper.interfaces.filters;

import java.awt.Graphics2D;

/**
 * Implemented by filters that render a fixed-position overlay directly into the viewport
 * rather than baking the overlay into the filtered image.
 *
 * <p>When a {@link Filter} also implements this interface, {@code ImageSliceImpl} skips
 * compositing the (null) filtered image and instead calls
 * {@link #paintViewportOverlay(Graphics2D, int, int, int, int)} after the source image has
 * been drawn, passing the viewport's screen-space rectangle.  The overlay is therefore
 * always visible regardless of pan or zoom.</p>
 */
public interface ViewportOverlayPainter {

    /**
     * Paints the overlay at a fixed position inside the viewport.
     *
     * @param g2d    the graphics context, already clipped to the viewport
     * @param x      screen-space left edge of the viewport
     * @param y      screen-space top edge of the viewport
     * @param width  viewport width in screen pixels
     * @param height viewport height in screen pixels
     */
    void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height);
}
