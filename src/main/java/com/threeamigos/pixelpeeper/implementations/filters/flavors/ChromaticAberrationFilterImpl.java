package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.ChromaticAberrationFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ChromaticAberrationFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Detects lateral (transverse) chromatic aberration by measuring R/G and B/G channel
 * misalignment at high-contrast luminance edges.
 *
 * <h3>Algorithm</h3>
 * <ol>
 *   <li><strong>Pass 1 — Sobel gradient</strong>: compute luminance Sobel gradient magnitude
 *       for every interior pixel in parallel (column-strip).  Pixels whose normalised magnitude
 *       exceeds the configured <em>edge threshold</em> are marked as high-contrast edges.</li>
 *   <li><strong>Pass 2 — Fringe map</strong>: at each edge pixel, sample the raw R, G, B channels
 *       and compute |R−G| and |B−G|.  The maximum of the two is the <em>fringe level</em> for
 *       that pixel.  Each edge pixel is coloured in the output overlay from blue (low fringe) to
 *       red (high fringe) using HSB colour; non-edge pixels are left fully transparent.</li>
 *   <li><strong>Stats accumulation</strong>: mean |R−G| and |B−G| are collected globally and
 *       separately for the outer 30% corner band, to show how CA varies across the frame.</li>
 * </ol>
 *
 * <p>A {@link ViewportOverlayPainter} overlay is produced simultaneously, showing the numeric
 * scores in the bottom-right corner of each image viewport.</p>
 *
 * <p>The overlay image uses {@code TYPE_INT_ARGB}.  Transparent pixels let the original image
 * show through at full opacity; coloured edge pixels are semi-opaque so they can be further
 * blended with the transparency slider.</p>
 *
 * @author Stefano Reksten
 */
public class ChromaticAberrationFilterImpl implements ChromaticAberrationFilter, ViewportOverlayPainter {

    // ── Overlay geometry ──────────────────────────────────────────────────────
    private static final int PAD = 8;
    private static final int LINE_H = 15;
    private static final int OVERLAY_W = 230;

    // ── Overlay colours ───────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color LABEL_COLOR = new Color(140, 140, 140);
    private static final Color VALUE_COLOR = new Color(240, 240, 100);
    private static final Color CORNER_COLOR = new Color(100, 200, 255);
    private static final Color SEPARATOR_COLOR = new Color(70, 70, 70);

    // ── Fringe overlay: hue range blue → red (= 2/3 → 0 in HSB) ─────────────
    private static final float HUE_LOW = 2f / 3f;   // blue  — low fringe
    private static final float HUE_HIGH = 0f;         // red   — high fringe
    private static final int FRINGE_SCALE = 40;       // |chan diff| mapped to full red at this value
    private static final int OVERLAY_ALPHA_FRINGE = 200;
    private static final int OVERLAY_ALPHA_EDGE = 80; // edge but fringe < sensitivity

    private final ChromaticAberrationFilterPreferences preferences;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    /** Computed statistics, written by background thread, read by EDT. */
    private volatile CaData caData;

    public ChromaticAberrationFilterImpl(ChromaticAberrationFilterPreferences preferences) {
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
        caData = null;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int edgeThreshold = preferences.getEdgeThreshold();
        int sensitivity = preferences.getSensitivity();

        filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // ── Pass 1: Sobel gradient magnitude ─────────────────────────────────
        // Normalise to 0–255 across the full image so edgeThreshold is on a
        // stable scale regardless of image content.
        int[] gradient = new int[width * height]; // raw Sobel magnitude
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
                        int lum00 = luma(sourceImage.getRGB(x - 1, y - 1));
                        int lum01 = luma(sourceImage.getRGB(x - 1, y));
                        int lum02 = luma(sourceImage.getRGB(x - 1, y + 1));
                        int lum10 = luma(sourceImage.getRGB(x, y - 1));
                        int lum12 = luma(sourceImage.getRGB(x, y + 1));
                        int lum20 = luma(sourceImage.getRGB(x + 1, y - 1));
                        int lum21 = luma(sourceImage.getRGB(x + 1, y));
                        int lum22 = luma(sourceImage.getRGB(x + 1, y + 1));

                        int gx = -lum00 + lum02 - 2 * lum01 + 2 * lum12 - lum20 + lum22;
                        int gy = -lum00 - 2 * lum10 - lum20 + lum02 + 2 * lum12 + lum22;
                        int mag = (int) Math.sqrt((double) gx * gx + (double) gy * gy);
                        gradient[y * width + x] = mag;
                        if (mag > localMax) {
                            localMax = mag;
                        }
                    }
                }
                localMaxima[ti] = localMax;
            });
            threads[t].start();
        }
        joinAll(threads);

        if (isAborted) {
            filteredImage = null;
            return;
        }

        int globalMax = 0;
        for (int m : localMaxima) {
            if (m > globalMax) globalMax = m;
        }
        if (globalMax == 0) {
            caData = CaData.noEdges();
            return;
        }

        // Normalise threshold: user picks 0–150 on a 0–255 normalised scale
        final double scale = 255.0 / globalMax;
        final int normThreshold = edgeThreshold; // already on 0–255 scale

        // ── Pass 2: fringe map + statistics ───────────────────────────────────
        // Corner band: outer 30 % of each half-axis
        int cx = width / 2;
        int cy = height / 2;
        int cornerBandX = (int) (cx * 0.70);
        int cornerBandY = (int) (cy * 0.70);

        // Atomic accumulators for thread-safe statistics
        AtomicLong sumRgAll = new AtomicLong();
        AtomicLong sumBgAll = new AtomicLong();
        AtomicLong countEdgeAll = new AtomicLong();
        AtomicLong countFringeAll = new AtomicLong();
        AtomicLong sumRgCorner = new AtomicLong();
        AtomicLong sumBgCorner = new AtomicLong();
        AtomicLong countEdgeCorner = new AtomicLong();

        for (int t = 0; t < processors; t++) {
            final int startCol = 1 + t * colsPerProc;
            final int endCol = Math.min(startCol + colsPerProc, width - 1);
            threads[t] = new Thread(() -> {
                long lSumRgAll = 0, lSumBgAll = 0, lEdgeAll = 0, lFringeAll = 0;
                long lSumRgC = 0, lSumBgC = 0, lEdgeC = 0;

                for (int x = startCol; x < endCol && !isAborted; x++) {
                    for (int y = 1; y < height - 1; y++) {
                        int normMag = (int) (gradient[y * width + x] * scale);
                        if (normMag < normThreshold) {
                            continue; // not an edge
                        }

                        int rgb = sourceImage.getRGB(x, y);
                        int r = (rgb >> 16) & 0xff;
                        int g = (rgb >> 8) & 0xff;
                        int b = rgb & 0xff;
                        int rg = Math.abs(r - g);
                        int bg = Math.abs(b - g);
                        int fringeLevel = Math.max(rg, bg);

                        // Stats
                        lSumRgAll += rg;
                        lSumBgAll += bg;
                        lEdgeAll++;
                        if (fringeLevel >= sensitivity) {
                            lFringeAll++;
                        }

                        boolean isCorner = Math.abs(x - cx) > cornerBandX
                                || Math.abs(y - cy) > cornerBandY;
                        if (isCorner) {
                            lSumRgC += rg;
                            lSumBgC += bg;
                            lEdgeC++;
                        }

                        // Colour pixel: hue = blue→red, alpha encodes whether fringe > sensitivity
                        float t_hue = Math.min(fringeLevel, FRINGE_SCALE) / (float) FRINGE_SCALE;
                        float hue = HUE_LOW + t_hue * (HUE_HIGH - HUE_LOW);
                        int rgb24 = Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0x00ffffff;
                        int alpha = (fringeLevel >= sensitivity) ? OVERLAY_ALPHA_FRINGE : OVERLAY_ALPHA_EDGE;
                        filteredImage.setRGB(x, y, (alpha << 24) | rgb24);
                    }
                }

                sumRgAll.addAndGet(lSumRgAll);
                sumBgAll.addAndGet(lSumBgAll);
                countEdgeAll.addAndGet(lEdgeAll);
                countFringeAll.addAndGet(lFringeAll);
                sumRgCorner.addAndGet(lSumRgC);
                sumBgCorner.addAndGet(lSumBgC);
                countEdgeCorner.addAndGet(lEdgeC);
            });
            threads[t].start();
        }
        joinAll(threads);

        if (isAborted) {
            filteredImage = null;
            return;
        }

        long edgeAll = countEdgeAll.get();
        long edgeCorner = countEdgeCorner.get();

        double meanRgAll = edgeAll > 0 ? sumRgAll.get() / (double) edgeAll : 0.0;
        double meanBgAll = edgeAll > 0 ? sumBgAll.get() / (double) edgeAll : 0.0;
        double meanRgCorner = edgeCorner > 0 ? sumRgCorner.get() / (double) edgeCorner : 0.0;
        double meanBgCorner = edgeCorner > 0 ? sumBgCorner.get() / (double) edgeCorner : 0.0;
        double fringePct = edgeAll > 0
                ? 100.0 * countFringeAll.get() / (double) edgeAll : 0.0;

        caData = new CaData(meanRgAll, meanBgAll, meanRgCorner, meanBgCorner,
                edgeAll, fringePct);
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
        CaData data = caData;
        if (data == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String[] lines = buildLines(data);
        boolean[] isSeparator = buildSeparatorFlags(lines);
        boolean[] isCorner = buildCornerFlags(lines);

        int totalLines = lines.length;
        int overlayH = PAD + totalLines * LINE_H + PAD;

        // Position: bottom-right corner
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
            if (isSeparator[i]) {
                g2d.setColor(SEPARATOR_COLOR);
                g2d.drawLine(textX, curY - fm.getAscent() / 2,
                        ox + OVERLAY_W - PAD, curY - fm.getAscent() / 2);
            } else if (i == 0) {
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2d.setColor(TITLE_COLOR);
                g2d.drawString(lines[i], textX, curY);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            } else {
                Color valueCol = isCorner[i] ? CORNER_COLOR : VALUE_COLOR;
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

    // ── Text builders ─────────────────────────────────────────────────────────

    private String[] buildLines(CaData d) {
        if (d.noEdges) {
            return new String[]{
                    "CHROMATIC ABERRATION", "",
                    "No high-contrast edges found.",
                    "(Lower the edge threshold.)"
            };
        }

        return new String[]{
                "CHROMATIC ABERRATION",
                "",
                "Whole frame:",
                String.format("  |R\u2212G|: %.2f", d.meanRgAll),
                String.format("  |B\u2212G|: %.2f", d.meanBgAll),
                "",
                "Corner band:",
                String.format("  |R\u2212G|: %.2f", d.meanRgCorner),
                String.format("  |B\u2212G|: %.2f", d.meanBgCorner),
                "",
                String.format("Edge pixels: %d", d.edgeCount),
                String.format("Fringed: %.1f%%", d.fringePct)
        };
    }

    private boolean[] buildSeparatorFlags(String[] lines) {
        boolean[] flags = new boolean[lines.length];
        for (int i = 0; i < lines.length; i++) {
            flags[i] = lines[i].isEmpty();
        }
        return flags;
    }

    private boolean[] buildCornerFlags(String[] lines) {
        boolean[] flags = new boolean[lines.length];
        boolean inCorner = false;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("Corner")) {
                inCorner = true;
            } else if (lines[i].isEmpty()) {
                inCorner = false;
            }
            flags[i] = inCorner && lines[i].startsWith("  ");
        }
        return flags;
    }

    // ── Description ───────────────────────────────────────────────────────────

    @Override
    public String getDescription() {
        return "Detects lateral (transverse) chromatic aberration by measuring R/G and B/G " +
                "channel misalignment at high-contrast luminance edges.\n\n" +
                "Algorithm: a Sobel gradient identifies high-contrast edges, then the absolute " +
                "channel differences |R\u2212G| and |B\u2212G| are measured at each edge pixel. " +
                "Edge pixels are coloured blue\u2192red in the overlay (blue = low fringing, " +
                "red = strong fringing); non-edge pixels remain fully transparent.\n\n" +
                "Whole-frame and corner-only mean channel differences are reported separately, " +
                "since lateral CA is strongest at the image corners.\n\n" +
                "\u2022 Edge threshold \u2014 Sobel magnitude (0\u2013255 normalised) above which a " +
                "pixel is classified as a high-contrast edge. Increase to analyse only the " +
                "sharpest edges; decrease to include softer transitions.\n" +
                "\u2022 Sensitivity \u2014 minimum |R\u2212G| or |B\u2212G| value for a pixel to count " +
                "as visibly fringed. Pixels below this threshold are shown at reduced opacity.\n\n" +
                "Chromatic aberration shows up as coloured fringes — usually purple or green — along " +
                "high-contrast edges, most noticeably at the corners of the frame. It is a common " +
                "weakness of wide-angle and budget zoom lenses, and is often partially corrected " +
                "in-camera or in the raw converter. This filter lets you compare two lenses (or the " +
                "same lens with and without correction applied) to see which produces less fringing. " +
                "Best test images contain sharp, high-contrast silhouettes: dark tree branches against " +
                "a bright sky, white architectural details against a dark background, or any subject " +
                "where a hard edge separates a very bright and a very dark area. " +
                "Because CA is strongest at the corners, make sure the subject extends to the frame edges.";
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

    // ── Data snapshot ─────────────────────────────────────────────────────────

    static final class CaData {
        final boolean noEdges;
        final double meanRgAll;
        final double meanBgAll;
        final double meanRgCorner;
        final double meanBgCorner;
        final long edgeCount;
        final double fringePct;

        static CaData noEdges() {
            return new CaData(true, 0, 0, 0, 0, 0, 0);
        }

        CaData(double meanRgAll, double meanBgAll,
               double meanRgCorner, double meanBgCorner,
               long edgeCount, double fringePct) {
            this(false, meanRgAll, meanBgAll, meanRgCorner, meanBgCorner, edgeCount, fringePct);
        }

        private CaData(boolean noEdges,
                       double meanRgAll, double meanBgAll,
                       double meanRgCorner, double meanBgCorner,
                       long edgeCount, double fringePct) {
            this.noEdges = noEdges;
            this.meanRgAll = meanRgAll;
            this.meanBgAll = meanBgAll;
            this.meanRgCorner = meanRgCorner;
            this.meanBgCorner = meanBgCorner;
            this.edgeCount = edgeCount;
            this.fringePct = fringePct;
        }
    }
}
