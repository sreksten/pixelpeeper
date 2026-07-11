package com.threeamigos.pixelpeeper.implementations.filters.flavors;

/**
 * Pure-calculation helper for Depth of Field formulae.
 *
 * <p>All distance inputs and outputs use <em>millimetres</em> unless the parameter name
 * explicitly includes a unit suffix ({@code _m} = metres, {@code _mm} = millimetres).</p>
 *
 * <p>Reference formulae follow <em>Sidney F. Ray, Applied Photographic Optics</em> and
 * the standard lens maker's thin-lens approximation.</p>
 */
final class DepthOfFieldCalculator {

    /** Standard 35 mm full-frame sensor diagonal in mm (sqrt(36²+24²)). */
    static final double FULL_FRAME_DIAGONAL_MM = Math.sqrt(36.0 * 36.0 + 24.0 * 24.0);

    /** Standard 35 mm full-frame sensor width in mm. */
    private static final double FULL_FRAME_WIDTH_MM = 36.0;

    /** Peak wavelength of green light in mm (≈ 550 nm). */
    private static final double LAMBDA_MM = 0.00055;

    /** Rayleigh criterion factor (1.22 × 2 for two-point resolution). */
    private static final double RAYLEIGH_FACTOR = 2.44;

    private DepthOfFieldCalculator() {
    }

    /**
     * Computes the Circle of Confusion diameter.
     *
     * @param cropFactor     sensor crop factor (1.0 = full frame, 1.5 = APS-C, etc.)
     * @param cocDenominator divisor applied to the sensor diagonal (default 1500)
     * @return CoC in mm
     */
    static double computeCoC(double cropFactor, int cocDenominator) {
        double sensorDiagonal_mm = FULL_FRAME_DIAGONAL_MM / cropFactor;
        return sensorDiagonal_mm / cocDenominator;
    }

    /**
     * Computes the hyperfocal distance.
     *
     * @param focalLength_mm focal length in mm
     * @param aperture       f-number (e.g. 2.8 for f/2.8)
     * @param coc_mm         circle of confusion in mm
     * @return hyperfocal distance in mm
     */
    static double computeHyperfocal_mm(double focalLength_mm, double aperture, double coc_mm) {
        return (focalLength_mm * focalLength_mm) / (aperture * coc_mm) + focalLength_mm;
    }

    /**
     * Computes the near depth-of-field limit.
     *
     * @param hyperfocal_mm  hyperfocal distance in mm
     * @param subject_mm     subject (focus) distance in mm
     * @param focalLength_mm focal length in mm
     * @return near DoF limit in mm
     */
    static double computeNear_mm(double hyperfocal_mm, double subject_mm, double focalLength_mm) {
        double denom = hyperfocal_mm + subject_mm - focalLength_mm;
        if (denom <= 0) {
            return 0.0;
        }
        return (hyperfocal_mm * subject_mm) / denom;
    }

    /**
     * Computes the far depth-of-field limit.
     *
     * @param hyperfocal_mm  hyperfocal distance in mm
     * @param subject_mm     subject (focus) distance in mm
     * @param focalLength_mm focal length in mm
     * @return far DoF limit in mm, or {@link Double#POSITIVE_INFINITY} when focused at or beyond hyperfocal
     */
    static double computeFar_mm(double hyperfocal_mm, double subject_mm, double focalLength_mm) {
        double denom = hyperfocal_mm - subject_mm + focalLength_mm;
        if (denom <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (hyperfocal_mm * subject_mm) / denom;
    }

    /**
     * Computes the f-number at which diffraction becomes visible (Rayleigh criterion).
     * If the shooting aperture exceeds this value, diffraction softening is likely.
     *
     * @param cropFactor      sensor crop factor
     * @param imageWidthPixels image width in pixels (used as a proxy for pixel count)
     * @return diffraction-limit f-number
     */
    static double computeDiffractionLimitFNumber(double cropFactor, int imageWidthPixels) {
        double sensorWidth_mm = FULL_FRAME_WIDTH_MM / cropFactor;
        double pixelPitch_mm = sensorWidth_mm / imageWidthPixels;
        return pixelPitch_mm / (RAYLEIGH_FACTOR * LAMBDA_MM);
    }
}
