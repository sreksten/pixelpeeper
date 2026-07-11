package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.filters.ViewportOverlayPainter;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.DepthOfFieldFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DepthOfFieldFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Computes Depth of Field metrics from the image's EXIF metadata and displays them as a
 * compact text overlay in the top-right corner of each viewport.
 *
 * <p>No pixel processing is performed; all values are derived purely from EXIF tags:</p>
 * <ul>
 *   <li>{@code FOCAL_LENGTH} (mm)</li>
 *   <li>{@code APERTURE} (f-number)</li>
 *   <li>{@code DISTANCE_FROM_SUBJECT} (metres)</li>
 *   <li>{@code FOCAL_LENGTH_35MM_EQUIVALENT} (for crop-factor derivation)</li>
 * </ul>
 *
 * <p>When EXIF data is absent or incomplete, the overlay shows a helpful placeholder so the
 * panel slot is never unexpectedly empty.</p>
 *
 * <p>The diffraction warning fires when the shooting f-number exceeds the Rayleigh-criterion
 * limit for the sensor's estimated pixel pitch.</p>
 *
 * <p>This filter implements {@link com.threeamigos.pixelpeeper.interfaces.filters.ExifAwareFilter};
 * {@code PictureData} automatically injects the {@link ExifMap} before calling
 * {@link #process()}.</p>
 *
 * @author Stefano Reksten
 */
public class DepthOfFieldFilterImpl implements DepthOfFieldFilter, ViewportOverlayPainter {

    // ── Overlay geometry (screen pixels) ─────────────────────────────────────
    private static final int PAD = 8;
    private static final int LINE_H = 15;
    private static final int OVERLAY_W = 215;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(20, 20, 20, 180);
    private static final Color TITLE_COLOR = new Color(200, 200, 200);
    private static final Color LABEL_COLOR = new Color(140, 140, 140);
    private static final Color VALUE_COLOR = new Color(240, 240, 100);
    private static final Color HYPER_COLOR = new Color(100, 200, 240);
    private static final Color WARN_COLOR = new Color(255, 100, 60);
    private static final Color SEPARATOR_COLOR = new Color(70, 70, 70);

    private final DepthOfFieldFilterPreferences preferences;
    private BufferedImage sourceImage;
    private ExifMap exifMap;
    private volatile boolean isAborted;

    /** Computed data snapshot, written by the background thread, read by the EDT. */
    private volatile DofData dofData;

    public DepthOfFieldFilterImpl(DepthOfFieldFilterPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void setExifMap(ExifMap exifMap) {
        this.exifMap = exifMap;
    }

    // ── Filter.process() ─────────────────────────────────────────────────────

    @Override
    public void process() {
        isAborted = false;
        dofData = null;

        if (exifMap == null) {
            dofData = DofData.noExif();
            return;
        }

        Float fF = exifMap.getAsFloat(ExifTag.FOCAL_LENGTH);
        Float nF = exifMap.getAsFloat(ExifTag.APERTURE);
        Float f35F = exifMap.getAsFloat(ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT);
        Float sF = exifMap.getAsFloat(ExifTag.DISTANCE_FROM_SUBJECT);

        if (fF == null || nF == null || fF <= 0 || nF <= 0) {
            dofData = DofData.incompleteExif();
            return;
        }

        double f = fF;       // focal length [mm]
        double N = nF;       // f-number

        // Derive crop factor from 35 mm equivalent; fall back to 1.0 (full frame)
        double cropFactor = (f35F != null && f > 0) ? f35F / f : 1.0;
        if (cropFactor <= 0) {
            cropFactor = 1.0;
        }

        int cocDenominator = preferences.getCocDenominator();
        double coc_mm = DepthOfFieldCalculator.computeCoC(cropFactor, cocDenominator);
        double hyperfocal_mm = DepthOfFieldCalculator.computeHyperfocal_mm(f, N, coc_mm);
        double hyperfocal_m = hyperfocal_mm / 1000.0;

        double subject_m = Double.NaN;
        double near_m = Double.NaN;
        double far_m = Double.NaN;
        double dof_m = Double.NaN;

        if (sF != null && sF > 0 && !Float.isInfinite(sF)) {
            subject_m = sF;                       // EXIF SubjectDistance is in metres
            double subject_mm = subject_m * 1000.0;
            near_m = DepthOfFieldCalculator.computeNear_mm(hyperfocal_mm, subject_mm, f) / 1000.0;
            far_m = DepthOfFieldCalculator.computeFar_mm(hyperfocal_mm, subject_mm, f) / 1000.0;
            dof_m = Double.isInfinite(far_m) ? Double.POSITIVE_INFINITY : far_m - near_m;
        }

        double diffLimitN = Double.NaN;
        boolean diffractionLimited = false;
        if (sourceImage != null && cropFactor > 0) {
            diffLimitN = DepthOfFieldCalculator.computeDiffractionLimitFNumber(cropFactor, sourceImage.getWidth());
            diffractionLimited = N > diffLimitN;
        }

        dofData = new DofData(f, N, cropFactor, coc_mm, hyperfocal_m,
                subject_m, near_m, far_m, dof_m, diffLimitN, diffractionLimited);
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    /**
     * DoF is a pure calculation: no filtered image is produced.
     * All output is rendered via the viewport overlay.
     */
    @Override
    public BufferedImage getResultingImage() {
        return null;
    }

    // ── ViewportOverlayPainter ────────────────────────────────────────────────

    @Override
    public void paintViewportOverlay(Graphics2D g2d, int x, int y, int width, int height) {
        DofData data = dofData; // volatile read — safe on EDT
        if (data == null) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Build the list of lines to render
        String[] lines = buildLines(data);
        boolean[] isSeparator = buildSeparatorFlags(data);
        boolean[] isWarning = buildWarningFlags(data);
        boolean[] isHyper = buildHyperFlags(data);

        int totalLines = lines.length;
        int overlayH = PAD + totalLines * LINE_H + PAD;

        // Position: top-right corner
        int ox = x + width - OVERLAY_W - PAD;
        int oy = y + PAD;
        if (ox < x) {
            ox = x;
        }

        // Background
        g2d.setColor(BG_COLOR);
        g2d.fillRoundRect(ox, oy, OVERLAY_W, overlayH, 8, 8);

        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();

        int textX = ox + PAD;
        int curY = oy + PAD + fm.getAscent();

        for (int i = 0; i < totalLines; i++) {
            if (isSeparator[i]) {
                g2d.setColor(SEPARATOR_COLOR);
                g2d.drawLine(textX, curY - fm.getAscent() / 2, ox + OVERLAY_W - PAD, curY - fm.getAscent() / 2);
            } else if (isWarning[i]) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(lines[i], textX + 1, curY + 1);
                g2d.setColor(WARN_COLOR);
                g2d.drawString(lines[i], textX, curY);
            } else if (isHyper[i]) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(lines[i], textX + 1, curY + 1);
                g2d.setColor(HYPER_COLOR);
                g2d.drawString(lines[i], textX, curY);
            } else if (i == 0) {
                g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2d.setColor(TITLE_COLOR);
                g2d.drawString(lines[i], textX, curY);
                g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            } else {
                // Key: value lines — render label part dimmer, value brighter
                int colon = lines[i].indexOf(':');
                if (colon > 0) {
                    String label = lines[i].substring(0, colon + 1);
                    String value = lines[i].substring(colon + 1);
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(label, textX, curY);
                    int labelW = fm.stringWidth(label);
                    g2d.setColor(VALUE_COLOR);
                    g2d.drawString(value, textX + labelW, curY);
                } else {
                    g2d.setColor(LABEL_COLOR);
                    g2d.drawString(lines[i], textX, curY);
                }
            }
            curY += LINE_H;
        }
    }

    // ── Text-line builders ────────────────────────────────────────────────────

    private String[] buildLines(DofData d) {
        if (d.noExif) {
            return new String[]{"DEPTH OF FIELD", "", "No EXIF data available"};
        }
        if (d.incompleteExif) {
            return new String[]{"DEPTH OF FIELD", "", "EXIF incomplete", "(focal length / aperture", " not found)"};
        }

        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("DEPTH OF FIELD");
        list.add(String.format("%.0f mm \u00b7 f/%.1f", d.focalLength_mm, d.aperture));
        list.add(""); // separator
        if (!Double.isNaN(d.subject_m)) {
            list.add(String.format("Focus: %s", formatDist(d.subject_m)));
            list.add(String.format("Near:  %s", formatDist(d.near_m)));
            list.add(String.format("Far:   %s", formatDist(d.far_m)));
            list.add(String.format("DoF:   %s", formatDist(d.dof_m)));
        } else {
            list.add("Focus: unknown");
        }
        list.add(""); // separator
        list.add(String.format("Hyperfocal: %s", formatDist(d.hyperfocal_m)));
        list.add(String.format("CoC: %.3f mm", d.coc_mm));
        if (d.diffractionLimited) {
            list.add(String.format("\u26a0 Diffraction > f/%.0f", d.diffLimitN));
        }
        return list.toArray(new String[0]);
    }

    private boolean[] buildSeparatorFlags(DofData d) {
        String[] lines = buildLines(d);
        boolean[] flags = new boolean[lines.length];
        for (int i = 0; i < lines.length; i++) {
            flags[i] = lines[i].isEmpty();
        }
        return flags;
    }

    private boolean[] buildWarningFlags(DofData d) {
        String[] lines = buildLines(d);
        boolean[] flags = new boolean[lines.length];
        for (int i = 0; i < lines.length; i++) {
            flags[i] = lines[i].startsWith("\u26a0");
        }
        return flags;
    }

    private boolean[] buildHyperFlags(DofData d) {
        String[] lines = buildLines(d);
        boolean[] flags = new boolean[lines.length];
        for (int i = 0; i < lines.length; i++) {
            flags[i] = lines[i].startsWith("Hyperfocal");
        }
        return flags;
    }

    private String formatDist(double metres) {
        if (Double.isNaN(metres)) {
            return "N/A";
        }
        if (Double.isInfinite(metres)) {
            return "\u221e";
        }
        if (metres >= 1000.0) {
            return String.format("%.1f km", metres / 1000.0);
        }
        if (metres >= 1.0) {
            return String.format("%.2f m", metres);
        }
        return String.format("%.0f cm", metres * 100.0);
    }

    // ── Description ──────────────────────────────────────────────────────────

    @Override
    public String getDescription() {
        return "Computes Depth of Field metrics purely from EXIF metadata — no pixel processing is performed. " +
                "Required tags: focal length (mm), f-number, and subject distance (m). " +
                "The crop factor is derived automatically from the 35 mm equivalent focal length; " +
                "if that tag is absent, a full-frame sensor (crop factor 1.0) is assumed.\n\n" +
                "Displayed values:\n" +
                "\u2022 Near / Far — the closest and farthest distances that fall within acceptable sharpness.\n" +
                "\u2022 DoF — total depth of acceptable sharpness (Far \u2212 Near).\n" +
                "\u2022 Hyperfocal — focusing here maximises depth of field; everything from half this distance to \u221e is sharp.\n" +
                "\u2022 CoC — the Circle of Confusion diameter used in all calculations.\n" +
                "\u2022 Diffraction warning (\u26a0) — shown when the shooting aperture exceeds the Rayleigh-criterion " +
                "diffraction limit for the sensor's estimated pixel pitch.\n\n" +
                "CoC Denominator adjusts the CoC formula (CoC = sensor diagonal / denominator). " +
                "The default of 1500 targets a standard 8\u00d710 inch print at 25 cm viewing distance. " +
                "Increase to 2000 for more critical sharpness standards (e.g. large prints); " +
                "decrease to 1000 for casual display sizes.";
    }

    // ── Data snapshot ─────────────────────────────────────────────────────────

    static final class DofData {
        final boolean noExif;
        final boolean incompleteExif;
        final double focalLength_mm;
        final double aperture;
        final double cropFactor;
        final double coc_mm;
        final double hyperfocal_m;
        final double subject_m;
        final double near_m;
        final double far_m;
        final double dof_m;
        final double diffLimitN;
        final boolean diffractionLimited;

        /** Factory: filter has no EXIF map at all. */
        static DofData noExif() {
            return new DofData(true, false,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false);
        }

        /** Factory: required EXIF tags (focal length / aperture) are missing. */
        static DofData incompleteExif() {
            return new DofData(false, true,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
                    Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, false);
        }

        DofData(double focalLength_mm, double aperture, double cropFactor, double coc_mm,
                double hyperfocal_m, double subject_m, double near_m, double far_m, double dof_m,
                double diffLimitN, boolean diffractionLimited) {
            this(false, false, focalLength_mm, aperture, cropFactor, coc_mm, hyperfocal_m,
                    subject_m, near_m, far_m, dof_m, diffLimitN, diffractionLimited);
        }

        private DofData(boolean noExif, boolean incompleteExif,
                        double focalLength_mm, double aperture, double cropFactor, double coc_mm,
                        double hyperfocal_m, double subject_m, double near_m, double far_m, double dof_m,
                        double diffLimitN, boolean diffractionLimited) {
            this.noExif = noExif;
            this.incompleteExif = incompleteExif;
            this.focalLength_mm = focalLength_mm;
            this.aperture = aperture;
            this.cropFactor = cropFactor;
            this.coc_mm = coc_mm;
            this.hyperfocal_m = hyperfocal_m;
            this.subject_m = subject_m;
            this.near_m = near_m;
            this.far_m = far_m;
            this.dof_m = dof_m;
            this.diffLimitN = diffLimitN;
            this.diffractionLimited = diffractionLimited;
        }
    }
}
