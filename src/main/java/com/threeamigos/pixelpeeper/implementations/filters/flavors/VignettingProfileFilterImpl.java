package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.VignettingProfileFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.VignettingProfileFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Measures the radial brightness falloff (vignetting) of a lens by dividing the image
 * into concentric rings centred on the image and computing the mean luminance in each ring.
 *
 * <p>The ring nearest the centre establishes the reference brightness. Every outer ring is
 * expressed as an EV loss relative to that reference using the formula
 * <code>EV = −log₂(ringMean / centreMean)</code>. Positive EV values indicate a darker
 * ring (i.e. real vignetting); negative values indicate a lighter ring (uncommon).</p>
 *
 * <p>Two complementary views are produced:</p>
 * <ol>
 *   <li><b>Filter image</b> – a false-colour overlay painted atop the source image.
 *   Each pixel is coloured according to its ring's EV loss: no loss → transparent
 *   (original shows through); increasing loss → progressively more opaque and red.
 *   The transparency slider controls the overall blend.</li>
 *   <li><b>Viewport overlay</b> – a compact line-graph in the bottom-left corner of each
 *   viewport showing EV loss vs. normalised distance from centre, with a corner-EV
 *   readout below the graph.  Side-by-side comparison between slices is immediate.</li>
 * </ol>
 *
 * <p>Computation is parallelised with row-strip partitioning.</p>
 *
 * @author Stefano Reksten
 */
public class VignettingProfileFilterImpl implements VignettingProfileFilter, ViewportOverlayPainter {

    // ── Overlay geometry (screen pixels) ─────────────────────────────────────
    private static final int PAD = 8;
    private static final int LABEL_W = 30; // left margin reserved for Y-axis labels (e.g. "-2.0")
    private static final int GRAPH_W = 160;
    private static final int GRAPH_H = 80;
    private static final int TITLE_H = 16;
    private static final int TEXT_H = 16;
    private static final int OVERLAY_W = LABEL_W + GRAPH_W + PAD;
    private static final int OVERLAY_H = PAD + TITLE_H + PAD + GRAPH_H + PAD + TEXT_H + PAD;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color AXIS_COLOR = new Color(120, 120, 120);
    private static final Color GRID_COLOR = new Color(60, 60, 60);
    private static final Color CURVE_COLOR = new Color(255, 220, 60);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color CORNER_LABEL_COLOR = new Color(255, 140, 60);

    private final VignettingProfileFilterPreferences preferences;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    /**
     * Immutable snapshot written by the background thread, read by the EDT.
     */
    private volatile VignettingData vignettingData;

    public VignettingProfileFilterImpl(VignettingProfileFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {
        isAborted = false;
        vignettingData = null;
        filteredImage = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int ringCount = preferences.getRingCount();

        double cx = width / 2.0;
        double cy = height / 2.0;
        // Distance from centre to the nearest corner equals the half-diagonal
        double halfDiagonal = Math.sqrt(cx * cx + cy * cy);
        double ringWidth = halfDiagonal / ringCount;

        // ── Pass 1: accumulate per-ring luminance sums in parallel (row-strip) ─
        int processors = Runtime.getRuntime().availableProcessors();
        int rowsPerProcessor = (height + processors - 1) / processors;

        long[][] ringSums = new long[processors][ringCount];
        int[][] ringCounts = new int[processors][ringCount];
        Thread[] threads = new Thread[processors];

        for (int t = 0; t < processors; t++) {
            final int startRow = t * rowsPerProcessor;
            final int endRow = Math.min(startRow + rowsPerProcessor, height);
            final int ti = t;
            threads[t] = new Thread(() -> {
                long[] localSums = ringSums[ti];
                int[] localCounts = ringCounts[ti];
                for (int y = startRow; y < endRow && !isAborted; y++) {
                    double dy = y - cy;
                    for (int x = 0; x < width; x++) {
                        double dx = x - cx;
                        double r = Math.sqrt(dx * dx + dy * dy);
                        int ringIdx = Math.min((int) (r / ringWidth), ringCount - 1);
                        int rgb = sourceImage.getRGB(x, y);
                        int red = (rgb >> 16) & 0xff;
                        int green = (rgb >> 8) & 0xff;
                        int blue = rgb & 0xff;
                        int luma = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);
                        localSums[ringIdx] += luma;
                        localCounts[ringIdx]++;
                    }
                }
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            return;
        }

        // ── Merge ring accumulators ───────────────────────────────────────────
        long[] totalSums = new long[ringCount];
        int[] totalCounts = new int[ringCount];
        for (int t = 0; t < processors; t++) {
            for (int i = 0; i < ringCount; i++) {
                totalSums[i] += ringSums[t][i];
                totalCounts[i] += ringCounts[t][i];
            }
        }

        double[] ringMeans = new double[ringCount];
        for (int i = 0; i < ringCount; i++) {
            ringMeans[i] = totalCounts[i] > 0 ? (double) totalSums[i] / totalCounts[i] : 0.0;
        }

        // ── Compute EV loss per ring ──────────────────────────────────────────
        double centreMean = ringMeans[0] > 0 ? ringMeans[0] : 1.0;
        double[] evLoss = new double[ringCount];
        double maxEvLoss = 0.0;
        double minEvLoss = 0.0;

        for (int i = 0; i < ringCount; i++) {
            if (ringMeans[i] > 0) {
                double ratio = ringMeans[i] / centreMean;
                evLoss[i] = -Math.log(ratio) / Math.log(2.0); // −log₂(ratio)
            }
            if (evLoss[i] > maxEvLoss) maxEvLoss = evLoss[i];
            if (evLoss[i] < minEvLoss) minEvLoss = evLoss[i];
        }

        // Graph Y-axis: ceiling to nearest 0.5 EV above max (min 1.0); floor to nearest 0.5 EV below min
        double graphMaxEV = Math.max(1.0, Math.ceil(maxEvLoss * 2.0) / 2.0);
        double graphMinEV = minEvLoss < 0 ? Math.floor(minEvLoss * 2.0) / 2.0 : 0.0;

        // Pre-compute per-ring ARGB colours for the false-colour overlay image
        int[] ringColors = computeRingColors(evLoss, ringCount, maxEvLoss);

        // ── Pass 2: render false-colour vignetting overlay image ──────────────
        BufferedImage noiseMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int processors2 = Runtime.getRuntime().availableProcessors();
        int rowsPerProcessor2 = (height + processors2 - 1) / processors2;
        Thread[] threads2 = new Thread[processors2];

        for (int t = 0; t < processors2; t++) {
            final int startRow = t * rowsPerProcessor2;
            final int endRow = Math.min(startRow + rowsPerProcessor2, height);
            threads2[t] = new Thread(() -> {
                for (int y = startRow; y < endRow && !isAborted; y++) {
                    double dy = y - cy;
                    for (int x = 0; x < width; x++) {
                        double dx = x - cx;
                        double r = Math.sqrt(dx * dx + dy * dy);
                        int ringIdx = Math.min((int) (r / ringWidth), ringCount - 1);
                        noiseMap.setRGB(x, y, ringColors[ringIdx]);
                    }
                }
            });
            threads2[t].start();
        }

        joinAll(threads2);

        if (!isAborted) {
            vignettingData = new VignettingData(evLoss, ringMeans, centreMean,
                    evLoss[ringCount - 1], graphMaxEV, graphMinEV, ringCount);
            filteredImage = noiseMap;
        }
    }

    /**
     * Returns one ARGB colour per ring for the false-colour overlay.
     * Ring 0 (centre) is transparent. Outer rings transition from blue (low EV loss)
     * to red (high EV loss), with alpha increasing with EV loss.
     */
    private int[] computeRingColors(double[] evLoss, int ringCount, double maxEvLoss) {
        int[] colors = new int[ringCount];
        colors[0] = 0x00000000; // centre ring — transparent
        if (maxEvLoss <= 0.0) {
            return colors; // no vignetting detected; leave all transparent
        }
        for (int i = 1; i < ringCount; i++) {
            double ev = evLoss[i];
            if (ev <= 0.0) {
                colors[i] = 0x00000000;
                continue;
            }
            float t = (float) Math.min(1.0, ev / maxEvLoss);
            // hue: 2/3 (blue) → 0 (red) as vignetting increases
            float hue = (1f - t) * (2f / 3f);
            int alpha = Math.min(200, (int) (ev * 80));
            colors[i] = (Color.HSBtoRGB(hue, 0.9f, 1.0f) & 0x00FFFFFF) | (alpha << 24);
        }
        return colors;
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        VignettingData data = vignettingData; // volatile read — safe on EDT
        if (data == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Position overlay in the bottom-left corner
        int ox = x + PAD;
        int oy = y + height - OVERLAY_H - PAD;
        if (oy < y) {
            oy = y;
        }

        // Background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_W, OVERLAY_H, 8, 8);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics fm = g2d.getFontMetrics();

        // Title
        String title = "Vignetting Profile";
        g2d.setColor(TITLE_COLOR);
        g2d.drawString(title, ox + PAD, oy + PAD + fm.getAscent());

        // Graph area origin — gx is offset by LABEL_W to leave room for Y-axis labels
        int gx = ox + LABEL_W;
        int gy = oy + PAD + TITLE_H + PAD;

        // Grid lines and Y-axis labels at 0.5 EV intervals
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        FontMetrics fmSmall = g2d.getFontMetrics();
        double graphRange = data.graphMaxEV - data.graphMinEV;
        double evStep = 0.5;
        for (double ev = data.graphMinEV; ev <= data.graphMaxEV + 0.01; ev += evStep) {
            int lineY = gy + (int) ((ev - data.graphMinEV) * GRAPH_H / graphRange);
            if (lineY > gy + GRAPH_H) {
                break;
            }
            g2d.setColor(GRID_COLOR);
            g2d.drawLine(gx, lineY, gx + GRAPH_W, lineY);
            g2d.setColor(AXIS_COLOR);
            String evLabel;
            if (Math.abs(ev) < 0.01) {
                evLabel = "0";
            } else if (ev < 0) {
                evLabel = String.format("+%.1f", -ev); // ring brighter than centre
            } else {
                evLabel = String.format("-%.1f", ev);  // ring darker than centre (vignetting)
            }
            g2d.drawString(evLabel, gx - fmSmall.stringWidth(evLabel) - 2, lineY + fmSmall.getAscent() / 2);
        }

        // Axis borders
        g2d.setColor(AXIS_COLOR);
        g2d.drawRect(gx, gy, GRAPH_W, GRAPH_H);

        // Curve — map EV to screen Y using the full [graphMinEV, graphMaxEV] range
        g2d.setColor(CURVE_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        int ringCount = data.ringCount;
        int prevScreenX = -1;
        int prevScreenY = -1;
        for (int i = 0; i < ringCount; i++) {
            int screenX = gx + (ringCount > 1 ? i * GRAPH_W / (ringCount - 1) : 0);
            double clampedEV = Math.max(data.graphMinEV, Math.min(data.evLoss[i], data.graphMaxEV));
            int screenY = gy + (int) ((clampedEV - data.graphMinEV) * GRAPH_H / graphRange);
            if (prevScreenX >= 0) {
                g2d.drawLine(prevScreenX, prevScreenY, screenX, screenY);
            }
            prevScreenX = screenX;
            prevScreenY = screenY;
        }
        g2d.setStroke(new BasicStroke(1f));

        // X-axis labels
        g2d.setColor(AXIS_COLOR);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        fmSmall = g2d.getFontMetrics();
        g2d.drawString("Ctr", gx, gy + GRAPH_H + fmSmall.getAscent() + 1);
        String cornerLabel = "Edge";
        g2d.drawString(cornerLabel, gx + GRAPH_W - fmSmall.stringWidth(cornerLabel), gy + GRAPH_H + fmSmall.getAscent() + 1);

        // Corner EV loss text below graph
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        fm = g2d.getFontMetrics();
        int textY = gy + GRAPH_H + PAD + fm.getAscent();
        String cornerText = String.format("Corner: %.2f EV", data.cornerEvLoss);
        g2d.setColor(Color.BLACK);
        g2d.drawString(cornerText, ox + PAD + 1, textY + 1);
        g2d.setColor(CORNER_LABEL_COLOR);
        g2d.drawString(cornerText, ox + PAD, textY);
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
        return "Measures the radial brightness falloff (vignetting) by dividing the image into concentric " +
                "rings centred on the frame and computing the mean luminance in each ring. " +
                "The innermost ring serves as the reference; every outer ring is expressed as an EV loss " +
                "using \u2212log\u2082(ring mean / centre mean). Positive values indicate a darker ring " +
                "(i.e. real vignetting).\n\n" +
                "A false-colour overlay is composited atop the source image: pixels with no EV loss are " +
                "left transparent so the original shows through; increasing loss is rendered from blue " +
                "(mild) to red (severe). Use the transparency slider to blend the overlay with the original.\n\n" +
                "A line graph in the bottom-left corner of each viewport plots EV loss vs. normalised " +
                "distance from centre. Loading the same lens at different apertures into separate slices " +
                "allows direct side-by-side vignetting comparison.\n\n" +
                "Ring Count sets the number of concentric analysis rings (5\u201330). More rings give a " +
                "smoother profile curve but each ring is narrower and may sample fewer pixels near the centre.\n\n" +
                "Vignetting is one of the most visible optical weaknesses of fast lenses shot wide open: " +
                "the corners appear noticeably darker than the centre. This filter lets you quantify that " +
                "falloff in stops (EV) and compare how it changes as you stop down — loading the same lens " +
                "at f/1.8 and f/4 into two slices will show how quickly the vignette clears. " +
                "You can also compare two lenses on the same body at the same aperture to see which controls " +
                "light falloff better. " +
                "For reliable results, use evenly lit, neutral-coloured subjects: a clear overcast sky, a large " +
                "white or grey wall, or a light table. Scenes with strong subject brightness variation from " +
                "centre to edge (e.g. a dark foreground and bright sky) will skew the ring averages and " +
                "produce misleading EV loss readings.";
    }

    // ── Vignetting data snapshot ──────────────────────────────────────────────

    static final class VignettingData {
        final double[] evLoss;
        final double[] ringMeans;
        final double centreMean;
        final double cornerEvLoss;
        final double graphMaxEV;
        final double graphMinEV;
        final int ringCount;

        VignettingData(double[] evLoss, double[] ringMeans, double centreMean,
                       double cornerEvLoss, double graphMaxEV, double graphMinEV, int ringCount) {
            this.evLoss = evLoss;
            this.ringMeans = ringMeans;
            this.centreMean = centreMean;
            this.cornerEvLoss = cornerEvLoss;
            this.graphMaxEV = graphMaxEV;
            this.graphMinEV = graphMinEV;
            this.ringCount = ringCount;
        }
    }
}
