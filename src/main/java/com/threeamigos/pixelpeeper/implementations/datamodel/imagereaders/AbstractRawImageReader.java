package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.common.util.implementations.concurrency.ParallelTaskExecutor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for the LibRaw-style RAW readers ({@link PanasonicRawImageReader} and
 * {@link CanonCr3RawImageReader}). It gathers the helpers both readers share verbatim: the small
 * clamping primitives, the parallel task partitioning/execution scaffolding, the LibRaw
 * {@code cam_xyz_coeff}/{@code pseudoinverse} camera-to-sRGB matrix maths and the LibRaw
 * {@code gamma_curve} tone-curve builder.
 *
 * <p>Reader-specific decoding, metadata parsing, demosaicing and the per-model colour matrices stay
 * in the concrete subclasses, as do the helpers whose bodies or diagnostics differ between formats
 * (e.g. TIFF {@code typeSize}, {@code normalizeModel}, {@code bayerChannel}, the byte readers).</p>
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

    /** A single unit of parallel work, indexed by task within a {@link #runParallelTasks} batch. */
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

    protected static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
     * is the input value mapped to full scale (65536 with no auto-brightness and bright == 1).
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
     * camera-to-XYZ matrix (integers scaled by 10000) into the camera-to-sRGB(linear) matrix
     * {@code rgb_cam} used by both the colour conversion and the AHD Lab transform. The daylight
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
}
