package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.EquivalentExposureFilter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Computes the full-frame-equivalent exposure settings from EXIF metadata and displays
 * them as a compact text overlay in the top-left corner of each viewport.
 *
 * <p>No pixel processing is performed.  All values are derived from:</p>
 * <ul>
 *   <li>{@code FOCAL_LENGTH} (mm) — native focal length</li>
 *   <li>{@code FOCAL_LENGTH_35MM_EQUIVALENT} (mm) — used to derive the crop factor and
 *       display the equivalent focal length on a full-frame body</li>
 *   <li>{@code APERTURE} (f-number) — native aperture</li>
 *   <li>{@code EXPOSURE_TIME} (seconds)</li>
 *   <li>{@code ISO}</li>
 * </ul>
 *
 * <p><strong>Physics:</strong> The total light gathered by a sensor is proportional to
 * {@code t / (N × cropFactor)²}, where {@code t} is the shutter speed and {@code N} is the
 * f-number.  Two cameras shooting at different settings capture the same total light when
 * this value (the <em>light index</em>) is equal.  Expressed in EV (stops), the light
 * index EV = log₂(t) − 2·log₂(N × cropFactor).</p>
 *
 * <p>The overlay also shows the full-frame-equivalent settings that would produce the same
 * exposure on a full-frame body: same shutter speed, aperture multiplied by the crop factor,
 * ISO multiplied by the crop factor squared, and the 35 mm equivalent focal length.</p>
 *
 * @author Stefano Reksten
 */
public class EquivalentExposureFilterImpl implements EquivalentExposureFilter, ViewportOverlayPainter {

    // ── Overlay geometry (screen pixels) ─────────────────────────────────────
    private static final int PAD = 8;
    private static final int LINE_H = 15;
    private static final int OVERLAY_W = 230;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color LABEL_COLOR = new Color(140, 140, 140);
    private static final Color VALUE_COLOR = new Color(240, 240, 100);
    private static final Color FF_VALUE_COLOR = new Color(100, 220, 140);
    private static final Color INDEX_COLOR = new Color(100, 180, 255);
    private static final Color SEPARATOR_COLOR = new Color(70, 70, 70);

    private ExifMap exifMap;
    private volatile boolean isAborted;

    /** Computed data snapshot, written by the background thread, read by the EDT. */
    private volatile ExposureData exposureData;

    // ── ExifAwareFilter ───────────────────────────────────────────────────────

    @Override
    public void setExifMap(ExifMap exifMap) {
        this.exifMap = exifMap;
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        // No pixel processing — source image not used.
    }

    @Override
    public void process() {
        isAborted = false;
        exposureData = null;

        if (exifMap == null) {
            exposureData = ExposureData.noExif();
            return;
        }

        Float fF = exifMap.getAsFloat(ExifTag.FOCAL_LENGTH);
        Float f35F = exifMap.getAsFloat(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
        Float apertureF = exifMap.getAsFloat(ExifTag.APERTURE);
        Float shutterF = exifMap.getAsFloat(ExifTag.EXPOSURE_TIME);
        Float isoF = exifMap.getAsFloat(ExifTag.ISO);

        if (apertureF == null || apertureF <= 0 || shutterF == null || shutterF <= 0) {
            exposureData = ExposureData.incompleteExif();
            return;
        }

        double N = apertureF;
        double t = shutterF;
        double iso = (isoF != null && isoF > 0) ? isoF : Double.NaN;
        double f = (fF != null && fF > 0) ? fF : Double.NaN;
        double f35 = (f35F != null && f35F > 0) ? f35F : Double.NaN;

        // Derive crop factor: f35 / f; fall back to 1.0 (full frame)
        double cropFactor = 1.0;
        if (!Double.isNaN(f) && !Double.isNaN(f35) && f > 0) {
            cropFactor = f35 / f;
        }
        if (cropFactor <= 0) {
            cropFactor = 1.0;
        }

        // FF-equivalent settings
        double N_ff = N * cropFactor;
        double iso_ff = Double.isNaN(iso) ? Double.NaN : iso * cropFactor * cropFactor;

        // Light index: total light gathered ∝ t / (N × cropFactor)²
        // Expressed in EV: lightEV = log2(t) - 2·log2(N_ff)
        double lightIndexEV = Math.log(t) / Math.log(2.0) - 2.0 * Math.log(N_ff) / Math.log(2.0);

        exposureData = new ExposureData(f, f35, N, t, iso, cropFactor, N_ff, iso_ff, lightIndexEV);
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    /**
     * Equivalent Exposure is a pure calculation: no filtered image is produced.
     * All output is rendered via the viewport overlay.
     */
    @Override
    public BufferedImage getResultingImage() {
        return null;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        ExposureData data = exposureData; // volatile read — safe on EDT
        if (data == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String[] lines = buildLines(data);
        LineType[] types = buildLineTypes(data);

        int totalLines = lines.length;
        int overlayH = PAD + totalLines * LINE_H + PAD;

        // Position: top-left corner
        int ox = x + PAD;
        int oy = y + PAD;

        // Background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_W, overlayH, 8, 8);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();

        int textX = ox + PAD;
        int curY = oy + PAD + fm.getAscent();

        for (int i = 0; i < totalLines; i++) {
            switch (types[i]) {
                case SEPARATOR:
                    g2d.setColor(SEPARATOR_COLOR);
                    g2d.drawLine(textX, curY - fm.getAscent() / 2,
                            ox + OVERLAY_W - PAD, curY - fm.getAscent() / 2);
                    break;
                case TITLE:
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                    g2d.setColor(TITLE_COLOR);
                    g2d.drawString(lines[i], textX, curY);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                    break;
                case FF_VALUE:
                    renderLabelValue(g2d, fm, lines[i], textX, curY, LABEL_COLOR, FF_VALUE_COLOR);
                    break;
                case INDEX:
                    renderLabelValue(g2d, fm, lines[i], textX, curY, LABEL_COLOR, INDEX_COLOR);
                    break;
                case PLAIN:
                default:
                    renderLabelValue(g2d, fm, lines[i], textX, curY, LABEL_COLOR, VALUE_COLOR);
                    break;
            }
            curY += LINE_H;
        }
    }

    private void renderLabelValue(Graphics2D g2d, FontMetrics fm,
                                  String text, int textX, int curY,
                                  Color labelColor, Color valueColor) {
        int colon = text.indexOf(':');
        if (colon > 0) {
            String label = text.substring(0, colon + 1);
            String value = text.substring(colon + 1);
            g2d.setColor(labelColor);
            g2d.drawString(label, textX, curY);
            int labelW = fm.stringWidth(label);
            g2d.setColor(valueColor);
            g2d.drawString(value, textX + labelW, curY);
        } else {
            g2d.setColor(labelColor);
            g2d.drawString(text, textX, curY);
        }
    }

    // ── Text-line and type builders ───────────────────────────────────────────

    private enum LineType { TITLE, SEPARATOR, PLAIN, FF_VALUE, INDEX }

    private String[] buildLines(ExposureData d) {
        if (d.noExif) {
            return new String[]{"EQUIVALENT EXPOSURE", "", "No EXIF data available"};
        }
        if (d.incompleteExif) {
            return new String[]{"EQUIVALENT EXPOSURE", "", "EXIF incomplete",
                    "(aperture / shutter not found)"};
        }

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("EQUIVALENT EXPOSURE");

        // Raw settings
        list.add("");  // separator
        list.add("Raw settings:");
        if (!Double.isNaN(d.focalLength_mm)) {
            list.add(String.format("  Focal:  %.0f mm", d.focalLength_mm));
        }
        list.add(String.format("  f/%.1f  %s  ISO %s",
                d.aperture,
                formatShutter(d.shutter_s),
                formatISO(d.iso)));

        // FF-equivalent settings
        list.add("");  // separator
        list.add("35\u200amm equivalent:");
        if (!Double.isNaN(d.focalLength35_mm)) {
            list.add(String.format("  Focal:  %.0f mm", d.focalLength35_mm));
        }
        list.add(String.format("  f/%.1f  %s  ISO %s",
                d.aperture_ff,
                formatShutter(d.shutter_s),
                formatISO(d.iso_ff)));

        // Crop factor
        list.add("");  // separator
        if (Math.abs(d.cropFactor - 1.0) < 0.01) {
            list.add("Crop factor: 1.0× (Full Frame)");
        } else {
            list.add(String.format("Crop factor: %.2f\u00d7", d.cropFactor));
        }

        // Light index
        list.add(String.format("Light index: %+.2f EV", d.lightIndexEV));

        return list.toArray(new String[0]);
    }

    private LineType[] buildLineTypes(ExposureData d) {
        String[] lines = buildLines(d);
        LineType[] types = new LineType[lines.length];
        types[0] = LineType.TITLE;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                types[i] = LineType.SEPARATOR;
            } else if (lines[i].startsWith("35") || lines[i].startsWith("  f/") && i > 5) {
                // Lines under the "35 mm equivalent" section use FF_VALUE colour
                types[i] = LineType.FF_VALUE;
            } else if (lines[i].startsWith("Light index")) {
                types[i] = LineType.INDEX;
            } else {
                types[i] = LineType.PLAIN;
            }
        }
        // Paint the FF section lines differently: find the "35 mm equivalent" header and
        // colour subsequent non-separator lines until the next separator.
        boolean inFFSection = false;
        for (int i = 1; i < lines.length; i++) {
            if (types[i] == LineType.SEPARATOR) {
                inFFSection = false;
            } else if (lines[i].startsWith("35\u200amm")) {
                inFFSection = true;
                types[i] = LineType.FF_VALUE;
            } else if (inFFSection) {
                types[i] = LineType.FF_VALUE;
            }
        }
        return types;
    }

    // ── Formatters ────────────────────────────────────────────────────────────

    private String formatShutter(double t) {
        if (Double.isNaN(t)) {
            return "?s";
        }
        if (t >= 1.0) {
            return String.format("%.0fs", t);
        }
        // Express as 1/N
        long denom = Math.round(1.0 / t);
        return "1/" + denom + "s";
    }

    private String formatISO(double iso) {
        if (Double.isNaN(iso)) {
            return "?";
        }
        return String.format("%.0f", iso);
    }

    // ── Description ───────────────────────────────────────────────────────────

    @Override
    public String getDescription() {
        return "Computes full-frame-equivalent exposure settings from EXIF metadata — no pixel processing performed.\n\n" +
                "The crop factor is derived automatically from the ratio of 35\u200amm equivalent focal length to " +
                "native focal length.  If that tag is absent, a full-frame sensor (crop\u00a01.0\u00d7) is assumed.\n\n" +
                "Displayed values:\n" +
                "\u2022 Raw settings \u2014 the actual EXIF values written by the camera.\n" +
                "\u2022 35\u200amm equivalent \u2014 the settings that a full-frame body would need to gather the " +
                "same total light: aperture \u00d7 crop, ISO \u00d7 crop\u00b2, same shutter speed.\n" +
                "\u2022 Light index \u2014 log\u2082(t\u00a0/\u00a0(N\u00a0\u00d7\u00a0crop)\u00b2) in EV stops.  " +
                "Two images with equal light index values captured the same total light regardless of " +
                "sensor size, allowing fair ISO-noise comparisons between different systems.\n\n" +
                "This filter is essential when comparing cameras with different sensor sizes and you want " +
                "to know whether the comparison is fair. For example, a Micro Four Thirds camera at f/2 " +
                "ISO\u202f800 and a full-frame camera at f/4 ISO\u202f3200 are actually equivalent exposures " +
                "gathering the same total light — this filter makes that equivalence explicit. " +
                "Load side-by-side images from a crop and a full-frame body shot at the same scene " +
                "and use the light index readout to confirm whether the test conditions were truly " +
                "matched. Any image with EXIF focal length, f-number, shutter speed, and ISO works; " +
                "the 35\u202fmm equivalent focal length tag must be present for the crop factor to be " +
                "derived automatically.";
    }

    // ── Data snapshot ─────────────────────────────────────────────────────────

    static final class ExposureData {
        final boolean noExif;
        final boolean incompleteExif;
        final double focalLength_mm;
        final double focalLength35_mm;
        final double aperture;
        final double shutter_s;
        final double iso;
        final double cropFactor;
        final double aperture_ff;
        final double iso_ff;
        final double lightIndexEV;

        static ExposureData noExif() {
            return new ExposureData(true, false,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }

        static ExposureData incompleteExif() {
            return new ExposureData(false, true,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }

        ExposureData(double focalLength_mm, double focalLength35_mm,
                     double aperture, double shutter_s, double iso,
                     double cropFactor, double aperture_ff, double iso_ff,
                     double lightIndexEV) {
            this(false, false, focalLength_mm, focalLength35_mm, aperture, shutter_s, iso,
                    cropFactor, aperture_ff, iso_ff, lightIndexEV);
        }

        private ExposureData(boolean noExif, boolean incompleteExif,
                             double focalLength_mm, double focalLength35_mm,
                             double aperture, double shutter_s, double iso,
                             double cropFactor, double aperture_ff, double iso_ff,
                             double lightIndexEV) {
            this.noExif = noExif;
            this.incompleteExif = incompleteExif;
            this.focalLength_mm = focalLength_mm;
            this.focalLength35_mm = focalLength35_mm;
            this.aperture = aperture;
            this.shutter_s = shutter_s;
            this.iso = iso;
            this.cropFactor = cropFactor;
            this.aperture_ff = aperture_ff;
            this.iso_ff = iso_ff;
            this.lightIndexEV = lightIndexEV;
        }
    }
}
