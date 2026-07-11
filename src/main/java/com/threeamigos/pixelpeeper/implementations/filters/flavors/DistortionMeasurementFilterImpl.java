package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.DistortionMeasurementFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DistortionMeasurementFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Measures barrel and pincushion lens distortion by analysing the curvature of detected
 * near-horizontal and near-vertical edges.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li><strong>Sobel gradient</strong> (parallel column-strip): computes a normalised luminance
 *       gradient map.  Pixels above the configured edge threshold are classified as edges.</li>
 *   <li><strong>Hough accumulator</strong>: counts edge pixels per row (horizontal lines) and
 *       per column (vertical lines), forming two 1-D accumulator arrays.</li>
 *   <li><strong>Peak detection</strong>: non-maximum suppression on the accumulators with a
 *       5 px merge radius; peaks below 30 % of image width/height are discarded as too sparse.</li>
 *   <li><strong>Sagitta measurement</strong>: for each detected line, the edge pixels are
 *       divided into three equal thirds by their position along the line axis.  The median
 *       transverse position of the middle third versus the average of the outer thirds gives the
 *       <em>sagitta</em> — the midpoint bow of the line.</li>
 *   <li><strong>Distortion percentage</strong>: TV-standard formula
 *       {@code d% = sign × sagitta / (halfDim) × 100}, where sign ensures barrel is negative
 *       and pincushion is positive.  The median across all qualifying lines is reported.</li>
 *   <li><strong>k₁ estimation</strong>: from the mean distortion and the normalised half-span
 *       of each contributing segment, using {@code k₁ ≈ Δr / r²} in normalised coords.</li>
 *   <li><strong>Deformed-grid overlay</strong>: a configurable N×N grid drawn with each line
 *       curved according to the estimated k₁, visualising how much correction is needed.
 *       When no lines are detected the grid is straight (k₁ = 0).</li>
 * </ol>
 *
 * <p><strong>Sign convention</strong>: barrel distortion → negative %, pincushion → positive %.</p>
 *
 * <p>The deformed grid is the filtered image (composited via the transparency slider).
 * A numeric summary is painted via {@link ViewportOverlayPainter} in the bottom-left corner.</p>
 *
 * @author Stefano Reksten
 */
public class DistortionMeasurementFilterImpl implements DistortionMeasurementFilter, ViewportOverlayPainter {

    // ── Overlay geometry ──────────────────────────────────────────────────────
    private static final int PAD = 8;
    private static final int LINE_H = 15;
    private static final int OVERLAY_W = 230;

    // ── Overlay colours ───────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color LABEL_COLOR = new Color(140, 140, 140);
    private static final Color VALUE_COLOR = new Color(240, 240, 100);
    private static final Color BARREL_COLOR = new Color(100, 160, 255);
    private static final Color PINCUSHION_COLOR = new Color(255, 140, 60);
    private static final Color SEPARATOR_COLOR = new Color(70, 70, 70);

    // ── Grid overlay colours ──────────────────────────────────────────────────
    private static final Color GRID_COLOR_H = new Color(0, 220, 200, 180);  // teal — horizontal
    private static final Color GRID_COLOR_V = new Color(220, 180, 0, 180);  // amber — vertical
    private static final Color GRID_CENTER_COLOR = new Color(255, 255, 255, 200); // white — axis

    // ── Distortion grid rendering ─────────────────────────────────────────────
    private static final int GRID_STEPS = 200; // sub-pixel steps per grid line

    // ── Hough detection constants ─────────────────────────────────────────────
    /** Minimum fraction of image dimension for a line segment to be used for distortion. */
    private static final double MIN_SPAN_FRACTION = 0.25;
    /** Non-max suppression merge radius for Hough peaks (pixels). */
    private static final int PEAK_MERGE_RADIUS = 5;
    /** Minimum normalised distance from image centre for a line to contribute to k₁ estimation. */
    private static final double MIN_NORM_DIST = 0.10;

    private final DistortionMeasurementFilterPreferences preferences;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    /** Computed data snapshot, written by background thread, read by EDT. */
    private volatile DistortionData distortionData;

    public DistortionMeasurementFilterImpl(DistortionMeasurementFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    // ── Filter.process() ─────────────────────────────────────────────────────

    @Override
    public void process() {
        isAborted = false;
        distortionData = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int edgeThreshold = preferences.getEdgeThreshold();
        int gridSize = preferences.getGridSize();

        filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // ── Pass 1: Sobel gradient (parallel column-strip) ────────────────────
        int[] gradient = new int[width * height];
        int processors = Runtime.getRuntime().availableProcessors();
        int usableCols = width - 2;
        int colsPerProc = (usableCols + processors - 1) / processors;
        int[] localMaxima = new int[processors];
        Thread[] threads = new Thread[processors];

        for (int t = 0; t < processors; t++) {
            final int ti = t;
            final int startCol = 1 + ti * colsPerProc;
            final int endCol = Math.min(startCol + colsPerProc, width - 1);
            threads[t] = new Thread(() -> {
                int localMax = 0;
                for (int x = startCol; x < endCol && !isAborted; x++) {
                    for (int y = 1; y < height - 1; y++) {
                        int l00 = luma(sourceImage.getRGB(x - 1, y - 1));
                        int l01 = luma(sourceImage.getRGB(x - 1, y));
                        int l02 = luma(sourceImage.getRGB(x - 1, y + 1));
                        int l10 = luma(sourceImage.getRGB(x, y - 1));
                        int l12 = luma(sourceImage.getRGB(x, y + 1));
                        int l20 = luma(sourceImage.getRGB(x + 1, y - 1));
                        int l21 = luma(sourceImage.getRGB(x + 1, y));
                        int l22 = luma(sourceImage.getRGB(x + 1, y + 1));
                        int gx = -l00 + l02 - 2 * l01 + 2 * l12 - l20 + l22;
                        int gy = -l00 - 2 * l10 - l20 + l02 + 2 * l12 + l22;
                        int mag = (int) Math.sqrt((double) gx * gx + (double) gy * gy);
                        gradient[y * width + x] = mag;
                        if (mag > localMax) localMax = mag;
                    }
                }
                localMaxima[ti] = localMax;
            });
            threads[t].start();
        }
        joinAll(threads);
        if (isAborted) { filteredImage = null; return; }

        int globalMax = 0;
        for (int m : localMaxima) if (m > globalMax) globalMax = m;
        if (globalMax == 0) {
            distortionData = DistortionData.noEdges();
            drawGrid(filteredImage, width, height, 0.0, gridSize);
            return;
        }

        // Normalise edge map (0–255)
        final double normScale = 255.0 / globalMax;
        boolean[] isEdge = new boolean[width * height];
        for (int i = 0; i < width * height; i++) {
            isEdge[i] = (int) (gradient[i] * normScale) >= edgeThreshold;
        }
        if (isAborted) { filteredImage = null; return; }

        // ── Pass 2: Hough row/column accumulators ─────────────────────────────
        int[] horizAccum = new int[height]; // edge pixel count per row
        int[] vertAccum = new int[width];   // edge pixel count per column

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (isEdge[y * width + x]) {
                    horizAccum[y]++;
                    vertAccum[x]++;
                }
            }
        }
        if (isAborted) { filteredImage = null; return; }

        // ── Pass 3: Peak detection (non-maximum suppression) ─────────────────
        int minHorizVotes = (int) (width * 0.06);   // at least 6 % of width
        int minVertVotes = (int) (height * 0.06);

        List<Integer> horizLines = extractPeaks(horizAccum, minHorizVotes, PEAK_MERGE_RADIUS);
        List<Integer> vertLines = extractPeaks(vertAccum, minVertVotes, PEAK_MERGE_RADIUS);

        if (isAborted) { filteredImage = null; return; }

        // ── Pass 4: Sagitta measurement ───────────────────────────────────────
        int cx = width / 2;
        int cy = height / 2;
        int minHorizSpan = (int) (width * MIN_SPAN_FRACTION);
        int minVertSpan = (int) (height * MIN_SPAN_FRACTION);

        List<Double> distortionSamples = new ArrayList<>();
        List<Double> k1Samples = new ArrayList<>();

        // Horizontal lines
        for (int y0 : horizLines) {
            if (isAborted) { filteredImage = null; return; }
            List<int[]> segments = extractHorizSegments(isEdge, width, height, y0, minHorizSpan);
            for (int[] seg : segments) {
                int x1 = seg[0], x2 = seg[1];
                double sagitta = measureHorizSagitta(isEdge, width, y0, x1, x2);
                if (Double.isNaN(sagitta)) continue;

                double sign = (y0 >= cy) ? 1.0 : -1.0;
                double dpct = sign * sagitta / cy * 100.0;
                distortionSamples.add(dpct);

                // k₁ estimate from this segment
                double yn = (y0 - cy) / (double) cy;
                if (Math.abs(yn) >= MIN_NORM_DIST) {
                    double halfSpanN = (x2 - x1) / (2.0 * cx);
                    if (halfSpanN > 0.05) {
                        double sn = sagitta / cy;
                        double k1est = sn / (yn * halfSpanN * halfSpanN);
                        k1Samples.add(k1est);
                    }
                }
            }
        }

        // Vertical lines (symmetric treatment, swap roles of x/y)
        for (int x0 : vertLines) {
            if (isAborted) { filteredImage = null; return; }
            List<int[]> segments = extractVertSegments(isEdge, width, height, x0, minVertSpan);
            for (int[] seg : segments) {
                int y1 = seg[0], y2 = seg[1];
                double sagitta = measureVertSagitta(isEdge, width, x0, y1, y2);
                if (Double.isNaN(sagitta)) continue;

                double sign = (x0 >= cx) ? 1.0 : -1.0;
                double dpct = sign * sagitta / cx * 100.0;
                distortionSamples.add(dpct);

                double xn = (x0 - cx) / (double) cx;
                if (Math.abs(xn) >= MIN_NORM_DIST) {
                    double halfSpanN = (y2 - y1) / (2.0 * cy);
                    if (halfSpanN > 0.05) {
                        double sn = sagitta / cx;
                        double k1est = sn / (xn * halfSpanN * halfSpanN);
                        k1Samples.add(k1est);
                    }
                }
            }
        }

        if (isAborted) { filteredImage = null; return; }

        // ── Pass 5: Aggregate ─────────────────────────────────────────────────
        double distortionPct = Double.NaN;
        double k1 = 0.0;
        int lineCount = horizLines.size() + vertLines.size();

        if (!distortionSamples.isEmpty()) {
            Collections.sort(distortionSamples);
            distortionPct = distortionSamples.get(distortionSamples.size() / 2); // median
        }
        if (!k1Samples.isEmpty()) {
            Collections.sort(k1Samples);
            k1 = k1Samples.get(k1Samples.size() / 2); // median k₁
        }

        distortionData = new DistortionData(distortionPct, k1, lineCount,
                distortionSamples.size(), horizLines.size(), vertLines.size());

        // ── Pass 6: Draw deformed grid overlay ────────────────────────────────
        drawGrid(filteredImage, width, height, k1, gridSize);
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        DistortionData data = distortionData;
        if (data == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String[] lines = buildLines(data);
        int totalLines = lines.length;
        int overlayH = PAD + totalLines * LINE_H + PAD;

        // Position: bottom-left
        int ox = x + PAD;
        int oy = y + height - overlayH - PAD;
        if (oy < y) oy = y;

        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_W, overlayH, 8, 8);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = ox + PAD;
        int curY = oy + PAD + fm.getAscent();

        for (int i = 0; i < totalLines; i++) {
            if (lines[i].isEmpty()) {
                g2d.setColor(SEPARATOR_COLOR);
                g2d.drawLine(textX, curY - fm.getAscent() / 2,
                        ox + OVERLAY_W - PAD, curY - fm.getAscent() / 2);
            } else if (i == 0) {
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2d.setColor(TITLE_COLOR);
                g2d.drawString(lines[i], textX, curY);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            } else {
                Color valueCol = resolveValueColor(data, lines[i]);
                int colon = lines[i].indexOf(':');
                if (colon > 0) {
                    String label = lines[i].substring(0, colon + 1);
                    String value = lines[i].substring(colon + 1);
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(label, textX, curY);
                    int labelW = fm.stringWidth(label);
                    g2d.setColor(valueCol);
                    g2d.drawString(value, textX + labelW, curY);
                } else {
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(lines[i], textX, curY);
                }
            }
            curY += LINE_H;
        }
    }

    private Color resolveValueColor(DistortionData d, String line) {
        if (line.contains("Barrel")) return BARREL_COLOR;
        if (line.contains("Pincushion")) return PINCUSHION_COLOR;
        return VALUE_COLOR;
    }

    // ── Deformed grid drawing ─────────────────────────────────────────────────

    /**
     * Draws a deformed grid onto the overlay image.
     *
     * <p>Each grid line is a sequence of small segments, where each node is mapped through
     * the radial distortion model {@code r_d = r_u(1 + k₁ × r_u²)}.  Horizontal grid lines
     * are drawn in teal, vertical in amber; the central axes are drawn white for reference.</p>
     *
     * <p>The forward model with the <em>estimated</em> k₁ shows how straight world lines
     * appear in the distorted image, making the distortion magnitude immediately visible.</p>
     */
    private void drawGrid(BufferedImage img, int width, int height, double k1, int gridSize) {
        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(1.2f));

            double cx = width / 2.0;
            double cy = height / 2.0;

            // Grid line positions in normalised coords [-1, 1]
            // gridSize lines in each direction (symmetric, odd → centre line at 0)
            double[] positions = new double[gridSize];
            for (int i = 0; i < gridSize; i++) {
                positions[i] = -1.0 + 2.0 * i / (gridSize - 1);
            }

            // Draw horizontal grid lines (fixed yn, x sweeps from -1 to 1)
            for (int li = 0; li < gridSize; li++) {
                double yn = positions[li];
                boolean isCenterH = (li == gridSize / 2);
                g2d.setColor(isCenterH ? GRID_CENTER_COLOR : GRID_COLOR_H);

                int[] px = new int[GRID_STEPS + 1];
                int[] py = new int[GRID_STEPS + 1];
                boolean valid = false;
                for (int s = 0; s <= GRID_STEPS; s++) {
                    double xn = -1.0 + 2.0 * s / GRID_STEPS;
                    double r2 = xn * xn + yn * yn;
                    double factor = 1.0 + k1 * r2;
                    double xd = xn * factor;
                    double yd = yn * factor;
                    px[s] = (int) Math.round(cx + xd * cx);
                    py[s] = (int) Math.round(cy + yd * cy);
                    valid = true;
                }
                if (valid) {
                    for (int s = 0; s < GRID_STEPS; s++) {
                        if (inBounds(px[s], py[s], width, height)
                                || inBounds(px[s + 1], py[s + 1], width, height)) {
                            g2d.drawLine(px[s], py[s], px[s + 1], py[s + 1]);
                        }
                    }
                }
            }

            // Draw vertical grid lines (fixed xn, y sweeps from -1 to 1)
            for (int li = 0; li < gridSize; li++) {
                double xn = positions[li];
                boolean isCenterV = (li == gridSize / 2);
                g2d.setColor(isCenterV ? GRID_CENTER_COLOR : GRID_COLOR_V);

                int[] px = new int[GRID_STEPS + 1];
                int[] py = new int[GRID_STEPS + 1];
                boolean valid = false;
                for (int s = 0; s <= GRID_STEPS; s++) {
                    double yn = -1.0 + 2.0 * s / GRID_STEPS;
                    double r2 = xn * xn + yn * yn;
                    double factor = 1.0 + k1 * r2;
                    double xd = xn * factor;
                    double yd = yn * factor;
                    px[s] = (int) Math.round(cx + xd * cx);
                    py[s] = (int) Math.round(cy + yd * cy);
                    valid = true;
                }
                if (valid) {
                    for (int s = 0; s < GRID_STEPS; s++) {
                        if (inBounds(px[s], py[s], width, height)
                                || inBounds(px[s + 1], py[s + 1], width, height)) {
                            g2d.drawLine(px[s], py[s], px[s + 1], py[s + 1]);
                        }
                    }
                }
            }
        } finally {
            g2d.dispose();
        }
    }

    private boolean inBounds(int px, int py, int width, int height) {
        return px >= 0 && px < width && py >= 0 && py < height;
    }

    // ── Hough peak extraction ─────────────────────────────────────────────────

    private List<Integer> extractPeaks(int[] accum, int minVotes, int mergeRadius) {
        int n = accum.length;
        List<Integer> peaks = new ArrayList<>();
        for (int i = mergeRadius; i < n - mergeRadius; i++) {
            if (accum[i] < minVotes) continue;
            boolean isMax = true;
            for (int d = -mergeRadius; d <= mergeRadius; d++) {
                if (d != 0 && accum[i + d] >= accum[i]) {
                    isMax = false;
                    break;
                }
            }
            if (isMax) peaks.add(i);
        }
        return peaks;
    }

    // ── Segment extraction ────────────────────────────────────────────────────

    /** Extracts horizontal edge segments in the band [y0-2, y0+2], min length {@code minSpan}. */
    private List<int[]> extractHorizSegments(boolean[] isEdge, int width, int height,
                                              int y0, int minSpan) {
        List<int[]> segments = new ArrayList<>();
        int yMin = Math.max(1, y0 - 2);
        int yMax = Math.min(height - 2, y0 + 2);

        // Build a presence map: x → has edge in band
        boolean[] present = new boolean[width];
        for (int y = yMin; y <= yMax; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (isEdge[y * width + x]) present[x] = true;
            }
        }
        // Find connected runs (gap tolerance: 3 pixels)
        int runStart = -1;
        int gap = 0;
        for (int x = 1; x < width; x++) {
            if (present[x]) {
                if (runStart < 0) runStart = x;
                gap = 0;
            } else {
                gap++;
                if (gap > 3 && runStart >= 0) {
                    int runEnd = x - gap;
                    if (runEnd - runStart >= minSpan) {
                        segments.add(new int[]{runStart, runEnd});
                    }
                    runStart = -1;
                    gap = 0;
                }
            }
        }
        if (runStart >= 0) {
            int runEnd = width - 2;
            if (runEnd - runStart >= minSpan) {
                segments.add(new int[]{runStart, runEnd});
            }
        }
        return segments;
    }

    /** Extracts vertical edge segments in the band [x0-2, x0+2], min length {@code minSpan}. */
    private List<int[]> extractVertSegments(boolean[] isEdge, int width, int height,
                                             int x0, int minSpan) {
        List<int[]> segments = new ArrayList<>();
        int xMin = Math.max(1, x0 - 2);
        int xMax = Math.min(width - 2, x0 + 2);

        boolean[] present = new boolean[height];
        for (int x = xMin; x <= xMax; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (isEdge[y * width + x]) present[y] = true;
            }
        }
        int runStart = -1;
        int gap = 0;
        for (int y = 1; y < height; y++) {
            if (present[y]) {
                if (runStart < 0) runStart = y;
                gap = 0;
            } else {
                gap++;
                if (gap > 3 && runStart >= 0) {
                    int runEnd = y - gap;
                    if (runEnd - runStart >= minSpan) {
                        segments.add(new int[]{runStart, runEnd});
                    }
                    runStart = -1;
                    gap = 0;
                }
            }
        }
        if (runStart >= 0) {
            int runEnd = height - 2;
            if (runEnd - runStart >= minSpan) {
                segments.add(new int[]{runStart, runEnd});
            }
        }
        return segments;
    }

    // ── Sagitta measurement ───────────────────────────────────────────────────

    /**
     * Measures the midpoint bow (sagitta) of a horizontal edge segment.
     * Divides [x1,x2] into three equal thirds; takes the median transverse (y) position
     * of edge pixels in each third; sagitta = middleMedianY − (leftMedianY + rightMedianY)/2.
     * Positive sagitta = midpoint bows downward.
     */
    private double measureHorizSagitta(boolean[] isEdge, int width, int y0, int x1, int x2) {
        int yMin = Math.max(1, y0 - 2);
        int yMax = Math.min((isEdge.length / width) - 2, y0 + 2);

        int third = (x2 - x1) / 3;
        if (third < 5) return Double.NaN;

        double leftMedian = horizMedianY(isEdge, width, yMin, yMax, x1, x1 + third);
        double midMedian = horizMedianY(isEdge, width, yMin, yMax, x1 + third, x2 - third);
        double rightMedian = horizMedianY(isEdge, width, yMin, yMax, x2 - third, x2);

        if (Double.isNaN(leftMedian) || Double.isNaN(midMedian) || Double.isNaN(rightMedian)) {
            return Double.NaN;
        }
        return midMedian - (leftMedian + rightMedian) / 2.0;
    }

    /** Measures sagitta of a vertical edge segment (horizontal axis becomes y). */
    private double measureVertSagitta(boolean[] isEdge, int width, int x0, int y1, int y2) {
        int height = isEdge.length / width;
        int xMin = Math.max(1, x0 - 2);
        int xMax = Math.min(width - 2, x0 + 2);

        int third = (y2 - y1) / 3;
        if (third < 5) return Double.NaN;

        double topMedian = vertMedianX(isEdge, width, height, xMin, xMax, y1, y1 + third);
        double midMedian = vertMedianX(isEdge, width, height, xMin, xMax, y1 + third, y2 - third);
        double botMedian = vertMedianX(isEdge, width, height, xMin, xMax, y2 - third, y2);

        if (Double.isNaN(topMedian) || Double.isNaN(midMedian) || Double.isNaN(botMedian)) {
            return Double.NaN;
        }
        return midMedian - (topMedian + botMedian) / 2.0;
    }

    private double horizMedianY(boolean[] isEdge, int width, int yMin, int yMax,
                                 int xStart, int xEnd) {
        List<Integer> ys = new ArrayList<>();
        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yMin; y <= yMax; y++) {
                if (isEdge[y * width + x]) ys.add(y);
            }
        }
        if (ys.isEmpty()) return Double.NaN;
        Collections.sort(ys);
        return ys.get(ys.size() / 2);
    }

    private double vertMedianX(boolean[] isEdge, int width, int height, int xMin, int xMax,
                                int yStart, int yEnd) {
        List<Integer> xs = new ArrayList<>();
        for (int y = yStart; y <= yEnd; y++) {
            for (int x = xMin; x <= xMax; x++) {
                if (isEdge[y * width + x]) xs.add(x);
            }
        }
        if (xs.isEmpty()) return Double.NaN;
        Collections.sort(xs);
        return xs.get(xs.size() / 2);
    }

    // ── Text-line builders ────────────────────────────────────────────────────

    private String[] buildLines(DistortionData d) {
        if (d.noEdges || d.linesDetected == 0) {
            return new String[]{
                    "DISTORTION", "",
                    "No lines detected.",
                    "(Lower the edge threshold",
                    " or use an image with",
                    " straight edges.)"
            };
        }
        if (d.qualifyingSegments == 0) {
            return new String[]{
                    "DISTORTION", "",
                    String.format("Lines: %d (H:%d V:%d)",
                            d.linesDetected, d.horizLineCount, d.vertLineCount),
                    "No qualifying segments.",
                    "(Lines too short or",
                    " too close to centre.)"
            };
        }

        String typeStr = Double.isNaN(d.distortionPct) ? "N/A"
                : d.distortionPct < -0.05 ? String.format("%.2f%% — Barrel", d.distortionPct)
                : d.distortionPct > 0.05 ? String.format("+%.2f%% — Pincushion", d.distortionPct)
                : String.format("%.2f%% — None", d.distortionPct);

        return new String[]{
                "DISTORTION",
                "",
                String.format("Lines: %d (H:%d V:%d)",
                        d.linesDetected, d.horizLineCount, d.vertLineCount),
                String.format("Segments: %d", d.qualifyingSegments),
                "",
                String.format("Distortion: %s", typeStr),
                String.format("k\u2081: %+.5f", d.k1)
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int luma(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
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

    // ── Description ───────────────────────────────────────────────────────────

    @Override
    public String getDescription() {
        return "Measures barrel and pincushion lens distortion by detecting and analysing " +
                "the curvature of dominant straight edges in the image.\n\n" +
                "Algorithm: a Sobel gradient identifies high-contrast edges; a row/column Hough " +
                "accumulator finds dominant horizontal and vertical lines; for each qualifying line " +
                "segment the midpoint deviation from a straight chord (sagitta) is measured using " +
                "the median transverse positions of three equal thirds of the segment.\n\n" +
                "The TV-standard distortion % is reported: negative = barrel (lines bow outward), " +
                "positive = pincushion (lines bow inward).  A radial distortion coefficient k\u2081 " +
                "is estimated from the measured sagittae.\n\n" +
                "The overlay shows a deformed grid with the estimated k\u2081 applied, making the " +
                "distortion field immediately visible.  Straight world lines appear curved in the " +
                "overlay according to how they would look through this lens.\n\n" +
                "\u2022 Edge threshold \u2014 Sobel magnitude (0\u2013255 normalised) for edge " +
                "classification.  Lower values detect softer edges; raise it on noisy images.\n" +
                "\u2022 Grid size \u2014 number of lines in each axis of the deformed-grid overlay " +
                "(3\u201311, odd values only for a centre reference line).\n\n" +
                "Distortion matters most in architecture, real estate, and product photography, where " +
                "straight lines that appear curved betray the lens. Wide-angle and kit zooms typically " +
                "show barrel distortion (lines bowing outward) especially at their shortest focal " +
                "length; telephoto zooms tend toward pincushion (lines bowing inward). This filter " +
                "lets you compare two lenses side by side and see both the numeric percentage and the " +
                "visual deformed-grid overlay that shows what correction would look like. " +
                "Best test images contain many real-world straight lines that extend close to the " +
                "frame edges: building façades, corridors, bookshelves, tiled floors, or window frames. " +
                "The more edge-to-edge straight lines the image contains, the more data points the " +
                "Hough accumulator can use and the more reliable the distortion estimate.";
    }

    // ── Data snapshot ─────────────────────────────────────────────────────────

    static final class DistortionData {
        final boolean noEdges;
        final double distortionPct;
        final double k1;
        final int linesDetected;
        final int qualifyingSegments;
        final int horizLineCount;
        final int vertLineCount;

        static DistortionData noEdges() {
            return new DistortionData(true, Double.NaN, 0.0, 0, 0, 0, 0);
        }

        DistortionData(double distortionPct, double k1, int linesDetected,
                       int qualifyingSegments, int horizLineCount, int vertLineCount) {
            this(false, distortionPct, k1, linesDetected, qualifyingSegments,
                    horizLineCount, vertLineCount);
        }

        private DistortionData(boolean noEdges, double distortionPct, double k1,
                               int linesDetected, int qualifyingSegments,
                               int horizLineCount, int vertLineCount) {
            this.noEdges = noEdges;
            this.distortionPct = distortionPct;
            this.k1 = k1;
            this.linesDetected = linesDetected;
            this.qualifyingSegments = qualifyingSegments;
            this.horizLineCount = horizLineCount;
            this.vertLineCount = vertLineCount;
        }
    }
}
