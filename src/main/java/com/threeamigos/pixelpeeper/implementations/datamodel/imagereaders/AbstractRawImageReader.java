package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.common.util.implementations.concurrency.ParallelTaskExecutor;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for the LibRaw-style RAW readers ({@link PanasonicRawImageReader} and
 * {@link CanonCr3RawImageReader}). It gathers the helpers both readers share verbatim: the small
 * clamping primitives, the parallel task partitioning/execution scaffolding, the LibRaw
 * {@code cam_xyz_coeff}/{@code pseudoinverse} camera-to-sRGB matrix maths and the LibRaw
 * {@code gamma_curve} tone-curve builder.
 *
 * <p>Reader-specific decoding, metadata parsing, demosaicing, and the per-model color matrices stay
 * in the concrete subclasses, as do the helpers whose bodies or diagnostics differ between formats
 * (e.g., TIFF {@code typeSize}, {@code normalizeModel}, {@code bayerChannel}, the byte readers).</p>
 *
 * @author Stefano Reksten
 */
public abstract class AbstractRawImageReader {

    /** sRGB(linear) -> XYZ (D65), LibRaw_constants::xyz_rgb. */
    protected static final double[][] XYZ_RGB = {
            {0.4124564, 0.3575761, 0.1804375},
            {0.2126729, 0.7151522, 0.0721750},
            {0.0193339, 0.1191920, 0.9503041}
    };

    private static final int GAMMA_LUT_SIZE = 0x10000;

    /** Color-plane indices used by {@link #bayerChannel} and the {@link LibRawRenderer} inputs. */
    protected static final int RED = 0;
    protected static final int GREEN = 1;
    protected static final int BLUE = 2;

    // LibRaw dcraw_process defaults emulated by the shared renderer (see LibRaw init_close_utils.cpp):
    //   use_camera_wb = 1 (camera/as-shot WB), no_auto_bright = 1 (fixed exposure),
    //   highlight = 0 (clip), output_color = 1 (sRGB), output_bps = 8,
    //   gamm = {0.45, 4.5} (BT.709), user_qual = 3 (AHD), bright = 1.
    private static final double GAMMA_POWER = 0.45;                 // gamm[0]
    private static final double GAMMA_TOE_SLOPE = 4.5;             // gamm[1]
    private static final int GAMMA_IMAX = 0x10000;                  // (0x2000<<3)/bright, no_auto_bright
    private static final int OUTPUT_WHITE = 65535;                  // 16-bit full scale
    private static final double ADJUST_MAXIMUM_THRESHOLD = 0.75;    // LIBRAW_DEFAULT_ADJUST_MAXIMUM_THRESHOLD

    // LibRaw AHD demosaic tile size (libraw/libraw_const.h LIBRAW_AHD_TILE) and its 6-pixel overlap.
    private static final int AHD_TILE = 512;
    private static final int AHD_TILE_OVERLAP = 6;
    private static final int AHD_BORDER = 5;

    // LibRaw_constants::d65_white.
    private static final double[] D65_WHITE = {0.95047, 1.00000, 1.08883};

    /** A single unit of parallel work, indexed by a task within a {@link #runParallelTasks} batch. */
    protected interface ParallelTask {

        void run(int taskIndex) throws Exception;
    }

    protected static int clip16(int value) {
        return value < 0 ? 0 : (Math.min(value, 65535));
    }

    protected static int ulim(int value, int bound1, int bound2) {
        if (bound1 < bound2) {
            return Math.max(bound1, Math.min(value, bound2));
        }
        return Math.max(bound2, Math.min(value, bound1));
    }

    protected static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Validates that {@code length} bytes are readable at {@code offset} in {@code data}. */
    protected static void ensureAvailable(byte[] data, int offset, int length) throws EOFException {
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new EOFException("Unexpected end of RAW data.");
        }
    }

    /** Validates that {@code length} bytes are readable at {@code offset} in {@code data}. */
    protected static void ensureAvailable(byte[] data, int offset, long length) throws EOFException {
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new EOFException("Unexpected end of RAW data.");
        }
    }

    /**
     * Returns {@code width * height}, verifying it is a positive count that fits in an {@code int}
     * (i.e., addressable as a single Java array).
     */
    protected static int checkedPixelCount(int width, int height) throws IOException {
        long pixelCount = (long) width * height;
        if (pixelCount <= 0 || pixelCount > Integer.MAX_VALUE) {
            throw new IOException("Invalid RAW dimensions: " + width + "x" + height);
        }
        return (int) pixelCount;
    }

    protected static int parallelTaskCount(int units, int minUnitsPerTask) {
        if (units <= 1) {
            return 1;
        }
        int tasksBySize = (int) Math.max(1L, ((long) units + minUnitsPerTask - 1L) / minUnitsPerTask);
        return Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), tasksBySize));
    }

    protected static int taskStart(int taskIndex, int taskCount, int units) {
        return (int) ((long) taskIndex * units / taskCount);
    }

    protected static int taskEnd(int taskIndex, int taskCount, int units) {
        return (int) ((long) (taskIndex + 1) * units / taskCount);
    }

    protected static void runParallelTasks(int taskCount, String failureMessage, ParallelTask task) throws IOException {
        if (taskCount <= 1) {
            try {
                task.run(0);
            } catch (Throwable e) {
                rethrowParallelFailure(e, failureMessage);
            }
            return;
        }

        AtomicReference<Throwable> failure = new AtomicReference<>();
        try (ParallelTaskExecutor executor = ParallelTaskExecutor.createExecutor(taskCount)) {
            for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
                int currentTaskIndex = taskIndex;
                executor.schedulePlatformThread(() -> {
                    if (failure.get() != null) {
                        return;
                    }
                    try {
                        task.run(currentTaskIndex);
                    } catch (Throwable e) {
                        failure.compareAndSet(null, e);
                    }
                });
            }
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(failureMessage, e);
        }

        Throwable taskFailure = failure.get();
        if (taskFailure != null) {
            rethrowParallelFailure(taskFailure, failureMessage);
        }
    }

    protected static void rethrowParallelFailure(Throwable failure, String failureMessage) throws IOException {
        if (failure instanceof IOException) {
            throw (IOException) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        throw new IOException(failureMessage, failure);
    }

    /**
     * Port of LibRaw's {@code gamma_curve} (src/utils/curves.cpp) for the forward tone curve
     * (mode 2). Builds the 0x10000-entry 16-bit lookup table for the BT.709 curve; {@code imax}
     * is the input value mapped to the full scale (65,536 with no auto-brightness and bright == 1).
     */
    protected static char[] buildGammaCurve(double power, double toeSlope, int imax) {
        double[] g = new double[5];
        double[] bnd = {0.0, 0.0};
        g[0] = power;
        g[1] = toeSlope;
        bnd[g[1] >= 1 ? 1 : 0] = 1.0;
        if (g[1] != 0 && (g[1] - 1) * (g[0] - 1) <= 0) {
            for (int i = 0; i < 48; i++) {
                g[2] = (bnd[0] + bnd[1]) / 2;
                if (g[0] != 0) {
                    bnd[((Math.pow(g[2] / g[1], -g[0]) - 1) / g[0] - 1 / g[2]) > -1 ? 1 : 0] = g[2];
                } else {
                    bnd[(g[2] / Math.exp(1 - 1 / g[2]) < g[1]) ? 1 : 0] = g[2];
                }
            }
            g[3] = g[2] / g[1];
            if (g[0] != 0) {
                g[4] = g[2] * (1 / g[0] - 1);
            }
        }

        char[] curve = new char[GAMMA_LUT_SIZE];
        for (int i = 0; i < GAMMA_LUT_SIZE; i++) {
            curve[i] = (char) 0xffff;
            double r = (double) i / imax;
            if (r < 1) {
                double value = r < g[3]
                        ? r * g[1]
                        : (g[0] != 0 ? Math.pow(r, g[0]) * (1 + g[4]) - g[4] : Math.log(r) * g[2] + 1);
                curve[i] = (char) clip16((int) (0x10000 * value));
            }
        }
        return curve;
    }

    /**
     * Port of LibRaw's {@code cam_xyz_coeff} (src/utils/utils_dcraw.cpp): converts the Adobe
     * camera-to-XYZ matrix (integers scaled by 10,000) into the camera-to-sRGB(linear) matrix
     * {@code rgb_cam} used by both the color conversion and the AHD Lab transform. The daylight
     * {@code pre_mul} row sums LibRaw derives here are not needed because these readers use the
     * as-shot camera white balance.
     */
    protected static double[][] camXyzCoeff(int[] adobeColorMatrix) {
        if (adobeColorMatrix.length != 9) {
            throw new IllegalArgumentException("Color matrices must contain 9 values.");
        }

        double[][] camXyz = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                camXyz[i][j] = adobeColorMatrix[i * 3 + j] / 10000.0;
            }
        }

        // cam_rgb = cam_xyz * xyz_rgb
        double[][] camRgb = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double sum = 0.0;
                for (int k = 0; k < 3; k++) {
                    sum += camXyz[i][k] * XYZ_RGB[k][j];
                }
                camRgb[i][j] = sum;
            }
        }

        // Normalize cam_rgb so that cam_rgb * (1,1,1) is (1,1,1).
        for (int i = 0; i < 3; i++) {
            double num = camRgb[i][0] + camRgb[i][1] + camRgb[i][2];
            if (num > 0.00001) {
                for (int j = 0; j < 3; j++) {
                    camRgb[i][j] /= num;
                }
            } else {
                for (int j = 0; j < 3; j++) {
                    camRgb[i][j] = 0.0;
                }
            }
        }

        double[][] inverse = pseudoinverse(camRgb);
        double[][] rgbCam = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                rgbCam[i][j] = inverse[j][i];
            }
        }
        return rgbCam;
    }

    /**
     * Port of LibRaw's {@code pseudoinverse} (src/utils/utils_dcraw.cpp) for a 3x3 input.
     * Returns the Moore-Penrose pseudoinverse (here the ordinary inverse) as a 3x3 matrix.
     */
    protected static double[][] pseudoinverse(double[][] in) {
        double[][] work = new double[3][6];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j++) {
                work[i][j] = (j == i + 3) ? 1.0 : 0.0;
            }
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    work[i][j] += in[k][i] * in[k][j];
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            double num = work[i][i];
            for (int j = 0; j < 6; j++) {
                if (Math.abs(num) > 0.00001) {
                    work[i][j] /= num;
                }
            }
            for (int k = 0; k < 3; k++) {
                if (k == i) {
                    continue;
                }
                num = work[k][i];
                for (int j = 0; j < 6; j++) {
                    work[k][j] -= work[i][j] * num;
                }
            }
        }
        double[][] out = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double sum = 0.0;
                for (int k = 0; k < 3; k++) {
                    sum += work[j][k + 3] * in[i][k];
                }
                out[i][j] = sum;
            }
        }
        return out;
    }

    protected static boolean hasModelBoundary(String model, int prefixLength) {
        if (prefixLength >= model.length()) {
            return true;
        }
        char next = model.charAt(prefixLength);
        return !((next >= 'A' && next <= 'Z') || (next >= '0' && next <= '9'));
    }

    /**
     * Resolves the color plane ({@link #RED}/{@link #GREEN}/{@link #BLUE}) of a Bayer site from a
     * 4-entry pattern indexed by {@code ((row & 1) << 1) | (col & 1)}. Both readers normalize their
     * format-specific CFA descriptor into this array, so a single, branch-free lookup serves both.
     */
    protected static int bayerChannel(int[] bayerPattern, int row, int col) {
        return bayerPattern[((row & 1) << 1) | (col & 1)];
    }

    /**
     * Renders a decoded Bayer mosaic to an 8-bit sRGB image following LibRaw's default
     * {@code dcraw_process} pipeline shared by the concrete readers: per-channel black subtraction
     * and {@code adjust_maximum}, {@code scale_colors} with the as-shot camera white balance and
     * highlight clipping, AHD demosaicing ({@code ahd_interpolate}), camera-to-sRGB color
     * conversion ({@code convert_to_rgb}), and the BT.709 output tone curve ({@code gamma_curve}).
     * Only the active crop is emitted.
     *
     * <p>The Bayer layout is supplied as a 4-entry pattern (see {@link #bayerChannel}); the
     * {@code blackLevel} and {@code whiteBalance} arrays are indexed by
     * {@link #RED}/{@link #GREEN}/{@link #BLUE}. The emulated parameters are camera/as-shot white
     * balance, highlight clipping, fixed exposure (no auto-brightness), sRGB output color and
     * 8-bit output.</p>
     */
    protected static final class LibRawRenderer {

        private final int[] raw;
        private final int width;
        private final int height;
        private final int bitsPerSample;
        private final int[] bayerPattern;
        private final int[] blackLevel;
        private final double[] whiteBalance;
        private final double[][] rgbCam;
        private final int cropLeft;
        private final int cropTop;
        private final int cropWidth;
        private final int cropHeight;

        protected LibRawRenderer(int[] raw, int width, int height, int bitsPerSample, int[] bayerPattern,
                                 int[] blackLevel, double[] whiteBalance, double[][] rgbCam,
                                 int cropLeft, int cropTop, int cropWidth, int cropHeight) {
            this.raw = raw;
            this.width = width;
            this.height = height;
            this.bitsPerSample = bitsPerSample;
            this.bayerPattern = bayerPattern;
            this.blackLevel = blackLevel;
            this.whiteBalance = whiteBalance;
            this.rgbCam = rgbCam;
            this.cropLeft = cropLeft;
            this.cropTop = cropTop;
            this.cropWidth = cropWidth;
            this.cropHeight = cropHeight;
        }

        protected BufferedImage render() throws IOException {
            char[] image = scaleColors();
            ahdInterpolate(image);
            char[] curve = buildGammaCurve(GAMMA_POWER, GAMMA_TOE_SLOPE, GAMMA_IMAX);
            return convertToRgb(image, curve);
        }

        /**
         * Port of LibRaw's inline black subtraction ({@code copy_bayer}), {@code adjust_maximum} and
         * {@code scale_colors}. Produces the 4-plane interleaved image LibRaw's demosaic consumes,
         * with the single Bayer sample of each site placed on its color plane (both greens in plane
         * 1) after per-channel black subtraction and white-balance scaling, clipped to 16-bit range.
         */
        private char[] scaleColors() throws IOException {
            int minBlack = Math.min(blackLevel[RED], Math.min(blackLevel[GREEN], blackLevel[BLUE]));

            // data_maximum: max of the black-subtracted (floored) samples, matching copy_bayer.
            // Computed as a parallel reduction over row bands (each task writes its own slot).
            int dataMaxTaskCount = parallelTaskCount(height, 1);
            int[] dataMaxPerTask = new int[dataMaxTaskCount];
            runParallelTasks(dataMaxTaskCount, "data-maximum task failed.", taskIndex -> {
                int startRow = taskStart(taskIndex, dataMaxTaskCount, height);
                int endRow = taskEnd(taskIndex, dataMaxTaskCount, height);
                int localMaximum = 0;
                for (int row = startRow; row < endRow; row++) {
                    int base = row * width;
                    for (int col = 0; col < width; col++) {
                        int value = raw[base + col] - blackLevel[bayerChannel(bayerPattern, row, col)];
                        if (value > localMaximum) {
                            localMaximum = value;
                        }
                    }
                }
                dataMaxPerTask[taskIndex] = localMaximum;
            });
            int dataMaximum = 0;
            for (int localMaximum : dataMaxPerTask) {
                if (localMaximum > dataMaximum) {
                    dataMaximum = localMaximum;
                }
            }

            // maximum -= black (black == minBlack), then adjust_maximum (only lowers, top 25% band).
            int maximum = ((1 << bitsPerSample) - 1) - minBlack;
            if (dataMaximum > 0 && dataMaximum < maximum && dataMaximum > maximum * ADJUST_MAXIMUM_THRESHOLD) {
                maximum = dataMaximum;
            }
            if (maximum <= 0) {
                maximum = 1;
            }

            // Camera (as-shot) white balance: pre_mul == cam_mul, green == 1, pre_mul[3] == pre_mul[1].
            double[] preMul = {whiteBalance[RED], whiteBalance[GREEN], whiteBalance[BLUE], whiteBalance[GREEN]};
            if (preMul[1] == 0) {
                preMul[1] = 1;
            }
            if (preMul[3] == 0) {
                preMul[3] = preMul[1];
            }
            double dmin = Double.MAX_VALUE;
            for (int c = 0; c < 4; c++) {
                if (dmin > preMul[c]) {
                    dmin = preMul[c];
                }
            }
            // highlight == 0 (clip): dmax is forced to dmin so the darkest multiplier maps maximum to
            // full scale and brighter channels clip.
            double dmax = dmin;
            double[] scaleMul = new double[4];
            if (dmax > 0.00001) {
                for (int c = 0; c < 4; c++) {
                    scaleMul[c] = (preMul[c] / dmax) * (double) OUTPUT_WHITE / maximum;
                }
            } else {
                Arrays.fill(scaleMul, 1.0);
            }

            long pixelCount = (long) width * height;
            if (pixelCount * 4L > Integer.MAX_VALUE) {
                throw new IOException("RAW image is too large to render: " + width + "x" + height);
            }
            char[] image = new char[(int) (pixelCount * 4L)];
            int taskCount = parallelTaskCount(height, 1);
            runParallelTasks(taskCount, "scale-colors task failed.", taskIndex -> {
                int startRow = taskStart(taskIndex, taskCount, height);
                int endRow = taskEnd(taskIndex, taskCount, height);
                for (int row = startRow; row < endRow; row++) {
                    int base = row * width;
                    for (int col = 0; col < width; col++) {
                        int channel = bayerChannel(bayerPattern, row, col);
                        int value = raw[base + col] - blackLevel[channel];
                        if (value < 0) {
                            value = 0;
                        }
                        image[(base + col) * 4 + channel] = (char) clip16((int) (value * scaleMul[channel]));
                    }
                }
            });
            return image;
        }

        /**
         * Port of LibRaw's AHD demosaic ({@code ahd_interpolate} in src/demosaic/ahd_demosaic.cpp):
         * fills the R/G/B planes of {@code image} in place. The five-pixel frame is filled by
         * {@link #borderInterpolate} and the interior is processed in overlapping tiles distributed
         * across parallel tasks; per-tile scratch buffers are thread-local.
         */
        private void ahdInterpolate(char[] image) throws IOException {
            // cielab initialization: cbrt LUT and xyz_cam = xyz_rgb * rgb_cam / d65_white.
            float[] cbrt = new float[0x10000];
            for (int i = 0; i < cbrt.length; i++) {
                double r = i / 65535.0;
                cbrt[i] = (float) (r > 0.008856 ? Math.pow(r, 1.0 / 3.0) : 7.787 * r + 16.0 / 116.0);
            }
            double[][] xyzCam = new double[3][3];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < 3; k++) {
                        sum += XYZ_RGB[i][k] * rgbCam[k][j];
                    }
                    xyzCam[i][j] = sum / D65_WHITE[i];
                }
            }

            borderInterpolate(image, AHD_BORDER);

            // Enumerate every overlapping tile (top-left corners). Distinct tiles write disjoint image
            // regions, so they can all run in parallel; tiles are spread round-robin over a pool
            // capped at the available cores (each thread reuses one set of scratch buffers).
            int step = AHD_TILE - AHD_TILE_OVERLAP;
            int tileCount = 0;
            for (int top = 2; top < height - 5; top += step) {
                for (int left = 2; left < width - 5; left += step) {
                    tileCount++;
                }
            }
            if (tileCount <= 0) {
                return;
            }
            int[] tileTops = new int[tileCount];
            int[] tileLefts = new int[tileCount];
            int index = 0;
            for (int top = 2; top < height - 5; top += step) {
                for (int left = 2; left < width - 5; left += step) {
                    tileTops[index] = top;
                    tileLefts[index] = left;
                    index++;
                }
            }

            int totalTiles = tileCount;
            int taskCount = parallelTaskCount(totalTiles, 1);
            runParallelTasks(taskCount, "AHD interpolation task failed.", taskIndex -> {
                int directionStride = AHD_TILE * AHD_TILE * 3;
                char[] tileRgb = new char[2 * directionStride];
                short[] tileLab = new short[2 * directionStride];
                byte[] homogeneity = new byte[AHD_TILE * AHD_TILE * 2];
                for (int tile = taskIndex; tile < totalTiles; tile += taskCount) {
                    int top = tileTops[tile];
                    int left = tileLefts[tile];
                    ahdInterpolateGreen(image, top, left, tileRgb);
                    ahdInterpolateRedBlue(image, top, left, tileRgb, tileLab, xyzCam, cbrt);
                    ahdBuildHomogeneity(tileLab, top, left, homogeneity);
                    ahdCombine(image, top, left, tileRgb, homogeneity);
                }
            });
        }

        /** LibRaw {@code ahd_interpolate_green_h_and_v}: green interpolation in both directions. */
        private void ahdInterpolateGreen(char[] image, int top, int left, char[] tileRgb) {
            int rowStride = width * 4;
            int directionStride = AHD_TILE * AHD_TILE * 3;
            int rowLimit = Math.min(top + AHD_TILE, height - 2);
            int colLimit = Math.min(left + AHD_TILE, width - 2);
            for (int row = top; row < rowLimit; row++) {
                int col = left + (bayerChannel(bayerPattern, row, left) & 1);
                int channel = bayerChannel(bayerPattern, row, col);
                for (; col < colLimit; col += 2) {
                    int p = (row * width + col) * 4;
                    int center = image[p + channel];

                    int greenLeft = image[p - 4 + 1];
                    int greenRight = image[p + 4 + 1];
                    int valueH = ((greenLeft + center + greenRight) * 2 - image[p - 8 + channel] - image[p + 8 + channel]) >> 2;

                    int greenUp = image[p - rowStride + 1];
                    int greenDown = image[p + rowStride + 1];
                    int valueV = ((greenUp + center + greenDown) * 2
                            - image[p - 2 * rowStride + channel] - image[p + 2 * rowStride + channel]) >> 2;

                    int tileIndex = ((row - top) * AHD_TILE + (col - left)) * 3;
                    tileRgb[tileIndex + 1] = (char) ulim(valueH, greenLeft, greenRight);
                    tileRgb[directionStride + tileIndex + 1] = (char) ulim(valueV, greenUp, greenDown);
                }
            }
        }

        /**
         * LibRaw {@code ahd_interpolate_r_and_b_and_convert_to_cielab}: interpolates the remaining two
         * colors for both directions and converts each candidate to CIE Lab.
         */
        private void ahdInterpolateRedBlue(char[] image, int top, int left, char[] tileRgb, short[] tileLab,
                                           double[][] xyzCam, float[] cbrt) {
            int rowStride = width * 4;
            int directionStride = AHD_TILE * AHD_TILE * 3;
            int tileRowStride = AHD_TILE * 3;
            int rowLimit = Math.min(top + AHD_TILE - 1, height - 3);
            int colLimit = Math.min(left + AHD_TILE - 1, width - 3);
            for (int direction = 0; direction < 2; direction++) {
                int dirBase = direction * directionStride;
                for (int row = top + 1; row < rowLimit; row++) {
                    for (int col = left + 1; col < colLimit; col++) {
                        int p = (row * width + col) * 4;
                        int rix = dirBase + ((row - top) * AHD_TILE + (col - left)) * 3;
                        int channel = 2 - bayerChannel(bayerPattern, row, col);
                        int value;
                        if (channel == 1) {
                            channel = bayerChannel(bayerPattern, row + 1, col);
                            int other = 2 - channel;
                            value = image[p + 1] + ((image[p - 4 + other] + image[p + 4 + other]
                                    - tileRgb[rix - 3 + 1] - tileRgb[rix + 3 + 1]) >> 1);
                            tileRgb[rix + other] = (char) clip16(value);
                            value = image[p + 1] + ((image[p - rowStride + channel] + image[p + rowStride + channel]
                                    - tileRgb[rix - tileRowStride + 1] - tileRgb[rix + tileRowStride + 1]) >> 1);
                        } else {
                            int above = p - rowStride;
                            int below = p + rowStride;
                            value = tileRgb[rix + 1] + ((image[above - 4 + channel] + image[above + 4 + channel]
                                    + image[below - 4 + channel] + image[below + 4 + channel]
                                    - tileRgb[rix - tileRowStride - 3 + 1] - tileRgb[rix - tileRowStride + 3 + 1]
                                    - tileRgb[rix + tileRowStride - 3 + 1] - tileRgb[rix + tileRowStride + 3 + 1] + 1) >> 2);
                        }
                        tileRgb[rix + channel] = (char) clip16(value);
                        channel = bayerChannel(bayerPattern, row, col);
                        tileRgb[rix + channel] = image[p + channel];
                        cielab(tileRgb, rix, tileLab, rix, xyzCam, cbrt);
                    }
                }
            }
        }

        /** LibRaw {@code cielab}: converts a camera-RGB triple to scaled CIE Lab shorts. */
        private void cielab(char[] tileRgb, int rgbIndex, short[] tileLab, int labIndex, double[][] xyzCam, float[] cbrt) {
            double x = 0.5;
            double y = 0.5;
            double z = 0.5;
            for (int c = 0; c < 3; c++) {
                int value = tileRgb[rgbIndex + c];
                x += xyzCam[0][c] * value;
                y += xyzCam[1][c] * value;
                z += xyzCam[2][c] * value;
            }
            double fx = cbrt[clip16((int) x)];
            double fy = cbrt[clip16((int) y)];
            double fz = cbrt[clip16((int) z)];
            tileLab[labIndex] = (short) (64 * (116 * fy - 16));
            tileLab[labIndex + 1] = (short) (64 * 500 * (fx - fy));
            tileLab[labIndex + 2] = (short) (64 * 200 * (fy - fz));
        }

        /** LibRaw {@code ahd_interpolate_build_homogeneity_map}. */
        private void ahdBuildHomogeneity(short[] tileLab, int top, int left, byte[] homogeneity) {
            Arrays.fill(homogeneity, (byte) 0);
            int directionStride = AHD_TILE * AHD_TILE * 3;
            int tileRowStride = AHD_TILE * 3;
            int[] neighborOffsets = {-3, 3, -tileRowStride, tileRowStride};
            int[] lDiff = new int[8];
            int[] abDiff = new int[8];
            int rowLimit = Math.min(top + AHD_TILE - 2, height - 4);
            int colLimit = Math.min(left + AHD_TILE - 2, width - 4);
            for (int row = top + 2; row < rowLimit; row++) {
                int tr = row - top;
                for (int col = left + 2; col < colLimit; col++) {
                    int tc = col - left;
                    int tileOffset = (tr * AHD_TILE + tc) * 3;
                    for (int direction = 0; direction < 2; direction++) {
                        int lix = direction * directionStride + tileOffset;
                        for (int i = 0; i < 4; i++) {
                            int adjacent = lix + neighborOffsets[i];
                            lDiff[direction * 4 + i] = Math.abs(tileLab[lix] - tileLab[adjacent]);
                            int da = tileLab[lix + 1] - tileLab[adjacent + 1];
                            int db = tileLab[lix + 2] - tileLab[adjacent + 2];
                            abDiff[direction * 4 + i] = da * da + db * db;
                        }
                    }
                    int leps = Math.min(Math.max(lDiff[0], lDiff[1]), Math.max(lDiff[6], lDiff[7]));
                    int abeps = Math.min(Math.max(abDiff[0], abDiff[1]), Math.max(abDiff[6], abDiff[7]));
                    for (int direction = 0; direction < 2; direction++) {
                        int homogeneous = 0;
                        for (int i = 0; i < 4; i++) {
                            if (lDiff[direction * 4 + i] <= leps && abDiff[direction * 4 + i] <= abeps) {
                                homogeneous++;
                            }
                        }
                        homogeneity[(tr * AHD_TILE + tc) * 2 + direction] = (byte) homogeneous;
                    }
                }
            }
        }

        /** LibRaw {@code ahd_interpolate_combine_homogeneous_pixels}: writes the chosen RGB into the image. */
        private void ahdCombine(char[] image, int top, int left, char[] tileRgb, byte[] homogeneity) {
            int directionStride = AHD_TILE * AHD_TILE * 3;
            int rowLimit = Math.min(top + AHD_TILE - 3, height - 5);
            int colLimit = Math.min(left + AHD_TILE - 3, width - 5);
            for (int row = top + 3; row < rowLimit; row++) {
                int tr = row - top;
                for (int col = left + 3; col < colLimit; col++) {
                    int tc = col - left;
                    int hm0 = 0;
                    int hm1 = 0;
                    for (int i = tr - 1; i <= tr + 1; i++) {
                        for (int j = tc - 1; j <= tc + 1; j++) {
                            int base = (i * AHD_TILE + j) * 2;
                            hm0 += homogeneity[base];
                            hm1 += homogeneity[base + 1];
                        }
                    }
                    int p = (row * width + col) * 4;
                    int tileOffset = (tr * AHD_TILE + tc) * 3;
                    if (hm0 != hm1) {
                        int source = (hm1 > hm0 ? directionStride : 0) + tileOffset;
                        image[p] = tileRgb[source];
                        image[p + 1] = tileRgb[source + 1];
                        image[p + 2] = tileRgb[source + 2];
                    } else {
                        int v = directionStride + tileOffset;
                        image[p] = (char) ((tileRgb[tileOffset] + tileRgb[v]) >> 1);
                        image[p + 1] = (char) ((tileRgb[tileOffset + 1] + tileRgb[v + 1]) >> 1);
                        image[p + 2] = (char) ((tileRgb[tileOffset + 2] + tileRgb[v + 2]) >> 1);
                    }
                }
            }
        }

        /** LibRaw {@code border_interpolate}: fills the outer frame by averaging same-color neighbours. */
        private void borderInterpolate(char[] image, int border) {
            int[] sum = new int[8];
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (col == border && row >= border && row < height - border) {
                        col = width - border;
                    }
                    Arrays.fill(sum, 0);
                    for (int y = row - 1; y <= row + 1; y++) {
                        for (int x = col - 1; x <= col + 1; x++) {
                            if (y >= 0 && y < height && x >= 0 && x < width) {
                                int f = bayerChannel(bayerPattern, y, x);
                                sum[f] += image[(y * width + x) * 4 + f];
                                sum[f + 4]++;
                            }
                        }
                    }
                    int f = bayerChannel(bayerPattern, row, col);
                    for (int c = 0; c < 3; c++) {
                        if (c != f && sum[c + 4] != 0) {
                            image[(row * width + col) * 4 + c] = (char) (sum[c] / sum[c + 4]);
                        }
                    }
                }
            }
        }

        /**
         * Port of LibRaw's {@code convert_to_rgb} (sRGB output, so out_cam == rgb_cam) followed by the
         * 8-bit tone-curve output ({@code curve[value] >> 8}). Only the active crop is emitted.
         */
        private BufferedImage convertToRgb(char[] image, char[] curve) throws IOException {
            BufferedImage output = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            int taskCount = parallelTaskCount(cropHeight, 1);
            runParallelTasks(taskCount, "convert-to-rgb task failed.", taskIndex -> {
                int startY = taskStart(taskIndex, taskCount, cropHeight);
                int endY = taskEnd(taskIndex, taskCount, cropHeight);
                for (int y = startY; y < endY; y++) {
                    int rawRow = cropTop + y;
                    int destBase = y * cropWidth;
                    for (int x = 0; x < cropWidth; x++) {
                        int p = (rawRow * width + cropLeft + x) * 4;
                        int r = image[p];
                        int g = image[p + 1];
                        int b = image[p + 2];
                        int red = clip16((int) (rgbCam[0][0] * r + rgbCam[0][1] * g + rgbCam[0][2] * b));
                        int green = clip16((int) (rgbCam[1][0] * r + rgbCam[1][1] * g + rgbCam[1][2] * b));
                        int blue = clip16((int) (rgbCam[2][0] * r + rgbCam[2][1] * g + rgbCam[2][2] * b));
                        int r8 = curve[red] >> 8;
                        int g8 = curve[green] >> 8;
                        int b8 = curve[blue] >> 8;
                        pixels[destBase + x] = (r8 << 16) | (g8 << 8) | b8;
                    }
                }
            });
            return output;
        }
    }
}
