package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.HistogramClippingDetectorFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.HistogramClippingDetectorFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Computes per-channel (R, G, B, luminance) histograms for the source image and
 * exposes them as a fixed-position overlay rendered directly into each viewport via
 * {@link ViewportOverlayPainter}.
 *
 * <p>Because the overlay is painted in screen space rather than being baked into the
 * filtered image, it is always visible regardless of pan or zoom level.</p>
 *
 * <p>Computation is parallelised across available CPU cores using row-strips.
 * Each thread accumulates its own per-channel histograms which are merged after all
 * threads finish.  The result is published as an immutable {@link HistogramData} snapshot
 * via a volatile field, ensuring safe cross-thread visibility.</p>
 *
 * @author Stefano Reksten
 */
public class HistogramClippingDetectorFilterImpl implements HistogramClippingDetectorFilter, ViewportOverlayPainter {

    // Overlay layout constants (screen pixels)
    private static final int PADDING = 8;
    private static final int HIST_WIDTH = 256;
    private static final int HIST_HEIGHT = 96;
    private static final int TEXT_LINE_HEIGHT = 16;
    static final int OVERLAY_WIDTH = HIST_WIDTH + 2 * PADDING;
    static final int OVERLAY_HEIGHT = HIST_HEIGHT + 2 * PADDING + TEXT_LINE_HEIGHT * 2 + 4;

    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color SHADOW_ZONE_COLOR = new Color(60, 60, 200, 80);
    private static final Color HIGHLIGHT_ZONE_COLOR = new Color(200, 60, 60, 80);
    private static final Color LUMA_COLOR = new Color(200, 200, 200, 140);
    private static final Color RED_CHANNEL_COLOR = new Color(220, 60, 60, 140);
    private static final Color GREEN_CHANNEL_COLOR = new Color(60, 200, 60, 140);
    private static final Color BLUE_CHANNEL_COLOR = new Color(60, 100, 220, 140);
    private static final Color SHADOW_TEXT_COLOR = new Color(120, 120, 255);
    private static final Color HIGHLIGHT_TEXT_COLOR = new Color(255, 120, 120);

    private final HistogramClippingDetectorFilterPreferences preferences;
    private BufferedImage sourceImage;
    private volatile boolean isAborted;

    /**
     * Immutable snapshot of the computed histogram data, published as a volatile
     * reference so the EDT sees a consistent state after the background thread writes it.
     */
    private volatile HistogramData histogramData;

    public HistogramClippingDetectorFilterImpl(HistogramClippingDetectorFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {
        isAborted = false;
        histogramData = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int totalPixels = width * height;

        int shadowThreshold = preferences.getShadowThreshold();
        int highlightThreshold = preferences.getHighlightThreshold();

        // ── Pass 1: compute histograms in parallel (row-strip) ────────────────
        int processors = Runtime.getRuntime().availableProcessors();
        int rowsPerProcessor = (height + processors - 1) / processors;

        int[][] rHists = new int[processors][256];
        int[][] gHists = new int[processors][256];
        int[][] bHists = new int[processors][256];
        int[][] lHists = new int[processors][256];
        long[] shadowCounts = new long[processors];
        long[] highlightCounts = new long[processors];

        Thread[] threads = new Thread[processors];

        for (int t = 0; t < processors; t++) {
            final int startRow = t * rowsPerProcessor;
            final int endRow = Math.min(startRow + rowsPerProcessor, height);
            final int ti = t;
            threads[t] = new Thread(() -> {
                int[] rH = rHists[ti];
                int[] gH = gHists[ti];
                int[] bH = bHists[ti];
                int[] lH = lHists[ti];
                for (int y = startRow; y < endRow && !isAborted; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb = sourceImage.getRGB(x, y);
                        int r = (rgb >> 16) & 0xff;
                        int g = (rgb >> 8) & 0xff;
                        int b = rgb & 0xff;
                        int luma = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
                        rH[r]++;
                        gH[g]++;
                        bH[b]++;
                        lH[luma]++;
                        if (luma <= shadowThreshold) shadowCounts[ti]++;
                        if (luma >= highlightThreshold) highlightCounts[ti]++;
                    }
                }
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            return;
        }

        // Merge thread-local histograms
        int[] rHist = new int[256];
        int[] gHist = new int[256];
        int[] bHist = new int[256];
        int[] lHist = new int[256];
        long shadowCount = 0;
        long highlightCount = 0;

        for (int t = 0; t < processors; t++) {
            for (int i = 0; i < 256; i++) {
                rHist[i] += rHists[t][i];
                gHist[i] += gHists[t][i];
                bHist[i] += bHists[t][i];
                lHist[i] += lHists[t][i];
            }
            shadowCount += shadowCounts[t];
            highlightCount += highlightCounts[t];
        }

        // Publish snapshot — volatile write ensures EDT visibility
        histogramData = new HistogramData(rHist, gHist, bHist, lHist,
                shadowCount, highlightCount, totalPixels, shadowThreshold, highlightThreshold);
    }

    /**
     * Returns {@code null}: this filter does not produce a composited image.
     * All rendering is done via {@link #paintViewportOverlay}.
     */
    @Override
    public BufferedImage getResultingImage() {
        return null;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        HistogramData data = histogramData;  // volatile read — safe on EDT
        if (data == null) {
            return;
        }

        // Position overlay in the bottom-left corner of the viewport
        int ox = x + PADDING;
        int oy = Math.max(y, y + height - OVERLAY_HEIGHT - PADDING);

        // Clamp horizontally if the viewport is narrower than the overlay
        if (ox + OVERLAY_WIDTH > x + width) {
            ox = Math.max(x, x + width - OVERLAY_WIDTH);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Semi-transparent dark background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_WIDTH, OVERLAY_HEIGHT, 8, 8);

        // Histogram area origin (inside padding)
        int hx = ox + PADDING;
        int hy = oy + PADDING;

        // Shadow clipping zone (blue tint over bins 0..shadowThreshold)
        if (data.shadowThreshold >= 0) {
            g2d.setColor(SHADOW_ZONE_COLOR);
            g2d.fillRect(hx, hy, Math.min(data.shadowThreshold + 1, HIST_WIDTH), HIST_HEIGHT);
        }

        // Highlight clipping zone (red tint over bins highlightThreshold..255)
        if (data.highlightThreshold <= 255) {
            g2d.setColor(HIGHLIGHT_ZONE_COLOR);
            int hzStart = Math.max(0, data.highlightThreshold);
            g2d.fillRect(hx + hzStart, hy, HIST_WIDTH - hzStart, HIST_HEIGHT);
        }

        // Find peak count for normalisation
        int maxCount = 0;
        for (int i = 0; i < 256; i++) {
            if (data.rHist[i] > maxCount) maxCount = data.rHist[i];
            if (data.gHist[i] > maxCount) maxCount = data.gHist[i];
            if (data.bHist[i] > maxCount) maxCount = data.bHist[i];
            if (data.lHist[i] > maxCount) maxCount = data.lHist[i];
        }

        // Draw histograms back-to-front: Luma, Blue, Green, Red
        if (maxCount > 0) {
            drawHistogram(g2d, hx, hy, data.lHist, maxCount, LUMA_COLOR);
            drawHistogram(g2d, hx, hy, data.bHist, maxCount, BLUE_CHANNEL_COLOR);
            drawHistogram(g2d, hx, hy, data.gHist, maxCount, GREEN_CHANNEL_COLOR);
            drawHistogram(g2d, hx, hy, data.rHist, maxCount, RED_CHANNEL_COLOR);
        }

        // Percentage text below the histogram
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textY = hy + HIST_HEIGHT + fm.getAscent() + 4;

        double shadowPct = data.totalPixels > 0 ? 100.0 * data.shadowCount / data.totalPixels : 0.0;
        double highlightPct = data.totalPixels > 0 ? 100.0 * data.highlightCount / data.totalPixels : 0.0;

        String shadowText = String.format("Shadows \u2264%d: %.2f%%", data.shadowThreshold, shadowPct);
        String highlightText = String.format("Highlights \u2265%d: %.2f%%", data.highlightThreshold, highlightPct);

        g2d.setColor(Color.BLACK);
        g2d.drawString(shadowText, hx + 1, textY + 1);
        g2d.setColor(SHADOW_TEXT_COLOR);
        g2d.drawString(shadowText, hx, textY);

        textY += TEXT_LINE_HEIGHT;

        g2d.setColor(Color.BLACK);
        g2d.drawString(highlightText, hx + 1, textY + 1);
        g2d.setColor(HIGHLIGHT_TEXT_COLOR);
        g2d.drawString(highlightText, hx, textY);
    }

    private void drawHistogram(Graphics2D g2d, int hx, int hy, int[] hist, int maxCount, Color color) {
        g2d.setColor(color);
        for (int i = 0; i < 256; i++) {
            int barHeight = (int) ((long) hist[i] * HIST_HEIGHT / maxCount);
            if (barHeight > 0) {
                g2d.fillRect(hx + i, hy + HIST_HEIGHT - barHeight, 1, barHeight);
            }
        }
    }

    private void joinAll(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public String getDescription() {
        return "Renders a per-channel histogram overlay in the bottom-left corner of each viewport. " +
                "Four overlapping histograms are drawn: luminance (grey), red, green, and blue.\n\n" +
                "The shadow zone (left side of the histogram, up to the Shadow threshold) is tinted blue " +
                "and the highlight zone (right side, from the Highlight threshold) is tinted red. " +
                "Clipping percentages — the fraction of pixels at or below the shadow threshold and at or " +
                "above the highlight threshold — are printed below the bars.\n\n" +
                "Shadow threshold sets the luminance value below which a pixel is considered clipped in the shadows (default 2, range 0–15). " +
                "Highlight threshold sets the luminance value above which a pixel is considered clipped in the highlights (default 253, range 240–255).";
    }

    // ── Histogram data snapshot ───────────────────────────────────────────────

    static final class HistogramData {
        final int[] rHist;
        final int[] gHist;
        final int[] bHist;
        final int[] lHist;
        final long shadowCount;
        final long highlightCount;
        final int totalPixels;
        final int shadowThreshold;
        final int highlightThreshold;

        HistogramData(int[] rHist, int[] gHist, int[] bHist, int[] lHist,
                      long shadowCount, long highlightCount, int totalPixels,
                      int shadowThreshold, int highlightThreshold) {
            this.rHist = rHist;
            this.gHist = gHist;
            this.bHist = bHist;
            this.lHist = lHist;
            this.shadowCount = shadowCount;
            this.highlightCount = highlightCount;
            this.totalPixels = totalPixels;
            this.shadowThreshold = shadowThreshold;
            this.highlightThreshold = highlightThreshold;
        }
    }
}
