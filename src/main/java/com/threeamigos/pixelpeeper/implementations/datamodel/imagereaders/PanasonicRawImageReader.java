package com.threeamigos.pixelpeeper.implementations.datamodel.imagereaders;

import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageReader;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A Panasonic RW2 reader for the ImageReader layer.
 *
 * <p>This reader decodes the RW2 sensor data and then renders it following LibRaw's default
 * {@code dcraw_process} pipeline, so the output matches LibRaw's rendering. It reads RW2/TIFF
 * metadata and Panasonic RAW metadata, decompresses/unpacks the sensor data (a faithful port of
 * LibRaw's Panasonic decoders), then applies LibRaw's post-processing: inline per-channel black
 * subtraction and {@code adjust_maximum}, {@code scale_colors} with the as-shot camera white
 * balance and highlight clipping, {@code ahd_interpolate} (AHD demosaic), {@code convert_to_rgb}
 * with a model-specific camera-to-sRGB matrix ({@code cam_xyz_coeff}), and the BT.709 output tone
 * curve ({@code gamma_curve}). It returns an 8-bit {@link BufferedImage} cropped to the active
 * sensor area.</p>
 *
 * <p>The emulated LibRaw parameters are: camera/as-shot white balance ({@code use_camera_wb}),
 * fixed exposure ({@code no_auto_bright}, no histogram auto-brightness), highlight clipping
 * ({@code highlight == 0}), sRGB output colour ({@code output_color == 1}), 8-bit output
 * ({@code output_bps == 8}) and the BT.709 gamma ({@code gamm = {0.45, 4.5}}).</p>
 *
 * <p>It deliberately does not use the embedded JPEG preview and does not perform optical
 * corrections beyond LibRaw's default processing: no chromatic-aberration correction, no lens
 * module corrections, no distortion, perspective or vignetting correction, no sharpening, no
 * denoise, and no camera/DxO-style tone curve, contrast, saturation, vibrance, HSL, or local
 * contrast rendering.</p>
 */
public class PanasonicRawImageReader extends AbstractRawImageReader implements ImageReader {

    private static final int TAG_IMAGE_WIDTH = 0x0100;
    private static final int TAG_IMAGE_LENGTH = 0x0101;
    private static final int TAG_BITS_PER_SAMPLE = 0x0102;
    private static final int TAG_COMPRESSION = 0x0103;
    private static final int TAG_MAKE = 0x010f;
    private static final int TAG_MODEL = 0x0110;
    private static final int TAG_STRIP_OFFSETS = 0x0111;
    private static final int TAG_STRIP_BYTE_COUNTS = 0x0117;
    private static final int TAG_EXIF_IFD = 0x8769;
    private static final int TAG_MAKER_NOTE = 0x927c;

    private static final int PANASONIC_TAG_RAW_WIDTH = 0x0002;
    private static final int PANASONIC_TAG_RAW_HEIGHT = 0x0003;
    private static final int PANASONIC_TAG_SENSOR_TOP = 0x0004;
    private static final int PANASONIC_TAG_SENSOR_LEFT = 0x0005;
    private static final int PANASONIC_TAG_SENSOR_BOTTOM = 0x0006;
    private static final int PANASONIC_TAG_SENSOR_RIGHT = 0x0007;
    private static final int PANASONIC_TAG_CFA_PATTERN = 0x0009;
    private static final int PANASONIC_TAG_BITS_PER_SAMPLE = 0x000a;
    private static final int PANASONIC_TAG_COMPRESSION = 0x000b;
    private static final int PANASONIC_TAG_LINEARITY_LIMIT_RED = 0x000e;
    private static final int PANASONIC_TAG_LINEARITY_LIMIT_GREEN = 0x000f;
    private static final int PANASONIC_TAG_LINEARITY_LIMIT_BLUE = 0x0010;
    private static final int PANASONIC_TAG_RED_BALANCE = 0x0011;
    private static final int PANASONIC_TAG_BLUE_BALANCE = 0x0012;
    private static final int PANASONIC_TAG_BLACK_LEVEL_RED = 0x001c;
    private static final int PANASONIC_TAG_BLACK_LEVEL_GREEN = 0x001d;
    private static final int PANASONIC_TAG_BLACK_LEVEL_BLUE = 0x001e;
    private static final int PANASONIC_TAG_WB_RED_LEVEL = 0x0024;
    private static final int PANASONIC_TAG_WB_GREEN_LEVEL = 0x0025;
    private static final int PANASONIC_TAG_WB_BLUE_LEVEL = 0x0026;
    private static final int PANASONIC_TAG_RAW_FORMAT = 0x002d;
    private static final int PANASONIC_TAG_CROP_TOP = 0x002f;
    private static final int PANASONIC_TAG_CROP_LEFT = 0x0030;
    private static final int PANASONIC_TAG_CROP_BOTTOM = 0x0031;
    private static final int PANASONIC_TAG_CROP_RIGHT = 0x0032;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_TABLE_39 = 0x0039;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_TABLE_3A = 0x003a;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_DATA_MAX = 0x003b;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_INITIAL_0 = 0x003c;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_INITIAL_3 = 0x003f;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_HUFFMAN_40 = 0x0040;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_HUFFMAN_41 = 0x0041;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_COUNT = 0x0042;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_TAG_43 = 0x0043;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_OFFSETS = 0x0044;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_LEFT = 0x0045;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_COMPRESSED_SIZE = 0x0046;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_WIDTH = 0x0047;
    private static final int PANASONIC_TAG_RAW_FORMAT_8_STRIPE_HEIGHT = 0x0048;
    private static final int PANASONIC_TAG_RW2_OFFSET = 0x0118;

    private static final int TYPE_BYTE = 1;
    private static final int TYPE_ASCII = 2;
    private static final int TYPE_SHORT = 3;
    private static final int TYPE_LONG = 4;
    private static final int TYPE_RATIONAL = 5;
    private static final int TYPE_UNDEFINED = 7;
    private static final int TYPE_SLONG = 9;
    private static final int TYPE_SRATIONAL = 10;

    private static final int COMPRESSION_NONE = 1;
    private static final int COMPRESSION_PANASONIC_LOSSLESS = 34316;
    private static final int PANASONIC_RAW_FORMAT_8 = 8;

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    private static final int MIN_DECODE_UNITS_PER_TASK = 262_144;
    private static final int[] BIT_REVERSE_TABLE = buildBitReverseTable();

    // LibRaw dcraw_process defaults emulated here (see LibRaw src/utils/init_close_utils.cpp):
    //   use_camera_wb = 1 (camera/as-shot WB), no_auto_bright = 1 (fixed exposure),
    //   highlight = 0 (clip), output_color = 1 (sRGB), output_bps = 8,
    //   gamm = {0.45, 4.5} (BT.709), user_qual = 3 (AHD), bright = 1.
    private static final double GAMMA_POWER = 0.45;                 // gamm[0]
    private static final double GAMMA_TOE_SLOPE = 4.5;              // gamm[1]
    private static final int OUTPUT_WHITE = 65535;                  // 16-bit full scale
    private static final int GAMMA_IMAX = 0x10000;                  // (0x2000<<3)/bright, no_auto_bright
    private static final double ADJUST_MAXIMUM_THRESHOLD = 0.75;    // LIBRAW_DEFAULT_ADJUST_MAXIMUM_THRESHOLD

    // LibRaw AHD demosaic tile size (libraw/libraw_const.h LIBRAW_AHD_TILE) and its 6-pixel overlap.
    private static final int AHD_TILE = 512;
    private static final int AHD_TILE_OVERLAP = 6;
    private static final int AHD_BORDER = 5;

    // LibRaw_constants::d65_white.
    private static final double[] D65_WHITE = {0.95047, 1.00000, 1.08883};

    private static final CameraMatrix DEFAULT_COLOR_MATRIX = matrix("DC-GH5",
            7641, -2336, -605, -3218, 11299, 2187, -485, 1338, 5121);

    private static final CameraMatrix[] PANASONIC_COLOR_MATRICES = {
            matrix("DC-S1R", 11822, -5321, -1249, -5958, 15114, 766, -614, 1264, 7043),
            matrix("DC-S1H", 9397, -3719, -805, -5425, 13326, 2309, -972, 1715, 6034),
            matrix(new String[]{"DC-S1", "DC-S5"},
                    9744, -3905, -779, -4899, 12807, 2324, -798, 1630, 5827),
            matrix("DC-S5M2", 10308, -4206, -783, -4088, 12102, 2229, -125, 1051, 5912),
            matrix("DC-S9", 9983, -3890, -841, -4180, 12164, 2263, -249, 1139, 5766),

            matrix(new String[]{"DMC-CM1", "DMC-CM10"},
                    8770, -3194, -820, -2871, 11281, 1803, -513, 1552, 4434),

            matrix("DC-FZ1000M2", 9803, -4185, -992, -4066, 12578, 1628, -838, 1824, 5288),
            matrix("DMC-FZ1000", 7830, -2696, -763, -3325, 11667, 1866, -641, 1712, 4824),
            matrix("DMC-FZ2500", 7386, -2443, -743, -3437, 11864, 1757, -608, 1660, 4766),
            matrix("DMC-FZ100", 16197, -6146, -1761, -2393, 10765, 1869, 366, 2238, 5248),
            matrix("DMC-FZ150", 11904, -4541, -1189, -2355, 10899, 1662, -296, 1586, 4289),
            matrix("DMC-FZ200", 8112, -2563, -740, -3730, 11784, 2197, -941, 2075, 4933),
            matrix("DMC-FZ300", 8378, -2798, -769, -3068, 11410, 1877, -538, 1792, 4623),
            matrix("DMC-FZ18", 9932, -3060, -935, -5809, 13331, 2753, -1267, 2155, 5575),
            matrix("DMC-FZ28", 10109, -3488, -993, -5412, 12812, 2916, -1305, 2140, 5543),
            matrix("DMC-FZ30", 10976, -4029, -1141, -7918, 15491, 2600, -1670, 2071, 8246),
            matrix("DMC-FZ35", 9938, -2780, -890, -4604, 12393, 2480, -1117, 2304, 4620),
            matrix("DMC-FZ40", 13639, -5535, -1371, -1698, 9633, 2430, 316, 1152, 4108),
            matrix("DMC-FZ50", 7906, -2709, -594, -6231, 13351, 3220, -1922, 2631, 6537),
            matrix("DMC-FZ70", 11532, -4324, -1066, -2375, 10847, 1749, -564, 1699, 4351),
            matrix(new String[]{"DC-FZ80", "DC-FZ82"},
                    8550, -2908, -842, -3195, 11529, 1881, -338, 1603, 4631),
            matrix("DMC-FZ8", 8986, -2755, -802, -6341, 13575, 3077, -1476, 2144, 6379),

            matrix("DMC-L10", 8025, -1942, -1050, -7920, 15904, 2100, -2456, 3005, 7039),
            matrix("DMC-L1", 8054, -1885, -1025, -8349, 16367, 2040, -2805, 3542, 7629),
            matrix("DMC-LC1", 11340, -4069, -1275, -7555, 15266, 2448, -2960, 3426, 7685),
            matrix("DMC-LF1", 9379, -3267, -816, -3227, 11560, 1881, -926, 1928, 5340),

            matrix("DC-LX100M2", 8585, -3127, -833, -4005, 12250, 1953, -650, 1494, 4862),
            matrix("DMC-LX100", 8844, -3538, -768, -3709, 11762, 2200, -698, 1792, 5220),
            matrix("DMC-LX1", 10704, -4187, -1230, -8314, 15952, 2501, -920, 945, 8927),
            matrix("DMC-LX2", 8048, -2810, -623, -6450, 13519, 3272, -1700, 2146, 7049),
            matrix("DMC-LX3", 8128, -2668, -655, -6134, 13307, 3161, -1782, 2568, 6083),
            matrix("DMC-LX5", 10909, -4295, -948, -1333, 9306, 2399, 22, 1738, 4582),
            matrix("DMC-LX7", 10148, -3743, -991, -2837, 11366, 1659, -701, 1893, 4899),
            matrix("DMC-LX9", 7790, -2736, -755, -3452, 11870, 1769, -628, 1647, 4898),

            matrix("DMC-FX150", 9082, -2907, -925, -6119, 13377, 3058, -1797, 2641, 5609),

            matrix(new String[]{"DC-G99", "DC-G90", "DC-G91", "DC-G95"},
                    9657, -3963, -748, -3361, 11378, 2258, -568, 1415, 5158),
            matrix("DC-G100", 8370, -2869, -710, -3389, 11372, 2298, -640, 1599, 4887),
            matrix("DMC-G10", 10113, -3400, -1114, -4765, 12683, 2317, -377, 1437, 6710),
            matrix("DMC-G1", 8199, -2065, -1056, -8124, 16156, 2033, -2458, 3022, 7220),
            matrix("DMC-G2", 10113, -3400, -1114, -4765, 12683, 2317, -377, 1437, 6710),
            matrix("DMC-G3", 6763, -1919, -863, -3868, 11515, 2684, -1216, 2387, 5879),
            matrix("DMC-G5", 7798, -2562, -740, -3879, 11584, 2613, -1055, 2248, 5434),
            matrix("DMC-G6", 8294, -2891, -651, -3869, 11590, 2595, -1183, 2267, 5352),
            matrix("DMC-G7", 7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix(new String[]{"DMC-G8", "DMC-G80", "DMC-G81", "DMC-G85"},
                    7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix("DC-G9M2", 8325, -3456, -623, -4330, 12089, 2528, -860, 2646, 5984),
            matrix("DC-G9", 7685, -2375, -634, -3687, 11700, 2249, -748, 1546, 5111),

            matrix("DMC-GH1", 6299, -1466, -532, -6535, 13852, 2969, -2331, 3112, 5984),
            matrix("DMC-GH2", 7780, -2410, -806, -3913, 11724, 2484, -1018, 2390, 5298),
            matrix("DMC-GH3", 6559, -1752, -491, -3672, 11407, 2586, -962, 1875, 5130),
            matrix("DMC-GH4", 7122, -2108, -512, -3155, 11201, 2231, -541, 1423, 5045),
            matrix("DC-GH5S", 6929, -2355, -708, -4192, 12534, 1828, -1097, 1989, 5195),
            matrix("DC-GH5M2", 9300, -3659, -755, -2981, 10988, 2287, -190, 1077, 5016),
            matrix("DC-GH5", 7641, -2336, -605, -3218, 11299, 2187, -485, 1338, 5121),
            matrix("DC-GH6", 7949, -3491, -710, -3435, 11681, 1977, -503, 1622, 5065),
            matrix("DC-GH7", 8573, -3575, -678, -4252, 12079, 2451, -808, 2524, 5936),

            matrix("DMC-GM1", 6770, -1895, -744, -5232, 13145, 2303, -1664, 2691, 5703),
            matrix("DMC-GM5", 8238, -3244, -679, -3921, 11814, 2384, -836, 2022, 5852),

            matrix("DC-GF10", 7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix("DMC-GF1", 7888, -1902, -1011, -8106, 16085, 2099, -2353, 2866, 7330),
            matrix("DMC-GF2", 7888, -1902, -1011, -8106, 16085, 2099, -2353, 2866, 7330),
            matrix("DMC-GF3", 9051, -2468, -1204, -5212, 13276, 2121, -1197, 2510, 6890),
            matrix("DMC-GF5", 8228, -2945, -660, -3938, 11792, 2430, -1094, 2278, 5793),
            matrix("DMC-GF6", 8130, -2801, -946, -3520, 11289, 2552, -1314, 2511, 5791),
            matrix("DMC-GF7", 7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix("DMC-GF8", 7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix(new String[]{"DC-GF9", "DMC-GF9", "DMC-GX800", "DMC-GX850"},
                    7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),

            matrix(new String[]{"DMC-GX85", "DMC-GX80", "DMC-GX7MK2"},
                    7771, -3020, -629, -4029, 11950, 2345, -821, 1977, 6119),
            matrix("DMC-GX1", 6763, -1919, -863, -3868, 11515, 2684, -1216, 2387, 5879),
            matrix("DMC-GX7", 7610, -2780, -576, -4614, 12195, 2733, -1375, 2393, 6490),
            matrix("DMC-GX8", 7564, -2263, -606, -3148, 11239, 2177, -540, 1435, 4853),
            matrix("DC-GX9", 7564, -2263, -606, -3148, 11239, 2177, -540, 1435, 4853),

            matrix(new String[]{"DMC-ZS100", "DMC-TZ100", "DMC-TZ101", "DMC-TZ110"},
                    7790, -2736, -755, -3452, 11870, 1769, -628, 1647, 4898),
            matrix(new String[]{"DC-ZS200", "DC-TZ200", "DC-TZ202"},
                    7790, -2736, -755, -3452, 11870, 1769, -628, 1647, 4898),
            matrix(new String[]{"DMC-ZS40", "DMC-TZ60"},
                    8607, -2822, -808, -3755, 11930, 2049, -820, 2060, 5224),
            matrix(new String[]{"DMC-ZS50", "DMC-TZ70"},
                    8802, -3135, -789, -3151, 11468, 1904, -550, 1745, 4810),
            matrix(new String[]{"DMC-ZS60", "DMC-TZ80", "DMC-TZ81"},
                    8550, -2908, -842, -3195, 11529, 1881, -338, 1603, 4631),
            matrix(new String[]{"DC-ZS70", "DC-TZ90", "DC-TZ91", "DC-TZ92"},
                    9052, -3117, -883, -3045, 11346, 1927, -205, 1520, 4730),
            matrix(new String[]{"DC-ZS80", "DC-TZ95", "DC-TZ96"},
                    12194, -5340, -1329, -3035, 11394, 1858, -50, 1418, 5219)
    };

    @Override
    public BufferedImage readImage(File file) throws Exception {
        byte[] data = Files.readAllBytes(file.toPath());
        RawMetadata metadata = new TiffMetadataReader(data).read();
        metadata.finish(data.length);

        int[] raw = decodeRaw(data, metadata);
        return render(raw, metadata);
    }

    private int[] decodeRaw(byte[] data, RawMetadata metadata) throws IOException {
        if (metadata.panasonicEncoding == PANASONIC_RAW_FORMAT_8) {
            return decodePanasonicRawFormat8(data, metadata);
        }

        if (metadata.panasonicEncoding == 5) {
            return decodePanasonicPacked(data, metadata);
        }

        if (metadata.compression == COMPRESSION_NONE) {
            return decodeUncompressed(data, metadata);
        }

        if (metadata.compression == COMPRESSION_PANASONIC_LOSSLESS
                || metadata.panasonicEncoding > 0
                || metadata.panasonicRaw) {
            return decodePanasonicLossless(data, metadata);
        }

        throw new IOException("Unsupported Panasonic RW2 compression: " + metadata.compression);
    }

    private int[] decodeUncompressed(byte[] data, RawMetadata metadata) throws IOException {
        int pixelCount = checkedPixelCount(metadata);
        int[] raw = new int[pixelCount];

        if (metadata.bitsPerSample == 12 && metadata.rawByteCount >= pixelCount * 2L) {
            decodeLittleEndianContainers(data, metadata, raw);
        } else if (metadata.bitsPerSample == 12) {
            decodePacked12BigEndian(data, metadata, raw);
        } else if (metadata.bitsPerSample == 14 && metadata.rawByteCount >= pixelCount * 2L) {
            decodeLittleEndianContainers(data, metadata, raw);
        } else if (metadata.bitsPerSample == 14) {
            decodePacked14LittleEndian(data, metadata, raw);
        } else {
            throw new IOException("Unsupported Panasonic RW2 bits per sample: " + metadata.bitsPerSample);
        }

        return raw;
    }

    private void decodeLittleEndianContainers(byte[] data, RawMetadata metadata, int[] raw) throws IOException {
        int max = (1 << metadata.bitsPerSample) - 1;
        int shift = shouldShift16BitContainers(data, metadata, max) ? 16 - metadata.bitsPerSample : 0;
        ensureAvailable(data, metadata.rawOffset, raw.length * 2L);

        int taskCount = parallelTaskCount(raw.length, MIN_DECODE_UNITS_PER_TASK);
        runParallelTasks(taskCount, "Panasonic uncompressed container decode failed.", taskIndex -> {
            int start = taskStart(taskIndex, taskCount, raw.length);
            int end = taskEnd(taskIndex, taskCount, raw.length);
            for (int i = start; i < end; i++) {
                int offset = metadata.rawOffset + i * 2;
                int value = unsignedShortLittleEndian(data, offset) >> shift;
                raw[i] = clamp(value, 0, max);
            }
        });
    }

    private boolean shouldShift16BitContainers(byte[] data, RawMetadata metadata, int max) throws IOException {
        int samples = Math.min(1024, checkedPixelCount(metadata));
        int offset = metadata.rawOffset;
        for (int i = 0; i < samples; i++) {
            ensureAvailable(data, offset, 2);
            if (unsignedShortLittleEndian(data, offset) > max) {
                return true;
            }
            offset += 2;
        }
        return false;
    }

    private void decodePacked12BigEndian(byte[] data, RawMetadata metadata, int[] raw) throws IOException {
        int max = (1 << metadata.bitsPerSample) - 1;
        int groups = (raw.length + 1) / 2;
        ensureAvailable(data, metadata.rawOffset, groups * 3L);

        int taskCount = parallelTaskCount(groups, MIN_DECODE_UNITS_PER_TASK);
        runParallelTasks(taskCount, "Panasonic packed 12-bit decode failed.", taskIndex -> {
            int startGroup = taskStart(taskIndex, taskCount, groups);
            int endGroup = taskEnd(taskIndex, taskCount, groups);
            for (int group = startGroup; group < endGroup; group++) {
                int offset = metadata.rawOffset + group * 3;
                int i = group * 2;
                int byte0 = data[offset] & 0xff;
                int byte1 = data[offset + 1] & 0xff;
                int byte2 = data[offset + 2] & 0xff;
                raw[i] = clamp((byte0 << 4) | (byte1 >> 4), 0, max);
                if (i + 1 < raw.length) {
                    raw[i + 1] = clamp(((byte1 & 0x0f) << 8) | byte2, 0, max);
                }
            }
        });
    }

    private void decodePacked14LittleEndian(byte[] data, RawMetadata metadata, int[] raw) throws IOException {
        int max = (1 << metadata.bitsPerSample) - 1;
        int groups = (raw.length + 3) / 4;
        ensureAvailable(data, metadata.rawOffset, groups * 7L);

        int taskCount = parallelTaskCount(groups, MIN_DECODE_UNITS_PER_TASK);
        runParallelTasks(taskCount, "Panasonic packed 14-bit decode failed.", taskIndex -> {
            int startGroup = taskStart(taskIndex, taskCount, groups);
            int endGroup = taskEnd(taskIndex, taskCount, groups);
            for (int group = startGroup; group < endGroup; group++) {
                int offset = metadata.rawOffset + group * 7;
                int i = group * 4;
                int byte0 = data[offset] & 0xff;
                int byte1 = data[offset + 1] & 0xff;
                int byte2 = data[offset + 2] & 0xff;
                int byte3 = data[offset + 3] & 0xff;
                int byte4 = data[offset + 4] & 0xff;
                int byte5 = data[offset + 5] & 0xff;
                int byte6 = data[offset + 6] & 0xff;

                raw[i] = clamp(byte0 | ((byte1 & 0x3f) << 8), 0, max);
                if (i + 1 < raw.length) {
                    raw[i + 1] = clamp((byte1 >> 6) | (byte2 << 2) | ((byte3 & 0x0f) << 10), 0, max);
                }
                if (i + 2 < raw.length) {
                    raw[i + 2] = clamp((byte3 >> 4) | (byte4 << 4) | ((byte5 & 0x03) << 12), 0, max);
                }
                if (i + 3 < raw.length) {
                    raw[i + 3] = clamp(((byte5 & 0xfc) >> 2) | (byte6 << 6), 0, max);
                }
            }
        });
    }

    private int[] decodePanasonicPacked(byte[] data, RawMetadata metadata) throws IOException {
        int[] raw = new int[checkedPixelCount(metadata)];
        PanasonicDataReader reader = new PanasonicDataReader(data, metadata);
        int[] bytes = new int[16];
        int blockSize = metadata.bitsPerSample == 12 ? 10 : 9;

        if (metadata.bitsPerSample != 12 && metadata.bitsPerSample != 14) {
            throw new IOException("Unsupported Panasonic packed bits per sample: " + metadata.bitsPerSample);
        }

        for (int row = 0; row < metadata.rawHeight; row++) {
            for (int col = 0; col < metadata.rawWidth; col += blockSize) {
                reader.readBlock(bytes);
                int index = row * metadata.rawWidth + col;
                if (metadata.bitsPerSample == 12) {
                    putRaw(raw, index, col, metadata.rawWidth, 0, ((bytes[1] & 0x0f) << 8) | bytes[0]);
                    putRaw(raw, index, col, metadata.rawWidth, 1, (bytes[2] << 4) | (bytes[1] >> 4));
                    putRaw(raw, index, col, metadata.rawWidth, 2, ((bytes[4] & 0x0f) << 8) | bytes[3]);
                    putRaw(raw, index, col, metadata.rawWidth, 3, (bytes[5] << 4) | (bytes[4] >> 4));
                    putRaw(raw, index, col, metadata.rawWidth, 4, ((bytes[7] & 0x0f) << 8) | bytes[6]);
                    putRaw(raw, index, col, metadata.rawWidth, 5, (bytes[8] << 4) | (bytes[7] >> 4));
                    putRaw(raw, index, col, metadata.rawWidth, 6, ((bytes[10] & 0x0f) << 8) | bytes[9]);
                    putRaw(raw, index, col, metadata.rawWidth, 7, (bytes[11] << 4) | (bytes[10] >> 4));
                    putRaw(raw, index, col, metadata.rawWidth, 8, ((bytes[13] & 0x0f) << 8) | bytes[12]);
                    putRaw(raw, index, col, metadata.rawWidth, 9, (bytes[14] << 4) | (bytes[13] >> 4));
                } else {
                    putRaw(raw, index, col, metadata.rawWidth, 0, bytes[0] | ((bytes[1] & 0x3f) << 8));
                    putRaw(raw, index, col, metadata.rawWidth, 1,
                            (bytes[1] >> 6) | (bytes[2] << 2) | ((bytes[3] & 0x0f) << 10));
                    putRaw(raw, index, col, metadata.rawWidth, 2,
                            (bytes[3] >> 4) | (bytes[4] << 4) | ((bytes[5] & 0x03) << 12));
                    putRaw(raw, index, col, metadata.rawWidth, 3, ((bytes[5] & 0xfc) >> 2) | (bytes[6] << 6));
                    putRaw(raw, index, col, metadata.rawWidth, 4, bytes[7] | ((bytes[8] & 0x3f) << 8));
                    putRaw(raw, index, col, metadata.rawWidth, 5,
                            (bytes[8] >> 6) | (bytes[9] << 2) | ((bytes[10] & 0x0f) << 10));
                    putRaw(raw, index, col, metadata.rawWidth, 6,
                            (bytes[10] >> 4) | (bytes[11] << 4) | ((bytes[12] & 0x03) << 12));
                    putRaw(raw, index, col, metadata.rawWidth, 7, ((bytes[12] & 0xfc) >> 2) | (bytes[13] << 6));
                    putRaw(raw, index, col, metadata.rawWidth, 8, bytes[14] | ((bytes[15] & 0x3f) << 8));
                }
            }
        }

        return raw;
    }

    private int[] decodePanasonicRawFormat8(byte[] data, RawMetadata metadata) throws IOException {
        int[] raw = new int[checkedPixelCount(metadata)];
        PanasonicRawFormat8Metadata rawFormat8 = metadata.rawFormat8;
        validatePanasonicRawFormat8(data, metadata, rawFormat8);

        PanasonicRawFormat8Parameters parameters = new PanasonicRawFormat8Parameters(rawFormat8);
        runParallelTasks(rawFormat8.stripeCount, "Panasonic RW2 RawFormat 8 stripe decode failed.", stripe -> {
            PanasonicRawFormat8Buffer buffer = new PanasonicRawFormat8Buffer(
                    data,
                    rawFormat8.stripeOffsets[stripe],
                    exactBytesForCompressedBits(rawFormat8.stripeCompressedSize[stripe]));
            if (!parameters.decodeStrip(buffer, raw, metadata.rawWidth, metadata.rawHeight,
                    rawFormat8.stripeWidth[stripe], rawFormat8.stripeHeight[stripe],
                    rawFormat8.stripeLeft[stripe])) {
                throw new IOException("Invalid Panasonic RW2 RawFormat 8 compressed stripe.");
            }
        });

        return raw;
    }

    private void validatePanasonicRawFormat8(byte[] data, RawMetadata metadata,
                                             PanasonicRawFormat8Metadata rawFormat8) throws IOException {
        if (rawFormat8.stripeCount <= 0 || rawFormat8.stripeCount > 5) {
            throw new IOException("Invalid Panasonic RW2 RawFormat 8 stripe count: " + rawFormat8.stripeCount);
        }

        boolean[] coveredColumns = new boolean[metadata.rawWidth];
        int coveredWidth = 0;
        for (int stripe = 0; stripe < rawFormat8.stripeCount; stripe++) {
            int stripeWidth = rawFormat8.stripeWidth[stripe];
            int stripeHeight = rawFormat8.stripeHeight[stripe];
            int stripeLeft = rawFormat8.stripeLeft[stripe];
            long stripeOffset = rawFormat8.stripeOffsets[stripe];
            int stripeBytes = exactBytesForCompressedBits(rawFormat8.stripeCompressedSize[stripe]);

            if (stripeWidth <= 0 || stripeHeight != metadata.rawHeight) {
                throw new IOException("Invalid Panasonic RW2 RawFormat 8 stripe dimensions.");
            }
            if (stripeLeft < 0 || stripeLeft + stripeWidth > metadata.rawWidth) {
                throw new IOException("Invalid Panasonic RW2 RawFormat 8 stripe position.");
            }
            if (stripeOffset < 0 || stripeOffset > data.length - (long) stripeBytes) {
                throw new EOFException("Unexpected end of Panasonic RW2 RawFormat 8 stripe data.");
            }
            for (int col = stripeLeft; col < stripeLeft + stripeWidth; col++) {
                if (coveredColumns[col]) {
                    throw new IOException("Panasonic RW2 RawFormat 8 stripes overlap.");
                }
                coveredColumns[col] = true;
                coveredWidth++;
            }
        }

        if (coveredWidth != metadata.rawWidth) {
            throw new IOException("Panasonic RW2 RawFormat 8 stripes do not cover the raw width.");
        }
    }

    private int exactBytesForCompressedBits(long compressedBits) throws IOException {
        if (compressedBits <= 0) {
            throw new IOException("Invalid Panasonic RW2 RawFormat 8 compressed stripe size.");
        }
        long exactBytes = (compressedBits + 7L) / 8L;
        if (exactBytes <= 0 || exactBytes > Integer.MAX_VALUE) {
            throw new IOException("Invalid Panasonic RW2 RawFormat 8 compressed stripe size: " + compressedBits);
        }
        return (int) exactBytes;
    }

    private void putRaw(int[] raw, int index, int col, int width, int offset, int value) {
        if (col + offset < width) {
            raw[index + offset] = value;
        }
    }

    private int[] decodePanasonicLossless(byte[] data, RawMetadata metadata) throws IOException {
        if (metadata.bitsPerSample != 12) {
            throw new IOException("Panasonic predictor compression is only implemented for 12-bit RW2 data.");
        }

        int[] raw = new int[checkedPixelCount(metadata)];
        PanasonicDataReader reader = new PanasonicDataReader(data, metadata);
        int max = (1 << metadata.bitsPerSample) - 1;
        int sh = 0;
        int[] pred = new int[2];
        int[] nonz = new int[2];

        for (int row = 0; row < metadata.rawHeight; row++) {
            for (int col = 0; col < metadata.rawWidth; col++) {
                int i = col % 14;
                int predictorIndex = i & 1;

                if (i == 0) {
                    pred[0] = 0;
                    pred[1] = 0;
                    nonz[0] = 0;
                    nonz[1] = 0;
                }

                if (i % 3 == 2) {
                    sh = 4 >> (3 - reader.readBits(2));
                }

                if (nonz[predictorIndex] != 0) {
                    int j = reader.readBits(8);
                    if (j != 0) {
                        pred[predictorIndex] -= 0x80 << sh;
                        if (pred[predictorIndex] < 0 || sh == 4) {
                            pred[predictorIndex] &= (1 << sh) - 1;
                        }
                        pred[predictorIndex] += j << sh;
                    }
                } else {
                    nonz[predictorIndex] = reader.readBits(8);
                    if (nonz[predictorIndex] != 0 || i > 11) {
                        pred[predictorIndex] = (nonz[predictorIndex] << 4) | reader.readBits(4);
                    }
                }

                raw[row * metadata.rawWidth + col] = clamp(pred[col & 1], 0, max);
            }
        }

        return raw;
    }

    /**
     * Renders the decoded Bayer mosaic to an 8-bit sRGB image following LibRaw's default
     * {@code dcraw_process} pipeline: black subtraction and white-point handling
     * ({@code raw2image}/{@code adjust_maximum}), camera white balance and per-channel scaling
     * ({@code scale_colors}), AHD demosaicing ({@code ahd_interpolate}), camera-to-sRGB colour
     * conversion ({@code convert_to_rgb}), and the BT.709 output tone curve ({@code gamma_curve}).
     * The emulated parameters are camera/as-shot white balance, highlight clipping, fixed exposure
     * (no auto-brightness), sRGB output colour and 8-bit output.
     */
    private BufferedImage render(int[] raw, RawMetadata metadata) throws IOException {
        int width = metadata.rawWidth;
        int height = metadata.rawHeight;
        double[][] rgbCam = metadata.colorMatrix.rgbCam;

        char[] image = scaleColors(raw, metadata, width, height);
        ahdInterpolate(image, width, height, metadata, rgbCam);
        char[] curve = buildGammaCurve(GAMMA_POWER, GAMMA_TOE_SLOPE, GAMMA_IMAX);
        return convertToRgb(image, width, metadata, rgbCam, curve);
    }

    /**
     * Port of LibRaw's inline black subtraction ({@code copy_bayer}), {@code adjust_maximum} and
     * {@code scale_colors}. Produces the 4-plane interleaved image LibRaw's demosaic consumes, with
     * the single Bayer sample of each site placed in its colour plane (both greens in plane 1) after
     * per-channel black subtraction and white-balance scaling, clipped to 16-bit range.
     */
    private char[] scaleColors(int[] raw, RawMetadata metadata, int width, int height) throws IOException {
        int redBlack = metadata.blackLevel(RED);
        int greenBlack = metadata.blackLevel(GREEN);
        int blueBlack = metadata.blackLevel(BLUE);
        int minBlack = Math.min(redBlack, Math.min(greenBlack, blueBlack));

        // data_maximum: max of the black-subtracted (floored) samples, matching copy_bayer.
        // Computed as a parallel reduction over row bands (each task writes its own slot).
        int dataMaxTaskCount = parallelTaskCount(height, 1);
        int[] dataMaxPerTask = new int[dataMaxTaskCount];
        runParallelTasks(dataMaxTaskCount, "Panasonic data-maximum task failed.", taskIndex -> {
            int startRow = taskStart(taskIndex, dataMaxTaskCount, height);
            int endRow = taskEnd(taskIndex, dataMaxTaskCount, height);
            int localMaximum = 0;
            for (int row = startRow; row < endRow; row++) {
                int base = row * width;
                for (int col = 0; col < width; col++) {
                    int value = raw[base + col] - metadata.blackLevel(bayerChannel(metadata, row, col));
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
        int maximum = ((1 << metadata.bitsPerSample) - 1) - minBlack;
        if (dataMaximum > 0 && dataMaximum < maximum && dataMaximum > maximum * ADJUST_MAXIMUM_THRESHOLD) {
            maximum = dataMaximum;
        }
        if (maximum <= 0) {
            maximum = 1;
        }

        // Camera (as-shot) white balance: pre_mul == cam_mul, green == 1, pre_mul[3] == pre_mul[1].
        double[] preMul = {
                metadata.whiteBalance[RED], metadata.whiteBalance[GREEN], metadata.whiteBalance[BLUE],
                metadata.whiteBalance[GREEN]
        };
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
            throw new IOException("Panasonic RW2 image is too large to render: " + width + "x" + height);
        }
        char[] image = new char[(int) (pixelCount * 4L)];
        int taskCount = parallelTaskCount(height, 1);
        runParallelTasks(taskCount, "Panasonic scale-colors task failed.", taskIndex -> {
            int startRow = taskStart(taskIndex, taskCount, height);
            int endRow = taskEnd(taskIndex, taskCount, height);
            for (int row = startRow; row < endRow; row++) {
                int base = row * width;
                for (int col = 0; col < width; col++) {
                    int channel = bayerChannel(metadata, row, col);
                    int value = raw[base + col] - metadata.blackLevel(channel);
                    if (value < 0) {
                        value = 0;
                    }
                    int scaled = clip16((int) (value * scaleMul[channel]));
                    image[(base + col) * 4 + channel] = (char) scaled;
                }
            }
        });
        return image;
    }

    /**
     * Port of LibRaw's AHD demosaic ({@code ahd_interpolate} in src/demosaic/ahd_demosaic.cpp):
     * fills the R/G/B planes of {@code image} in place. The five-pixel frame is filled by
     * {@link #borderInterpolate} and the interior is processed in overlapping tiles. Tiles are
     * distributed across parallel tasks by tile row; per-tile scratch buffers are thread-local.
     */
    private void ahdInterpolate(char[] image, int width, int height, RawMetadata metadata, double[][] rgbCam)
            throws IOException {
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

        borderInterpolate(image, width, height, metadata, AHD_BORDER);

        // Enumerate every overlapping tile (top-left corners). Distinct tiles write disjoint image
        // regions, so they can all run in parallel; tiles are spread round-robin over a pool capped
        // at the available cores (each thread reuses one set of scratch buffers across its tiles).
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
        runParallelTasks(taskCount, "Panasonic AHD interpolation task failed.", taskIndex -> {
            int directionStride = AHD_TILE * AHD_TILE * 3;
            char[] tileRgb = new char[2 * directionStride];
            short[] tileLab = new short[2 * directionStride];
            byte[] homogeneity = new byte[AHD_TILE * AHD_TILE * 2];
            for (int tile = taskIndex; tile < totalTiles; tile += taskCount) {
                int top = tileTops[tile];
                int left = tileLefts[tile];
                ahdInterpolateGreen(image, width, height, metadata, top, left, tileRgb);
                ahdInterpolateRedBlue(image, width, height, metadata, top, left, tileRgb, tileLab, xyzCam, cbrt);
                ahdBuildHomogeneity(tileLab, top, left, width, height, homogeneity);
                ahdCombine(image, width, height, top, left, tileRgb, homogeneity);
            }
        });
    }

    /** LibRaw {@code ahd_interpolate_green_h_and_v}: green interpolation in both directions. */
    private void ahdInterpolateGreen(char[] image, int width, int height, RawMetadata metadata,
                                     int top, int left, char[] tileRgb) {
        int rowStride = width * 4;
        int directionStride = AHD_TILE * AHD_TILE * 3;
        int rowLimit = Math.min(top + AHD_TILE, height - 2);
        int colLimit = Math.min(left + AHD_TILE, width - 2);
        for (int row = top; row < rowLimit; row++) {
            int col = left + (bayerChannel(metadata, row, left) & 1);
            int channel = bayerChannel(metadata, row, col);
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
     * colours for both directions and converts each candidate to CIE Lab.
     */
    private void ahdInterpolateRedBlue(char[] image, int width, int height, RawMetadata metadata,
                                       int top, int left, char[] tileRgb, short[] tileLab,
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
                    int channel = 2 - bayerChannel(metadata, row, col);
                    int value;
                    if (channel == 1) {
                        channel = bayerChannel(metadata, row + 1, col);
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
                    channel = bayerChannel(metadata, row, col);
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
    private void ahdBuildHomogeneity(short[] tileLab, int top, int left, int width, int height, byte[] homogeneity) {
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

    /** LibRaw {@code ahd_interpolate_combine_homogeneous_pixels}: writes the chosen RGB into image. */
    private void ahdCombine(char[] image, int width, int height, int top, int left,
                            char[] tileRgb, byte[] homogeneity) {
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
                    int h = tileOffset;
                    int v = directionStride + tileOffset;
                    image[p] = (char) ((tileRgb[h] + tileRgb[v]) >> 1);
                    image[p + 1] = (char) ((tileRgb[h + 1] + tileRgb[v + 1]) >> 1);
                    image[p + 2] = (char) ((tileRgb[h + 2] + tileRgb[v + 2]) >> 1);
                }
            }
        }
    }

    /** LibRaw {@code border_interpolate}: fills the outer frame by averaging same-colour neighbours. */
    private void borderInterpolate(char[] image, int width, int height, RawMetadata metadata, int border) {
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
                            int f = bayerChannel(metadata, y, x);
                            sum[f] += image[(y * width + x) * 4 + f];
                            sum[f + 4]++;
                        }
                    }
                }
                int f = bayerChannel(metadata, row, col);
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
    private BufferedImage convertToRgb(char[] image, int width, RawMetadata metadata,
                                       double[][] rgbCam, char[] curve) throws IOException {
        BufferedImage output = new BufferedImage(metadata.cropWidth, metadata.cropHeight,
                BufferedImage.TYPE_INT_RGB);
        int[] pixels = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
        int taskCount = parallelTaskCount(metadata.cropHeight, 1);
        runParallelTasks(taskCount, "Panasonic convert-to-rgb task failed.", taskIndex -> {
            int startY = taskStart(taskIndex, taskCount, metadata.cropHeight);
            int endY = taskEnd(taskIndex, taskCount, metadata.cropHeight);
            for (int y = startY; y < endY; y++) {
                int rawRow = metadata.cropTop + y;
                int destBase = y * metadata.cropWidth;
                for (int x = 0; x < metadata.cropWidth; x++) {
                    int p = (rawRow * width + metadata.cropLeft + x) * 4;
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

    private int bayerChannel(RawMetadata metadata, int row, int col) {
        return metadata.bayerPattern[((row & 1) << 1) | (col & 1)];
    }

    private boolean insideRaw(RawMetadata metadata, int row, int col) {
        return row >= 0 && row < metadata.rawHeight && col >= 0 && col < metadata.rawWidth;
    }

    private int checkedPixelCount(RawMetadata metadata) throws IOException {
        long pixelCount = (long) metadata.rawWidth * metadata.rawHeight;
        if (pixelCount <= 0 || pixelCount > Integer.MAX_VALUE) {
            throw new IOException("Invalid Panasonic RW2 raw dimensions: "
                    + metadata.rawWidth + "x" + metadata.rawHeight);
        }
        return (int) pixelCount;
    }

    private static int unsignedShortLittleEndian(byte[] data, int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
    }

    private void ensureAvailable(byte[] data, int offset, int length) throws EOFException {
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new EOFException("Unexpected end of Panasonic RW2 raw data.");
        }
    }

    private void ensureAvailable(byte[] data, int offset, long length) throws EOFException {
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new EOFException("Unexpected end of Panasonic RW2 raw data.");
        }
    }

    private static int[] buildBitReverseTable() {
        int[] table = new int[256];
        for (int value = 0; value < table.length; value++) {
            int source = value;
            int reversed = 0;
            for (int bit = 0; bit < 8; bit++) {
                reversed = (reversed << 1) | (source & 1);
                source >>= 1;
            }
            table[value] = reversed;
        }
        return table;
    }

    private static CameraMatrix matrix(String modelPrefix, int... adobeColorMatrix) {
        return matrix(new String[]{modelPrefix}, adobeColorMatrix);
    }

    private static CameraMatrix matrix(String[] modelPrefixes, int... adobeColorMatrix) {
        return new CameraMatrix(modelPrefixes, adobeColorMatrix);
    }

    private static CameraMatrix findColorMatrix(String model) {
        String normalizedModel = normalizeModel(model);
        if (!normalizedModel.isEmpty()) {
            for (CameraMatrix matrix : PANASONIC_COLOR_MATRICES) {
                if (matrix.matches(normalizedModel)) {
                    return matrix;
                }
            }
        }
        return DEFAULT_COLOR_MATRIX;
    }

    private static String normalizeModel(String model) {
        if (model == null) {
            return "";
        }
        return model.trim().toUpperCase(Locale.ROOT);
    }

    private static class CameraMatrix {

        private final String[] modelPrefixes;
        private final double[][] rgbCam;

        private CameraMatrix(String[] modelPrefixes, int[] adobeColorMatrix) {
            this.modelPrefixes = new String[modelPrefixes.length];
            for (int i = 0; i < modelPrefixes.length; i++) {
                this.modelPrefixes[i] = normalizeModel(modelPrefixes[i]);
            }
            this.rgbCam = camXyzCoeff(adobeColorMatrix);
        }

        private boolean matches(String normalizedModel) {
            for (String modelPrefix : modelPrefixes) {
                if (normalizedModel.equals(modelPrefix)
                        || (normalizedModel.startsWith(modelPrefix)
                        && hasModelBoundary(normalizedModel, modelPrefix.length()))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class RawMetadata {

        private boolean panasonicRaw;
        private String make;
        private String model;
        private int rawWidth;
        private int rawHeight;
        private int fallbackWidth;
        private int fallbackHeight;
        private int bitsPerSample;
        private int compression;
        private int panasonicEncoding;
        private int cfaPattern = -1;
        private int rawOffset = -1;
        private long rawByteCount = -1;
        private int cropTop = -1;
        private int cropLeft = -1;
        private int cropBottom = -1;
        private int cropRight = -1;
        private int cropWidth;
        private int cropHeight;
        private final int[] blackLevel = {-1, -1, -1};
        private final int[] linearityLimit = {-1, -1, -1};
        private int redBalance = -1;
        private int blueBalance = -1;
        private int wbRedLevel = -1;
        private int wbGreenLevel = -1;
        private int wbBlueLevel = -1;
        private final double[] whiteBalance = {1.0, 1.0, 1.0};
        private final int[] whiteLevel = {-1, -1, -1};
        private CameraMatrix colorMatrix = DEFAULT_COLOR_MATRIX;
        private final int[] bayerPattern = {RED, GREEN, GREEN, BLUE};
        private final PanasonicRawFormat8Metadata rawFormat8 = new PanasonicRawFormat8Metadata();

        private void finish(int fileLength) throws IOException {
            if (rawWidth <= 0) {
                rawWidth = fallbackWidth;
            }
            if (rawHeight <= 0) {
                rawHeight = fallbackHeight;
            }
            if (bitsPerSample <= 0) {
                bitsPerSample = 12;
            }
            if (rawByteCount < 0 && rawOffset >= 0) {
                rawByteCount = fileLength - rawOffset;
            }

            if (rawWidth <= 0 || rawHeight <= 0) {
                throw new IOException("Panasonic RW2 raw dimensions were not found.");
            }
            if (rawOffset < 0 || rawOffset >= fileLength) {
                throw new IOException("Panasonic RW2 raw data offset was not found.");
            }
            if (rawByteCount <= 0 || rawOffset + rawByteCount > fileLength) {
                rawByteCount = fileLength - rawOffset;
            }

            finishCrop();
            finishBlackLevels();
            finishWhiteLevels();
            finishWhiteBalance();
            finishColorMatrix();
            finishBayerPattern();
        }

        private void finishCrop() {
            int top = Math.max(cropTop, 0);
            int left = Math.max(cropLeft, 0);
            int bottom = cropBottom > top ? cropBottom : rawHeight;
            int right = cropRight > left ? cropRight : rawWidth;

            cropTop = clamp(top, 0, rawHeight - 1);
            cropLeft = clamp(left, 0, rawWidth - 1);
            cropBottom = clamp(bottom, cropTop + 1, rawHeight);
            cropRight = clamp(right, cropLeft + 1, rawWidth);
            cropWidth = cropRight - cropLeft;
            cropHeight = cropBottom - cropTop;
        }

        private void finishWhiteLevels() {
            int nativeWhite = (1 << bitsPerSample) - 1;
            for (int channel = 0; channel < whiteLevel.length; channel++) {
                int limit = linearityLimit[channel];
                int channelWhite = limit > 0 ? Math.min(nativeWhite, limit) : nativeWhite;
                whiteLevel[channel] = Math.max(blackLevel[channel] + 1, channelWhite);
            }
        }

        private void finishWhiteBalance() {
            if (wbGreenLevel > 0) {
                if (wbRedLevel > 0) {
                    whiteBalance[RED] = wbRedLevel / (double) wbGreenLevel;
                }
                if (wbBlueLevel > 0) {
                    whiteBalance[BLUE] = wbBlueLevel / (double) wbGreenLevel;
                }
            } else {
                if (redBalance > 0) {
                    whiteBalance[RED] = redBalance / 256.0;
                }
                if (blueBalance > 0) {
                    whiteBalance[BLUE] = blueBalance / 256.0;
                }
            }
        }

        private void finishBlackLevels() {
            int defaultBlack = bitsPerSample == 14 ? 256 : 64;
            for (int i = 0; i < blackLevel.length; i++) {
                if (blackLevel[i] < 0) {
                    blackLevel[i] = defaultBlack;
                } else if (panasonicEncoding == 4) {
                    blackLevel[i] += 15;
                }
            }
        }

        private void finishColorMatrix() {
            colorMatrix = findColorMatrix(model);
        }

        private void finishBayerPattern() {
            int[] pattern = bayerPatternFor(cfaPattern);
            System.arraycopy(pattern, 0, bayerPattern, 0, bayerPattern.length);
        }

        private int blackLevel(int channel) {
            return blackLevel[channel];
        }

        private int whiteLevel(int channel) {
            return whiteLevel[channel];
        }
    }

    private static int[] bayerPatternFor(int cfaPattern) {
        switch (cfaPattern) {
            case 1:
                return new int[]{RED, GREEN, GREEN, BLUE};      // RGGB
            case 2:
                return new int[]{GREEN, RED, BLUE, GREEN};      // GRBG
            case 3:
                return new int[]{GREEN, BLUE, RED, GREEN};      // GBRG
            case 4:
                return new int[]{BLUE, GREEN, GREEN, RED};      // BGGR
            default:
                return new int[]{RED, GREEN, GREEN, BLUE};
        }
    }

    private static class PanasonicRawFormat8Metadata {

        private final int[] tag39 = new int[6];
        private final int[] tag3A = new int[6];
        private int tag3B;
        private int tag43;
        private final int[] initial = new int[4];
        private final int[] tag40A = new int[17];
        private final int[] tag40B = new int[17];
        private final int[] tag41 = new int[17];
        private int stripeCount;
        private final long[] stripeOffsets = new long[5];
        private final int[] stripeLeft = new int[5];
        private final long[] stripeCompressedSize = new long[5];
        private final int[] stripeWidth = new int[5];
        private final int[] stripeHeight = new int[5];
    }

    private static class TiffMetadataReader {

        private final byte[] data;
        private final RawMetadata metadata = new RawMetadata();
        private boolean littleEndian;

        private TiffMetadataReader(byte[] data) {
            this.data = data;
        }

        private RawMetadata read() throws IOException {
            if (data.length < 8) {
                throw new IOException("File is too small to be a Panasonic RW2 file.");
            }

            int byteOrder = ((data[0] & 0xff) << 8) | (data[1] & 0xff);
            if (byteOrder == 0x4949) {
                littleEndian = true;
            } else if (byteOrder == 0x4d4d) {
                littleEndian = false;
            } else {
                throw new IOException("Panasonic RW2 byte order marker was not found.");
            }

            long firstIfdOffset = readUnsignedInt(4);
            parseIfd(firstIfdOffset, false, 0);
            return metadata;
        }

        private void parseIfd(long ifdOffset, boolean forcePanasonicTags, int depth) throws IOException {
            if (ifdOffset <= 0 || ifdOffset > data.length - 2L || depth > 8) {
                return;
            }

            int offset = toIntOffset(ifdOffset);
            int entriesCount = readUnsignedShort(offset);
            if (entriesCount < 0 || entriesCount > 512) {
                return;
            }

            List<IfdEntry> entries = new ArrayList<>();
            int entryOffset = offset + 2;
            for (int i = 0; i < entriesCount; i++) {
                if (entryOffset > data.length - 12) {
                    return;
                }
                IfdEntry entry = readEntry(entryOffset);
                entries.add(entry);
                entryOffset += 12;
            }

            boolean panasonicTags = forcePanasonicTags || hasPanasonicRawMarker(entries);
            if (panasonicTags) {
                metadata.panasonicRaw = true;
            }

            for (IfdEntry entry : entries) {
                handleStandardTag(entry, depth);
                if (panasonicTags) {
                    handlePanasonicTag(entry);
                }
            }

            if (depth == 0) {
                long nextIfdOffset = readUnsignedInt(entryOffset);
                parseIfd(nextIfdOffset, false, depth + 1);
            }
        }

        private boolean hasPanasonicRawMarker(List<IfdEntry> entries) {
            for (IfdEntry entry : entries) {
                if (entry.tag == 0x0001 && entry.count == 4) {
                    return true;
                }
            }
            return false;
        }

        private void handleStandardTag(IfdEntry entry, int depth) throws IOException {
            switch (entry.tag) {
                case TAG_IMAGE_WIDTH:
                    if (metadata.fallbackWidth <= 0) {
                        metadata.fallbackWidth = entry.firstInt();
                    }
                    break;
                case TAG_IMAGE_LENGTH:
                    if (metadata.fallbackHeight <= 0) {
                        metadata.fallbackHeight = entry.firstInt();
                    }
                    break;
                case TAG_BITS_PER_SAMPLE:
                    if (metadata.bitsPerSample <= 0) {
                        metadata.bitsPerSample = entry.firstInt();
                    }
                    break;
                case TAG_COMPRESSION:
                    if (metadata.compression <= 0) {
                        metadata.compression = entry.firstInt();
                    }
                    break;
                case TAG_MAKE:
                    if (metadata.make == null || metadata.make.isEmpty()) {
                        metadata.make = entry.ascii();
                    }
                    break;
                case TAG_MODEL:
                    if (metadata.model == null || metadata.model.isEmpty()) {
                        metadata.model = entry.ascii();
                    }
                    break;
                case TAG_STRIP_OFFSETS:
                    if (metadata.rawOffset < 0) {
                        metadata.rawOffset = entry.firstInt();
                    }
                    break;
                case TAG_STRIP_BYTE_COUNTS:
                    if (metadata.rawByteCount < 0) {
                        metadata.rawByteCount = entry.firstLong();
                    }
                    break;
                case TAG_EXIF_IFD:
                    parseIfd(entry.firstLong(), false, depth + 1);
                    break;
                case TAG_MAKER_NOTE:
                    parsePanasonicMakerNote(entry.valueOffset());
                    break;
                default:
                    break;
            }
        }

        private void handlePanasonicTag(IfdEntry entry) throws IOException {
            switch (entry.tag) {
                case PANASONIC_TAG_RAW_WIDTH:
                    metadata.rawWidth = entry.firstInt();
                    break;
                case PANASONIC_TAG_RAW_HEIGHT:
                    metadata.rawHeight = entry.firstInt();
                    break;
                case PANASONIC_TAG_SENSOR_TOP:
                case PANASONIC_TAG_CROP_TOP:
                    metadata.cropTop = entry.firstInt();
                    break;
                case PANASONIC_TAG_SENSOR_LEFT:
                case PANASONIC_TAG_CROP_LEFT:
                    metadata.cropLeft = entry.firstInt();
                    break;
                case PANASONIC_TAG_SENSOR_BOTTOM:
                case PANASONIC_TAG_CROP_BOTTOM:
                    metadata.cropBottom = entry.firstInt();
                    break;
                case PANASONIC_TAG_SENSOR_RIGHT:
                case PANASONIC_TAG_CROP_RIGHT:
                    metadata.cropRight = entry.firstInt();
                    break;
                case PANASONIC_TAG_CFA_PATTERN:
                    metadata.cfaPattern = entry.firstInt();
                    break;
                case PANASONIC_TAG_BITS_PER_SAMPLE:
                    metadata.bitsPerSample = entry.firstInt();
                    break;
                case PANASONIC_TAG_COMPRESSION:
                    metadata.compression = entry.firstInt();
                    break;
                case PANASONIC_TAG_LINEARITY_LIMIT_RED:
                    metadata.linearityLimit[RED] = entry.firstInt();
                    break;
                case PANASONIC_TAG_LINEARITY_LIMIT_GREEN:
                    metadata.linearityLimit[GREEN] = entry.firstInt();
                    break;
                case PANASONIC_TAG_LINEARITY_LIMIT_BLUE:
                    metadata.linearityLimit[BLUE] = entry.firstInt();
                    break;
                case PANASONIC_TAG_RED_BALANCE:
                    metadata.redBalance = entry.firstInt();
                    break;
                case PANASONIC_TAG_BLUE_BALANCE:
                    metadata.blueBalance = entry.firstInt();
                    break;
                case PANASONIC_TAG_BLACK_LEVEL_RED:
                    metadata.blackLevel[RED] = entry.firstInt();
                    break;
                case PANASONIC_TAG_BLACK_LEVEL_GREEN:
                    metadata.blackLevel[GREEN] = entry.firstInt();
                    break;
                case PANASONIC_TAG_BLACK_LEVEL_BLUE:
                    metadata.blackLevel[BLUE] = entry.firstInt();
                    break;
                case PANASONIC_TAG_WB_RED_LEVEL:
                    metadata.wbRedLevel = entry.firstInt();
                    break;
                case PANASONIC_TAG_WB_GREEN_LEVEL:
                    metadata.wbGreenLevel = entry.firstInt();
                    break;
                case PANASONIC_TAG_WB_BLUE_LEVEL:
                    metadata.wbBlueLevel = entry.firstInt();
                    break;
                case PANASONIC_TAG_RAW_FORMAT:
                    metadata.panasonicEncoding = entry.firstInt();
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_TABLE_39:
                    readRawFormat8Tag39(entry);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_TABLE_3A:
                    readRawFormat8Tag3A(entry);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_DATA_MAX:
                    metadata.rawFormat8.tag3B = entry.firstInt();
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_INITIAL_0:
                case PANASONIC_TAG_RAW_FORMAT_8_INITIAL_0 + 1:
                case PANASONIC_TAG_RAW_FORMAT_8_INITIAL_0 + 2:
                case PANASONIC_TAG_RAW_FORMAT_8_INITIAL_3:
                    metadata.rawFormat8.initial[entry.tag - PANASONIC_TAG_RAW_FORMAT_8_INITIAL_0] = entry.firstInt();
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_HUFFMAN_40:
                    readRawFormat8Huffman40(entry);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_HUFFMAN_41:
                    readRawFormat8Huffman41(entry);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_COUNT:
                    metadata.rawFormat8.stripeCount = Math.min(5, entry.firstInt());
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_TAG_43:
                    metadata.rawFormat8.tag43 = Math.min(5, entry.firstInt());
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_OFFSETS:
                    readRawFormat8LongArray(entry, metadata.rawFormat8.stripeOffsets);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_LEFT:
                    readRawFormat8IntArray(entry, metadata.rawFormat8.stripeLeft);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_COMPRESSED_SIZE:
                    readRawFormat8LongArray(entry, metadata.rawFormat8.stripeCompressedSize);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_WIDTH:
                    readRawFormat8ShortArray(entry, metadata.rawFormat8.stripeWidth);
                    break;
                case PANASONIC_TAG_RAW_FORMAT_8_STRIPE_HEIGHT:
                    readRawFormat8ShortArray(entry, metadata.rawFormat8.stripeHeight);
                    break;
                case PANASONIC_TAG_RW2_OFFSET:
                case TAG_STRIP_OFFSETS:
                    metadata.rawOffset = entry.firstInt();
                    break;
                case TAG_STRIP_BYTE_COUNTS:
                    metadata.rawByteCount = entry.firstLong();
                    break;
                default:
                    break;
            }
        }

        private void readRawFormat8Tag39(IfdEntry entry) throws IOException {
            if (entry.type != TYPE_UNDEFINED || entry.count != 26) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(6, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                metadata.rawFormat8.tag39[i] = (int) readUnsignedInt(offset + 2 + i * 4);
            }
        }

        private void readRawFormat8Tag3A(IfdEntry entry) throws IOException {
            if (entry.type != TYPE_UNDEFINED || entry.count != 26) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(6, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                metadata.rawFormat8.tag3A[i] = readUnsignedShort(offset + 4 + i * 4);
            }
        }

        private void readRawFormat8Huffman40(IfdEntry entry) throws IOException {
            if (entry.type != TYPE_UNDEFINED || entry.count != 70) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(17, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                int valueA = readUnsignedShort(offset + 2 + i * 4);
                int valueB = readUnsignedShort(offset + 4 + i * 4);
                metadata.rawFormat8.tag40A[i] = Math.min(valueA, 16);
                metadata.rawFormat8.tag40B[i] = Math.min(valueB, 0x0fff);
            }
        }

        private void readRawFormat8Huffman41(IfdEntry entry) throws IOException {
            if (entry.type != TYPE_UNDEFINED || entry.count != 36) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(17, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                metadata.rawFormat8.tag41[i] = Math.min(readUnsignedShort(offset + 2 + i * 2), 64);
            }
        }

        private void readRawFormat8LongArray(IfdEntry entry, long[] target) throws IOException {
            if (entry.type != TYPE_UNDEFINED) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(target.length, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                target[i] = readUnsignedInt(offset + 2 + i * 4);
            }
        }

        private void readRawFormat8IntArray(IfdEntry entry, int[] target) throws IOException {
            if (entry.type != TYPE_UNDEFINED) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(target.length, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                target[i] = (int) readUnsignedInt(offset + 2 + i * 4);
            }
        }

        private void readRawFormat8ShortArray(IfdEntry entry, int[] target) throws IOException {
            if (entry.type != TYPE_UNDEFINED) {
                return;
            }

            int offset = entry.valueOffset();
            int count = Math.min(target.length, readUnsignedShort(offset));
            for (int i = 0; i < count; i++) {
                target[i] = readUnsignedShort(offset + 2 + i * 2);
            }
        }

        private void parsePanasonicMakerNote(int makerNoteOffset) throws IOException {
            if (makerNoteOffset < 0 || makerNoteOffset > data.length - 14 || !startsWithPanasonicSignature(makerNoteOffset)) {
                return;
            }

            int ifdOffset = makerNoteOffset + 12;
            int entriesCount = readUnsignedShort(ifdOffset);
            if (entriesCount < 0 || entriesCount > 512) {
                return;
            }

            int entryOffset = ifdOffset + 2;
            for (int i = 0; i < entriesCount; i++) {
                if (entryOffset > data.length - 12) {
                    return;
                }
                handlePanasonicTag(readEntry(entryOffset));
                entryOffset += 12;
            }
        }

        private boolean startsWithPanasonicSignature(int offset) {
            byte[] signature = {'P', 'a', 'n', 'a', 's', 'o', 'n', 'i', 'c'};
            for (int i = 0; i < signature.length; i++) {
                if (data[offset + i] != signature[i]) {
                    return false;
                }
            }
            return true;
        }

        private IfdEntry readEntry(int offset) throws IOException {
            int tag = readUnsignedShort(offset);
            int type = readUnsignedShort(offset + 2);
            long count = readUnsignedInt(offset + 4);
            long value = readUnsignedInt(offset + 8);
            return new IfdEntry(tag, type, count, value, offset);
        }

        private int readUnsignedByte(int offset) {
            return data[offset] & 0xff;
        }

        private int readUnsignedShort(int offset) throws IOException {
            ensureOffset(offset, 2);
            if (littleEndian) {
                return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
            }
            return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
        }

        private long readUnsignedInt(int offset) throws IOException {
            ensureOffset(offset, 4);
            if (littleEndian) {
                return (data[offset] & 0xffL)
                        | ((data[offset + 1] & 0xffL) << 8)
                        | ((data[offset + 2] & 0xffL) << 16)
                        | ((data[offset + 3] & 0xffL) << 24);
            }
            return ((data[offset] & 0xffL) << 24)
                    | ((data[offset + 1] & 0xffL) << 16)
                    | ((data[offset + 2] & 0xffL) << 8)
                    | (data[offset + 3] & 0xffL);
        }

        private int readInt(int offset) throws IOException {
            long unsigned = readUnsignedInt(offset);
            return (int) unsigned;
        }

        private void ensureOffset(int offset, int length) throws IOException {
            if (offset < 0 || offset > data.length - length) {
                throw new EOFException("Unexpected end of Panasonic RW2 metadata.");
            }
        }

        private int toIntOffset(long offset) throws IOException {
            if (offset < 0 || offset > Integer.MAX_VALUE) {
                throw new IOException("Invalid Panasonic RW2 offset: " + offset);
            }
            return (int) offset;
        }

        private class IfdEntry {

            private final int tag;
            private final int type;
            private final long count;
            private final long value;
            private final int entryOffset;

            private IfdEntry(int tag, int type, long count, long value, int entryOffset) {
                this.tag = tag;
                this.type = type;
                this.count = count;
                this.value = value;
                this.entryOffset = entryOffset;
            }

            private int firstInt() throws IOException {
                return (int) firstLong();
            }

            private long firstLong() throws IOException {
                int offset = valueOffset();
                switch (type) {
                    case TYPE_BYTE:
                    case TYPE_ASCII:
                    case TYPE_UNDEFINED:
                        return readUnsignedByte(offset);
                    case TYPE_SHORT:
                        return readUnsignedShort(offset);
                    case TYPE_LONG:
                        return readUnsignedInt(offset);
                    case TYPE_SLONG:
                        return readInt(offset);
                    case TYPE_RATIONAL:
                    case TYPE_SRATIONAL:
                        return readRationalAsLong(offset);
                    default:
                        return 0;
                }
            }

            private long readRationalAsLong(int offset) throws IOException {
                long numerator = readUnsignedInt(offset);
                long denominator = readUnsignedInt(offset + 4);
                if (denominator == 0) {
                    return 0;
                }
                return Math.round(numerator / (double) denominator);
            }

            private String ascii() throws IOException {
                int offset = valueOffset();
                if (offset < 0 || offset >= data.length) {
                    return "";
                }

                int length = (int) Math.min(count, data.length - (long) offset);
                StringBuilder value = new StringBuilder(length);
                for (int i = 0; i < length; i++) {
                    int character = data[offset + i] & 0xff;
                    if (character == 0) {
                        break;
                    }
                    value.append((char) character);
                }
                return value.toString().trim();
            }

            private int valueOffset() throws IOException {
                long bytes = count * typeSize(type);
                if (bytes <= 4) {
                    return entryOffset + 8;
                }
                return toIntOffset(value);
            }
        }
    }

    private static int typeSize(int type) {
        switch (type) {
            case TYPE_SHORT:
                return 2;
            case TYPE_LONG:
            case TYPE_SLONG:
                return 4;
            case TYPE_RATIONAL:
            case TYPE_SRATIONAL:
                return 8;
            default:
                return 1;
        }
    }

    private static class PanasonicRawFormat8Parameters {

        private final int[] tag3A = new int[6];
        private final int[] tag39 = new int[6];
        private final int[] huffmanCoefficients = new int[18];
        private final long[] huffmanTable1 = new long[17];
        private final long[] huffmanTable2 = new long[17];
        private final int[] initial = new int[4];
        private final int tag3B;
        private final int tag3B2;
        private boolean noGamma = true;
        private int[] gammaTable;
        private byte[] extraHuffmanTable;

        private PanasonicRawFormat8Parameters(PanasonicRawFormat8Metadata metadata) {
            System.arraycopy(metadata.tag3A, 0, tag3A, 0, tag3A.length);
            System.arraycopy(metadata.tag39, 0, tag39, 0, tag39.length);
            System.arraycopy(metadata.initial, 0, initial, 0, initial.length);
            tag3B = metadata.tag3B;
            tag3B2 = metadata.tag3B;

            for (int i = 0; i < 17; i++) {
                huffmanCoefficients[i] = ((metadata.tag41[i] & 0xff) << 24)
                        | ((metadata.tag40A[i] & 0xffff) << 16)
                        | (metadata.tag40B[i] & 0xffff);
            }

            buildGammaTable();
            buildHuffmanTables();
        }

        private void buildGammaTable() {
            int[] candidate = new int[0x10000];
            for (int i = 0; i < candidate.length; i++) {
                int value = gammaCurve(i) & 0xffff;
                candidate[i] = value;
                if (value != i) {
                    noGamma = false;
                }
            }
            if (!noGamma) {
                gammaTable = candidate;
            }
        }

        private void buildHuffmanTables() {
            int longestCode = 0;
            for (int index = 0; index < 17; index++) {
                int coefficient = huffmanCoefficients[index];
                int lowBits = (coefficient >>> 16) & 0x1f;
                int mask = 0;
                if ((coefficient & 0x1f0000) != 0) {
                    int lowBitsMod8 = ((coefficient >>> 16) & 0xffff) & 7;
                    if (lowBits - 1 >= 7) {
                        int difference = lowBitsMod8 - lowBits;
                        do {
                            mask = ((mask & 0xffff) << 8) | 0xff;
                            difference += 8;
                        } while (difference != 0);
                    }
                    for (; lowBitsMod8 > 0; lowBitsMod8--) {
                        mask = ((mask & 0xffff) << 1) | 1;
                    }
                }

                int value = coefficient & (mask & 0xffff);
                if (longestCode < lowBits) {
                    longestCode = lowBits;
                }
                huffmanTable2[index] = lowBits == 0 ? 0L : -1L << (64 - lowBits);
                huffmanTable1[index] = lowBits == 0 ? 0L : (value & 0xffffL) << (64 - lowBits);
            }

            if (longestCode < 17) {
                extraHuffmanTable = new byte[0x10000];
                long prefix = 0L;
                for (int i = 0; i < extraHuffmanTable.length; i++) {
                    extraHuffmanTable[i] = (byte) (getDeltaBit(prefix) & 0xff);
                    prefix += 0x1000000000000L;
                }
            }
        }

        private boolean decodeStrip(PanasonicRawFormat8Buffer buffer, int[] raw, int rawWidth, int rawHeight,
                                    int stripeWidth, int stripeHeight, int leftMargin) {
            int halfWidth = stripeWidth >> 1;
            int halfHeight = stripeHeight >> 1;
            if (halfWidth <= 0 || halfHeight <= 0 || buffer.size() < 9) {
                return false;
            }

            int qwords = buffer.size() >> 3;
            int doubleWidth = 4 * halfWidth;
            int[] lineBase = new int[4];
            int[] currentBase = new int[4];
            int[] group = new int[4];
            for (int i = 0; i < lineBase.length; i++) {
                lineBase[i] = initial[i] & 0xffff;
            }

            long bitTail = 0L;
            int bitPortion = 0;
            int inputQword = 0;

            for (int rowPair = 0; rowPair < halfHeight; rowPair++) {
                System.arraycopy(lineBase, 0, currentBase, 0, currentBase.length);
                int destRow = rowPair * 2;

                for (int col = 0; col < doubleWidth; col++) {
                    long pixelBits;
                    if (bitPortion < 0) {
                        int nextQword = inputQword + 1;
                        if (nextQword >= qwords) {
                            return false;
                        }
                        bitPortion += 64;
                        long current = buffer.getQWord(inputQword);
                        long next = buffer.getQWord(nextQword);
                        pixelBits = (next >>> bitPortion) | (current << (64 - (bitPortion & 0xff)));
                        if (inputQword < qwords) {
                            inputQword = nextQword;
                        }
                    } else {
                        if (inputQword >= qwords) {
                            return false;
                        }
                        long current = buffer.getQWord(inputQword);
                        pixelBits = (current >>> bitPortion) | bitTail;
                        if (bitPortion == 0) {
                            bitPortion = 64;
                            inputQword++;
                        }
                    }

                    int huffmanIndex = huffmanIndex(pixelBits);
                    int coefficient = huffmanCoefficients[huffmanIndex];
                    int highBits = (coefficient >>> 24) & 0x1f;
                    long shiftedBits = pixelBits << (((coefficient >>> 16) & 0xffff) & 0x1f);
                    long consumedTailBits = huffmanIndex - highBits;
                    int shift = ((highBits & 0xff) - (huffmanIndex & 0xff)) & 63;
                    int valueBits = (int) ((shiftedBits >>> shift) & 0xffffL);
                    int value = valueBits << (((coefficient >>> 24) & 0xff) & 31);

                    if (huffmanIndex - highBits <= 0) {
                        value &= 0xffff0000;
                    }

                    int delta;
                    if (shiftedBits < 0) {
                        delta = value & 0xffff;
                    } else if (huffmanIndex != 0) {
                        int base = -1 << huffmanIndex;
                        delta = (value & 0xffff) + base + (highBits == 0 ? 1 : 0);
                    } else {
                        delta = 0;
                    }
                    if (highBits != 0) {
                        delta += 1 << (highBits - 1);
                    }

                    int nextBitPortion = bitPortion - ((coefficient >>> 16) & 0x1f);
                    int component = componentIndex(col & 3);
                    int sample = clamp(currentBase[component] + delta, 0, tag3B2);
                    group[component] = sample;
                    putRawFormat8Sample(raw, rawWidth, rawHeight, leftMargin, destRow, col, gamma(sample));

                    if (huffmanIndex <= highBits) {
                        consumedTailBits = 0L;
                    }
                    bitTail = shiftedBits << (int) consumedTailBits;
                    bitPortion = (int) (nextBitPortion - consumedTailBits);

                    if ((col & 3) == 3) {
                        System.arraycopy(group, 0, currentBase, 0, currentBase.length);
                    }
                    if (col == 3) {
                        System.arraycopy(group, 0, lineBase, 0, lineBase.length);
                    }
                }
            }

            return true;
        }

        private int huffmanIndex(long pixelBits) {
            if (extraHuffmanTable != null) {
                return extraHuffmanTable[(int) ((pixelBits >>> 48) & 0xffff)] & 0xff;
            }
            return getDeltaBit(pixelBits);
        }

        private int componentIndex(int colMod4) {
            switch (colMod4) {
                case 1:
                    return 2;
                case 2:
                    return 1;
                case 3:
                    return 3;
                default:
                    return 0;
            }
        }

        private void putRawFormat8Sample(int[] raw, int rawWidth, int rawHeight, int leftMargin,
                                         int destRow, int encodedCol, int value) {
            int rawCol = (encodedCol >> 2) * 2 + (encodedCol >= 2 && (encodedCol & 3) >= 2 ? 1 : 0);
            int rawRow = destRow + (((encodedCol & 3) == 1 || (encodedCol & 3) == 3) ? 1 : 0);
            if (rawRow < rawHeight && leftMargin + rawCol < rawWidth) {
                raw[rawRow * rawWidth + leftMargin + rawCol] = value;
            }
        }

        private int gamma(int value) {
            if (noGamma || gammaTable == null) {
                return value;
            }
            return gammaTable[value & 0xffff];
        }

        private int getDeltaBit(long value) {
            for (int i = 0; i < 16; i++) {
                if ((value & huffmanTable2[i]) == huffmanTable1[i]) {
                    return i;
                }
            }
            return (((value & huffmanTable2[16]) == huffmanTable1[16]) ? 1 : 0) ^ 0x11;
        }

        private int gammaCurve(int index) {
            int value = index | 0xffff0000;
            if ((index & 0x10000) == 0) {
                value = index & 0x1ffff;
            }

            int gammaInput = gammaBase() + value;
            int limitedInput = Math.min(gammaInput, 0xffff);
            if (limitedInput < 0) {
                limitedInput = 0;
            }

            int segment = 0;
            if (limitedInput >= (tag3A[1] & 0xffff)) {
                segment = 1;
                if (limitedInput >= (tag3A[2] & 0xffff)) {
                    segment = 2;
                    if (limitedInput >= (tag3A[3] & 0xffff)) {
                        segment = 3;
                        if (limitedInput >= (tag3A[4] & 0xffff)) {
                            long packed = ((long) limitedInput | 0x500000000L) - (tag3A[5] & 0xffffL);
                            segment = (int) (packed >>> 32);
                        }
                    }
                }
            }

            int segmentStart = tag3A[segment];
            int shift = tag39[segment];
            int delta = limitedInput - (segmentStart & 0xffff);
            int shiftLow = shift & 0x1f;
            long result;

            if (shiftLow == 31) {
                result = segment == 5 ? 0xffffL : ((tag3A[segment + 1] >>> 16) & 0xffffL);
                return (int) Math.min(result, tag3B & 0xffffffffL);
            }
            if ((shift & 0x10) == 0) {
                if (shiftLow == 15) {
                    result = (segmentStart >>> 16) & 0xffffL;
                    return (int) Math.min(result, tag3B & 0xffffffffL);
                } else if (shiftLow != 0) {
                    delta = (delta + (1 << (shiftLow - 1))) >> shiftLow;
                }
            } else {
                delta <<= shift & 0x0f;
            }

            result = (delta & 0xffffffffL) + ((segmentStart >>> 16) & 0xffffL);
            return (int) Math.min(result, tag3B & 0xffffffffL);
        }

        private int gammaBase() {
            return 0;
        }
    }

    private static class PanasonicRawFormat8Buffer {

        private final byte[] source;
        private final long baseOffset;
        private final int exactBytes;
        private final int paddedBytes;

        private PanasonicRawFormat8Buffer(byte[] source, long baseOffset, int exactBytes) {
            this.source = source;
            this.baseOffset = baseOffset;
            this.exactBytes = exactBytes;
            this.paddedBytes = (int) (((exactBytes + 7L) / 8L) * 8L);
        }

        private int size() {
            return paddedBytes;
        }

        private long getQWord(int wordOffset) {
            long byteOffset = baseOffset + wordOffset * 8L;
            long result = 0L;
            for (int i = 0; i < 8; i++) {
                long relativeOffset = wordOffset * 8L + i;
                int value = 0;
                if (relativeOffset >= 0 && relativeOffset < exactBytes) {
                    int sourceOffset = (int) (byteOffset + i);
                    if (sourceOffset >= 0 && sourceOffset < source.length) {
                        value = source[sourceOffset] & 0xff;
                    }
                }
                result |= (long) BIT_REVERSE_TABLE[value] << (56 - i * 8);
            }
            return result;
        }
    }

    private static class PanasonicDataReader {

        private static final int LOAD_FLAGS = 0x2008;
        private static final int BUFFER_LENGTH = 0x4000;
        private static final int BIT_POSITION_MASK = 0x1ffff;
        private static final int BYTE_POSITION_MASK = 0x3fff;

        private final byte[] source;
        private final byte[] buffer = new byte[BUFFER_LENGTH + 2];
        private final int endOffset;
        private int sourceOffset;
        private int vpos;

        private PanasonicDataReader(byte[] data, RawMetadata metadata) {
            this.source = data;
            this.sourceOffset = metadata.rawOffset;
            this.endOffset = (int) Math.min(data.length, metadata.rawOffset + metadata.rawByteCount);
        }

        private int readBits(int bits) throws IOException {
            if (bits <= 0 || bits > 16) {
                return 0;
            }
            loadIfNeeded();
            vpos = (vpos - bits) & BIT_POSITION_MASK;
            int byteIndex = (vpos >> 3) ^ 0x3ff0;
            int value = ((buffer[byteIndex] & 0xff) | ((buffer[byteIndex + 1] & 0xff) << 8)) >> (vpos & 7);
            return value & ((1 << bits) - 1);
        }

        private void readBlock(int[] bytes) throws IOException {
            loadIfNeeded();
            for (int i = 0; i < 16; i++) {
                bytes[i] = buffer[vpos] & 0xff;
                vpos = (vpos + 1) & BYTE_POSITION_MASK;
            }
        }

        private void loadIfNeeded() throws IOException {
            if (vpos != 0) {
                return;
            }
            clearBuffer();
            readIntoBuffer(LOAD_FLAGS, BUFFER_LENGTH - LOAD_FLAGS);
            readIntoBuffer(0, LOAD_FLAGS);
        }

        private void clearBuffer() {
            Arrays.fill(buffer, (byte) 0);
        }

        private void readIntoBuffer(int bufferOffset, int length) throws IOException {
            if (sourceOffset >= endOffset) {
                return;
            }
            int readable = Math.min(length, endOffset - sourceOffset);
            if (readable < 0 || bufferOffset > buffer.length - readable) {
                throw new EOFException("Unexpected end of Panasonic RW2 compressed data.");
            }
            System.arraycopy(source, sourceOffset, buffer, bufferOffset, readable);
            sourceOffset += readable;
        }
    }
}
