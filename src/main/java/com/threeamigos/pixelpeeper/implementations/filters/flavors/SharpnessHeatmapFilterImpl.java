package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.flavors.SharpnessHeatmapFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.SharpnessHeatmapFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Divides an image into a configurable N×N grid and computes a sharpness score for
 * each cell using the <em>Laplacian variance</em> method.  Scores are normalised
 * and rendered as a blue→green→red heatmap overlay so the operator can identify
 * which part of the frame is rendered most sharply by the lens.
 *
 * <p>Processing is parallelised across available CPU cores: each thread handles a
 * disjoint subset of cells during the scoring pass, then the rendering pass fills
 * each cell rectangle and draws the score text.</p>
 *
 * @author Stefano Reksten
 */
public class SharpnessHeatmapFilterImpl implements SharpnessHeatmapFilter {

    private static final int CELL_PADDING = 4;
    private static final float FONT_SIZE_FRACTION = 0.12f; // fraction of cell height

    private final SharpnessHeatmapFilterPreferences preferences;

    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    public SharpnessHeatmapFilterImpl(SharpnessHeatmapFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {
        isAborted = false;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        int gridSize = preferences.getGridSize();

        // Cell boundaries (last column/row absorbs remainder pixels)
        int cellW = width / gridSize;
        int cellH = height / gridSize;

        double[] scores = new double[gridSize * gridSize];

        // ── Pass 1: compute Laplacian variance per cell in parallel ──────────
        int processors = Runtime.getRuntime().availableProcessors();
        int totalCells = gridSize * gridSize;
        int cellsPerProcessor = (totalCells + processors - 1) / processors;
        Thread[] threads = new Thread[processors];

        for (int t = 0; t < processors; t++) {
            final int startCell = t * cellsPerProcessor;
            final int endCell = Math.min(startCell + cellsPerProcessor, totalCells);
            threads[t] = new Thread(() -> {
                for (int cell = startCell; cell < endCell && !isAborted; cell++) {
                    int row = cell / gridSize;
                    int col = cell % gridSize;
                    int x0 = col * cellW;
                    int y0 = row * cellH;
                    int x1 = (col + 1 < gridSize) ? x0 + cellW : width;
                    int y1 = (row + 1 < gridSize) ? y0 + cellH : height;
                    scores[cell] = laplacianVariance(x0, y0, x1, y1);
                }
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            filteredImage = null;
            return;
        }

        // ── Find global min/max for normalisation ─────────────────────────────
        double minScore = Double.MAX_VALUE;
        double maxScore = -Double.MAX_VALUE;
        for (double s : scores) {
            if (s < minScore) minScore = s;
            if (s > maxScore) maxScore = s;
        }
        double range = maxScore - minScore;

        // ── Pass 2: render heatmap ────────────────────────────────────────────
        filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = filteredImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int fontSize = Math.max(10, (int) (cellH * FONT_SIZE_FRACTION));
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();

        int centerRow = gridSize / 2;
        int centerCol = gridSize / 2;

        for (int row = 0; row < gridSize && !isAborted; row++) {
            for (int col = 0; col < gridSize; col++) {
                int x0 = col * cellW;
                int y0 = row * cellH;
                int x1 = (col + 1 < gridSize) ? x0 + cellW : width;
                int y1 = (row + 1 < gridSize) ? y0 + cellH : height;
                int cw = x1 - x0;
                int ch = y1 - y0;

                double score = scores[row * gridSize + col];
                double t = (range > 0) ? (score - minScore) / range : 0.5;

                // Fill cell with heatmap colour
                g2d.setColor(heatmapColor(t));
                g2d.fillRect(x0, y0, cw, ch);

                // Highlight the centre cell with a white border
                if (row == centerRow && col == centerCol) {
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3f));
                    g2d.drawRect(x0 + 1, y0 + 1, cw - 3, ch - 3);
                    g2d.setStroke(new BasicStroke(1f));
                }

                // Score label
                String label = String.format("%.0f", score);
                int labelW = fm.stringWidth(label);
                int labelX = x0 + (cw - labelW) / 2;
                int labelY = y0 + (ch + fm.getAscent() - fm.getDescent()) / 2;

                // Shadow
                g2d.setColor(Color.BLACK);
                g2d.drawString(label, labelX + 1, labelY + 1);
                // Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(label, labelX, labelY);
            }
        }

        if (isAborted) {
            filteredImage = null;
            g2d.dispose();
            return;
        }

        // Draw grid lines (white, 1px)
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1f));
        for (int col = 1; col < gridSize; col++) {
            int x = col * cellW;
            g2d.drawLine(x, 0, x, height);
        }
        for (int row = 1; row < gridSize; row++) {
            int y = row * cellH;
            g2d.drawLine(0, y, width, y);
        }

        g2d.dispose();
    }

    /**
     * Computes the Laplacian variance for the pixel region [x0,x1) × [y0,y1).
     * Border pixels are skipped since the 3×3 kernel cannot be applied there.
     * <p>
     * Laplacian kernel:
     * <pre>
     *  0  1  0
     *  1 -4  1
     *  0  1  0
     * </pre>
     * Variance = E[L²] − E[L]² where L is the per-pixel Laplacian response.
     */
    private double laplacianVariance(int x0, int y0, int x1, int y1) {
        if (x1 - x0 < 3 || y1 - y0 < 3) {
            return 0.0;
        }

        double sum = 0.0;
        double sumSq = 0.0;
        int count = 0;

        for (int x = x0 + 1; x < x1 - 1; x++) {
            for (int y = y0 + 1; y < y1 - 1; y++) {
                int lap = luminance(x - 1, y) + luminance(x + 1, y)
                        + luminance(x, y - 1) + luminance(x, y + 1)
                        - 4 * luminance(x, y);
                sum += lap;
                sumSq += (double) lap * lap;
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }
        double mean = sum / count;
        return sumSq / count - mean * mean;
    }

    /** ITU-R BT.709 luminance from a packed ARGB int. */
    private int luminance(int x, int y) {
        int rgb = sourceImage.getRGB(x, y);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    /**
     * Maps a normalised score t ∈ [0,1] to a heatmap colour using the HSB
     * colour wheel: t=0 → blue (hue 2/3), t=1 → red (hue 0).
     */
    private Color heatmapColor(double t) {
        float hue = (float) ((1.0 - t) * 2.0 / 3.0);
        return Color.getHSBColor(hue, 0.9f, 1.0f);
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
    public BufferedImage getResultingImage() {
        return filteredImage;
    }
}
