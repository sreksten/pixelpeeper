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
 * Canon CR3 reader that decodes the CRX raw stream and renders it like LibRaw.
 *
 * <p>The CRX decoder is a faithful port of LibRaw's {@code crx.cpp}, covering both the lossless
 * line-predictive branch (encType 0) and the C-RAW lossy branch (imageLevels &gt; 0, 5/3 inverse
 * wavelet with quantization, and the encType 3 transformed-plane reconstruction). The decoded
 * Bayer mosaic is then rendered following LibRaw's default {@code dcraw_process} pipeline: per-channel
 * black subtraction and {@code adjust_maximum}, {@code scale_colors} with the as-shot camera white
 * balance (from Canon ColorData) and highlight clipping, {@code ahd_interpolate} (AHD demosaic),
 * {@code convert_to_rgb} with a model-specific camera-to-sRGB matrix ({@code cam_xyz_coeff}), and the
 * BT.709 output tone curve ({@code gamma_curve}). It crops to the active sensor area
 * ({@code DefaultCropAbsolute} from Canon SensorInfo) and returns an 8-bit {@link BufferedImage}.</p>
 *
 * <p>The emulated LibRaw parameters mirror the Panasonic reader: camera/as-shot white balance,
 * fixed exposure (no auto-brightness), highlight clipping, sRGB output, 8-bit output and BT.709
 * gamma. It does not use the embedded JPEG preview and performs no optical corrections beyond
 * LibRaw's default processing (no CA/distortion/vignetting correction, sharpening, denoise, or
 * camera JPEG tone curve). This class handles CR3/CRX only; older Canon CRW/CR2 files need their
 * own container/RAW decoder.</p>
 */
public class CanonCr3RawImageReader extends AbstractRawImageReader implements ImageReader {

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    private static final int TILE_RIGHT = 1;
    private static final int TILE_LEFT = 2;
    private static final int TILE_BOTTOM = 4;
    private static final int TILE_TOP = 8;

    private static final int[] JS = {
            1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 8, 8, 8, 8,
            0x10, 0x10, 0x20, 0x20, 0x40, 0x40, 0x80, 0x80,
            0x100, 0x200, 0x400, 0x800, 0x1000, 0x2000, 0x4000, 0x8000
    };

    // LibRaw crx.cpp C-RAW tables.
    private static final int[] INCR_BIT_TABLE = {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0};
    private static final int[] Q_STEP_TBL = {0x28, 0x2D, 0x33, 0x39, 0x40, 0x48, 0, 0};
    private static final int[] EX_COEF_NUM_TBL = {
            1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0,
            0, 0, 1, 2, 2, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 2, 2,
            1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 2, 2, 2, 2, 1, 1, 1,
            1, 2, 2, 1, 1, 1, 1, 2, 2, 1, 1, 0, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };

    private static final int[] J = {
            0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
            4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    };

    private static final byte[] CANON_UUID = {
            (byte) 0x85, (byte) 0xc0, (byte) 0xb6, (byte) 0x87,
            (byte) 0x82, 0x0f, 0x11, (byte) 0xe0,
            (byte) 0x81, 0x11, (byte) 0xf4, (byte) 0xce,
            0x46, 0x2b, 0x6a, 0x48
    };

    // LibRaw dcraw_process defaults emulated here (see PanasonicRawImageReader): camera/as-shot WB,
    // no auto-brightness (fixed exposure), highlight clip, sRGB output, 8-bit, BT.709 gamma, AHD.
    private static final double GAMMA_POWER = 0.45;                 // gamm[0]
    private static final double GAMMA_TOE_SLOPE = 4.5;             // gamm[1]
    private static final int OUTPUT_WHITE = 65535;
    private static final int GAMMA_IMAX = 0x10000;                  // (0x2000<<3)/bright, no_auto_bright
    private static final double ADJUST_MAXIMUM_THRESHOLD = 0.75;    // LIBRAW_DEFAULT_ADJUST_MAXIMUM_THRESHOLD

    private static final int AHD_TILE = 512;                       // LIBRAW_AHD_TILE
    private static final int AHD_TILE_OVERLAP = 6;
    private static final int AHD_BORDER = 5;

    // LibRaw_constants::d65_white.
    private static final double[] D65_WHITE = {0.95047, 1.00000, 1.08883};

    private static final CameraMatrix DEFAULT_COLOR_MATRIX = matrix("EOS RP",
            8608, -2097, -1178, -5425, 13265, 2383, -1149, 2238, 5680);

    // Canon CR3-era camera-to-XYZ matrices (LibRaw src/tables/colordata.cpp, integers /10000).
    // "Mark II"/"Mark III" and longer variants precede their bases so prefix matching resolves the
    // most specific model first. EOS R5 C shares the EOS R5 matrix (matched via the "EOS R5" prefix).
    private static final CameraMatrix[] CANON_COLOR_MATRICES = {
            matrix("EOS-1D X Mark III", 8971, -2022, -1242, -5405, 13249, 2380, -1280, 2483, 6072),
            matrix("EOS R5 Mark II", 9396, -2598, -1207, -4408, 12296, 2369, -505, 1575, 6077),
            matrix("EOS R6 Mark II", 9539, -2795, -1224, -4175, 11998, 2458, -465, 1755, 6048),
            matrix("EOS M6 Mark II", 11498, -3759, -1516, -5073, 12954, 2349, -892, 1867, 6118),
            matrix("EOS M50 Mark II", 10463, -2173, -1437, -4856, 12635, 2482, -1216, 2915, 7237),
            matrix("EOS R100", 8230, -1515, -1032, -4179, 12005, 2454, -649, 2076, 4711),
            matrix("EOS R50", 9269, -2012, -1107, -3990, 11762, 2527, -569, 2093, 4913),
            matrix("EOS R10", 9269, -2012, -1107, -3990, 11762, 2527, -569, 2093, 4913),
            matrix("EOS R1", 9020, -2275, -1209, -4916, 12856, 2284, -923, 1953, 5928),
            matrix("EOS R3", 9423, -2839, -1195, -4532, 12377, 2415, -483, 1374, 5276),
            matrix("EOS R5", 9766, -2953, -1254, -4276, 12116, 2433, -437, 1336, 5131),
            matrix("EOS R6", 8293, -1611, -1132, -4759, 12711, 2275, -1013, 2415, 5509),
            matrix("EOS R7", 10424, -3138, -1300, -4221, 11938, 2584, -547, 1658, 6183),
            matrix("EOS R8", 9539, -2795, -1224, -4175, 11998, 2458, -465, 1755, 6048),
            matrix("EOS RP", 8608, -2097, -1178, -5425, 13265, 2383, -1149, 2238, 5680),
            matrix("EOS Ra", 22880, -11531, -2223, -2034, 10469, 1809, 316, 1401, 5769),
            matrix("EOS R", 8293, -1789, -1094, -5025, 12925, 2327, -1199, 2769, 6108),
            matrix("EOS M50", 8532, -701, -1167, -4095, 11879, 2508, -797, 2424, 7010),
            matrix("EOS M6", 8532, -701, -1167, -4095, 11879, 2508, -797, 2424, 7010),
            matrix("EOS M200", 10463, -2173, -1437, -4856, 12635, 2482, -1216, 2915, 7237),
            matrix("EOS 90D", 11498, -3759, -1516, -5073, 12954, 2349, -892, 1867, 6118),
            matrix("EOS 850D", 9079, -1923, -1236, -4677, 12454, 2492, -922, 2319, 5565),
            matrix("EOS 250D", 9079, -1923, -1236, -4677, 12454, 2492, -922, 2319, 5565),
            matrix("PowerShot G5 X Mark II", 11629, -5713, -914, -2706, 11090, 1842, -206, 1225, 5515),
            matrix("PowerShot G7 X Mark III", 11629, -5713, -914, -2706, 11090, 1842, -206, 1225, 5515)
    };

    @Override
    public BufferedImage readImage(File file) throws Exception {
        byte[] data = Files.readAllBytes(file.toPath());
        Cr3ContainerParser parser = new Cr3ContainerParser(data);
        Cr3Track track = parser.parse();
        CanonColorMetadata colorMetadata = new CanonMetadataReader(data).read(parser.ctmdOffset, parser.ctmdSize);
        int[] raw = new CrxDecoder(data, track).decode();
        colorMetadata.finish(raw, track.header);
        return new LibRawRenderer(raw, track.header, colorMetadata).render();
    }

    private static final class CanonMetadataReader {

        private final byte[] data;
        private final CanonColorMetadata metadata = new CanonColorMetadata();

        private CanonMetadataReader(byte[] data) {
            this.data = data;
        }

        private CanonColorMetadata read(long ctmdOffset, long ctmdSize) throws IOException {
            parseAtoms(0, data.length, false);
            // Canon CR3 stores ColorData (as-shot WB, per-channel black, specular white) in the
            // Canon MakerNote embedded in the CTMD (timed-metadata) track sample, not in the CMT
            // boxes. Parse that sample for the ColorData tag.
            parseCtmd(ctmdOffset, ctmdSize);
            if (metadata.model == null || metadata.model.isEmpty()) {
                metadata.model = findKnownModelInFile(data);
            }
            return metadata;
        }

        /**
         * Scans the CTMD track sample for embedded Canon MakerNote TIFF blocks and parses their
         * ColorData (tag 0x4001). CTMD records carry a self-contained TIFF (II/MM header); the byte
         * scan is bounded to the sample and {@link #parseCanonTiff} validates each candidate, so a
         * spurious header match cannot corrupt metadata.
         */
        private void parseCtmd(long ctmdOffset, long ctmdSize) throws IOException {
            if (ctmdOffset < 0 || ctmdSize <= 8 || ctmdOffset > data.length - ctmdSize) {
                return;
            }
            int start = (int) ctmdOffset;
            int end = (int) (ctmdOffset + ctmdSize);
            for (int i = start; i + 4 <= end && !metadata.hasColorData; i++) {
                boolean tiff = (data[i] == 'I' && data[i + 1] == 'I' && data[i + 2] == 0x2a && data[i + 3] == 0)
                        || (data[i] == 'M' && data[i + 1] == 'M' && data[i + 2] == 0 && data[i + 3] == 0x2a);
                if (tiff) {
                    try {
                        parseCanonTiff(i, end - i);
                    } catch (IOException ignored) {
                        // A spurious TIFF-header match; keep scanning.
                    }
                }
            }
        }

        private void parseAtoms(long start, long size, boolean canonUuid) throws IOException {
            long end = checkedEnd(start, size);
            long offset = start;
            while (offset + 8 <= end) {
                Atom atom = readAtom(offset, end);
                if (atom.size < atom.headerSize || atom.offset + atom.size > end) {
                    throw new IOException("Invalid CR3 metadata atom at offset " + offset);
                }

                if ("moov".equals(atom.type)) {
                    parseAtoms(atom.contentOffset, atom.contentSize, false);
                } else if ("uuid".equals(atom.type) && atom.contentSize >= CANON_UUID.length
                        && hasCanonUuid(atom.contentOffset)) {
                    parseAtoms(atom.contentOffset + CANON_UUID.length,
                            atom.contentSize - CANON_UUID.length, true);
                } else if (canonUuid && ("CMT1".equals(atom.type) || "CMT3".equals(atom.type))) {
                    parseCanonTiff(atom.contentOffset, atom.contentSize);
                }

                offset += atom.size;
            }
        }

        private boolean hasCanonUuid(long offset) throws IOException {
            int intOffset = toIntOffset(data, offset, CANON_UUID.length);
            for (int i = 0; i < CANON_UUID.length; i++) {
                if (data[intOffset + i] != CANON_UUID[i]) {
                    return false;
                }
            }
            return true;
        }

        private void parseCanonTiff(long baseOffset, long size) throws IOException {
            if (size < 8 || baseOffset < 0 || baseOffset > data.length - size) {
                return;
            }

            int base = toIntOffset(data, baseOffset, (int) Math.min(size, Integer.MAX_VALUE));
            boolean littleEndian;
            if (data[base] == 'I' && data[base + 1] == 'I') {
                littleEndian = true;
            } else if (data[base] == 'M' && data[base + 1] == 'M') {
                littleEndian = false;
            } else {
                return;
            }

            if (readUnsignedShort(data, base + 2, littleEndian) != 42) {
                return;
            }

            long firstIfd = readUnsignedInt(data, base + 4, littleEndian);
            parseCanonIfd(base, baseOffset + size, base + firstIfd, littleEndian, 0);
        }

        private void parseCanonIfd(int base, long tiffEnd, long ifdOffset, boolean littleEndian, int depth)
                throws IOException {
            if (depth > 4 || ifdOffset <= 0 || ifdOffset > tiffEnd - 2L) {
                return;
            }

            int offset = toIntOffset(data, ifdOffset, 2);
            int entries = readUnsignedShort(data, offset, littleEndian);
            if (entries < 0 || entries > 1024 || offset + 2L + entries * 12L > tiffEnd) {
                return;
            }

            int entryOffset = offset + 2;
            for (int i = 0; i < entries; i++) {
                int tag = readUnsignedShort(data, entryOffset, littleEndian);
                int type = readUnsignedShort(data, entryOffset + 2, littleEndian);
                long count = readUnsignedInt(data, entryOffset + 4, littleEndian);
                long value = readUnsignedInt(data, entryOffset + 8, littleEndian);
                int valueOffset = valueOffset(base, entryOffset, type, count, value);

                if (tag == 0x0006 || tag == 0x0110) {
                    String model = ascii(valueOffset, count);
                    if (!model.isEmpty() && (metadata.model == null || metadata.model.isEmpty())) {
                        metadata.model = model;
                    }
                } else if (tag == 0x4001) {
                    readColorData(valueOffset, type, count, littleEndian);
                } else if (tag == 0x00e0) {
                    readSensorInfo(valueOffset, type, count, littleEndian);
                }

                entryOffset += 12;
            }

            if (entryOffset <= tiffEnd - 4L) {
                long nextIfd = readUnsignedInt(data, entryOffset, littleEndian);
                if (nextIfd > 0) {
                    parseCanonIfd(base, tiffEnd, base + nextIfd, littleEndian, depth + 1);
                }
            }
        }

        private int valueOffset(int base, int entryOffset, int type, long count, long value) throws IOException {
            int typeSize = typeSize(type);
            if (typeSize <= 0 || count < 0 || count > Long.MAX_VALUE / typeSize) {
                return -1;
            }
            long bytes = count * typeSize;
            if (bytes <= 4) {
                return entryOffset + 8;
            }
            return toIntOffset(data, base + value, 0);
        }

        /**
         * Parses Canon MakerNote ColorData (tag 0x4001, LibRaw src/metadata/canon.cpp). Reads the
         * as-shot white balance (cam_mul), per-channel black level and specular white level at the
         * ColorData-version offsets keyed by record length. Values are 16-bit words at
         * {@code save1 + wordOffset*2}, in RGGB order remapped to R-G-B-G2 via {@code RGGB_2_RGBG}.
         */
        private void readColorData(int save1, int type, long count, boolean littleEndian) throws IOException {
            if (metadata.hasColorData || type != 3 || count <= 500 || save1 < 0) {
                return;
            }
            int[] offsets = colorDataOffsets((int) count);
            if (offsets == null) {
                return;
            }
            int asShot = offsets[0];
            int blackOffset = offsets[1];
            int whiteOffset = offsets[2];
            int blackOffset2 = offsets[3];

            for (int c = 0; c < 4; c++) {
                metadata.camMul[rggb2rgbg(c)] = readWord(save1, asShot + c, littleEndian);
            }

            int[] black = new int[4];
            for (int c = 0; c < 4; c++) {
                black[rggb2rgbg(c)] = readWord(save1, blackOffset + c, littleEndian);
            }
            if (black[0] + black[1] + black[2] + black[3] <= 0) {
                for (int c = 0; c < 4; c++) {
                    black[rggb2rgbg(c)] = readWord(save1, blackOffset2 + c, littleEndian);
                }
            }
            if (black[0] + black[1] + black[2] + black[3] > 0) {
                metadata.blackLevel[RED] = black[0];
                metadata.blackLevel[GREEN] = black[1];
                metadata.blackLevel[BLUE] = black[2];
            }

            metadata.specularWhiteLevel = readWord(save1, whiteOffset + 1, littleEndian);
            metadata.hasColorData = true;
        }

        /**
         * Returns the ColorData word offsets {asShot WB, channel black, white levels, fallback black}
         * for the given record length, matching LibRaw's per-body switch, or {@code null} if unknown.
         */
        private int[] colorDataOffsets(int length) {
            switch (length) {
                case 1816:
                case 1820:
                case 1824: // ColorDataVer 9: R/RP/Ra/M50/250D/850D/90D/M6 II/M200/R100/G5X II/G7X III/SX70
                    return new int[]{0x0047, 0x0318, 0x031c, 0x0149};
                case 1770:
                case 2024:
                case 3656: // ColorDataVer 10: 1D X III, R5, R6 (and R5 CRM)
                    return new int[]{0x0055, 0x0326, 0x032a, 0x0157};
                case 3778:
                case 3973: // ColorDataVer 11: R3, R6 II, R7, R8, R10, R50
                    return new int[]{0x0069, 0x027c, 0x0280, 0x016b};
                case 4528: // ColorDataVer 12: R1, R5 Mark II
                    return new int[]{0x0069, 0x0290, 0x0294, 0x017f};
                default:
                    return null;
            }
        }

        /**
         * Parses Canon MakerNote SensorInfo (tag 0x00e0): sensor dimensions and the
         * DefaultCropAbsolute active-area rectangle (LibRaw get_CanonArea layout).
         */
        private void readSensorInfo(int base, int type, long count, boolean littleEndian) throws IOException {
            if (metadata.hasCrop || type != 3 || count < 9 || base < 0) {
                return;
            }
            metadata.sensorWidth = readWord(base, 1, littleEndian);
            metadata.sensorHeight = readWord(base, 2, littleEndian);
            metadata.cropLeft = readWord(base, 5, littleEndian);
            metadata.cropTop = readWord(base, 6, littleEndian);
            metadata.cropRight = readWord(base, 7, littleEndian);
            metadata.cropBottom = readWord(base, 8, littleEndian);
            metadata.hasCrop = true;
        }

        private int readWord(long base, int wordIndex, boolean littleEndian) throws IOException {
            long offset = base + (long) wordIndex * 2;
            if (offset < 0 || offset > data.length - 2L) {
                return 0;
            }
            return readUnsignedShort(data, (int) offset, littleEndian);
        }

        private String ascii(int offset, long count) throws IOException {
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

        private Atom readAtom(long offset, long parentEnd) throws IOException {
            long size = readUnsignedInt(data, toIntOffset(data, offset, 8));
            String type = atomType(offset + 4, 4);
            int headerSize = 8;
            if (size == 1) {
                size = (readUnsignedInt(data, toIntOffset(data, offset + 8, 8)) << 32)
                        | readUnsignedInt(data, toIntOffset(data, offset + 12, 4));
                headerSize = 16;
            } else if (size == 0) {
                size = parentEnd - offset;
            }
            return new Atom(offset, size, headerSize, type);
        }

        private String atomType(long offset, int length) throws IOException {
            int intOffset = toIntOffset(data, offset, length);
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = (char) (data[intOffset + i] & 0xff);
            }
            return new String(chars);
        }

        private long checkedEnd(long start, long size) throws IOException {
            if (start < 0 || size < 0 || start > data.length || size > data.length - start) {
                throw new EOFException("Invalid CR3 metadata atom bounds.");
            }
            return start + size;
        }
    }

    /**
     * Canon CR3 processing metadata, mirroring the LibRaw fields fed into dcraw_process: per-channel
     * black level and hard-clip maximum, as-shot camera white balance, the model colour matrix, and
     * the active-area crop from SensorInfo.
     */
    private static final class CanonColorMetadata {

        private String model;
        private final int[] blackLevel = {0, 0, 0};            // R, G, B (per-channel)
        private int maximum;                                    // hard clip = (1<<bps)-1
        private int specularWhiteLevel;                         // LibRaw linear_max (informational)
        private final float[] camMul = {0f, 0f, 0f, 0f};       // as-shot WB, R-G-B-G2
        private final double[] whiteBalance = {1.0, 1.0, 1.0}; // green-normalized multipliers
        private boolean hasColorData;
        private int sensorWidth = -1;
        private int sensorHeight = -1;
        private int cropLeft = -1;
        private int cropTop = -1;
        private int cropRight = -1;
        private int cropBottom = -1;
        private boolean hasCrop;
        private CameraMatrix colorMatrix = DEFAULT_COLOR_MATRIX;
        // Active-area crop rectangle used by the renderer (defaults to the full frame).
        private int activeLeft;
        private int activeTop;
        private int activeWidth;
        private int activeHeight;

        private void finish(int[] raw, CrxHeader header) {
            colorMatrix = findColorMatrix(model);
            maximum = (1 << header.bitsPerSample) - 1;

            if (!hasColorData) {
                // Fallback only (real CR3 files always carry ColorData): a conservative black guess.
                int fallback = Math.min(maximum - 1, 1 << Math.max(0, header.bitsPerSample - 3));
                blackLevel[RED] = blackLevel[GREEN] = blackLevel[BLUE] = fallback;
            }

            // As-shot camera white balance: pre_mul == cam_mul, normalized so green == 1.
            if (camMul[0] > 0.00001f && camMul[1] > 0.00001f && camMul[2] > 0.00001f) {
                whiteBalance[RED] = camMul[0] / camMul[1];
                whiteBalance[GREEN] = 1.0;
                whiteBalance[BLUE] = camMul[2] / camMul[1];
            }

            // Active-area crop from SensorInfo DefaultCropAbsolute (even-rounded), gated on sensor
            // dimensions matching the decoded raw, matching LibRaw's identify.cpp Canon block.
            int fullWidth = header.fullWidth;
            int fullHeight = header.fullHeight;
            if (hasCrop && sensorWidth == fullWidth && sensorHeight == fullHeight
                    && cropRight > cropLeft && cropBottom > cropTop) {
                activeLeft = (cropLeft + 1) & 0xfffe;
                activeTop = (cropTop + 1) & 0xfffe;
                activeWidth = clamp(cropRight - activeLeft, 1, fullWidth - activeLeft);
                activeHeight = clamp(cropBottom - activeTop, 1, fullHeight - activeTop);
            } else {
                activeLeft = 0;
                activeTop = 0;
                activeWidth = fullWidth;
                activeHeight = fullHeight;
            }
        }

        private int blackLevel(int channel) {
            return blackLevel[channel];
        }
    }

    private static final class Cr3ContainerParser {

        private static final int MAX_TRACKS = 16;

        private final byte[] data;
        private final List<Cr3Track> tracks = new ArrayList<>();
        private long ctmdOffset = -1;
        private long ctmdSize = -1;

        private Cr3ContainerParser(byte[] data) {
            this.data = data;
        }

        private Cr3Track parse() throws IOException {
            if (data.length < 16 || !"ftyp".equals(typeAt(4))) {
                throw new IOException("File is not a Canon CR3 ISO-BMFF container.");
            }

            parseAtoms(0, data.length, null);

            Cr3Track selected = null;
            long selectedBitCount = -1;
            for (Cr3Track track : tracks) {
                track.finishSampleSelection();
                if ("CTMD".equals(track.mediaFormat) && track.mediaOffset > 0 && track.mediaSize > 0) {
                    ctmdOffset = track.mediaOffset;
                    ctmdSize = track.mediaSize;
                }
                if (track.raw && track.mediaOffset > 0 && track.mediaSize > 0 && track.header != null) {
                    long bitCount = (long) track.header.bitsPerSample
                            * track.header.fullWidth * track.header.fullHeight;
                    if (bitCount > selectedBitCount) {
                        selectedBitCount = bitCount;
                        selected = track;
                    }
                }
            }

            if (selected == null) {
                throw new IOException("No decodable Canon CR3 CRAW raw track was found.");
            }
            return selected;
        }

        private void parseAtoms(long start, long size, Cr3Track track) throws IOException {
            long end = checkedEnd(start, size);
            long offset = start;
            while (offset + 8 <= end) {
                Atom atom = readAtom(offset, end);
                if (atom.size < atom.headerSize || atom.offset + atom.size > end) {
                    throw new IOException("Invalid CR3 atom at offset " + offset);
                }

                if ("trak".equals(atom.type)) {
                    if (tracks.size() < MAX_TRACKS) {
                        Cr3Track childTrack = new Cr3Track();
                        tracks.add(childTrack);
                        parseAtoms(atom.contentOffset, atom.contentSize, childTrack);
                    }
                } else if (track != null && "hdlr".equals(atom.type)) {
                    parseHandler(atom, track);
                } else if (track != null && "stsd".equals(atom.type)) {
                    parseSampleDescription(atom, track);
                } else if (track != null && "stsc".equals(atom.type)) {
                    parseSampleToChunk(atom, track);
                } else if (track != null && "stsz".equals(atom.type)) {
                    parseSampleSizes(atom, track);
                } else if (track != null && "stco".equals(atom.type)) {
                    parseChunkOffsets(atom, track, false);
                } else if (track != null && "co64".equals(atom.type)) {
                    parseChunkOffsets(atom, track, true);
                } else if (track != null && "CMP1".equals(atom.type)) {
                    track.header = CrxHeader.parse(data, atom.contentOffset, atom.contentSize);
                    track.raw = true;
                } else if (isContainer(atom.type)) {
                    parseAtoms(atom.contentOffset, atom.contentSize, track);
                }

                offset += atom.size;
            }
        }

        private void parseHandler(Atom atom, Cr3Track track) throws IOException {
            if (atom.contentSize < 12) {
                return;
            }
            track.handlerType = ascii(atom.contentOffset + 8, 4);
        }

        private void parseSampleDescription(Atom atom, Cr3Track track) throws IOException {
            if (atom.contentSize < 8) {
                return;
            }
            long offset = atom.contentOffset + 8;
            int entryCount = unsignedIntToInt(readUnsignedInt(atom.contentOffset + 4));
            long end = checkedEnd(atom.contentOffset, atom.contentSize);

            for (int i = 0; i < entryCount && offset + 8 <= end; i++) {
                long entrySize = readUnsignedInt(offset);
                String format = ascii(offset + 4, 4);
                if (entrySize < 8 || offset + entrySize > end) {
                    return;
                }

                if ("CRAW".equals(format)) {
                    track.mediaFormat = format;
                    parseCrawSampleEntry(offset + 8, entrySize - 8, track);
                } else if ("CTMD".equals(format)) {
                    track.mediaFormat = format;
                }
                offset += entrySize;
            }
        }

        private void parseCrawSampleEntry(long contentOffset, long contentSize, Cr3Track track) throws IOException {
            if (contentSize <= 82) {
                return;
            }
            parseAtoms(contentOffset + 82, contentSize - 82, track);
        }

        private void parseSampleToChunk(Atom atom, Cr3Track track) throws IOException {
            if (atom.contentSize < 8) {
                return;
            }
            int entries = unsignedIntToInt(readUnsignedInt(atom.contentOffset + 4));
            long offset = atom.contentOffset + 8;
            long end = checkedEnd(atom.contentOffset, atom.contentSize);
            for (int i = 0; i < entries && offset + 12 <= end; i++) {
                track.sampleToChunks.add(new SampleToChunk(
                        unsignedIntToInt(readUnsignedInt(offset)),
                        unsignedIntToInt(readUnsignedInt(offset + 4)),
                        unsignedIntToInt(readUnsignedInt(offset + 8))));
                offset += 12;
            }
        }

        private void parseSampleSizes(Atom atom, Cr3Track track) throws IOException {
            if (atom.contentSize < 12) {
                return;
            }
            track.defaultSampleSize = unsignedIntToInt(readUnsignedInt(atom.contentOffset + 4));
            int sampleCount = unsignedIntToInt(readUnsignedInt(atom.contentOffset + 8));
            long offset = atom.contentOffset + 12;
            long end = checkedEnd(atom.contentOffset, atom.contentSize);

            track.sampleSizes.clear();
            if (track.defaultSampleSize == 0) {
                for (int i = 0; i < sampleCount && offset + 4 <= end; i++) {
                    track.sampleSizes.add(unsignedIntToInt(readUnsignedInt(offset)));
                    offset += 4;
                }
            }
        }

        private void parseChunkOffsets(Atom atom, Cr3Track track, boolean longOffsets) throws IOException {
            if (atom.contentSize < 8) {
                return;
            }
            int entries = unsignedIntToInt(readUnsignedInt(atom.contentOffset + 4));
            long offset = atom.contentOffset + 8;
            long end = checkedEnd(atom.contentOffset, atom.contentSize);

            track.chunkOffsets.clear();
            for (int i = 0; i < entries && offset + (longOffsets ? 8 : 4) <= end; i++) {
                long chunkOffset = longOffsets
                        ? (readUnsignedInt(offset) << 32) | readUnsignedInt(offset + 4)
                        : readUnsignedInt(offset);
                track.chunkOffsets.add(chunkOffset);
                offset += longOffsets ? 8 : 4;
            }
        }

        private boolean isContainer(String type) {
            return "moov".equals(type)
                    || "mdia".equals(type)
                    || "minf".equals(type)
                    || "stbl".equals(type);
        }

        private Atom readAtom(long offset, long parentEnd) throws IOException {
            long size = readUnsignedInt(offset);
            String type = ascii(offset + 4, 4);
            int headerSize = 8;
            if (size == 1) {
                size = (readUnsignedInt(offset + 8) << 32) | readUnsignedInt(offset + 12);
                headerSize = 16;
            } else if (size == 0) {
                size = parentEnd - offset;
            }
            return new Atom(offset, size, headerSize, type);
        }

        private long checkedEnd(long start, long size) throws IOException {
            if (start < 0 || size < 0 || start > data.length || size > data.length - start) {
                throw new EOFException("Invalid CR3 atom bounds.");
            }
            return start + size;
        }

        private String typeAt(int offset) throws IOException {
            return ascii(offset, 4);
        }

        private String ascii(long offset, int length) throws IOException {
            int intOffset = toIntOffset(offset, length);
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = (char) (data[intOffset + i] & 0xff);
            }
            return new String(chars);
        }

        private long readUnsignedInt(long offset) throws IOException {
            int intOffset = toIntOffset(offset, 4);
            return CanonCr3RawImageReader.readUnsignedInt(data, intOffset);
        }

        private int toIntOffset(long offset, int length) throws IOException {
            if (offset < 0 || offset > Integer.MAX_VALUE || offset > data.length - length) {
                throw new EOFException("Unexpected end of CR3 container.");
            }
            return (int) offset;
        }
    }

    private static final class Atom {

        private final long offset;
        private final long size;
        private final int headerSize;
        private final String type;
        private final long contentOffset;
        private final long contentSize;

        private Atom(long offset, long size, int headerSize, String type) {
            this.offset = offset;
            this.size = size;
            this.headerSize = headerSize;
            this.type = type;
            this.contentOffset = offset + headerSize;
            this.contentSize = size - headerSize;
        }
    }

    private static final class Cr3Track {

        private String handlerType;
        private String mediaFormat;
        private boolean raw;
        private CrxHeader header;
        private long mediaOffset;
        private long mediaSize;
        private int defaultSampleSize;
        private final List<Integer> sampleSizes = new ArrayList<>();
        private final List<Long> chunkOffsets = new ArrayList<>();
        private final List<SampleToChunk> sampleToChunks = new ArrayList<>();

        private void finishSampleSelection() throws IOException {
            if (chunkOffsets.isEmpty()) {
                return;
            }
            mediaOffset = chunkOffsets.get(0);
            if (defaultSampleSize > 0) {
                mediaSize = defaultSampleSize;
            } else if (!sampleSizes.isEmpty()) {
                mediaSize = sampleSizes.get(0);
            }
            if (mediaSize <= 0) {
                throw new IOException("Canon CR3 raw track has no sample size.");
            }
        }
    }

    private static final class SampleToChunk {

        private final int firstChunk;
        private final int samplesPerChunk;
        private final int sampleDescriptionId;

        private SampleToChunk(int firstChunk, int samplesPerChunk, int sampleDescriptionId) {
            this.firstChunk = firstChunk;
            this.samplesPerChunk = samplesPerChunk;
            this.sampleDescriptionId = sampleDescriptionId;
        }
    }

    private static final class CrxHeader {

        private int version;
        private int fullWidth;
        private int fullHeight;
        private int tileWidth;
        private int tileHeight;
        private int bitsPerSample;
        private int planes;
        private int cfaLayout;
        private int encodingType;
        private int imageLevels;
        private int mdatHeaderSize;
        private int medianBits;

        private static CrxHeader parse(byte[] data, long offset, long size) throws IOException {
            if (size < 32) {
                throw new IOException("Canon CR3 CMP1 header is too small.");
            }

            int intOffset = toIntOffset(data, offset, (int) Math.min(size, 85));
            CrxHeader header = new CrxHeader();
            header.version = readUnsignedShort(data, intOffset + 4);
            header.fullWidth = unsignedIntToInt(readUnsignedInt(data, intOffset + 8));
            header.fullHeight = unsignedIntToInt(readUnsignedInt(data, intOffset + 12));
            header.tileWidth = unsignedIntToInt(readUnsignedInt(data, intOffset + 16));
            header.tileHeight = unsignedIntToInt(readUnsignedInt(data, intOffset + 20));
            header.bitsPerSample = data[intOffset + 24] & 0xff;
            header.planes = (data[intOffset + 25] & 0xff) >> 4;
            header.cfaLayout = data[intOffset + 25] & 0x0f;
            header.encodingType = (data[intOffset + 26] & 0xff) >> 4;
            header.imageLevels = data[intOffset + 26] & 0x0f;
            header.mdatHeaderSize = unsignedIntToInt(readUnsignedInt(data, intOffset + 28));
            header.medianBits = header.bitsPerSample;

            boolean extendedHeader = (data[intOffset + 32] & 0x80) != 0;
            boolean useMedianBits = extendedHeader && size >= 56 && header.planes == 4
                    && ((data[intOffset + 56] >> 6) & 1) == 1;
            if (useMedianBits && size >= 85) {
                header.medianBits = data[intOffset + 84] & 0xff;
            }

            header.validate();
            return header;
        }

        private void validate() throws IOException {
            if (version != 0x100 && version != 0x200) {
                throw new IOException("Unsupported Canon CRX version: 0x" + Integer.toHexString(version));
            }
            if (fullWidth <= 0 || fullHeight <= 0 || tileWidth <= 0 || tileHeight <= 0 || mdatHeaderSize <= 0) {
                throw new IOException("Invalid Canon CRX dimensions/header size.");
            }
            if (planes != 4) {
                throw new IOException("Only Canon CRX four-plane raw data is currently supported; planes=" + planes);
            }
            if (encodingType != 0 && encodingType != 1 && encodingType != 3) {
                throw new IOException("Unsupported Canon CRX encoding type: " + encodingType);
            }
            if (imageLevels < 0 || imageLevels > 3) {
                throw new IOException("Unsupported Canon CRX wavelet level count: " + imageLevels);
            }
            if ((fullWidth & 1) != 0 || (fullHeight & 1) != 0 || (tileWidth & 1) != 0 || (tileHeight & 1) != 0) {
                throw new IOException("Canon CRX four-plane dimensions must be even.");
            }
            if (cfaLayout < 0 || cfaLayout > 3) {
                throw new IOException("Unsupported Canon CRX CFA layout: " + cfaLayout);
            }
            if (bitsPerSample <= 8 || bitsPerSample > 14) {
                throw new IOException("Unsupported Canon CRX bit depth: " + bitsPerSample);
            }
        }
    }

    private static final class CrxDecoder {

        private final byte[] data;
        private final Cr3Track track;
        private CrxImage image;

        private CrxDecoder(byte[] data, Cr3Track track) {
            this.data = data;
            this.track = track;
        }

        private int[] decode() throws IOException {
            CrxHeader header = track.header;
            int rawWidth = header.fullWidth;
            int rawHeight = header.fullHeight;
            int planeWidth = rawWidth / 2;
            int planeHeight = rawHeight / 2;

            if (track.mediaOffset < 0 || track.mediaSize <= header.mdatHeaderSize
                    || track.mediaOffset + track.mediaSize > data.length) {
                throw new IOException("Invalid Canon CR3 raw sample bounds.");
            }

            int pixelCount = checkedPixelCount(rawWidth, rawHeight);
            int[] raw = new int[pixelCount];
            image = new CrxImage(header, raw, rawWidth, rawHeight, planeWidth, planeHeight,
                    track.mediaOffset + header.mdatHeaderSize, track.mediaSize);

            byte[] mdatHeader = copyRange(data, track.mediaOffset, header.mdatHeaderSize);
            if (image.encType == 3 && image.nPlanes == 4 && image.nBits > 8) {
                long planeBufLen = (long) image.nPlanes * image.planeWidth * image.planeHeight;
                if (planeBufLen <= 0 || planeBufLen > Integer.MAX_VALUE) {
                    throw new IOException("Canon CRX plane buffer too large.");
                }
                image.planeBuf = new int[(int) planeBufLen];
            }
            setupImageData(mdatHeader);
            for (int plane = 0; plane < header.planes; plane++) {
                decodePlane(plane);
            }
            if (image.encType == 3 && image.planeBuf != null) {
                for (int row = 0; row < image.planeHeight; row++) {
                    finalizePlaneLineE3(row);
                }
            }
            return raw;
        }

        private void setupImageData(byte[] mdatHeader) throws IOException {
            CrxHeader header = image.header;
            if (header.tileWidth / 2 < 0x16 || header.tileHeight / 2 < 0x16) {
                throw new IOException("Canon CRX tile dimensions are too small.");
            }

            image.tileWidth = header.tileWidth / 2;
            image.tileHeight = header.tileHeight / 2;
            image.tileCols = (image.planeWidth + image.tileWidth - 1) / image.tileWidth;
            image.tileRows = (image.planeHeight + image.tileHeight - 1) / image.tileHeight;
            image.tiles = new CrxTile[image.tileCols * image.tileRows];

            for (int tileIndex = 0; tileIndex < image.tiles.length; tileIndex++) {
                int tileCol = tileIndex % image.tileCols;
                int tileRow = tileIndex / image.tileCols;
                CrxTile tile = new CrxTile();
                tile.number = tileIndex;
                tile.width = tileCol + 1 < image.tileCols
                        ? image.tileWidth
                        : image.planeWidth - image.tileWidth * (image.tileCols - 1);
                tile.height = tileRow + 1 < image.tileRows
                        ? image.tileHeight
                        : image.planeHeight - image.tileHeight * (image.tileRows - 1);
                tile.flags = tileFlags(tileCol, tileRow);
                tile.components = new CrxPlaneComponent[image.header.planes];
                for (int plane = 0; plane < tile.components.length; plane++) {
                    CrxPlaneComponent component = new CrxPlaneComponent(tile.flags);
                    component.subbands = new CrxSubband[image.subbandCount];
                    for (int b = 0; b < image.subbandCount; b++) {
                        component.subbands[b] = new CrxSubband();
                    }
                    if (image.levels > 0) {
                        component.wavelet = new CrxWaveletTransform[image.levels];
                        for (int l = 0; l < image.levels; l++) {
                            component.wavelet[l] = new CrxWaveletTransform();
                        }
                    }
                    crxProcessSubbands(tile, component);
                    tile.components[plane] = component;
                }
                image.tiles[tileIndex] = tile;
            }

            int offset = 0;
            int tileDataOffset = 0;
            for (CrxTile tile : image.tiles) {
                ensureAvailable(mdatHeader, offset, 12);
                int tileHeaderSign = readUnsignedShort(mdatHeader, offset);
                int tileHeaderSize = readUnsignedShort(mdatHeader, offset + 2);
                if ((tileHeaderSign != 0xff01 || tileHeaderSize != 8)
                        && (tileHeaderSign != 0xff11 || (tileHeaderSize != 8 && tileHeaderSize != 16))) {
                    throw new IOException("Invalid Canon CRX tile header.");
                }
                ensureAvailable(mdatHeader, offset, tileHeaderSize + 4);
                int tailSign = readUnsignedShort(mdatHeader, offset + 10);
                if ((tileHeaderSize == 8 && tailSign != 0) || (tileHeaderSize == 16 && tailSign != 0x4000)) {
                    throw new IOException("Invalid Canon CRX tile header terminator.");
                }
                if (readUnsignedShort(mdatHeader, offset + 8) != tile.number) {
                    throw new IOException("Unexpected Canon CRX tile number.");
                }
                tile.size = unsignedIntToInt(readUnsignedInt(mdatHeader, offset + 4));
                tile.dataOffset = tileDataOffset;
                if (tileHeaderSize == 16) {
                    if (readUnsignedShort(mdatHeader, offset + 18) != 0) {
                        throw new IOException("Invalid Canon CRX extended tile header.");
                    }
                    tile.qpDataSize = unsignedIntToInt(readUnsignedInt(mdatHeader, offset + 12));
                    tile.extraSize = readUnsignedShort(mdatHeader, offset + 16);
                    tile.hasQpData = tile.qpDataSize > 0;
                }
                tileDataOffset += tile.size;
                offset += tileHeaderSize + 4;

                int componentDataOffset = 0;
                for (int plane = 0; plane < image.header.planes; plane++) {
                    ensureAvailable(mdatHeader, offset, 12);
                    int componentHeaderSign = readUnsignedShort(mdatHeader, offset);
                    int componentHeaderSize = readUnsignedShort(mdatHeader, offset + 2);
                    if ((componentHeaderSign != 0xff02 && componentHeaderSign != 0xff12)
                            || componentHeaderSize != 8) {
                        throw new IOException("Invalid Canon CRX component header.");
                    }
                    if (((mdatHeader[offset + 8] & 0xff) >> 4) != plane || readUnsigned24(mdatHeader, offset + 9) != 0) {
                        throw new IOException("Unexpected Canon CRX component number.");
                    }

                    CrxPlaneComponent component = tile.components[plane];
                    component.size = unsignedIntToInt(readUnsignedInt(mdatHeader, offset + 4));
                    component.dataOffset = componentDataOffset;
                    component.supportsPartial = (mdatHeader[offset + 8] & 0x08) != 0;
                    int roundedBits = ((mdatHeader[offset + 8] & 0xff) >> 1) & 3;
                    if (roundedBits != 0) {
                        if (image.levels > 0 || !component.supportsPartial) {
                            throw new IOException("Unsupported Canon CRX rounded component without partial mode.");
                        }
                        component.roundedBitsMask = 1 << (roundedBits - 1);
                    }
                    componentDataOffset += component.size;
                    offset += 12;

                    offset = readSubbandHeaders(mdatHeader, offset, component);
                }
            }

            if (image.header.version == 0x200) {
                for (CrxTile tile : image.tiles) {
                    if (tile.hasQpData) {
                        decodeQpData(tile);
                    }
                }
            }
        }

        private int tileFlags(int tileCol, int tileRow) {
            int flags = 0;
            if (image.tileCols > 1) {
                if (tileCol + 1 < image.tileCols) {
                    flags |= TILE_RIGHT;
                }
                if (tileCol > 0) {
                    flags |= TILE_LEFT;
                }
            }
            if (image.tileRows > 1) {
                if (tileRow + 1 < image.tileRows) {
                    flags |= TILE_BOTTOM;
                }
                if (tileRow > 0) {
                    flags |= TILE_TOP;
                }
            }
            return flags;
        }

        private int readSubbandHeaders(byte[] mdatHeader, int offset, CrxPlaneComponent component)
                throws IOException {
            int subbandOffset = 0;
            for (int b = 0; b < image.subbandCount; b++) {
                CrxSubband band = component.subbands[b];
                ensureAvailable(mdatHeader, offset, 12);
                int headerSign = readUnsignedShort(mdatHeader, offset);
                int headerSize = readUnsignedShort(mdatHeader, offset + 2);
                if ((headerSign != 0xff03 || headerSize != 8) && (headerSign != 0xff13 || headerSize != 16)) {
                    throw new IOException("Invalid Canon CRX subband header.");
                }
                ensureAvailable(mdatHeader, offset, headerSize + 4);

                int subbandSize = unsignedIntToInt(readUnsignedInt(mdatHeader, offset + 4));
                if (((mdatHeader[offset + 8] & 0xf0) >> 4) != b) {
                    throw new IOException("Unexpected Canon CRX subband number.");
                }

                band.dataOffset = subbandOffset;
                band.kParam = 0;
                band.bandParam = null;
                band.bandBuf = null;
                band.bandSize = 0;
                if (headerSign == 0xff03) {
                    int bitData = (int) readUnsignedInt(mdatHeader, offset + 8);
                    band.dataSize = subbandSize - (bitData & 0x7ffff);
                    band.supportsPartial = (bitData & 0x08000000) != 0;
                    band.qParam = (bitData >> 19) & 0xff;
                    band.qStepBase = 0;
                    band.qStepMult = 0;
                } else {
                    if ((readUnsignedShort(mdatHeader, offset + 8) & 0x0fff) != 0
                            || readUnsignedShort(mdatHeader, offset + 18) != 0) {
                        throw new IOException("Unsupported Canon CRX subband header flags.");
                    }
                    band.supportsPartial = false;
                    band.qParam = 0;
                    band.dataSize = subbandSize - readUnsignedShort(mdatHeader, offset + 16);
                    band.qStepBase = (int) readUnsignedInt(mdatHeader, offset + 12);
                    band.qStepMult = readUnsignedShort(mdatHeader, offset + 10);
                }

                if (band.dataSize < 0) {
                    throw new IOException("Invalid Canon CRX subband data size.");
                }
                subbandOffset += subbandSize;
                offset += headerSize + 4;
            }
            return offset;
        }

        private void decodePlane(int plane) throws IOException {
            int imageRow = 0;
            for (int tileRow = 0; tileRow < image.tileRows; tileRow++) {
                int imageCol = 0;
                for (int tileCol = 0; tileCol < image.tileCols; tileCol++) {
                    CrxTile tile = image.tiles[tileRow * image.tileCols + tileCol];
                    CrxPlaneComponent component = tile.components[plane];
                    long tileMdatOffset = tile.dataOffset + tile.qpDataSize + tile.extraSize + component.dataOffset;
                    crxSetupSubbandData(component, tile, image.mdatOffset + tileMdatOffset);

                    if (image.levels > 0) {
                        crxIdwt53FilterInitialize(component, image.levels, tile.qStep);
                        for (int row = 0; row < tile.height; row++) {
                            crxIdwt53FilterDecode(component, image.levels - 1, tile.qStep);
                            crxIdwt53FilterTransform(component, image.levels - 1);
                            int[] line = crxIdwt53FilterGetLine(component, image.levels - 1);
                            convertPlaneLine(plane, imageRow + row, imageCol, line, tile.width);
                        }
                    } else {
                        CrxSubband band = component.subbands[0];
                        if (band.dataSize > 0) {
                            for (int row = 0; row < tile.height; row++) {
                                decodeLine(band.bandParam, band.bandBuf);
                                convertPlaneLine(plane, imageRow + row, imageCol, band.bandBuf, tile.width);
                            }
                        }
                    }
                    imageCol += tile.width;
                }
                imageRow += image.tiles[tileRow * image.tileCols].height;
            }
        }

        private void decodeLine(CrxBandParam param, int[] target) throws IOException {
            if (param.currentLine >= param.height) {
                throw new IOException("Too many Canon CRX lines decoded.");
            }

            if (param.currentLine == 0) {
                param.sParam = 0;
                param.kParam = 0;
                if (param.supportsPartial) {
                    if (param.roundedBitsMask <= 0) {
                        decodeTopLine(param, param.current);
                    } else {
                        param.roundedBits = roundedBitCount(param.roundedBitsMask);
                        decodeTopLineRounded(param, param.current);
                    }
                } else {
                    decodeTopLineNoRefPrevLine(param, param.current, param.nonProgressive);
                }
                copyDecodedLine(param.current, target, param.width);
            } else {
                int[] previous = (param.currentLine & 1) == 1 ? param.current : param.previous;
                int[] current = (param.currentLine & 1) == 1 ? param.previous : param.current;
                clearLine(current);

                if (!param.supportsPartial) {
                    decodeLineNoRefPrevLine(param, previous, current, param.nonProgressive);
                } else if (param.roundedBitsMask <= 0) {
                    decodePredictiveLine(param, previous, current);
                } else {
                    decodeRoundedLine(param, previous, current);
                }
                copyDecodedLine(current, target, param.width);
            }
            param.currentLine++;
        }

        private int roundedBitCount(int roundedBitsMask) {
            int roundedBits = 1;
            if ((roundedBitsMask & ~1) != 0) {
                while ((roundedBitsMask >> roundedBits) != 0) {
                    roundedBits++;
                }
            }
            return roundedBits;
        }

        private void decodeTopLine(CrxBandParam param, int[] line) throws IOException {
            line[0] = 0;
            int pos = 0;
            int length = param.width;

            for (; length > 1; length--) {
                if (line[pos] != 0) {
                    line[pos + 1] = line[pos];
                } else {
                    int nSyms = readRunLength(param, length);
                    length -= nSyms;
                    while (nSyms-- > 0) {
                        line[pos + 1] = line[pos];
                        pos++;
                    }
                    if (length <= 0) {
                        break;
                    }
                    line[pos + 1] = 0;
                }

                int bitCode = readErrorCode(param, 41, 21);
                line[pos + 1] += signedCode(bitCode);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                pos++;
            }

            if (length == 1) {
                line[pos + 1] = line[pos];
                int bitCode = readErrorCode(param, 41, 21);
                line[pos + 1] += signedCode(bitCode);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                pos++;
            }
            line[pos + 1] = line[pos] + 1;
        }

        private void decodeTopLineRounded(CrxBandParam param, int[] line) throws IOException {
            line[0] = 0;
            int pos = 0;
            int length = param.width;

            for (; length > 1; length--) {
                if (Math.abs(line[pos]) > param.roundedBitsMask) {
                    line[pos + 1] = line[pos];
                } else {
                    int nSyms = readRunLength(param, length);
                    length -= nSyms;
                    while (nSyms-- > 0) {
                        line[pos + 1] = line[pos];
                        pos++;
                    }
                    if (length <= 0) {
                        break;
                    }
                    line[pos + 1] = 0;
                }

                int bitCode = readErrorCode(param, 41, 21);
                int signed = signedCode(bitCode);
                line[pos + 1] += param.roundedBitsMask * 2 * signed + (signed >> 31);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                pos++;
            }

            if (length == 1) {
                int bitCode = readErrorCode(param, 41, 21);
                int signed = signedCode(bitCode);
                line[pos + 1] += param.roundedBitsMask * 2 * signed + (signed >> 31);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                pos++;
            }
            line[pos + 1] = line[pos] + 1;
        }

        private void decodePredictiveLine(CrxBandParam param, int[] previous, int[] current) throws IOException {
            current[0] = previous[1];
            int prevPos = 0;
            int curPos = 0;
            int length = param.width;

            for (; length > 1; length--) {
                if (current[curPos] != previous[prevPos + 1] || current[curPos] != previous[prevPos + 2]) {
                    decodeSymbol(param, previous, current, prevPos, curPos, true, true);
                    prevPos++;
                    curPos++;
                } else {
                    int nSyms = readRunLength(param, length);
                    length -= nSyms;
                    prevPos += nSyms;
                    while (nSyms-- > 0) {
                        current[curPos + 1] = current[curPos];
                        curPos++;
                    }
                    if (length > 0) {
                        decodeSymbol(param, previous, current, prevPos, curPos, false, length > 1);
                        if (length > 1) {
                            prevPos++;
                        }
                        curPos++;
                    }
                }
            }

            if (length == 1) {
                decodeSymbol(param, previous, current, prevPos, curPos, true, false);
                curPos++;
            }
            current[curPos + 1] = current[curPos] + 1;
        }

        private void decodeSymbol(CrxBandParam param, int[] previous, int[] current,
                                  int prevPos, int curPos, boolean medianPrediction, boolean notEndOfLine)
                throws IOException {
            if (medianPrediction) {
                int delta = previous[prevPos + 1] - previous[prevPos];
                int left = current[curPos];
                int top = previous[prevPos + 1];
                int index = (((previous[prevPos] < left) ^ (delta < 0)) ? 2 : 0)
                        + (((left < top) ^ (delta < 0)) ? 1 : 0);
                current[curPos + 1] = index == 0 || index == 1 ? delta + left : (index == 2 ? left : top);
            } else {
                current[curPos + 1] = previous[prevPos + 1];
            }

            int bitCode = readErrorCode(param, 41, 21);
            current[curPos + 1] += signedCode(bitCode);

            int adjustedBitCode = bitCode;
            if (notEndOfLine) {
                int nextDelta = (previous[prevPos + 2] - previous[prevPos + 1]) << 1;
                adjustedBitCode = (bitCode + Math.abs(nextDelta)) >> 1;
            }
            param.kParam = predictKParameter(param.kParam, adjustedBitCode, 15);
        }

        private void decodeRoundedLine(CrxBandParam param, int[] previous, int[] current) throws IOException {
            previous[0] = previous[1];
            current[0] = previous[1];
            int valueReached = 0;
            int prevPos = 0;
            int curPos = 0;
            int length = param.width;

            for (; length > 1; length--) {
                if (Math.abs(previous[prevPos + 2] - previous[prevPos + 1]) > param.roundedBitsMask) {
                    decodeRoundedSymbol(param, previous, current, prevPos, curPos, true, true);
                    prevPos++;
                    curPos++;
                    valueReached = 1;
                } else if (valueReached != 0
                        || Math.abs(previous[prevPos] - current[curPos]) > param.roundedBitsMask) {
                    decodeRoundedSymbol(param, previous, current, prevPos, curPos, true, true);
                    prevPos++;
                    curPos++;
                    valueReached = 0;
                } else {
                    int nSyms = readRunLength(param, length);
                    length -= nSyms;
                    prevPos += nSyms;
                    while (nSyms-- > 0) {
                        current[curPos + 1] = current[curPos];
                        curPos++;
                    }

                    if (length > 1) {
                        decodeRoundedSymbol(param, previous, current, prevPos, curPos, false, true);
                        prevPos++;
                        curPos++;
                        valueReached = Math.abs(previous[prevPos + 1] - previous[prevPos]) > param.roundedBitsMask
                                ? 1 : 0;
                    } else if (length == 1) {
                        decodeRoundedSymbol(param, previous, current, prevPos, curPos, false, false);
                        curPos++;
                    }
                }
            }

            if (length == 1) {
                decodeRoundedSymbol(param, previous, current, prevPos, curPos, true, false);
                curPos++;
            }
            current[curPos + 1] = current[curPos] + 1;
        }

        private void decodeRoundedSymbol(CrxBandParam param, int[] previous, int[] current,
                                         int prevPos, int curPos, boolean predict, boolean updateFromNext)
                throws IOException {
            int symbol = previous[prevPos + 1];
            if (predict) {
                int deltaH = previous[prevPos + 1] - previous[prevPos];
                int left = current[curPos];
                int top = previous[prevPos + 1];
                int index = (((previous[prevPos] < left) ^ (deltaH < 0)) ? 2 : 0)
                        + (((left < top) ^ (deltaH < 0)) ? 1 : 0);
                symbol = index == 0 || index == 1 ? deltaH + left : (index == 2 ? left : top);
            }

            int bitCode = readErrorCode(param, 41, 21);
            int code = signedCode(bitCode);
            current[curPos + 1] = param.roundedBitsMask * 2 * code + (code >> 31) + symbol;

            if (updateFromNext) {
                if (previous[prevPos + 2] > previous[prevPos + 1]) {
                    code = (previous[prevPos + 2] - previous[prevPos + 1] + param.roundedBitsMask - 1)
                            >> param.roundedBits;
                } else {
                    code = -((previous[prevPos + 1] - previous[prevPos + 2] + param.roundedBitsMask)
                            >> param.roundedBits);
                }
                param.kParam = predictKParameter(param.kParam, (bitCode + 2 * Math.abs(code)) >> 1, 15);
            } else {
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
            }
        }

        private void decodeTopLineNoRefPrevLine(CrxBandParam param, int[] current, int[] kLine) throws IOException {
            current[0] = 0;
            kLine[0] = 0;
            int pos = 0;
            int length = param.width;

            for (; length > 1; length--) {
                if (current[pos] != 0) {
                    int bitCode = readErrorCode(param, 41, 21);
                    current[pos + 1] = signedCode(bitCode);
                    param.kParam = predictKParameter(param.kParam, bitCode, 15);
                } else {
                    int nSyms = readRunLength(param, length);
                    length -= nSyms;
                    while (nSyms-- > 0) {
                        kLine[pos] = 0;
                        current[pos + 1] = 0;
                        pos++;
                    }
                    if (length <= 0) {
                        break;
                    }
                    int bitCode = readErrorCode(param, 41, 21);
                    current[pos + 1] = signedCode(bitCode + 1);
                    param.kParam = predictKParameter(param.kParam, bitCode, 15);
                }
                kLine[pos] = param.kParam;
                pos++;
            }

            if (length == 1) {
                int bitCode = readErrorCode(param, 41, 21);
                current[pos + 1] = signedCode(bitCode);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                kLine[pos] = param.kParam;
                pos++;
            }
            current[pos + 1] = 0;
        }

        private void decodeLineNoRefPrevLine(CrxBandParam param, int[] previous, int[] current, int[] kLine)
                throws IOException {
            int i = 0;
            for (; i < param.width - 1; i++) {
                if ((previous[i + 2] | previous[i + 1] | current[i]) != 0) {
                    int bitCode = readErrorCode(param, 41, 21);
                    current[i + 1] = signedCode(bitCode);
                    param.kParam = predictKParameter(param.kParam, bitCode, 0);
                    if (kLine[i + 1] - param.kParam <= 1) {
                        if (param.kParam >= 15) {
                            param.kParam = 15;
                        }
                    } else {
                        param.kParam++;
                    }
                } else {
                    int nSyms = readRunLength(param, param.width - i);
                    if (nSyms > 0) {
                        for (int n = 0; n < nSyms && i + n < param.width; n++) {
                            current[i + n + 1] = 0;
                            kLine[i + n] = 0;
                        }
                        i += nSyms;
                    }
                    if (i >= param.width - 1) {
                        if (i == param.width - 1) {
                            int bitCode = readErrorCode(param, 41, 21);
                            current[i + 1] = signedCode(bitCode + 1);
                            param.kParam = predictKParameter(param.kParam, bitCode, 15);
                            kLine[i] = param.kParam;
                        }
                        continue;
                    }

                    int bitCode = readErrorCode(param, 41, 21);
                    current[i + 1] = signedCode(bitCode + 1);
                    param.kParam = predictKParameter(param.kParam, bitCode, 0);
                    if (kLine[i + 1] - param.kParam <= 1) {
                        if (param.kParam >= 15) {
                            param.kParam = 15;
                        }
                    } else {
                        param.kParam++;
                    }
                }
                kLine[i] = param.kParam;
            }

            if (i == param.width - 1) {
                int bitCode = readErrorCode(param, 41, 21);
                current[i + 1] = signedCode(bitCode);
                param.kParam = predictKParameter(param.kParam, bitCode, 15);
                kLine[i] = param.kParam;
            }
        }

        private int readRunLength(CrxBandParam param, int length) throws IOException {
            int nSyms = 0;
            if (param.bitstream.getBits(1) != 0) {
                nSyms = 1;
                while (param.bitstream.getBits(1) != 0) {
                    nSyms += JS[param.sParam];
                    if (nSyms > length) {
                        nSyms = length;
                        break;
                    }
                    if (param.sParam < 31) {
                        param.sParam++;
                    }
                    if (nSyms == length) {
                        break;
                    }
                }

                if (nSyms < length) {
                    if (J[param.sParam] != 0) {
                        nSyms += param.bitstream.getBits(J[param.sParam]);
                    }
                    if (param.sParam > 0) {
                        param.sParam--;
                    }
                    if (nSyms > length) {
                        throw new IOException("Invalid Canon CRX run length.");
                    }
                }
            }
            return nSyms;
        }

        private int readErrorCode(CrxBandParam param, int escapeZeros, int escapeBits) throws IOException {
            int bitCode = param.bitstream.getZeros();
            if (bitCode >= escapeZeros) {
                bitCode = param.bitstream.getBits(escapeBits);
            } else if (param.kParam != 0) {
                bitCode = param.bitstream.getBits(param.kParam) | (bitCode << param.kParam);
            }
            return bitCode;
        }

        private int signedCode(int bitCode) {
            return -((bitCode) & 1) ^ (bitCode >> 1);
        }

        private int predictKParameter(int previous, int bitCode, int max) {
            int next = previous
                    - (bitCode < (1 << previous >> 1) ? 1 : 0)
                    + ((bitCode >> previous) > 2 ? 1 : 0)
                    + ((bitCode >> previous) > 5 ? 1 : 0);
            return max == 0 || next < max ? next : max;
        }

        private void copyDecodedLine(int[] source, int[] target, int width) {
            System.arraycopy(source, 1, target, 0, width);
        }

        private void clearLine(int[] line) {
            for (int i = 0; i < line.length; i++) {
                line[i] = 0;
            }
        }

        private void convertPlaneLine(int plane, int imageRow, int imageCol, int[] line, int lineLength)
                throws IOException {
            if (image.encType == 3) {
                // Copy decoded plane row into the intermediate plane buffer; the RGGB colour transform
                // runs later in finalizePlaneLineE3.
                int planeSize = image.planeWidth * image.planeHeight;
                int base = plane * planeSize + image.planeWidth * imageRow + imageCol;
                for (int i = 0; i < lineLength; i++) {
                    if (base + i < image.planeBuf.length) {
                        image.planeBuf[base + i] = line[i];
                    }
                }
                return;
            }

            int planeOffset = image.planeOffsets[plane];
            int rawRowOffset = 4 * image.planeWidth * imageRow;
            if (image.encType == 1) {
                int maxVal = (1 << (image.nBits - 1)) - 1;
                int minVal = -(1 << (image.nBits - 1));
                for (int i = 0; i < lineLength; i++) {
                    int rawIndex = rawRowOffset + 2 * (imageCol + i) + planeOffset;
                    if (rawIndex < 0 || rawIndex >= image.raw.length) {
                        throw new IOException("Canon CRX decoded outside raw mosaic bounds.");
                    }
                    image.raw[rawIndex] = clamp(line[i], minVal, maxVal);
                }
            } else {
                int max = (1 << image.nBits) - 1;
                int median = 1 << (image.nBits - 1);
                for (int i = 0; i < lineLength; i++) {
                    int rawIndex = rawRowOffset + 2 * (imageCol + i) + planeOffset;
                    if (rawIndex < 0 || rawIndex >= image.raw.length) {
                        throw new IOException("Canon CRX decoded outside raw mosaic bounds.");
                    }
                    image.raw[rawIndex] = clamp(median + line[i], 0, max);
                }
            }
        }

        /** LibRaw encType==3 finalization: reconstructs RGGB from the four decoded planes. */
        private void finalizePlaneLineE3(int imageRow) {
            int planeSize = image.planeWidth * image.planeHeight;
            int p0 = imageRow * image.planeWidth;
            int p1 = p0 + planeSize;
            int p2 = p1 + planeSize;
            int p3 = p2 + planeSize;
            int[] pb = image.planeBuf;
            long median = ((long) (1 << (image.medianBits - 1))) << 10;
            int maxVal = (1 << image.medianBits) - 1;
            int rawLineOffset = 4 * image.planeWidth * imageRow;
            int[] po = image.planeOffsets;
            for (int i = 0; i < image.planeWidth; i++) {
                long base0 = median + ((long) pb[p0 + i] << 10);
                long gr = base0 - 168L * pb[p1 + i] - 585L * pb[p3 + i];
                gr = (gr < 0) ? -(((Math.abs(gr) + 512) >> 9) & ~1L) : (((Math.abs(gr) + 512) >> 9) & ~1L);
                int idx = rawLineOffset + 2 * i;
                image.raw[idx + po[0]] = clamp((int) ((base0 + 1510L * pb[p3 + i] + 512) >> 10), 0, maxVal);
                image.raw[idx + po[1]] = clamp((int) ((pb[p2 + i] + gr + 1) >> 1), 0, maxVal);
                image.raw[idx + po[2]] = clamp((int) ((gr - pb[p2 + i] + 1) >> 1), 0, maxVal);
                image.raw[idx + po[3]] = clamp((int) ((base0 + 1927L * pb[p1 + i] + 512) >> 10), 0, maxVal);
            }
        }

        // ---- C-RAW (wavelet / encType 3) machinery, ported from LibRaw crx.cpp ----

        private void crxSetupSubbandIdx(CrxSubband band, int level, int colStartIdx, int bandWidthExCoef,
                                        int rowStartIdx, int bandHeightExCoef) {
            if (image.header.version == 0x200) {
                band.rowStartAddOn = rowStartIdx;
                band.rowEndAddOn = bandHeightExCoef;
                band.colStartAddOn = colStartIdx;
                band.colEndAddOn = bandWidthExCoef;
                band.levelShift = 3 - level;
            } else {
                band.rowStartAddOn = 0;
                band.rowEndAddOn = 0;
                band.colStartAddOn = 0;
                band.colEndAddOn = 0;
                band.levelShift = 0;
            }
        }

        private void crxProcessSubbands(CrxTile tile, CrxPlaneComponent comp) {
            CrxSubband[] sub = comp.subbands;
            int bandIdx = image.subbandCount - 1;
            int bandHeight = tile.height;
            int bandWidth = tile.width;
            int bandWidthExCoef = 0;
            int bandHeightExCoef = 0;
            if (image.levels > 0) {
                int rowExBase = 0x30 * (image.levels - 1) + 6 * (tile.width & 7);
                int colExBase = 0x30 * (image.levels - 1) + 6 * (tile.height & 7);
                for (int level = 0; level < image.levels; level++) {
                    int widthOdd = bandWidth & 1;
                    int heightOdd = bandHeight & 1;
                    bandWidth = (widthOdd + bandWidth) >> 1;
                    bandHeight = (heightOdd + bandHeight) >> 1;
                    int bw0 = 0;
                    int bw1 = 0;
                    int bh0 = 0;
                    int bh1 = 0;
                    int colStart = 0;
                    int rowStart = 0;
                    if ((tile.flags & TILE_RIGHT) != 0) {
                        bw0 = EX_COEF_NUM_TBL[rowExBase + 2 * level];
                        bw1 = EX_COEF_NUM_TBL[rowExBase + 2 * level + 1];
                    }
                    if ((tile.flags & TILE_LEFT) != 0) {
                        bw0++;
                        colStart = 1;
                    }
                    if ((tile.flags & TILE_BOTTOM) != 0) {
                        bh0 = EX_COEF_NUM_TBL[colExBase + 2 * level];
                        bh1 = EX_COEF_NUM_TBL[colExBase + 2 * level + 1];
                    }
                    if ((tile.flags & TILE_TOP) != 0) {
                        bh0++;
                        rowStart = 1;
                    }
                    sub[bandIdx].width = bandWidth + bw0 - widthOdd;
                    sub[bandIdx].height = bandHeight + bh0 - heightOdd;
                    crxSetupSubbandIdx(sub[bandIdx], level + 1, colStart, bw0 - colStart, rowStart, bh0 - rowStart);
                    sub[bandIdx - 1].width = bandWidth + bw1;
                    sub[bandIdx - 1].height = bandHeight + bh0 - heightOdd;
                    crxSetupSubbandIdx(sub[bandIdx - 1], level + 1, 0, bw1, rowStart, bh0 - rowStart);
                    sub[bandIdx - 2].width = bandWidth + bw0 - widthOdd;
                    sub[bandIdx - 2].height = bandHeight + bh1;
                    crxSetupSubbandIdx(sub[bandIdx - 2], level + 1, colStart, bw0 - colStart, 0, bh1);
                    bandIdx -= 3;
                }
                if ((tile.flags & TILE_RIGHT) != 0) {
                    bandWidthExCoef = EX_COEF_NUM_TBL[rowExBase + 2 * image.levels - 1];
                }
                if ((tile.flags & TILE_BOTTOM) != 0) {
                    bandHeightExCoef = EX_COEF_NUM_TBL[colExBase + 2 * image.levels - 1];
                }
            }
            sub[bandIdx].width = bandWidthExCoef + bandWidth;
            sub[bandIdx].height = bandHeightExCoef + bandHeight;
            if (image.levels > 0) {
                crxSetupSubbandIdx(sub[bandIdx], image.levels, 0, bandWidthExCoef, 0, bandHeightExCoef);
            }
        }

        /** LibRaw QP-table decode + crxMakeQStep for version-0x200 per-tile quantization. */
        private void decodeQpData(CrxTile tile) throws IOException {
            CrxBitstream bs = new CrxBitstream(data, image.mdatOffset + tile.dataOffset, tile.qpDataSize);
            int qpWidth = (tile.width >> 3) + ((tile.width & 7) != 0 ? 1 : 0);
            int qpHeight = (tile.height >> 1) + (tile.height & 1);
            int totalQP = qpHeight * qpWidth;
            int[] qpTable = new int[totalQP + 2 * (qpWidth + 2)];
            int lineBase = totalQP;
            int curElem = 0;
            int kParam = 0;
            for (int qpRow = 0; qpRow < qpHeight; qpRow++) {
                int line0 = (qpRow & 1) != 0 ? lineBase + qpWidth + 2 : lineBase;
                int line1 = (qpRow & 1) != 0 ? lineBase : lineBase + qpWidth + 2;
                if (qpRow != 0) {
                    kParam = crxDecodeGolombNormal(bs, qpWidth, qpTable, line0, line1, kParam);
                } else {
                    kParam = crxDecodeGolombTop(bs, qpWidth, qpTable, line1, kParam);
                }
                for (int qpCol = 0; qpCol < qpWidth; qpCol++) {
                    qpTable[curElem++] = qpTable[line1 + qpCol + 1] + 4;
                }
            }
            crxMakeQStep(tile, qpTable);
        }

        private int crxReadQP(CrxBitstream bs, int kParam) throws IOException {
            int qp = bs.getZeros();
            if (qp >= 23) {
                qp = bs.getBits(8);
            } else if (kParam != 0) {
                qp = bs.getBits(kParam) | (qp << kParam);
            }
            return qp;
        }

        private int crxPrediction(int left, int top, int deltaH, int deltaV) {
            int[] symb = {left + deltaH, left + deltaH, left, top};
            return symb[(((deltaV < 0 ? 1 : 0) ^ (deltaH < 0 ? 1 : 0)) << 1) + ((left < top ? 1 : 0) ^ (deltaH < 0 ? 1 : 0))];
        }

        private int crxDecodeGolombTop(CrxBitstream bs, int width, int[] buf, int base, int kParam) throws IOException {
            buf[base] = 0;
            int pos = base;
            for (int i = 0; i < width; i++) {
                buf[pos + 1] = buf[pos];
                int qp = crxReadQP(bs, kParam);
                buf[pos + 1] += -(qp & 1) ^ (qp >> 1);
                kParam = predictKParameter(kParam, qp, 7);
                pos++;
            }
            buf[pos + 1] = buf[pos] + 1;
            return kParam;
        }

        private int crxDecodeGolombNormal(CrxBitstream bs, int width, int[] buf, int p0, int p1, int kParam)
                throws IOException {
            buf[p1] = buf[p0 + 1];
            int deltaH = buf[p0 + 1] - buf[p0];
            for (int i = 0; i < width; i++) {
                buf[p1 + 1] = crxPrediction(buf[p1], buf[p0 + 1], deltaH, buf[p0] - buf[p1]);
                int qp = crxReadQP(bs, kParam);
                buf[p1 + 1] += -(qp & 1) ^ (qp >> 1);
                if (i < width - 1) {
                    deltaH = buf[p0 + 2] - buf[p0 + 1];
                    kParam = predictKParameter(kParam, (qp + 2 * Math.abs(deltaH)) >> 1, 7);
                    p0++;
                } else {
                    kParam = predictKParameter(kParam, qp, 7);
                }
                p1++;
            }
            buf[p1 + 1] = buf[p1] + 1;
            return kParam;
        }

        private int qStepValue(int quantVal) {
            if (quantVal < 0) {
                quantVal = 0;
            }
            int div = quantVal / 6;
            int mod = quantVal % 6;
            if (div >= 6) {
                return Q_STEP_TBL[mod] << ((div - 6) & 0x1f);
            }
            return Q_STEP_TBL[mod] >> (6 - div);
        }

        private void crxMakeQStep(CrxTile tile, int[] qpTable) throws IOException {
            int levels = image.levels;
            if (levels > 3 || levels < 1) {
                throw new IOException("Canon CRX invalid level count for QStep.");
            }
            int qpWidth = (tile.width >> 3) + ((tile.width & 7) != 0 ? 1 : 0);
            int qpHeight = (tile.height >> 1) + (tile.height & 1);
            int qpHeight4 = (tile.height >> 2) + ((tile.height & 3) != 0 ? 1 : 0);
            int qpHeight8 = (tile.height >> 3) + ((tile.height & 7) != 0 ? 1 : 0);
            int totalHeight = qpHeight;
            if (levels > 1) {
                totalHeight += qpHeight4;
            }
            if (levels > 2) {
                totalHeight += qpHeight8;
            }
            int[] tbl = new int[totalHeight * qpWidth];
            tile.qStep = new CrxQStep[levels];
            for (int i = 0; i < levels; i++) {
                tile.qStep[i] = new CrxQStep();
            }
            int qsIdx = 0;
            int tblPos = 0;
            if (levels == 3) {
                CrxQStep qs = tile.qStep[qsIdx++];
                qs.qStepTbl = tbl;
                qs.base = tblPos;
                qs.width = qpWidth;
                qs.height = qpHeight8;
                for (int qpRow = 0; qpRow < qpHeight8; qpRow++) {
                    int r0 = qpWidth * Math.min(4 * qpRow, qpHeight - 1);
                    int r1 = qpWidth * Math.min(4 * qpRow + 1, qpHeight - 1);
                    int r2 = qpWidth * Math.min(4 * qpRow + 2, qpHeight - 1);
                    int r3 = qpWidth * Math.min(4 * qpRow + 3, qpHeight - 1);
                    for (int qpCol = 0; qpCol < qpWidth; qpCol++) {
                        int quantVal = qpTable[r0++] + qpTable[r1++] + qpTable[r2++] + qpTable[r3++];
                        quantVal = ((quantVal < 0 ? 3 : 0) + quantVal) >> 2;
                        tbl[tblPos++] = qStepValue(quantVal);
                    }
                }
            }
            if (levels >= 2) {
                CrxQStep qs = tile.qStep[qsIdx++];
                qs.qStepTbl = tbl;
                qs.base = tblPos;
                qs.width = qpWidth;
                qs.height = qpHeight4;
                for (int qpRow = 0; qpRow < qpHeight4; qpRow++) {
                    int r0 = qpWidth * Math.min(2 * qpRow, qpHeight - 1);
                    int r1 = qpWidth * Math.min(2 * qpRow + 1, qpHeight - 1);
                    for (int qpCol = 0; qpCol < qpWidth; qpCol++) {
                        int quantVal = (qpTable[r0++] + qpTable[r1++]) / 2;
                        tbl[tblPos++] = qStepValue(quantVal);
                    }
                }
            }
            CrxQStep qs = tile.qStep[qsIdx];
            qs.qStepTbl = tbl;
            qs.base = tblPos;
            qs.width = qpWidth;
            qs.height = qpHeight;
            int qi = 0;
            for (int qpRow = 0; qpRow < qpHeight; qpRow++) {
                for (int qpCol = 0; qpCol < qpWidth; qpCol++) {
                    tbl[tblPos++] = qStepValue(qpTable[qi++]);
                }
            }
        }

        private void crxSetupSubbandData(CrxPlaneComponent comp, CrxTile tile, long mdatOffset) throws IOException {
            int toSubbands = 3 * image.levels + 1;
            CrxSubband[] sub = comp.subbands;
            for (int i = 0; i < toSubbands; i++) {
                sub[i].bandSize = sub[i].width;
                sub[i].bandBuf = new int[Math.max(1, sub[i].width)];
            }
            if (image.levels > 0) {
                CrxWaveletTransform[] wv = comp.wavelet;
                wv[0].subband0Buf = sub[0].bandBuf;
                for (int level = 0; level < image.levels; level++) {
                    int band = 3 * level + 1;
                    int transformWidth;
                    if (level >= image.levels - 1) {
                        wv[level].height = tile.height;
                        transformWidth = tile.width;
                    } else {
                        wv[level].height = sub[band + 3].height;
                        transformWidth = sub[band + 4].width;
                    }
                    wv[level].width = transformWidth;
                    for (int k = 0; k < 8; k++) {
                        wv[level].lineBuf[k] = new int[Math.max(1, transformWidth)];
                    }
                    wv[level].curLine = 0;
                    wv[level].curH = 0;
                    wv[level].fltTapH = 0;
                    wv[level].subband1Buf = sub[band].bandBuf;
                    wv[level].subband2Buf = sub[band + 1].bandBuf;
                    wv[level].subband3Buf = sub[band + 2].bandBuf;
                }
            }
            for (int i = 0; i < toSubbands; i++) {
                CrxSubband band = sub[i];
                if (band.dataSize > 0) {
                    boolean supportsPartial = false;
                    int roundedBitsMask = 0;
                    if (comp.supportsPartial && i == 0) {
                        roundedBitsMask = comp.roundedBitsMask;
                        supportsPartial = true;
                    }
                    band.bandParam = new CrxBandParam(
                            new CrxBitstream(data, mdatOffset + band.dataOffset, band.dataSize),
                            band.width, band.height, supportsPartial, roundedBitsMask);
                } else {
                    band.bandParam = null;
                }
            }
        }

        private boolean crxUpdateQparam(CrxSubband band) throws IOException {
            int bitCode = band.bandParam.bitstream.getZeros();
            if (bitCode >= 23) {
                bitCode = band.bandParam.bitstream.getBits(8);
            } else if (band.kParam != 0) {
                bitCode = band.bandParam.bitstream.getBits(band.kParam) | (bitCode << band.kParam);
            }
            band.qParam += -(bitCode & 1) ^ (bitCode >> 1);
            band.kParam = predictKParameter(band.kParam, bitCode, 0);
            return band.kParam > 7;
        }

        private int getSubbandRow(CrxSubband band, int row) {
            if (row < band.rowStartAddOn) {
                return 0;
            }
            if (row < band.height - band.rowEndAddOn) {
                return row - band.rowEndAddOn;
            }
            return band.height - band.rowEndAddOn - band.rowStartAddOn - 1;
        }

        private void crxDecodeLineWithIQuantization(CrxSubband band, CrxQStep qStep) throws IOException {
            if (band.dataSize == 0) {
                Arrays.fill(band.bandBuf, 0, band.bandSize, 0);
                return;
            }
            if (band.supportsPartial && qStep == null && crxUpdateQparam(band)) {
                throw new IOException("Canon CRX quantization parameter overflow.");
            }
            decodeLine(band.bandParam, band.bandBuf);
            if (band.width <= 0) {
                return;
            }
            int[] bandBuf = band.bandBuf;
            if (qStep != null) {
                int rowIdx = getSubbandRow(band, band.bandParam.currentLine - 1);
                int tblRow = qStep.base + qStep.width * rowIdx;
                for (int i = 0; i < band.colStartAddOn; i++) {
                    int quantVal = band.qStepBase + (int) (((long) qStep.qStepTbl[tblRow] * band.qStepMult) >> 3);
                    bandBuf[i] *= clamp(quantVal, 1, 0x168000);
                }
                for (int i = band.colStartAddOn; i < band.width - band.colEndAddOn; i++) {
                    int idx = tblRow + ((i - band.colStartAddOn) >> band.levelShift);
                    int quantVal = band.qStepBase + (int) (((long) qStep.qStepTbl[idx] * band.qStepMult) >> 3);
                    bandBuf[i] *= clamp(quantVal, 1, 0x168000);
                }
                int lastIdx = (band.width - band.colEndAddOn - band.colStartAddOn - 1) >> band.levelShift;
                for (int i = band.width - band.colEndAddOn; i < band.width; i++) {
                    int quantVal = band.qStepBase + (int) (((long) qStep.qStepTbl[tblRow + lastIdx] * band.qStepMult) >> 3);
                    bandBuf[i] *= clamp(quantVal, 1, 0x168000);
                }
            } else {
                int div = band.qParam / 6;
                int mod = band.qParam % 6;
                int qScale = div >= 6 ? Q_STEP_TBL[mod] : Q_STEP_TBL[mod] >> (6 - div);
                if (qScale != 1) {
                    for (int i = 0; i < band.width; i++) {
                        bandBuf[i] *= qScale;
                    }
                }
            }
        }

        private void crxHorizontal53(int[] la, int[] lb, CrxWaveletTransform w, int tileFlag) {
            int[] b0 = w.subband0Buf;
            int[] b1 = w.subband1Buf;
            int[] b2 = w.subband2Buf;
            int[] b3 = w.subband3Buf;
            int i0 = 0;
            int i1 = 0;
            int i2 = 0;
            int i3 = 0;
            int p = 0;
            if (w.width <= 1) {
                la[0] = b0[0];
                lb[0] = b2[0];
                return;
            }
            if ((tileFlag & TILE_LEFT) != 0) {
                la[0] = b0[0] - ((b1[0] + b1[1] + 2) >> 2);
                lb[0] = b2[0] - ((b3[0] + b3[1] + 2) >> 2);
                i1++;
                i3++;
            } else {
                la[0] = b0[0] - ((b1[0] + 1) >> 1);
                lb[0] = b2[0] - ((b3[0] + 1) >> 1);
            }
            i0++;
            i2++;
            for (int i = 0; i < w.width - 3; i += 2) {
                int delta = b0[i0] - ((b1[i1] + b1[i1 + 1] + 2) >> 2);
                la[p + 1] = b1[i1] + ((delta + la[p]) >> 1);
                la[p + 2] = delta;
                delta = b2[i2] - ((b3[i3] + b3[i3 + 1] + 2) >> 2);
                lb[p + 1] = b3[i3] + ((delta + lb[p]) >> 1);
                lb[p + 2] = delta;
                i0++;
                i1++;
                i2++;
                i3++;
                p += 2;
            }
            if ((tileFlag & TILE_RIGHT) != 0) {
                int dA = b0[i0] - ((b1[i1] + b1[i1 + 1] + 2) >> 2);
                la[p + 1] = b1[i1] + ((dA + la[p]) >> 1);
                int dB = b2[i2] - ((b3[i3] + b3[i3 + 1] + 2) >> 2);
                lb[p + 1] = b3[i3] + ((dB + lb[p]) >> 1);
                if ((w.width & 1) != 0) {
                    la[p + 2] = dA;
                    lb[p + 2] = dB;
                }
            } else if ((w.width & 1) != 0) {
                la[p + 1] = b1[i1] + ((la[p] + b0[i0] - ((b1[i1] + 1) >> 1)) >> 1);
                la[p + 2] = b0[i0] - ((b1[i1] + 1) >> 1);
                lb[p + 1] = b3[i3] + ((lb[p] + b2[i2] - ((b3[i3] + 1) >> 1)) >> 1);
                lb[p + 2] = b2[i2] - ((b3[i3] + 1) >> 1);
            } else {
                la[p + 1] = la[p] + b1[i1];
                lb[p + 1] = lb[p] + b3[i3];
            }
        }

        private int[] crxIdwt53FilterGetLine(CrxPlaneComponent comp, int level) {
            CrxWaveletTransform w = comp.wavelet[level];
            int[] result = w.lineBuf[(w.fltTapH - w.curH + 5) % 5 + 3];
            w.curH--;
            return result;
        }

        private void crxIdwt53FilterDecode(CrxPlaneComponent comp, int level, CrxQStep[] qStep) throws IOException {
            CrxWaveletTransform w = comp.wavelet[level];
            if (w.curH != 0) {
                return;
            }
            int sband = 3 * level;
            CrxQStep qStepLevel = qStep != null ? qStep[level] : null;
            if (w.height - 3 <= w.curLine && (comp.tileFlag & TILE_BOTTOM) == 0) {
                if ((w.height & 1) != 0) {
                    if (level != 0) {
                        crxIdwt53FilterDecode(comp, level - 1, qStep);
                    } else {
                        crxDecodeLineWithIQuantization(comp.subbands[sband], qStepLevel);
                    }
                    crxDecodeLineWithIQuantization(comp.subbands[sband + 1], qStepLevel);
                }
            } else {
                if (level != 0) {
                    crxIdwt53FilterDecode(comp, level - 1, qStep);
                } else {
                    crxDecodeLineWithIQuantization(comp.subbands[sband], qStepLevel);
                }
                crxDecodeLineWithIQuantization(comp.subbands[sband + 1], qStepLevel);
                crxDecodeLineWithIQuantization(comp.subbands[sband + 2], qStepLevel);
                crxDecodeLineWithIQuantization(comp.subbands[sband + 3], qStepLevel);
            }
        }

        private void crxIdwt53FilterTransform(CrxPlaneComponent comp, int level) throws IOException {
            CrxWaveletTransform w = comp.wavelet[level];
            if (w.curH != 0) {
                return;
            }
            if (w.curLine >= w.height - 3) {
                if ((comp.tileFlag & TILE_BOTTOM) != 0) {
                    return;
                }
                if ((w.height & 1) != 0) {
                    if (level != 0) {
                        if (comp.wavelet[level - 1].curH == 0) {
                            crxIdwt53FilterTransform(comp, level - 1);
                        }
                        w.subband0Buf = crxIdwt53FilterGetLine(comp, level - 1);
                    }
                    int[] band0 = w.subband0Buf;
                    int[] band1 = w.subband1Buf;
                    int b0 = 0;
                    int b1 = 0;
                    int[] lineBufH0 = w.lineBuf[w.fltTapH + 3];
                    int[] lineBufH1 = w.lineBuf[(w.fltTapH + 1) % 5 + 3];
                    int[] lineBufH2 = w.lineBuf[(w.fltTapH + 2) % 5 + 3];
                    int[] lineBufL0 = w.lineBuf[0];
                    int l0 = 0;
                    int[] oldL1 = w.lineBuf[1];
                    w.lineBuf[1] = w.lineBuf[2];
                    w.lineBuf[2] = oldL1;
                    if (w.width <= 1) {
                        lineBufL0[0] = band0[0];
                    } else {
                        if ((comp.tileFlag & TILE_LEFT) != 0) {
                            lineBufL0[0] = band0[0] - ((band1[0] + band1[1] + 2) >> 2);
                            b1++;
                        } else {
                            lineBufL0[0] = band0[0] - ((band1[0] + 1) >> 1);
                        }
                        b0++;
                        for (int i = 0; i < w.width - 3; i += 2) {
                            int delta = band0[b0] - ((band1[b1] + band1[b1 + 1] + 2) >> 2);
                            lineBufL0[l0 + 1] = band1[b1] + ((lineBufL0[l0] + delta) >> 1);
                            lineBufL0[l0 + 2] = delta;
                            b0++;
                            b1++;
                            l0 += 2;
                        }
                        if ((comp.tileFlag & TILE_RIGHT) != 0) {
                            int delta = band0[b0] - ((band1[b1] + band1[b1 + 1] + 2) >> 2);
                            lineBufL0[l0 + 1] = band1[b1] + ((lineBufL0[l0] + delta) >> 1);
                            if ((w.width & 1) != 0) {
                                lineBufL0[l0 + 2] = delta;
                            }
                        } else if ((w.width & 1) != 0) {
                            int delta = band0[b0] - ((band1[b1] + 1) >> 1);
                            lineBufL0[l0 + 1] = band1[b1] + ((lineBufL0[l0] + delta) >> 1);
                            lineBufL0[l0 + 2] = delta;
                        } else {
                            lineBufL0[l0 + 1] = band1[b1] + lineBufL0[l0];
                        }
                    }
                    lineBufL0 = w.lineBuf[0];
                    int[] lineBufL1 = w.lineBuf[1];
                    for (int i = 0; i < w.width; i++) {
                        int delta = lineBufL0[i] - ((lineBufL1[i] + 1) >> 1);
                        lineBufH1[i] = lineBufL1[i] + ((delta + lineBufH0[i]) >> 1);
                        lineBufH2[i] = delta;
                    }
                    w.curH += 3;
                    w.curLine += 3;
                    w.fltTapH = (w.fltTapH + 3) % 5;
                } else {
                    int[] lineBufL2 = w.lineBuf[2];
                    int[] lineBufH0 = w.lineBuf[w.fltTapH + 3];
                    int[] lineBufH1 = w.lineBuf[(w.fltTapH + 1) % 5 + 3];
                    w.lineBuf[1] = lineBufL2;
                    w.lineBuf[2] = w.lineBuf[1];
                    for (int i = 0; i < w.width; i++) {
                        lineBufH1[i] = lineBufH0[i] + lineBufL2[i];
                    }
                    w.curH += 2;
                    w.curLine += 2;
                    w.fltTapH = (w.fltTapH + 2) % 5;
                }
                return;
            }

            if (level != 0) {
                if (comp.wavelet[level - 1].curH == 0) {
                    crxIdwt53FilterTransform(comp, level - 1);
                }
                w.subband0Buf = crxIdwt53FilterGetLine(comp, level - 1);
            }
            int[] band0 = w.subband0Buf;
            int[] band1 = w.subband1Buf;
            int[] band2 = w.subband2Buf;
            int[] band3 = w.subband3Buf;
            int b0 = 0;
            int b1 = 0;
            int b2 = 0;
            int b3 = 0;
            int[] lineBufL0 = w.lineBuf[0];
            int l0 = 0;
            int[] oldL1 = w.lineBuf[1];
            int l1 = 0;
            int[] lineBufH0 = w.lineBuf[w.fltTapH + 3];
            int[] lineBufH1 = w.lineBuf[(w.fltTapH + 1) % 5 + 3];
            int[] lineBufH2 = w.lineBuf[(w.fltTapH + 2) % 5 + 3];
            w.lineBuf[1] = w.lineBuf[2];
            w.lineBuf[2] = oldL1;
            int[] lineBufL1 = oldL1;
            if (w.width <= 1) {
                lineBufL0[0] = band0[0];
                lineBufL1[0] = band2[0];
            } else {
                if ((comp.tileFlag & TILE_LEFT) != 0) {
                    lineBufL0[0] = band0[0] - ((band1[0] + band1[1] + 2) >> 2);
                    lineBufL1[0] = band2[0] - ((band3[0] + band3[1] + 2) >> 2);
                    b1++;
                    b3++;
                } else {
                    lineBufL0[0] = band0[0] - ((band1[0] + 1) >> 1);
                    lineBufL1[0] = band2[0] - ((band3[0] + 1) >> 1);
                }
                b0++;
                b2++;
                for (int i = 0; i < w.width - 3; i += 2) {
                    int delta = band0[b0] - ((band1[b1] + band1[b1 + 1] + 2) >> 2);
                    lineBufL0[l0 + 1] = band1[b1] + ((delta + lineBufL0[l0]) >> 1);
                    lineBufL0[l0 + 2] = delta;
                    delta = band2[b2] - ((band3[b3] + band3[b3 + 1] + 2) >> 2);
                    lineBufL1[l1 + 1] = band3[b3] + ((delta + lineBufL1[l1]) >> 1);
                    lineBufL1[l1 + 2] = delta;
                    b0++;
                    b1++;
                    b2++;
                    b3++;
                    l0 += 2;
                    l1 += 2;
                }
                if ((comp.tileFlag & TILE_RIGHT) != 0) {
                    int dA = band0[b0] - ((band1[b1] + band1[b1 + 1] + 2) >> 2);
                    lineBufL0[l0 + 1] = band1[b1] + ((dA + lineBufL0[l0]) >> 1);
                    int dB = band2[b2] - ((band3[b3] + band3[b3 + 1] + 2) >> 2);
                    lineBufL1[l1 + 1] = band3[b3] + ((dB + lineBufL1[l1]) >> 1);
                    if ((w.width & 1) != 0) {
                        lineBufL0[l0 + 2] = dA;
                        lineBufL1[l1 + 2] = dB;
                    }
                } else if ((w.width & 1) != 0) {
                    int d = band0[b0] - ((band1[b1] + 1) >> 1);
                    lineBufL0[l0 + 1] = band1[b1] + ((d + lineBufL0[l0]) >> 1);
                    lineBufL0[l0 + 2] = d;
                    d = band2[b2] - ((band3[b3] + 1) >> 1);
                    lineBufL1[l1 + 1] = band3[b3] + ((d + lineBufL1[l1]) >> 1);
                    lineBufL1[l1 + 2] = d;
                } else {
                    lineBufL0[l0 + 1] = lineBufL0[l0] + band1[b1];
                    lineBufL1[l1 + 1] = lineBufL1[l1] + band3[b3];
                }
            }
            lineBufL0 = w.lineBuf[0];
            lineBufL1 = w.lineBuf[1];
            int[] lineBufL2 = w.lineBuf[2];
            for (int i = 0; i < w.width; i++) {
                int delta = lineBufL0[i] - ((lineBufL2[i] + lineBufL1[i] + 2) >> 2);
                lineBufH1[i] = lineBufL1[i] + ((delta + lineBufH0[i]) >> 1);
                lineBufH2[i] = delta;
            }
            if (w.curLine >= w.height - 3 && (w.height & 1) != 0) {
                w.curH += 3;
                w.curLine += 3;
                w.fltTapH = (w.fltTapH + 3) % 5;
            } else {
                w.curH += 2;
                w.curLine += 2;
                w.fltTapH = (w.fltTapH + 2) % 5;
            }
        }

        private void crxIdwt53FilterInitialize(CrxPlaneComponent comp, int level, CrxQStep[] qStep)
                throws IOException {
            if (level == 0) {
                return;
            }
            for (int curLevel = 0, curBand = 0; curLevel < level; curLevel++, curBand += 3) {
                CrxQStep qStepLevel = qStep != null ? qStep[curLevel] : null;
                CrxWaveletTransform w = comp.wavelet[curLevel];
                if (curLevel != 0) {
                    w.subband0Buf = crxIdwt53FilterGetLine(comp, curLevel - 1);
                } else {
                    crxDecodeLineWithIQuantization(comp.subbands[curBand], qStepLevel);
                }
                int[] lineBufH0 = w.lineBuf[w.fltTapH + 3];
                if (w.height > 1) {
                    crxDecodeLineWithIQuantization(comp.subbands[curBand + 1], qStepLevel);
                    crxDecodeLineWithIQuantization(comp.subbands[curBand + 2], qStepLevel);
                    crxDecodeLineWithIQuantization(comp.subbands[curBand + 3], qStepLevel);
                    int[] lineBufL0 = w.lineBuf[0];
                    int[] lineBufL1 = w.lineBuf[1];
                    int[] lineBufL2 = w.lineBuf[2];
                    if ((comp.tileFlag & TILE_TOP) != 0) {
                        crxHorizontal53(lineBufL0, w.lineBuf[1], w, comp.tileFlag);
                        crxDecodeLineWithIQuantization(comp.subbands[curBand + 3], qStepLevel);
                        crxDecodeLineWithIQuantization(comp.subbands[curBand + 2], qStepLevel);
                        int[] band2 = w.subband2Buf;
                        int[] band3 = w.subband3Buf;
                        int c2 = 0;
                        int c3 = 0;
                        int l2 = 0;
                        if (w.width <= 1) {
                            lineBufL2[0] = band2[0];
                        } else {
                            if ((comp.tileFlag & TILE_LEFT) != 0) {
                                lineBufL2[0] = band2[0] - ((band3[0] + band3[1] + 2) >> 2);
                                c3++;
                            } else {
                                lineBufL2[0] = band2[0] - ((band3[0] + 1) >> 1);
                            }
                            c2++;
                            for (int i = 0; i < w.width - 3; i += 2) {
                                int delta = band2[c2] - ((band3[c3] + band3[c3 + 1] + 2) >> 2);
                                lineBufL2[l2 + 1] = band3[c3] + ((lineBufL2[l2] + delta) >> 1);
                                lineBufL2[l2 + 2] = delta;
                                c2++;
                                c3++;
                                l2 += 2;
                            }
                            if ((comp.tileFlag & TILE_RIGHT) != 0) {
                                int delta = band2[c2] - ((band3[c3] + band3[c3 + 1] + 2) >> 2);
                                lineBufL2[l2 + 1] = band3[c3] + ((lineBufL2[l2] + delta) >> 1);
                                if ((w.width & 1) != 0) {
                                    lineBufL2[l2 + 2] = delta;
                                }
                            } else if ((w.width & 1) != 0) {
                                int delta = band2[c2] - ((band3[c3] + 1) >> 1);
                                lineBufL2[l2 + 1] = band3[c3] + ((lineBufL2[l2] + delta) >> 1);
                                lineBufL2[l2 + 2] = delta;
                            } else {
                                lineBufL2[l2 + 1] = band3[c3] + lineBufL2[l2];
                            }
                        }
                        for (int i = 0; i < w.width; i++) {
                            lineBufH0[i] = lineBufL0[i] - ((lineBufL1[i] + lineBufL2[i] + 2) >> 2);
                        }
                    } else {
                        crxHorizontal53(lineBufL0, w.lineBuf[2], w, comp.tileFlag);
                        for (int i = 0; i < w.width; i++) {
                            lineBufH0[i] = lineBufL0[i] - ((lineBufL2[i] + 1) >> 1);
                        }
                    }
                    crxIdwt53FilterDecode(comp, curLevel, qStep);
                    crxIdwt53FilterTransform(comp, curLevel);
                } else {
                    crxDecodeLineWithIQuantization(comp.subbands[curBand + 1], qStepLevel);
                    int[] band0 = w.subband0Buf;
                    int[] band1 = w.subband1Buf;
                    int a0 = 0;
                    int a1 = 0;
                    int h0 = 0;
                    if (w.width <= 1) {
                        lineBufH0[0] = band0[0];
                    } else {
                        if ((comp.tileFlag & TILE_LEFT) != 0) {
                            lineBufH0[0] = band0[0] - ((band1[0] + band1[1] + 2) >> 2);
                            a1++;
                        } else {
                            lineBufH0[0] = band0[0] - ((band1[0] + 1) >> 1);
                        }
                        a0++;
                        for (int i = 0; i < w.width - 3; i += 2) {
                            int delta = band0[a0] - ((band1[a1] + band1[a1 + 1] + 2) >> 2);
                            lineBufH0[h0 + 1] = band1[a1] + ((lineBufH0[h0] + delta) >> 1);
                            lineBufH0[h0 + 2] = delta;
                            a0++;
                            a1++;
                            h0 += 2;
                        }
                        if ((comp.tileFlag & TILE_RIGHT) != 0) {
                            int delta = band0[a0] - ((band1[a1] + band1[a1 + 1] + 2) >> 2);
                            lineBufH0[h0 + 1] = band1[a1] + ((lineBufH0[h0] + delta) >> 1);
                            lineBufH0[h0 + 2] = delta;
                        } else if ((w.width & 1) != 0) {
                            int delta = band0[a0] - ((band1[a1] + 1) >> 1);
                            lineBufH0[h0 + 1] = band1[a1] + ((lineBufH0[h0] + delta) >> 1);
                            lineBufH0[h0 + 2] = delta;
                        } else {
                            lineBufH0[h0 + 1] = band1[a1] + lineBufH0[h0];
                        }
                    }
                    w.curLine++;
                    w.curH++;
                    w.fltTapH = (w.fltTapH + 1) % 5;
                }
            }
        }
    }

    private static final class CrxImage {

        private final CrxHeader header;
        private final int[] raw;
        private final int rawWidth;
        private final int rawHeight;
        private final int planeWidth;
        private final int planeHeight;
        private final long mdatOffset;
        private final long mdatSize;
        private final int[] planeOffsets;
        private final int levels;
        private final int subbandCount;
        private final int encType;
        private final int nBits;
        private final int nPlanes;
        private final int medianBits;
        private final int samplePrecision;
        private int[] planeBuf;   // encType==3 intermediate planes (nPlanes * planeWidth * planeHeight)
        private int tileWidth;
        private int tileHeight;
        private int tileCols;
        private int tileRows;
        private CrxTile[] tiles;

        private CrxImage(CrxHeader header, int[] raw, int rawWidth, int rawHeight,
                         int planeWidth, int planeHeight, long mdatOffset, long mdatSize) {
            this.header = header;
            this.raw = raw;
            this.rawWidth = rawWidth;
            this.rawHeight = rawHeight;
            this.planeWidth = planeWidth;
            this.planeHeight = planeHeight;
            this.mdatOffset = mdatOffset;
            this.mdatSize = mdatSize;
            this.planeOffsets = planeOffsets(header.cfaLayout, planeWidth);
            this.levels = header.imageLevels;
            this.subbandCount = 3 * header.imageLevels + 1;
            this.encType = header.encodingType;
            this.nBits = header.bitsPerSample;
            this.nPlanes = header.planes;
            this.medianBits = header.medianBits;
            this.samplePrecision = header.bitsPerSample + INCR_BIT_TABLE[4 * header.encodingType + 2] + 1;
        }

        private static int[] planeOffsets(int cfaLayout, int planeWidth) {
            int rowSize = 2 * planeWidth;
            switch (cfaLayout) {
                case 0:
                    return new int[]{0, 1, rowSize, rowSize + 1};
                case 1:
                    return new int[]{1, 0, rowSize + 1, rowSize};
                case 2:
                    return new int[]{rowSize, rowSize + 1, 0, 1};
                case 3:
                    return new int[]{rowSize + 1, rowSize, 1, 0};
                default:
                    return new int[]{0, 1, rowSize, rowSize + 1};
            }
        }
    }

    private static final class CrxTile {

        private int number;
        private int flags;
        private int dataOffset;
        private int size;
        private int qpDataSize;
        private int extraSize;
        private int width;
        private int height;
        private boolean hasQpData;
        private CrxQStep[] qStep;   // one per level (C-RAW version 0x200 with QP data)
        private CrxPlaneComponent[] components;
    }

    private static final class CrxPlaneComponent {

        private int dataOffset;
        private int size;
        private int roundedBitsMask;
        private boolean supportsPartial = true;
        private final int tileFlag;
        private CrxSubband[] subbands = {new CrxSubband()};
        private CrxWaveletTransform[] wavelet; // one per level (C-RAW)

        private CrxPlaneComponent(int flags) {
            this.tileFlag = flags;
        }
    }

    private static final class CrxSubband {

        private int width;
        private int height;
        private int dataOffset;
        private int dataSize;
        private int qParam;
        private int kParam;
        private int qStepBase;
        private int qStepMult;
        private int rowStartAddOn;
        private int rowEndAddOn;
        private int colStartAddOn;
        private int colEndAddOn;
        private int levelShift;
        private boolean supportsPartial;
        private int bandSize;      // number of ints in bandBuf
        private int[] bandBuf;     // current decoded (and dequantized) line
        private CrxBandParam bandParam;
    }

    /** LibRaw CrxWaveletTransform: per-level 5/3 IDWT working buffers (see crx.cpp). */
    private static final class CrxWaveletTransform {

        private int[] subband0Buf;
        private int[] subband1Buf;
        private int[] subband2Buf;
        private int[] subband3Buf;
        private final int[][] lineBuf = new int[8][];
        private int curLine;
        private int curH;
        private int fltTapH;
        private int height;
        private int width;
    }

    /** LibRaw CrxQStep: per-level dequantization step table (C-RAW version 0x200). */
    private static final class CrxQStep {

        private int[] qStepTbl;
        private int base;   // offset into qStepTbl
        private int width;
        private int height;
    }

    private static final class CrxBandParam {

        private final CrxBitstream bitstream;
        private final int width;
        private final int height;
        private final boolean supportsPartial;
        private final int roundedBitsMask;
        private final int[] previous;
        private final int[] current;
        private final int[] nonProgressive;
        private int roundedBits;
        private int currentLine;
        private int sParam;
        private int kParam;

        private CrxBandParam(CrxBitstream bitstream, int width, int height,
                             boolean supportsPartial, int roundedBitsMask) {
            this.bitstream = bitstream;
            this.width = width;
            this.height = height;
            this.supportsPartial = supportsPartial;
            this.roundedBitsMask = roundedBitsMask;
            this.previous = new int[width + 2];
            this.current = new int[width + 2];
            this.nonProgressive = new int[width + 2];
        }
    }

    private static final class CrxBitstream {

        private static final long UINT32_MASK = 0xffffffffL;

        private final byte[] data;
        private final int end;
        private int offset;
        private long bitData;
        private int bitsLeft;

        private CrxBitstream(byte[] data, long offset, int length) throws IOException {
            this.data = data;
            int intOffset = toIntOffset(data, offset, length);
            this.offset = intOffset;
            this.end = intOffset + length;
        }

        private int getZeros() throws IOException {
            int nonZeroBit;
            int result;

            if (bitData != 0) {
                nonZeroBit = 63 - Long.numberOfLeadingZeros(bitData);
                result = 31 - nonZeroBit;
                bitData = shiftLeft32(bitData, 32 - nonZeroBit);
                bitsLeft -= 32 - nonZeroBit;
                return result;
            }

            int localBitsLeft = bitsLeft;
            long nextData = 0;
            while (true) {
                while (offset + 4 <= end) {
                    nextData = readUnsignedInt(data, offset);
                    offset += 4;
                    if (nextData != 0) {
                        nonZeroBit = 63 - Long.numberOfLeadingZeros(nextData);
                        result = localBitsLeft + 31 - nonZeroBit;
                        bitData = shiftLeft32(nextData, 32 - nonZeroBit);
                        bitsLeft = nonZeroBit;
                        return result;
                    }
                    localBitsLeft += 32;
                }
                if (offset >= end) {
                    throw new EOFException("Unexpected end of Canon CRX bitstream.");
                }
                nextData = data[offset++] & 0xffL;
                if (nextData != 0) {
                    break;
                }
                localBitsLeft += 8;
            }

            nonZeroBit = 63 - Long.numberOfLeadingZeros(nextData);
            result = localBitsLeft + 7 - nonZeroBit;
            bitData = shiftLeft32(nextData, 32 - nonZeroBit);
            bitsLeft = nonZeroBit;
            return result;
        }

        private int getBits(int bits) throws IOException {
            if (bits <= 0) {
                return 0;
            }
            if (bits > 31) {
                throw new IOException("Canon CRX bit reads above 31 bits are unsupported.");
            }

            int localBitsLeft = bitsLeft;
            long localBitData = bitData;
            long result;

            if (localBitsLeft < bits) {
                if (offset + 4 <= end) {
                    long nextWord = readUnsignedInt(data, offset);
                    offset += 4;
                    bitsLeft = 32 - (bits - localBitsLeft);
                    result = ((nextWord >>> localBitsLeft) | localBitData) >>> (32 - bits);
                    bitData = shiftLeft32(nextWord, bits - localBitsLeft);
                    return (int) result;
                }

                do {
                    if (offset >= end) {
                        throw new EOFException("Unexpected end of Canon CRX bitstream.");
                    }
                    localBitsLeft += 8;
                    long nextByte = data[offset++] & 0xffL;
                    localBitData |= shiftLeft32(nextByte, 32 - localBitsLeft);
                } while (localBitsLeft < bits);
            }

            result = localBitData >>> (32 - bits);
            bitData = shiftLeft32(localBitData, bits);
            bitsLeft = localBitsLeft - bits;
            return (int) result;
        }

        private long shiftLeft32(long value, int shift) {
            if (shift >= 32) {
                return 0;
            }
            return (value << shift) & UINT32_MASK;
        }
    }


    /**
     * Renders the decoded Bayer mosaic to an 8-bit sRGB image following LibRaw's default
     * {@code dcraw_process} pipeline (a direct copy of the pipeline in PanasonicRawImageReader):
     * per-channel black subtraction and {@code adjust_maximum}, {@code scale_colors} with the
     * as-shot camera white balance and highlight clipping, AHD demosaicing, camera-to-sRGB colour
     * conversion, and the BT.709 output tone curve. Only the active crop is emitted.
     */
    private static final class LibRawRenderer {

        private final int[] raw;
        private final int width;
        private final int height;
        private final int bitsPerSample;
        private final int cfaLayout;
        private final int[] blackLevel;
        private final double[] whiteBalance;
        private final double[][] rgbCam;
        private final int cropLeft;
        private final int cropTop;
        private final int cropWidth;
        private final int cropHeight;

        private LibRawRenderer(int[] raw, CrxHeader header, CanonColorMetadata color) {
            this.raw = raw;
            this.width = header.fullWidth;
            this.height = header.fullHeight;
            this.bitsPerSample = header.bitsPerSample;
            this.cfaLayout = header.cfaLayout;
            this.blackLevel = color.blackLevel;
            this.whiteBalance = color.whiteBalance;
            this.rgbCam = color.colorMatrix.rgbCam;
            this.cropLeft = color.activeLeft;
            this.cropTop = color.activeTop;
            this.cropWidth = color.activeWidth;
            this.cropHeight = color.activeHeight;
        }

        private BufferedImage render() throws IOException {
            char[] image = scaleColors();
            ahdInterpolate(image);
            char[] curve = buildGammaCurve(GAMMA_POWER, GAMMA_TOE_SLOPE, GAMMA_IMAX);
            return convertToRgb(image, curve);
        }

        /** LibRaw copy_bayer + adjust_maximum + scale_colors: builds the 4-plane interleaved image. */
        private char[] scaleColors() throws IOException {
            int minBlack = Math.min(blackLevel[RED], Math.min(blackLevel[GREEN], blackLevel[BLUE]));

            int dataMaxTaskCount = parallelTaskCount(height, 1);
            int[] dataMaxPerTask = new int[dataMaxTaskCount];
            runParallelTasks(dataMaxTaskCount, "Canon data-maximum task failed.", taskIndex -> {
                int startRow = taskStart(taskIndex, dataMaxTaskCount, height);
                int endRow = taskEnd(taskIndex, dataMaxTaskCount, height);
                int localMaximum = 0;
                for (int row = startRow; row < endRow; row++) {
                    int base = row * width;
                    for (int col = 0; col < width; col++) {
                        int value = raw[base + col] - blackLevel[bayerChannel(cfaLayout, row, col)];
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

            int maximum = ((1 << bitsPerSample) - 1) - minBlack;
            if (dataMaximum > 0 && dataMaximum < maximum && dataMaximum > maximum * ADJUST_MAXIMUM_THRESHOLD) {
                maximum = dataMaximum;
            }
            if (maximum <= 0) {
                maximum = 1;
            }

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
            double dmax = dmin; // highlight == 0 (clip)
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
                throw new IOException("Canon CR3 image is too large to render: " + width + "x" + height);
            }
            char[] image = new char[(int) (pixelCount * 4L)];
            int taskCount = parallelTaskCount(height, 1);
            runParallelTasks(taskCount, "Canon scale-colors task failed.", taskIndex -> {
                int startRow = taskStart(taskIndex, taskCount, height);
                int endRow = taskEnd(taskIndex, taskCount, height);
                for (int row = startRow; row < endRow; row++) {
                    int base = row * width;
                    for (int col = 0; col < width; col++) {
                        int channel = bayerChannel(cfaLayout, row, col);
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

        /** LibRaw ahd_interpolate: fills R/G/B planes in place (tiled, parallel, core-capped). */
        private void ahdInterpolate(char[] image) throws IOException {
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
            runParallelTasks(taskCount, "Canon AHD interpolation task failed.", taskIndex -> {
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

        private void ahdInterpolateGreen(char[] image, int top, int left, char[] tileRgb) {
            int rowStride = width * 4;
            int directionStride = AHD_TILE * AHD_TILE * 3;
            int rowLimit = Math.min(top + AHD_TILE, height - 2);
            int colLimit = Math.min(left + AHD_TILE, width - 2);
            for (int row = top; row < rowLimit; row++) {
                int col = left + (bayerChannel(cfaLayout, row, left) & 1);
                int channel = bayerChannel(cfaLayout, row, col);
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
                        int channel = 2 - bayerChannel(cfaLayout, row, col);
                        int value;
                        if (channel == 1) {
                            channel = bayerChannel(cfaLayout, row + 1, col);
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
                        channel = bayerChannel(cfaLayout, row, col);
                        tileRgb[rix + channel] = image[p + channel];
                        cielab(tileRgb, rix, tileLab, rix, xyzCam, cbrt);
                    }
                }
            }
        }

        private void cielab(char[] tileRgb, int rgbIndex, short[] tileLab, int labIndex,
                            double[][] xyzCam, float[] cbrt) {
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
                        int h = tileOffset;
                        int v = directionStride + tileOffset;
                        image[p] = (char) ((tileRgb[h] + tileRgb[v]) >> 1);
                        image[p + 1] = (char) ((tileRgb[h + 1] + tileRgb[v + 1]) >> 1);
                        image[p + 2] = (char) ((tileRgb[h + 2] + tileRgb[v + 2]) >> 1);
                    }
                }
            }
        }

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
                                int f = bayerChannel(cfaLayout, y, x);
                                sum[f] += image[(y * width + x) * 4 + f];
                                sum[f + 4]++;
                            }
                        }
                    }
                    int f = bayerChannel(cfaLayout, row, col);
                    for (int c = 0; c < 3; c++) {
                        if (c != f && sum[c + 4] != 0) {
                            image[(row * width + col) * 4 + c] = (char) (sum[c] / sum[c + 4]);
                        }
                    }
                }
            }
        }

        private BufferedImage convertToRgb(char[] image, char[] curve) throws IOException {
            BufferedImage output = new BufferedImage(cropWidth, cropHeight, BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            int taskCount = parallelTaskCount(cropHeight, 1);
            runParallelTasks(taskCount, "Canon convert-to-rgb task failed.", taskIndex -> {
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

    /** Port of LibRaw's gamma_curve (forward tone curve, mode 2): BT.709 16-bit LUT. */
    private static int rggb2rgbg(int index) {
        return index ^ (index >> 1);
    }

    private static CameraMatrix matrix(String modelPrefix, int... adobeColorMatrix) {
        return new CameraMatrix(modelPrefix, adobeColorMatrix);
    }

    private static CameraMatrix findColorMatrix(String model) {
        String normalizedModel = normalizeModel(model);
        if (!normalizedModel.isEmpty()) {
            for (CameraMatrix matrix : CANON_COLOR_MATRICES) {
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
        String normalized = model.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("CANON ")) {
            normalized = normalized.substring("CANON ".length()).trim();
        }
        return normalized;
    }

    private static String findKnownModelInFile(byte[] data) {
        for (CameraMatrix matrix : CANON_COLOR_MATRICES) {
            for (String modelPrefix : matrix.modelPrefixes) {
                if (containsAsciiIgnoreCase(data, modelPrefix)) {
                    return modelPrefix;
                }
            }
        }
        return "";
    }

    private static boolean containsAsciiIgnoreCase(byte[] data, String needle) {
        if (needle == null || needle.isEmpty() || data.length < needle.length()) {
            return false;
        }
        byte[] target = needle.toUpperCase(Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        for (int offset = 0; offset <= data.length - target.length; offset++) {
            int i = 0;
            while (i < target.length && toAsciiUpper(data[offset + i]) == target[i]) {
                i++;
            }
            if (i == target.length) {
                return true;
            }
        }
        return false;
    }

    private static int toAsciiUpper(byte value) {
        int character = value & 0xff;
        if (character >= 'a' && character <= 'z') {
            return character - ('a' - 'A');
        }
        return character;
    }

    private static int bayerChannel(int cfaLayout, int row, int col) {
        boolean evenRow = (row & 1) == 0;
        boolean evenCol = (col & 1) == 0;
        switch (cfaLayout) {
            case 0:
                return evenRow ? (evenCol ? RED : GREEN) : (evenCol ? GREEN : BLUE);
            case 1:
                return evenRow ? (evenCol ? GREEN : RED) : (evenCol ? BLUE : GREEN);
            case 2:
                return evenRow ? (evenCol ? GREEN : BLUE) : (evenCol ? RED : GREEN);
            case 3:
                return evenRow ? (evenCol ? BLUE : GREEN) : (evenCol ? GREEN : RED);
            default:
                return evenRow ? (evenCol ? RED : GREEN) : (evenCol ? GREEN : BLUE);
        }
    }

    private static final class CameraMatrix {

        private final String[] modelPrefixes;
        private final double[][] rgbCam;

        private CameraMatrix(String modelPrefix, int[] adobeColorMatrix) {
            this.modelPrefixes = new String[]{normalizeModel(modelPrefix)};
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

    private static byte[] copyRange(byte[] data, long offset, int length) throws IOException {
        int intOffset = toIntOffset(data, offset, length);
        byte[] copy = new byte[length];
        System.arraycopy(data, intOffset, copy, 0, length);
        return copy;
    }

    private static int checkedPixelCount(int width, int height) throws IOException {
        long pixelCount = (long) width * height;
        if (pixelCount <= 0 || pixelCount > Integer.MAX_VALUE) {
            throw new IOException("Invalid Canon CR3 raw dimensions: " + width + "x" + height);
        }
        return (int) pixelCount;
    }

    private static void ensureAvailable(byte[] data, int offset, int length) throws EOFException {
        if (offset < 0 || length < 0 || offset > data.length - length) {
            throw new EOFException("Unexpected end of Canon CR3 data.");
        }
    }

    private static int readUnsignedShort(byte[] data, int offset) throws EOFException {
        ensureAvailable(data, offset, 2);
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    private static int readUnsignedShort(byte[] data, int offset, boolean littleEndian) throws EOFException {
        ensureAvailable(data, offset, 2);
        if (littleEndian) {
            return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
        }
        return readUnsignedShort(data, offset);
    }

    private static int readUnsigned24(byte[] data, int offset) throws EOFException {
        ensureAvailable(data, offset, 3);
        return ((data[offset] & 0xff) << 16) | ((data[offset + 1] & 0xff) << 8) | (data[offset + 2] & 0xff);
    }

    private static long readUnsignedInt(byte[] data, int offset) throws EOFException {
        ensureAvailable(data, offset, 4);
        return ((data[offset] & 0xffL) << 24)
                | ((data[offset + 1] & 0xffL) << 16)
                | ((data[offset + 2] & 0xffL) << 8)
                | (data[offset + 3] & 0xffL);
    }

    private static long readUnsignedInt(byte[] data, int offset, boolean littleEndian) throws EOFException {
        ensureAvailable(data, offset, 4);
        if (littleEndian) {
            return (data[offset] & 0xffL)
                    | ((data[offset + 1] & 0xffL) << 8)
                    | ((data[offset + 2] & 0xffL) << 16)
                    | ((data[offset + 3] & 0xffL) << 24);
        }
        return readUnsignedInt(data, offset);
    }

    private static int typeSize(int tiffType) {
        switch (tiffType) {
            case 1:  // BYTE
            case 2:  // ASCII
            case 6:  // SBYTE
            case 7:  // UNDEFINED
                return 1;
            case 3:  // SHORT
            case 8:  // SSHORT
                return 2;
            case 4:  // LONG
            case 9:  // SLONG
            case 11: // FLOAT
                return 4;
            case 5:  // RATIONAL
            case 10: // SRATIONAL
            case 12: // DOUBLE
                return 8;
            default:
                return 0;
        }
    }

    private static int unsignedIntToInt(long value) throws IOException {
        if (value > Integer.MAX_VALUE) {
            throw new IOException("Canon CR3 value is too large for in-memory decoding: " + value);
        }
        return (int) value;
    }

    private static int toIntOffset(byte[] data, long offset, int length) throws IOException {
        if (offset < 0 || offset > Integer.MAX_VALUE || length < 0 || offset > data.length - (long) length) {
            throw new EOFException("Unexpected end of Canon CR3 data.");
        }
        return (int) offset;
    }

}
