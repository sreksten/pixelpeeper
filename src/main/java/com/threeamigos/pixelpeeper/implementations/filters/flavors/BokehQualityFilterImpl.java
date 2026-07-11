package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.BokehQualityFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.BokehQualityFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Estimates out-of-focus region smoothness and bokeh highlight quality as a composite
 * bokeh quality score.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li><strong>Pass 1 — Patch sharpness</strong> (parallel patch-strip): the image is divided
 *       into non-overlapping {@code patchSize×patchSize} patches.  The Laplacian variance
 *       of luminance is computed for each patch; patches above the sharpness threshold are
 *       classified as <em>in-focus</em>, the rest as <em>out-of-focus (OOF)</em>.</li>
 *   <li><strong>Pass 2 — OOF smoothness</strong> (parallel patch-strip): for each OOF patch,
 *       the mean horizontal Sobel magnitude is computed.  Low values indicate smooth/creamy
 *       bokeh; high values indicate busy or nervous backgrounds.</li>
 *   <li><strong>Pass 3 — Bokeh highlight detection</strong> (serial BFS): pixels that are both
 *       in an OOF patch and have luminance ≥ 215 are flood-filled to find connected highlight
 *       blobs.  Blobs smaller than 30 or larger than 8 000 pixels are discarded.  Up to
 *       {@value #MAX_HIGHLIGHTS} highlights are analysed.</li>
 *   <li><strong>Pass 4 — Highlight analysis</strong> (serial): for each highlight blob:
 *       <ul>
 *         <li><em>Circularity</em> = 4π·area / perimeter² ∈ [0,1] — 1.0 = perfect circle.</li>
 *         <li><em>Aspect ratio</em> = bbox width / bbox height — deviates from 1.0 for
 *             cat's-eye (elongated) highlights; the corner position of the centroid is also
 *             recorded.</li>
 *         <li><em>Interior uniformity</em> = 1 − (luminance σ / 50) — measures how smooth the
 *             fill is; low uniformity indicates onion-ring artefacts.</li>
 *       </ul></li>
 *   <li><strong>Pass 5 — Overlay rendering</strong> (parallel column-strip): in-focus patches
 *       receive a semi-transparent green tint; OOF patches are coloured blue (smooth) to red
 *       (busy); bokeh highlight borders are drawn in yellow (circular) or orange (cat's-eye).</li>
 * </ol>
 *
 * <h3>Bokeh Quality Score (0–10)</h3>
 * <ul>
 *   <li>OOF smoothness (weight 35%): {@code clamp(1 − meanOofGradient / 50, 0, 1)}</li>
 *   <li>Highlight circularity (weight 40%): mean circularity of detected highlights;
 *       omitted when no highlights are found.</li>
 *   <li>Interior uniformity (weight 25%): {@code clamp(1 − meanInteriorStd / 50, 0, 1)};
 *       omitted when no highlights are found.</li>
 * </ul>
 *
 * <p>When no highlights are detected the score is based solely on OOF smoothness.</p>
 *
 * <p>The viewport overlay is painted in the bottom-right corner of each image slice.</p>
 *
 * @author Stefano Reksten
 */
public class BokehQualityFilterImpl implements BokehQualityFilter, ViewportOverlayPainter {

    // ── Highlight detection constants ─────────────────────────────────────────
    /** Luminance threshold for a pixel to be considered a specular highlight. */
    private static final int BRIGHT_THRESHOLD = 215;
    /** Minimum blob area (pixels) to be considered a bokeh highlight. */
    private static final int MIN_HIGHLIGHT_SIZE = 30;
    /** Maximum blob area (pixels) — larger blobs are background, not highlights. */
    private static final int MAX_HIGHLIGHT_SIZE = 8_000;
    /** Maximum number of highlights to analyse (caps BFS time on image-wide bright areas). */
    private static final int MAX_HIGHLIGHTS = 25;
    /**
     * Normalised distance from the image centre beyond which a highlight centroid is
     * considered to be in the corner region (for cat's-eye detection).
     */
    private static final double CORNER_THRESHOLD = 0.55;
    /** Circularity below which a corner highlight is flagged as cat's-eye. */
    private static final double CAT_EYE_CIRCULARITY = 0.72;
    /** Interior luminance std-dev above which a highlight is flagged as onion-ring. */
    private static final double ONION_RING_STD = 28.0;

    // ── Scoring normalisation constants ───────────────────────────────────────
    private static final double SMOOTHNESS_NORM = 50.0; // gradient → score=0 at this value
    private static final double UNIFORMITY_NORM = 50.0; // interior std → score=0 at this value

    // ── Overlay geometry (screen pixels) ─────────────────────────────────────
    private static final int PAD = 8;
    private static final int LINE_H = 15;
    private static final int OVERLAY_W = 230;

    // ── Overlay colours ───────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color LABEL_COLOR = new Color(140, 140, 140);
    private static final Color VALUE_COLOR = new Color(240, 240, 100);
    private static final Color SCORE_HIGH_COLOR = new Color(80, 230, 80);
    private static final Color SCORE_MID_COLOR = new Color(230, 200, 60);
    private static final Color SCORE_LOW_COLOR = new Color(230, 80, 60);
    private static final Color SEPARATOR_COLOR = new Color(70, 70, 70);
    private static final Color FLAG_COLOR = new Color(255, 140, 60);

    // ── Pixel overlay colours ─────────────────────────────────────────────────
    private static final int ALPHA_INFOCUS = 70;
    private static final int ALPHA_OOF = 130;
    private static final int ALPHA_HIGHLIGHT_BORDER = 220;

    private final BokehQualityFilterPreferences preferences;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    /** Computed snapshot, written by background thread, read by EDT. */
    private volatile BokehData bokehData;

    public BokehQualityFilterImpl(BokehQualityFilterPreferences preferences) {
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
        bokehData = null;
        filteredImage = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int patchSize = preferences.getPatchSize();
        int sharpnessThreshold = preferences.getSharpnessThreshold();

        int gridW = (width + patchSize - 1) / patchSize;
        int gridH = (height + patchSize - 1) / patchSize;
        int totalPatches = gridW * gridH;

        double[] patchLapVariance = new double[totalPatches]; // Laplacian variance
        boolean[] patchIsOof = new boolean[totalPatches];
        double[] patchOofGradient = new double[totalPatches]; // mean gradient in OOF patches

        int processors = Runtime.getRuntime().availableProcessors();
        int patchesPerProc = (totalPatches + processors - 1) / processors;
        Thread[] threads = new Thread[processors];

        // ── Pass 1: Laplacian variance per patch (parallel) ───────────────────
        for (int t = 0; t < processors; t++) {
            final int startP = t * patchesPerProc;
            final int endP = Math.min(startP + patchesPerProc, totalPatches);
            threads[t] = new Thread(() -> {
                for (int p = startP; p < endP && !isAborted; p++) {
                    int row = p / gridW;
                    int col = p % gridW;
                    int x0 = col * patchSize;
                    int y0 = row * patchSize;
                    int x1 = Math.min(x0 + patchSize, width);
                    int y1 = Math.min(y0 + patchSize, height);

                    // Laplacian variance — only interior pixels have valid 4-neighbours
                    int x0i = Math.max(x0, 1);
                    int y0i = Math.max(y0, 1);
                    int x1i = Math.min(x1, width - 1);
                    int y1i = Math.min(y1, height - 1);
                    if (x1i <= x0i || y1i <= y0i) continue;

                    long sumL = 0, sumL2 = 0;
                    int count = 0;
                    for (int y = y0i; y < y1i; y++) {
                        for (int x = x0i; x < x1i; x++) {
                            int lap = 4 * luma(sourceImage.getRGB(x, y))
                                    - luma(sourceImage.getRGB(x - 1, y))
                                    - luma(sourceImage.getRGB(x + 1, y))
                                    - luma(sourceImage.getRGB(x, y - 1))
                                    - luma(sourceImage.getRGB(x, y + 1));
                            sumL += lap;
                            sumL2 += (long) lap * lap;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double mean = (double) sumL / count;
                        patchLapVariance[p] = Math.max(0, (double) sumL2 / count - mean * mean);
                    }
                }
            });
            threads[t].start();
        }
        joinAll(threads);
        if (isAborted) return;

        // Classify patches
        int oofCount = 0;
        int inFocusCount = 0;
        for (int p = 0; p < totalPatches; p++) {
            if (patchLapVariance[p] < sharpnessThreshold) {
                patchIsOof[p] = true;
                oofCount++;
            } else {
                inFocusCount++;
            }
        }

        // ── Pass 2: OOF gradient smoothness (parallel) ────────────────────────
        for (int t = 0; t < processors; t++) {
            final int startP = t * patchesPerProc;
            final int endP = Math.min(startP + patchesPerProc, totalPatches);
            threads[t] = new Thread(() -> {
                for (int p = startP; p < endP && !isAborted; p++) {
                    if (!patchIsOof[p]) continue;
                    int row = p / gridW;
                    int col = p % gridW;
                    int x0 = col * patchSize;
                    int y0 = row * patchSize;
                    int x1 = Math.min(x0 + patchSize, width);
                    int y1 = Math.min(y0 + patchSize, height);

                    long sumGrad = 0;
                    int count = 0;
                    for (int y = y0; y < y1; y++) {
                        for (int x = x0; x < x1 - 1; x++) {
                            sumGrad += Math.abs(luma(sourceImage.getRGB(x + 1, y))
                                    - luma(sourceImage.getRGB(x, y)));
                            count++;
                        }
                    }
                    patchOofGradient[p] = count > 0 ? (double) sumGrad / count : 0.0;
                }
            });
            threads[t].start();
        }
        joinAll(threads);
        if (isAborted) return;

        // Aggregate OOF smoothness
        double sumOofGrad = 0;
        int oofGradCount = 0;
        for (int p = 0; p < totalPatches; p++) {
            if (patchIsOof[p]) {
                sumOofGrad += patchOofGradient[p];
                oofGradCount++;
            }
        }
        double meanOofGradient = oofGradCount > 0 ? sumOofGrad / oofGradCount : 0.0;

        // ── Pass 3 & 4: Bokeh highlight BFS + analysis (serial) ───────────────
        boolean[] brightOof = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gx = Math.min(x / patchSize, gridW - 1);
                int gy = Math.min(y / patchSize, gridH - 1);
                if (patchIsOof[gy * gridW + gx] && luma(sourceImage.getRGB(x, y)) >= BRIGHT_THRESHOLD) {
                    brightOof[y * width + x] = true;
                }
            }
        }
        if (isAborted) return;

        List<BokehHighlight> highlights = findHighlights(brightOof, width, height);

        // Compute highlight aggregate statistics
        double sumCircularity = 0;
        double sumInteriorStd = 0;
        boolean hasCatEye = false;
        boolean hasOnionRings = false;

        double cx = width / 2.0;
        double cy = height / 2.0;
        double normRadius = Math.sqrt(cx * cx + cy * cy);

        for (BokehHighlight h : highlights) {
            sumCircularity += h.circularity;
            sumInteriorStd += h.interiorStd;

            // Cat's-eye: low circularity in corner region
            double distFromCenter = Math.sqrt(
                    Math.pow(h.centroidX - cx, 2) + Math.pow(h.centroidY - cy, 2));
            if (distFromCenter / normRadius > CORNER_THRESHOLD && h.circularity < CAT_EYE_CIRCULARITY) {
                hasCatEye = true;
            }
            if (h.interiorStd > ONION_RING_STD) {
                hasOnionRings = true;
            }
        }

        int n = highlights.size();
        double meanCircularity = n > 0 ? sumCircularity / n : Double.NaN;
        double meanInteriorStd = n > 0 ? sumInteriorStd / n : Double.NaN;

        // ── Score computation ──────────────────────────────────────────────────
        double smoothnessScore = Math.max(0, 1.0 - meanOofGradient / SMOOTHNESS_NORM);
        double bokehScore;
        if (n == 0) {
            bokehScore = 10.0 * smoothnessScore;
        } else {
            double circularityScore = meanCircularity;
            double uniformityScore = Math.max(0, 1.0 - meanInteriorStd / UNIFORMITY_NORM);
            bokehScore = 10.0 * (0.35 * smoothnessScore + 0.40 * circularityScore + 0.25 * uniformityScore);
        }

        // ── Pass 5: Render overlay image (parallel column-strip) ───────────────
        BufferedImage overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Build highlight-border mask
        boolean[] isHighlightBorder = new boolean[width * height];
        boolean[] isHighlightBorderCatEye = new boolean[width * height];
        for (BokehHighlight h : highlights) {
            boolean isCatEye = false;
            double distFromCenter = Math.sqrt(
                    Math.pow(h.centroidX - cx, 2) + Math.pow(h.centroidY - cy, 2));
            if (distFromCenter / normRadius > CORNER_THRESHOLD && h.circularity < CAT_EYE_CIRCULARITY) {
                isCatEye = true;
            }
            for (int idx : h.borderPixels) {
                if (isCatEye) {
                    isHighlightBorderCatEye[idx] = true;
                } else {
                    isHighlightBorder[idx] = true;
                }
            }
        }

        // Compute max OOF gradient for colour normalisation
        double maxOofGrad = 1.0;
        for (int p = 0; p < totalPatches; p++) {
            if (patchIsOof[p] && patchOofGradient[p] > maxOofGrad) {
                maxOofGrad = patchOofGradient[p];
            }
        }
        final double finalMaxOofGrad = maxOofGrad;

        int colsPerProc = (width + processors - 1) / processors;
        for (int t = 0; t < processors; t++) {
            final int startCol = t * colsPerProc;
            final int endCol = Math.min(startCol + colsPerProc, width);
            threads[t] = new Thread(() -> {
                for (int x = startCol; x < endCol && !isAborted; x++) {
                    for (int y = 0; y < height; y++) {
                        int idx = y * width + x;

                        if (isHighlightBorderCatEye[idx]) {
                            // Orange border — cat's-eye highlight
                            overlay.setRGB(x, y, (ALPHA_HIGHLIGHT_BORDER << 24) | 0xFF8C00);
                            continue;
                        }
                        if (isHighlightBorder[idx]) {
                            // Yellow border — circular highlight
                            overlay.setRGB(x, y, (ALPHA_HIGHLIGHT_BORDER << 24) | 0xFFDC00);
                            continue;
                        }

                        int gx = Math.min(x / patchSize, gridW - 1);
                        int gy = Math.min(y / patchSize, gridH - 1);
                        int p = gy * gridW + gx;

                        if (!patchIsOof[p]) {
                            // In-focus: green tint
                            overlay.setRGB(x, y, (ALPHA_INFOCUS << 24) | 0x00CC00);
                        } else {
                            // OOF: blue (smooth) → red (busy)
                            float t2 = finalMaxOofGrad > 0
                                    ? (float) Math.min(patchOofGradient[p] / finalMaxOofGrad, 1.0)
                                    : 0f;
                            float hue = (1f - t2) * (2f / 3f); // blue=2/3 → red=0
                            int rgb24 = Color.HSBtoRGB(hue, 0.85f, 1.0f) & 0x00FFFFFF;
                            overlay.setRGB(x, y, (ALPHA_OOF << 24) | rgb24);
                        }
                    }
                }
            });
            threads[t].start();
        }
        joinAll(threads);
        if (isAborted) return;

        filteredImage = overlay;
        double inFocusPct = totalPatches > 0 ? 100.0 * inFocusCount / totalPatches : 0.0;
        bokehData = new BokehData(inFocusPct, meanOofGradient, smoothnessScore,
                n, meanCircularity, meanInteriorStd,
                hasCatEye, hasOnionRings, bokehScore);
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    // ── Highlight detection (BFS) ─────────────────────────────────────────────

    private List<BokehHighlight> findHighlights(boolean[] brightOof, int width, int height) {
        List<BokehHighlight> results = new ArrayList<>();
        boolean[] visited = new boolean[width * height];

        for (int startIdx = 0; startIdx < width * height && results.size() < MAX_HIGHLIGHTS; startIdx++) {
            if (!brightOof[startIdx] || visited[startIdx]) continue;
            visited[startIdx] = true;

            List<Integer> component = new ArrayList<>();
            Deque<Integer> queue = new ArrayDeque<>();
            queue.add(startIdx);
            boolean tooBig = false;

            while (!queue.isEmpty()) {
                if (component.size() >= MAX_HIGHLIGHT_SIZE) {
                    // Drain remaining — mark as visited so they aren't re-processed
                    while (!queue.isEmpty()) {
                        int qi = queue.poll();
                        visited[qi] = true;
                    }
                    tooBig = true;
                    break;
                }
                int idx = queue.poll();
                component.add(idx);
                int x = idx % width;
                int y = idx / width;
                int[] nx = {x - 1, x + 1, x, x};
                int[] ny = {y, y, y - 1, y + 1};
                for (int d = 0; d < 4; d++) {
                    if (nx[d] < 0 || nx[d] >= width || ny[d] < 0 || ny[d] >= height) continue;
                    int nIdx = ny[d] * width + nx[d];
                    if (!brightOof[nIdx] || visited[nIdx]) continue;
                    visited[nIdx] = true;
                    queue.add(nIdx);
                }
            }

            if (tooBig || component.size() < MIN_HIGHLIGHT_SIZE) continue;

            results.add(analyzeHighlight(component, brightOof, width, height));
        }
        return results;
    }

    private BokehHighlight analyzeHighlight(List<Integer> component, boolean[] brightOof,
                                             int width, int height) {
        int area = component.size();
        int minX = Integer.MAX_VALUE, maxX = 0, minY = Integer.MAX_VALUE, maxY = 0;
        long sumX = 0, sumY = 0;
        long sumLuma = 0, sumLuma2 = 0;

        // Build a set for O(1) membership test (for perimeter computation)
        Set<Integer> compSet = new HashSet<>(area * 2);
        for (int idx : component) compSet.add(idx);

        for (int idx : component) {
            int x = idx % width;
            int y = idx / width;
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
            sumX += x;
            sumY += y;
            int l = luma(sourceImage.getRGB(x, y));
            sumLuma += l;
            sumLuma2 += (long) l * l;
        }

        double centroidX = (double) sumX / area;
        double centroidY = (double) sumY / area;
        double meanLuma = (double) sumLuma / area;
        double varLuma = Math.max(0, (double) sumLuma2 / area - meanLuma * meanLuma);
        double interiorStd = Math.sqrt(varLuma);

        // Perimeter: border pixels = pixels with at least one 4-neighbour NOT in component
        List<Integer> borderPixels = new ArrayList<>();
        int perimeter = 0;
        for (int idx : component) {
            int x = idx % width;
            int y = idx / width;
            boolean isBorder = false;
            if (x == 0 || !compSet.contains(y * width + (x - 1))) isBorder = true;
            if (x == width - 1 || !compSet.contains(y * width + (x + 1))) isBorder = true;
            if (y == 0 || !compSet.contains((y - 1) * width + x)) isBorder = true;
            if (y == height - 1 || !compSet.contains((y + 1) * width + x)) isBorder = true;
            if (isBorder) {
                perimeter++;
                borderPixels.add(idx);
            }
        }

        double circularity = 0.0;
        if (perimeter > 0) {
            circularity = Math.min(1.0, 4.0 * Math.PI * area / ((double) perimeter * perimeter));
        }

        double aspectRatio = (maxY > minY) ? (double) (maxX - minX) / (maxY - minY) : 1.0;

        return new BokehHighlight(centroidX, centroidY, area, circularity, aspectRatio,
                interiorStd, borderPixels);
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        BokehData data = bokehData;
        if (data == null) return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String[] lines = buildLines(data);
        int totalLines = lines.length;
        int overlayH = PAD + totalLines * LINE_H + PAD;

        // Position: bottom-right
        int ox = x + width - OVERLAY_W - PAD;
        int oy = y + height - overlayH - PAD;
        if (ox < x) ox = x;
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
                Color valueCol = resolveColor(data, lines[i]);
                int colon = lines[i].indexOf(':');
                if (colon > 0) {
                    String label = lines[i].substring(0, colon + 1);
                    String val = lines[i].substring(colon + 1);
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(label, textX, curY);
                    int labelW = fm.stringWidth(label);
                    g2d.setColor(valueCol);
                    g2d.drawString(val, textX + labelW, curY);
                } else {
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(lines[i], textX, curY);
                }
            }
            curY += LINE_H;
        }
    }

    private Color resolveColor(BokehData d, String line) {
        if (line.startsWith("Score")) {
            if (d.bokehScore >= 7.5) return SCORE_HIGH_COLOR;
            if (d.bokehScore >= 4.5) return SCORE_MID_COLOR;
            return SCORE_LOW_COLOR;
        }
        if (line.contains("Yes") && (line.startsWith("Cat") || line.startsWith("Onion"))) {
            return FLAG_COLOR;
        }
        return VALUE_COLOR;
    }

    private String[] buildLines(BokehData d) {
        List<String> list = new ArrayList<>();
        list.add("BOKEH QUALITY");
        list.add("");
        list.add(String.format("In-focus:  %.1f%%", d.inFocusPct));
        list.add(String.format("OOF grad:  %.2f", d.meanOofGradient));
        list.add(String.format("Smoothness: %.2f", d.smoothnessScore));
        list.add("");
        if (d.highlightCount == 0) {
            list.add("Highlights: none");
        } else {
            list.add(String.format("Highlights: %d", d.highlightCount));
            list.add(String.format("Circularity: %.3f", d.meanCircularity));
            list.add(String.format("Interior \u03c3: %.1f", d.meanInteriorStd));
            list.add(String.format("Cat's-eye: %s", d.hasCatEye ? "Yes" : "No"));
            list.add(String.format("Onion rings: %s", d.hasOnionRings ? "Yes" : "No"));
        }
        list.add("");
        list.add(String.format("Score: %.1f / 10", d.bokehScore));
        return list.toArray(new String[0]);
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
        return "Estimates out-of-focus region smoothness and specular highlight shape quality " +
                "as a composite bokeh quality score (0\u201310).\n\n" +
                "The image is divided into patches; those with Laplacian variance above the " +
                "Sharpness Threshold are classified as in-focus (green overlay), the rest as " +
                "out-of-focus (blue = smooth \u2192 red = busy, based on mean gradient).\n\n" +
                "Bright isolated blobs (\u2265215 luminance) within OOF patches are identified as " +
                "specular highlights and analysed:\n" +
                "\u2022 Circularity = 4\u03c0\u00b7area/perimeter\u00b2 (1.0 = perfect circle, outlined in yellow).\n" +
                "\u2022 Cat's-eye highlights (low circularity at corners) are outlined in orange.\n" +
                "\u2022 Onion-ring artefacts are flagged when the interior luminance standard deviation " +
                "exceeds " + (int) ONION_RING_STD + ".\n\n" +
                "Score = 10 \u00d7 (0.35\u00d7smoothness + 0.40\u00d7circularity + 0.25\u00d7uniformity); " +
                "only smoothness counts when no highlights are detected.\n\n" +
                "\u2022 Sharpness Threshold \u2014 Laplacian variance above which a patch is classified as " +
                "in-focus.  Increase for very sharp images; decrease for soft subjects.\n" +
                "\u2022 Patch Size \u2014 side length of each analysis patch (8\u201348 px).  Smaller patches " +
                "capture finer focus boundaries; larger patches are more stable on noisy images.\n\n" +
                "Bokeh quality is one of the most subjective yet commercially important lens characteristics: " +
                "a lens that produces smooth, circular out-of-focus highlights commands a significant price " +
                "premium over one that renders busy, nervous, or cat's-eye-shaped blur. This filter " +
                "quantifies that difference so you can compare portrait lenses objectively — for example " +
                "a vintage 50\u202fmm f/1.4 with busy bokeh against a modern 85\u202fmm f/1.8 with smoother rendering. " +
                "Best test images are portraits or still-life shots with a clearly separated in-focus " +
                "subject and an out-of-focus background containing small light sources or reflections " +
                "— candles, fairy lights, sunlight glinting off water or foliage, or any scene shot " +
                "wide open against a background of small bright points. Flat, uniformly blurred backgrounds " +
                "without specular highlights will only exercise the smoothness component of the score.";
    }

    // ── Data containers ───────────────────────────────────────────────────────

    private static final class BokehHighlight {
        final double centroidX, centroidY;
        final int area;
        final double circularity;
        final double aspectRatio;
        final double interiorStd;
        final List<Integer> borderPixels;

        BokehHighlight(double centroidX, double centroidY, int area,
                       double circularity, double aspectRatio, double interiorStd,
                       List<Integer> borderPixels) {
            this.centroidX = centroidX;
            this.centroidY = centroidY;
            this.area = area;
            this.circularity = circularity;
            this.aspectRatio = aspectRatio;
            this.interiorStd = interiorStd;
            this.borderPixels = borderPixels;
        }
    }

    static final class BokehData {
        final double inFocusPct;
        final double meanOofGradient;
        final double smoothnessScore;
        final int highlightCount;
        final double meanCircularity;
        final double meanInteriorStd;
        final boolean hasCatEye;
        final boolean hasOnionRings;
        final double bokehScore;

        BokehData(double inFocusPct, double meanOofGradient, double smoothnessScore,
                  int highlightCount, double meanCircularity, double meanInteriorStd,
                  boolean hasCatEye, boolean hasOnionRings, double bokehScore) {
            this.inFocusPct = inFocusPct;
            this.meanOofGradient = meanOofGradient;
            this.smoothnessScore = smoothnessScore;
            this.highlightCount = highlightCount;
            this.meanCircularity = meanCircularity;
            this.meanInteriorStd = meanInteriorStd;
            this.hasCatEye = hasCatEye;
            this.hasOnionRings = hasOnionRings;
            this.bokehScore = bokehScore;
        }
    }
}
