package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.NoiseEstimatorFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NoiseEstimatorFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Estimates image noise by detecting flat (uniform) regions and computing the standard deviation
 * of luminance and chroma within those regions.
 *
 * <p>The image is divided into non-overlapping patches of configurable size. Patches whose
 * luminance variance falls below the flat-variance threshold are considered uniform and used
 * for noise measurement. The remaining (textured) patches are left transparent so the original
 * image shows through.</p>
 *
 * <p>Flat patches are rendered as a false-colour noise map: blue = quiet, red = noisy.
 * Aggregate luma σ, chroma σ, and flat-patch statistics are drawn as a text overlay in the
 * bottom-right corner of each viewport via {@link ViewportOverlayPainter}.</p>
 *
 * <p>Processing is parallelised across available CPU cores using patch-strip partitioning.</p>
 *
 * @author Stefano Reksten
 */
public class NoiseEstimatorFilterImpl implements NoiseEstimatorFilter, ViewportOverlayPainter {

    // Overlay layout constants (screen pixels)
    private static final int PADDING = 8;
    private static final int TEXT_LINE_HEIGHT = 16;
    private static final int OVERLAY_WIDTH = 210;
    private static final int OVERLAY_LINES = 3;
    private static final int OVERLAY_HEIGHT = TEXT_LINE_HEIGHT * OVERLAY_LINES + 2 * PADDING;

    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color LUMA_TEXT_COLOR = new Color(220, 220, 100);
    private static final Color CHROMA_TEXT_COLOR = new Color(100, 200, 220);
    private static final Color INFO_TEXT_COLOR = new Color(180, 180, 180);

    private final NoiseEstimatorFilterPreferences preferences;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    /**
     * Immutable snapshot of the computed noise statistics, published as a volatile
     * reference so the EDT sees a consistent state after the background thread writes it.
     */
    private volatile NoiseData noiseData;

    public NoiseEstimatorFilterImpl(NoiseEstimatorFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {
        isAborted = false;
        noiseData = null;
        filteredImage = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int pSize = preferences.getPatchSize();
        int flatThreshold = preferences.getFlatVarianceThreshold();

        int numPatchesX = (width + pSize - 1) / pSize;
        int numPatchesY = (height + pSize - 1) / pSize;
        int totalPatches = numPatchesX * numPatchesY;

        double[] lumaVariances = new double[totalPatches];
        double[] chromaVariances = new double[totalPatches];

        // ── Pass 1: compute per-patch variance statistics in parallel ─────────
        int processors = Runtime.getRuntime().availableProcessors();
        int patchesPerProcessor = (totalPatches + processors - 1) / processors;
        Thread[] threads = new Thread[processors];

        for (int t = 0; t < processors; t++) {
            final int startPatch = t * patchesPerProcessor;
            final int endPatch = Math.min(startPatch + patchesPerProcessor, totalPatches);
            threads[t] = new Thread(() -> {
                for (int p = startPatch; p < endPatch && !isAborted; p++) {
                    int row = p / numPatchesX;
                    int col = p % numPatchesX;
                    int x0 = col * pSize;
                    int y0 = row * pSize;
                    int x1 = Math.min(x0 + pSize, width);
                    int y1 = Math.min(y0 + pSize, height);
                    int pixelCount = (x1 - x0) * (y1 - y0);
                    if (pixelCount == 0) {
                        continue;
                    }

                    long sumLuma = 0, sumLuma2 = 0;
                    long sumCb = 0, sumCb2 = 0;
                    long sumCr = 0, sumCr2 = 0;

                    for (int y = y0; y < y1; y++) {
                        for (int x = x0; x < x1; x++) {
                            int rgb = sourceImage.getRGB(x, y);
                            int r = (rgb >> 16) & 0xff;
                            int g = (rgb >> 8) & 0xff;
                            int b = rgb & 0xff;
                            int luma = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
                            int cb = b - luma;
                            int cr = r - luma;
                            sumLuma += luma;
                            sumLuma2 += (long) luma * luma;
                            sumCb += cb;
                            sumCb2 += (long) cb * cb;
                            sumCr += cr;
                            sumCr2 += (long) cr * cr;
                        }
                    }

                    double meanLuma = (double) sumLuma / pixelCount;
                    double varLuma = (double) sumLuma2 / pixelCount - meanLuma * meanLuma;
                    double meanCb = (double) sumCb / pixelCount;
                    double varCb = (double) sumCb2 / pixelCount - meanCb * meanCb;
                    double meanCr = (double) sumCr / pixelCount;
                    double varCr = (double) sumCr2 / pixelCount - meanCr * meanCr;

                    lumaVariances[p] = Math.max(0.0, varLuma);
                    chromaVariances[p] = Math.max(0.0, (varCb + varCr) / 2.0);
                }
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            return;
        }

        // ── Identify flat patches and aggregate noise statistics ──────────────
        int flatPatchCount = 0;
        double sumLumaStddev = 0.0;
        double sumChromaStddev = 0.0;
        double maxFlatLumaStddev = 0.0;

        for (int p = 0; p < totalPatches; p++) {
            if (lumaVariances[p] < flatThreshold) {
                double lumaStddev = Math.sqrt(lumaVariances[p]);
                double chromaStddev = Math.sqrt(chromaVariances[p]);
                flatPatchCount++;
                sumLumaStddev += lumaStddev;
                sumChromaStddev += chromaStddev;
                if (lumaStddev > maxFlatLumaStddev) {
                    maxFlatLumaStddev = lumaStddev;
                }
            }
        }

        double avgLumaStddev = flatPatchCount > 0 ? sumLumaStddev / flatPatchCount : 0.0;
        double avgChromaStddev = flatPatchCount > 0 ? sumChromaStddev / flatPatchCount : 0.0;

        // ── Pass 2: render the noise map image ────────────────────────────────
        BufferedImage noiseMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Non-flat patches stay at alpha=0 (transparent), so original image shows through.

        for (int p = 0; p < totalPatches && !isAborted; p++) {
            if (lumaVariances[p] >= flatThreshold) {
                continue; // textured patch — leave transparent
            }
            int row = p / numPatchesX;
            int col = p % numPatchesX;
            int x0 = col * pSize;
            int y0 = row * pSize;
            int x1 = Math.min(x0 + pSize, width);
            int y1 = Math.min(y0 + pSize, height);

            double lumaStddev = Math.sqrt(lumaVariances[p]);
            int color = noiseColor(lumaStddev, maxFlatLumaStddev);

            for (int y = y0; y < y1; y++) {
                for (int x = x0; x < x1; x++) {
                    noiseMap.setRGB(x, y, color);
                }
            }
        }

        if (!isAborted) {
            noiseData = new NoiseData(avgLumaStddev, avgChromaStddev, flatPatchCount, totalPatches);
            filteredImage = noiseMap;
        }
    }

    /**
     * Maps a luminance standard deviation to a false-colour ARGB value.
     * Blue (hue 2/3) for quiet patches, red (hue 0) for noisy patches.
     * Alpha is fixed at 160 (semi-transparent).
     */
    private int noiseColor(double stddev, double maxStddev) {
        float t = (maxStddev > 0.0) ? (float) (stddev / maxStddev) : 0f;
        t = Math.min(1f, Math.max(0f, t));
        float hue = (1f - t) * (2f / 3f);
        return (Color.HSBtoRGB(hue, 0.9f, 1.0f) & 0x00FFFFFF) | 0xA0000000;
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        NoiseData data = noiseData; // volatile read — safe on EDT
        if (data == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Position overlay in the bottom-right corner of the viewport
        int ox = x + width - OVERLAY_WIDTH - PADDING;
        int oy = y + height - OVERLAY_HEIGHT - PADDING;
        if (ox < x) {
            ox = x;
        }
        if (oy < y) {
            oy = y;
        }

        // Semi-transparent dark background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_WIDTH, OVERLAY_HEIGHT, 8, 8);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = ox + PADDING;
        int textY = oy + PADDING + fm.getAscent();

        // Luma noise
        String lumaText = String.format("Luma \u03c3:   %.2f", data.avgLumaStddev);
        g2d.setColor(Color.BLACK);
        g2d.drawString(lumaText, textX + 1, textY + 1);
        g2d.setColor(LUMA_TEXT_COLOR);
        g2d.drawString(lumaText, textX, textY);

        textY += TEXT_LINE_HEIGHT;

        // Chroma noise
        String chromaText = String.format("Chroma \u03c3: %.2f", data.avgChromaStddev);
        g2d.setColor(Color.BLACK);
        g2d.drawString(chromaText, textX + 1, textY + 1);
        g2d.setColor(CHROMA_TEXT_COLOR);
        g2d.drawString(chromaText, textX, textY);

        textY += TEXT_LINE_HEIGHT;

        // Flat patch count
        double flatPct = data.totalPatches > 0 ? 100.0 * data.flatPatchCount / data.totalPatches : 0.0;
        String patchText = String.format("Flat: %d/%d (%.0f%%)", data.flatPatchCount, data.totalPatches, flatPct);
        g2d.setColor(Color.BLACK);
        g2d.drawString(patchText, textX + 1, textY + 1);
        g2d.setColor(INFO_TEXT_COLOR);
        g2d.drawString(patchText, textX, textY);
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
        return "Estimates image noise by detecting flat (uniform) regions and measuring the standard deviation " +
                "of luminance and chroma within those regions. The image is divided into non-overlapping patches; " +
                "patches whose luminance variance falls below the Flat Variance Threshold are treated as uniform sky, " +
                "walls, or other featureless areas where variance can be attributed to sensor noise rather than scene content.\n\n" +
                "Flat patches are rendered as a false-colour noise map: blue = low noise, red = high noise. " +
                "Textured patches are left transparent so the original image remains visible. " +
                "A text overlay in the bottom-right corner shows the aggregate luma \u03c3, chroma \u03c3, " +
                "and the fraction of patches detected as flat.\n\n" +
                "Patch Size controls the side length of each analysis patch (8\u201364 px). Larger patches give a more " +
                "stable noise estimate but fewer data points. " +
                "Flat Variance Threshold sets the maximum luminance variance for a patch to be considered uniform. " +
                "Raise this value for very noisy images (high ISO) where flat regions still show significant variance.";
    }

    // ── Noise data snapshot ───────────────────────────────────────────────────

    static final class NoiseData {
        final double avgLumaStddev;
        final double avgChromaStddev;
        final int flatPatchCount;
        final int totalPatches;

        NoiseData(double avgLumaStddev, double avgChromaStddev, int flatPatchCount, int totalPatches) {
            this.avgLumaStddev = avgLumaStddev;
            this.avgChromaStddev = avgChromaStddev;
            this.flatPatchCount = flatPatchCount;
            this.totalPatches = totalPatches;
        }
    }
}
