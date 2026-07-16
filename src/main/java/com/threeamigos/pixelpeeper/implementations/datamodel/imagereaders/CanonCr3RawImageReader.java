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
 * Canon CR3 reader for the lossless CRX raw branch.
 *
 * <p>This reader decodes the CRX sensor mosaic, subtracts black level,
 * normalizes white level, AHD-demosaics the Bayer data, estimates a
 * conservative scene white balance for display, applies a model-specific
 * camera-to-sRGB matrix, neutralizes near-neutral clipped highlights, applies
 * bounded post-matrix display balance, applies a simple percentile display
 * exposure scale, applies sRGB gamma, and returns an 8-bit
 * {@link BufferedImage}.</p>
 *
 * <p>It does not use the embedded JPEG preview and it does not perform full
 * RAW-development or optical corrections: no chromatic-aberration correction,
 * lens/distortion/perspective correction, vignetting correction, sharpening,
 * denoise, or camera JPEG tone curve. This class is a CR3/CRX reader; older
 * Canon CRW/CR2 files need their own container/RAW decoder before they can use
 * these color matrices.</p>
 */
public class CanonCr3RawImageReader implements ImageReader {

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

    private static final int[] J = {
            0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
            4, 4, 5, 5, 6, 6, 7, 7, 8, 9, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    };

    private static final int[][] CARDINAL_OFFSETS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int[][] DIAGONAL_OFFSETS = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    private static final int[][] DIAGONAL_NW_SE_OFFSETS = {{-1, -1}, {1, 1}};
    private static final int[][] DIAGONAL_NE_SW_OFFSETS = {{-1, 1}, {1, -1}};
    private static final int[][] HORIZONTAL_OFFSETS = {{0, -1}, {0, 1}};
    private static final int[][] VERTICAL_OFFSETS = {{-1, 0}, {1, 0}};
    private static final int[][] HORIZONTAL_TWO_OFFSETS = {{0, -2}, {0, 2}};
    private static final int[][] VERTICAL_TWO_OFFSETS = {{-2, 0}, {2, 0}};

    private static final byte[] CANON_UUID = {
            (byte) 0x85, (byte) 0xc0, (byte) 0xb6, (byte) 0x87,
            (byte) 0x82, 0x0f, 0x11, (byte) 0xe0,
            (byte) 0x81, 0x11, (byte) 0xf4, (byte) 0xce,
            0x46, 0x2b, 0x6a, 0x48
    };

    private static final double[][] SRGB_TO_XYZ = {
            {0.4124564, 0.3575761, 0.1804375},
            {0.2126729, 0.7151522, 0.0721750},
            {0.0193339, 0.1191920, 0.9503041}
    };

    private static final double D65_WHITE_X = 0.95047;
    private static final double D65_WHITE_Y = 1.00000;
    private static final double D65_WHITE_Z = 1.08883;
    private static final double LAB_EPSILON = 216.0 / 24389.0;
    private static final double LAB_KAPPA = 24389.0 / 27.0;

    private static final CameraMatrix DEFAULT_COLOR_MATRIX = matrix("EOS RP",
            8608, -2097, -1178, -5425, 13265, 2383, -1149, 2238, 5680);

    private static final CameraMatrix[] CANON_COLOR_MATRICES = {
            matrix("EOS RP", 8608, -2097, -1178, -5425, 13265, 2383, -1149, 2238, 5680),
            matrix("EOS R50", 9269, -2012, -1107, -3990, 11762, 2527, -569, 2093, 4913),
            matrix("EOS 300D", 8250, -2044, -1127, -8092, 15606, 2664, -2893, 3453, 8348)
    };

    @Override
    public BufferedImage readImage(File file) throws Exception {
        byte[] data = Files.readAllBytes(file.toPath());
        Cr3Track track = new Cr3ContainerParser(data).parse();
        CanonColorMetadata colorMetadata = new CanonMetadataReader(data).read();
        int[] raw = new CrxDecoder(data, track).decode();
        colorMetadata.finish(raw, track.header);
        return new RawRenderer(raw, track.header, colorMetadata).render();
    }

    private static final class CanonMetadataReader {

        private final byte[] data;
        private final CanonColorMetadata metadata = new CanonColorMetadata();

        private CanonMetadataReader(byte[] data) {
            this.data = data;
        }

        private CanonColorMetadata read() throws IOException {
            parseAtoms(0, data.length, false);
            if (metadata.model == null || metadata.model.isEmpty()) {
                metadata.model = findKnownModelInFile(data);
            }
            return metadata;
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
                    if (!model.isEmpty()) {
                        metadata.model = model;
                    }
                } else if (tag == 0x00aa) {
                    readWhiteBalance(valueOffset, type, count, littleEndian);
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

        private void readWhiteBalance(int offset, int type, long count, boolean littleEndian) throws IOException {
            if (type != 3 || count < 5 || offset < 0 || offset > data.length - count * 2L) {
                return;
            }

            int[] values = new int[(int) Math.min(count, 8)];
            for (int i = 0; i < values.length; i++) {
                values[i] = readUnsignedShort(data, offset + i * 2, littleEndian);
            }

            int red = values[1];
            int green = (values[2] + values[3]) / 2;
            int blue = values[4];
            if (red > 0 && green > 0 && blue > 0) {
                metadata.whiteBalance[RED] = clamp(green / (double) red, 0.25, 8.0);
                metadata.whiteBalance[GREEN] = 1.0;
                metadata.whiteBalance[BLUE] = clamp(green / (double) blue, 0.25, 8.0);
                metadata.hasCameraWhiteBalance = true;
            }
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

    private static final class CanonColorMetadata {

        private String model;
        private final double[] whiteBalance = {1.0, 1.0, 1.0};
        private boolean hasCameraWhiteBalance;
        private CameraMatrix colorMatrix = DEFAULT_COLOR_MATRIX;

        private void finish(int[] raw, CrxHeader header) {
            colorMatrix = findColorMatrix(model);
            // Canon's CMT3 0x00aa WB-like values are not consistently usable as
            // the display WB across the tested RP/R50 files, so prefer a
            // decoded-scene estimate and keep parsed camera WB only as fallback.
            if (!estimateWhiteBalance(raw, header) && !hasCameraWhiteBalance) {
                whiteBalance[RED] = 1.0;
                whiteBalance[GREEN] = 1.0;
                whiteBalance[BLUE] = 1.0;
            }
        }

        private boolean estimateWhiteBalance(int[] raw, CrxHeader header) {
            int blackLevel = defaultBlackLevel(header);
            int whiteLevel = (1 << header.bitsPerSample) - 1;
            double[] sum = new double[3];
            long[] count = new long[3];
            int stepX = Math.max(1, header.fullWidth / 800);
            int stepY = Math.max(1, header.fullHeight / 800);

            for (int row = 0; row < header.fullHeight; row += stepY) {
                for (int col = 0; col < header.fullWidth; col += stepX) {
                    int channel = bayerChannel(header.cfaLayout, row, col);
                    double sample = (raw[row * header.fullWidth + col] - blackLevel)
                            / (double) (whiteLevel - blackLevel);
                    if (sample > 0.01 && sample < 0.98) {
                        sum[channel] += sample;
                        count[channel]++;
                    }
                }
            }

            if (count[RED] > 0 && count[GREEN] > 0 && count[BLUE] > 0) {
                double redAverage = sum[RED] / count[RED];
                double greenAverage = sum[GREEN] / count[GREEN];
                double blueAverage = sum[BLUE] / count[BLUE];
                whiteBalance[RED] = clamp(greenAverage / Math.max(0.000001, redAverage), 0.5, 4.0);
                whiteBalance[GREEN] = 1.0;
                whiteBalance[BLUE] = clamp(greenAverage / Math.max(0.000001, blueAverage), 0.5, 4.0);
                return true;
            }
            return false;
        }
    }

    private static final class Cr3ContainerParser {

        private static final int MAX_TRACKS = 16;

        private final byte[] data;
        private final List<Cr3Track> tracks = new ArrayList<>();

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
            if (encodingType != 0) {
                throw new IOException("Only Canon CRX lossless raw encoding is currently supported; encType="
                        + encodingType);
            }
            if (imageLevels != 0) {
                throw new IOException("Canon C-RAW/wavelet CRX is not implemented yet; imageLevels=" + imageLevels);
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
            setupImageData(mdatHeader);
            for (int plane = 0; plane < header.planes; plane++) {
                decodePlane(plane);
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
                    tile.components[plane] = new CrxPlaneComponent(tile.flags);
                    tile.components[plane].subbands[0].width = tile.width;
                    tile.components[plane].subbands[0].height = tile.height;
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
                        if (!component.supportsPartial) {
                            throw new IOException("Unsupported Canon CRX rounded component without partial mode.");
                        }
                        component.roundedBitsMask = 1 << (roundedBits - 1);
                    }
                    componentDataOffset += component.size;
                    offset += 12;

                    offset = readSubbandHeaders(mdatHeader, offset, component);
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
            CrxSubband band = component.subbands[0];
            ensureAvailable(mdatHeader, offset, 12);

            int headerSign = readUnsignedShort(mdatHeader, offset);
            int headerSize = readUnsignedShort(mdatHeader, offset + 2);
            if ((headerSign != 0xff03 || headerSize != 8) && (headerSign != 0xff13 || headerSize != 16)) {
                throw new IOException("Invalid Canon CRX subband header.");
            }
            ensureAvailable(mdatHeader, offset, headerSize + 4);

            int subbandSize = unsignedIntToInt(readUnsignedInt(mdatHeader, offset + 4));
            if (((mdatHeader[offset + 8] & 0xf0) >> 4) != 0) {
                throw new IOException("Unexpected Canon CRX subband number.");
            }

            band.dataOffset = 0;
            if (headerSign == 0xff03) {
                int bitData = (int) readUnsignedInt(mdatHeader, offset + 8);
                band.dataSize = subbandSize - (bitData & 0x7ffff);
                band.supportsPartial = (bitData & 0x08000000) != 0;
                band.qParam = (bitData >> 19) & 0xff;
            } else {
                if ((readUnsignedShort(mdatHeader, offset + 8) & 0x0fff) != 0
                        || readUnsignedShort(mdatHeader, offset + 18) != 0) {
                    throw new IOException("Unsupported Canon CRX subband header flags.");
                }
                band.dataSize = subbandSize - readUnsignedShort(mdatHeader, offset + 16);
                band.supportsPartial = false;
                band.qParam = 0;
            }

            if (band.dataSize < 0) {
                throw new IOException("Invalid Canon CRX subband data size.");
            }
            return offset + headerSize + 4;
        }

        private void decodePlane(int plane) throws IOException {
            int imageRow = 0;
            for (int tileRow = 0; tileRow < image.tileRows; tileRow++) {
                int imageCol = 0;
                for (int tileCol = 0; tileCol < image.tileCols; tileCol++) {
                    CrxTile tile = image.tiles[tileRow * image.tileCols + tileCol];
                    CrxPlaneComponent component = tile.components[plane];
                    CrxSubband band = component.subbands[0];

                    if (band.dataSize > 0) {
                        long tileMdatOffset = tile.dataOffset + tile.qpDataSize + tile.extraSize
                                + component.dataOffset + band.dataOffset;
                        CrxBandParam param = new CrxBandParam(
                                new CrxBitstream(data, image.mdatOffset + tileMdatOffset, band.dataSize),
                                band.width,
                                band.height,
                                component.supportsPartial,
                                component.roundedBitsMask);

                        int[] line = new int[band.width];
                        for (int row = 0; row < tile.height; row++) {
                            decodeLine(param, line);
                            convertPlaneLine(plane, imageRow + row, imageCol, line, tile.width);
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
            int max = (1 << image.header.bitsPerSample) - 1;
            int median = 1 << (image.header.bitsPerSample - 1);
            int planeOffset = image.planeOffsets[plane];
            int rawRowOffset = 4 * image.planeWidth * imageRow;

            for (int i = 0; i < lineLength; i++) {
                int rawIndex = rawRowOffset + 2 * (imageCol + i) + planeOffset;
                if (rawIndex < 0 || rawIndex >= image.raw.length) {
                    throw new IOException("Canon CRX decoded outside raw mosaic bounds.");
                }
                image.raw[rawIndex] = clamp(median + line[i], 0, max);
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
        private CrxPlaneComponent[] components;
    }

    private static final class CrxPlaneComponent {

        private int dataOffset;
        private int size;
        private int roundedBitsMask;
        private boolean supportsPartial = true;
        private final CrxSubband[] subbands = {new CrxSubband()};

        private CrxPlaneComponent(int flags) {
            // Tile flags are used by wavelet decoding. The phase-1 lossless branch keeps them for future extension.
        }
    }

    private static final class CrxSubband {

        private int width;
        private int height;
        private int dataOffset;
        private int dataSize;
        private int qParam;
        private boolean supportsPartial;
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

    private static final class RawRenderer {

        private final int[] raw;
        private final CrxHeader header;
        private final int blackLevel;
        private final int whiteLevel;
        private final double[] whiteBalance;
        private final double[][] cameraToSrgb;
        private final double displayScale;
        private final double[] displayChannelScale;
        private final AhdWorkspace ahdWorkspace = new AhdWorkspace();

        private RawRenderer(int[] raw, CrxHeader header, CanonColorMetadata colorMetadata) {
            this.raw = raw;
            this.header = header;
            this.whiteLevel = (1 << header.bitsPerSample) - 1;
            this.blackLevel = defaultBlackLevel(header);
            this.whiteBalance = colorMetadata.whiteBalance;
            this.cameraToSrgb = colorMetadata.colorMatrix.cameraToSrgb;
            this.displayScale = computeDisplayScale();
            this.displayChannelScale = computeDisplayChannelScale();
        }

        private BufferedImage render() {
            BufferedImage image = new BufferedImage(header.fullWidth, header.fullHeight, BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            double[] cameraRgb = new double[3];
            double[] linearSrgb = new double[3];

            for (int row = 0; row < header.fullHeight; row++) {
                for (int col = 0; col < header.fullWidth; col++) {
                    demosaic(row, col, cameraRgb);
                    for (int channel = 0; channel < cameraRgb.length; channel++) {
                        cameraRgb[channel] *= whiteBalance[channel];
                    }
                    colorConvert(cameraRgb, linearSrgb);
                    neutralizeClippedHighlight(cameraRgb, linearSrgb);

                    int red = toByte(clamp(linearSrgb[RED] * displayScale * displayChannelScale[RED], 0.0, 1.0));
                    int green = toByte(clamp(linearSrgb[GREEN] * displayScale * displayChannelScale[GREEN], 0.0, 1.0));
                    int blue = toByte(clamp(linearSrgb[BLUE] * displayScale * displayChannelScale[BLUE], 0.0, 1.0));
                    pixels[row * header.fullWidth + col] = (red << 16) | (green << 8) | blue;
                }
            }

            return image;
        }

        private double computeDisplayScale() {
            int stepX = Math.max(1, header.fullWidth / 360);
            int stepY = Math.max(1, header.fullHeight / 360);
            int columns = (header.fullWidth + stepX - 1) / stepX;
            int rows = (header.fullHeight + stepY - 1) / stepY;
            double[] luminanceValues = new double[columns * rows];
            double[] cameraRgb = new double[3];
            double[] linearSrgb = new double[3];
            int count = 0;

            for (int row = 0; row < header.fullHeight; row += stepY) {
                for (int col = 0; col < header.fullWidth; col += stepX) {
                    demosaic(row, col, cameraRgb);
                    for (int channel = 0; channel < cameraRgb.length; channel++) {
                        cameraRgb[channel] *= whiteBalance[channel];
                    }
                    colorConvert(cameraRgb, linearSrgb);

                    double luminance = 0.2126 * Math.max(0.0, linearSrgb[RED])
                            + 0.7152 * Math.max(0.0, linearSrgb[GREEN])
                            + 0.0722 * Math.max(0.0, linearSrgb[BLUE]);
                    if (Double.isFinite(luminance) && luminance > 0.0005) {
                        luminanceValues[count++] = luminance;
                    }
                }
            }

            if (count < 16) {
                return 1.0;
            }

            Arrays.sort(luminanceValues, 0, count);
            double highlight = luminanceValues[Math.min(count - 1, (int) Math.floor(count * 0.995))];
            if (highlight <= 0.001) {
                return 1.0;
            }
            return clamp(0.90 / highlight, 0.25, 6.0);
        }

        private double[] computeDisplayChannelScale() {
            int stepX = Math.max(1, header.fullWidth / 360);
            int stepY = Math.max(1, header.fullHeight / 360);
            double[] sum = new double[3];
            long count = 0;
            double[] cameraRgb = new double[3];
            double[] linearSrgb = new double[3];

            for (int row = 0; row < header.fullHeight; row += stepY) {
                for (int col = 0; col < header.fullWidth; col += stepX) {
                    demosaic(row, col, cameraRgb);
                    for (int channel = 0; channel < cameraRgb.length; channel++) {
                        cameraRgb[channel] *= whiteBalance[channel];
                    }
                    colorConvert(cameraRgb, linearSrgb);
                    neutralizeClippedHighlight(cameraRgb, linearSrgb);

                    double red = clamp(linearSrgb[RED] * displayScale, 0.0, 1.0);
                    double green = clamp(linearSrgb[GREEN] * displayScale, 0.0, 1.0);
                    double blue = clamp(linearSrgb[BLUE] * displayScale, 0.0, 1.0);
                    double luminance = 0.2126 * red + 0.7152 * green + 0.0722 * blue;
                    if (luminance > 0.03 && luminance < 0.95) {
                        sum[RED] += red;
                        sum[GREEN] += green;
                        sum[BLUE] += blue;
                        count++;
                    }
                }
            }

            if (count < 16 || sum[RED] <= 0.0 || sum[GREEN] <= 0.0 || sum[BLUE] <= 0.0) {
                return new double[]{1.0, 1.0, 1.0};
            }

            double redAverage = sum[RED] / count;
            double greenAverage = sum[GREEN] / count;
            double blueAverage = sum[BLUE] / count;
            return new double[]{
                    clamp(greenAverage / redAverage, 0.75, 1.35),
                    1.0,
                    clamp(greenAverage / blueAverage, 0.75, 1.35)
            };
        }

        private void colorConvert(double[] cameraRgb, double[] linearSrgb) {
            for (int channel = 0; channel < linearSrgb.length; channel++) {
                linearSrgb[channel] = cameraToSrgb[channel][RED] * cameraRgb[RED]
                        + cameraToSrgb[channel][GREEN] * cameraRgb[GREEN]
                        + cameraToSrgb[channel][BLUE] * cameraRgb[BLUE];
            }
        }

        private void neutralizeClippedHighlight(double[] cameraRgb, double[] linearSrgb) {
            double cameraMin = Math.min(cameraRgb[RED], Math.min(cameraRgb[GREEN], cameraRgb[BLUE]));
            double cameraMax = Math.max(cameraRgb[RED], Math.max(cameraRgb[GREEN], cameraRgb[BLUE]));
            double nearNeutralHighlight = clamp((cameraMin - 0.80) / 0.20, 0.0, 1.0);
            double clippedHighlight = clamp((cameraMax - 1.0) / 0.25, 0.0, 1.0);
            double blend = nearNeutralHighlight * clippedHighlight;

            if (blend <= 0.0) {
                return;
            }

            double neutral = Math.min(1.0, Math.max(linearSrgb[RED], Math.max(linearSrgb[GREEN], linearSrgb[BLUE])));
            for (int channel = 0; channel < linearSrgb.length; channel++) {
                linearSrgb[channel] = linearSrgb[channel] * (1.0 - blend) + neutral * blend;
            }
        }

        private void demosaic(int row, int col, double[] rgb) {
            if (!insideAhdWindow(row, col)) {
                demosaicBilinear(row, col, rgb);
                return;
            }

            interpolateAhdCandidate(row, col, true, ahdWorkspace.horizontalRgb);
            interpolateAhdCandidate(row, col, false, ahdWorkspace.verticalRgb);
            cameraRgbToLab(ahdWorkspace.horizontalRgb, ahdWorkspace.horizontalLab);
            cameraRgbToLab(ahdWorkspace.verticalRgb, ahdWorkspace.verticalLab);

            int horizontalHomogeneity = 0;
            int verticalHomogeneity = 0;
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                for (int colOffset = -1; colOffset <= 1; colOffset++) {
                    if (rowOffset == 0 && colOffset == 0) {
                        continue;
                    }

                    int neighborRow = row + rowOffset;
                    int neighborCol = col + colOffset;
                    interpolateAhdCandidate(neighborRow, neighborCol, true, ahdWorkspace.neighborHorizontalRgb);
                    interpolateAhdCandidate(neighborRow, neighborCol, false, ahdWorkspace.neighborVerticalRgb);
                    cameraRgbToLab(ahdWorkspace.neighborHorizontalRgb, ahdWorkspace.neighborHorizontalLab);
                    cameraRgbToLab(ahdWorkspace.neighborVerticalRgb, ahdWorkspace.neighborVerticalLab);

                    double horizontalDistance = labDistance(ahdWorkspace.horizontalLab, ahdWorkspace.neighborHorizontalLab);
                    double verticalDistance = labDistance(ahdWorkspace.verticalLab, ahdWorkspace.neighborVerticalLab);
                    if (horizontalDistance < verticalDistance) {
                        horizontalHomogeneity++;
                    } else if (verticalDistance < horizontalDistance) {
                        verticalHomogeneity++;
                    }
                }
            }

            if (horizontalHomogeneity > verticalHomogeneity) {
                copyRgb(ahdWorkspace.horizontalRgb, rgb);
            } else if (verticalHomogeneity > horizontalHomogeneity) {
                copyRgb(ahdWorkspace.verticalRgb, rgb);
            } else {
                averageRgb(ahdWorkspace.horizontalRgb, ahdWorkspace.verticalRgb, rgb);
            }
        }

        private boolean insideAhdWindow(int row, int col) {
            return row >= 2 && row < header.fullHeight - 2 && col >= 2 && col < header.fullWidth - 2;
        }

        private void interpolateAhdCandidate(int row, int col, boolean horizontal, double[] rgb) {
            int channel = bayerChannel(row, col);
            double center = sample(row, col);

            if (channel == RED) {
                rgb[RED] = center;
                rgb[GREEN] = interpolateGreenAhd(row, col, horizontal);
                rgb[BLUE] = interpolateDiagonalColorAhd(row, col, BLUE, horizontal, rgb[GREEN]);
            } else if (channel == BLUE) {
                rgb[GREEN] = interpolateGreenAhd(row, col, horizontal);
                rgb[RED] = interpolateDiagonalColorAhd(row, col, RED, horizontal, rgb[GREEN]);
                rgb[BLUE] = center;
            } else {
                rgb[RED] = interpolateCardinalColorAhd(row, col, RED, horizontal, center);
                rgb[GREEN] = center;
                rgb[BLUE] = interpolateCardinalColorAhd(row, col, BLUE, horizontal, center);
            }
        }

        private double interpolateGreenAhd(int row, int col, boolean horizontal) {
            int[][] greenOffsets = horizontal ? HORIZONTAL_OFFSETS : VERTICAL_OFFSETS;
            int[][] sameColorOffsets = horizontal ? HORIZONTAL_TWO_OFFSETS : VERTICAL_TWO_OFFSETS;
            int channel = bayerChannel(row, col);
            double green = average(row, col, GREEN, greenOffsets);
            double sameColor = averageFromOffsetsOrNaN(row, col, channel, sameColorOffsets);
            if (Double.isNaN(sameColor)) {
                return green;
            }
            return Math.max(0.0, green + (sample(row, col) - sameColor) * 0.5);
        }

        private double interpolateCardinalColorAhd(int row, int col, int targetChannel, boolean horizontal,
                                                  double greenCenter) {
            int[][] primaryOffsets = horizontal ? HORIZONTAL_OFFSETS : VERTICAL_OFFSETS;
            int[][] fallbackOffsets = horizontal ? VERTICAL_OFFSETS : HORIZONTAL_OFFSETS;
            double difference = colorDifferenceAverage(row, col, targetChannel, primaryOffsets);
            if (Double.isNaN(difference)) {
                difference = colorDifferenceAverage(row, col, targetChannel, fallbackOffsets);
            }
            if (Double.isNaN(difference)) {
                return average(row, col, targetChannel, CARDINAL_OFFSETS);
            }
            return Math.max(0.0, greenCenter + difference);
        }

        private double interpolateDiagonalColorAhd(int row, int col, int targetChannel, boolean horizontal,
                                                  double greenCenter) {
            int[][] primaryOffsets = horizontal ? DIAGONAL_NW_SE_OFFSETS : DIAGONAL_NE_SW_OFFSETS;
            double difference = colorDifferenceAverage(row, col, targetChannel, primaryOffsets);
            if (Double.isNaN(difference)) {
                difference = colorDifferenceAverage(row, col, targetChannel, DIAGONAL_OFFSETS);
            }
            if (Double.isNaN(difference)) {
                return average(row, col, targetChannel, DIAGONAL_OFFSETS);
            }
            return Math.max(0.0, greenCenter + difference);
        }

        private double colorDifferenceAverage(int row, int col, int targetChannel, int[][] offsets) {
            double sum = 0.0;
            int count = 0;
            for (int[] offset : offsets) {
                int sampleRow = row + offset[0];
                int sampleCol = col + offset[1];
                if (inside(sampleRow, sampleCol) && bayerChannel(sampleRow, sampleCol) == targetChannel) {
                    sum += sample(sampleRow, sampleCol) - greenAtSite(sampleRow, sampleCol);
                    count++;
                }
            }
            return count > 0 ? sum / count : Double.NaN;
        }

        private double greenAtSite(int row, int col) {
            if (bayerChannel(row, col) == GREEN) {
                return sample(row, col);
            }
            return average(row, col, GREEN, CARDINAL_OFFSETS);
        }

        private double averageFromOffsetsOrNaN(int row, int col, int targetChannel, int[][] offsets) {
            double sum = 0.0;
            int count = 0;
            for (int[] offset : offsets) {
                int sampleRow = row + offset[0];
                int sampleCol = col + offset[1];
                if (inside(sampleRow, sampleCol) && bayerChannel(sampleRow, sampleCol) == targetChannel) {
                    sum += sample(sampleRow, sampleCol);
                    count++;
                }
            }
            return count > 0 ? sum / count : Double.NaN;
        }

        private void demosaicBilinear(int row, int col, double[] rgb) {
            int channel = bayerChannel(row, col);
            if (channel == RED) {
                rgb[RED] = sample(row, col);
                rgb[GREEN] = average(row, col, GREEN, CARDINAL_OFFSETS);
                rgb[BLUE] = average(row, col, BLUE, DIAGONAL_OFFSETS);
            } else if (channel == BLUE) {
                rgb[RED] = average(row, col, RED, DIAGONAL_OFFSETS);
                rgb[GREEN] = average(row, col, GREEN, CARDINAL_OFFSETS);
                rgb[BLUE] = sample(row, col);
            } else {
                rgb[GREEN] = sample(row, col);
                if (greenHasHorizontalRed(row, col)) {
                    rgb[RED] = average(row, col, RED, HORIZONTAL_OFFSETS);
                    rgb[BLUE] = average(row, col, BLUE, VERTICAL_OFFSETS);
                } else {
                    rgb[RED] = average(row, col, RED, VERTICAL_OFFSETS);
                    rgb[BLUE] = average(row, col, BLUE, HORIZONTAL_OFFSETS);
                }
            }
        }

        private boolean greenHasHorizontalRed(int row, int col) {
            return (inside(row, col - 1) && bayerChannel(row, col - 1) == RED)
                    || (inside(row, col + 1) && bayerChannel(row, col + 1) == RED);
        }

        private double average(int row, int col, int targetChannel, int[][] offsets) {
            double sum = 0.0;
            int count = 0;
            for (int[] offset : offsets) {
                int sampleRow = row + offset[0];
                int sampleCol = col + offset[1];
                if (inside(sampleRow, sampleCol) && bayerChannel(sampleRow, sampleCol) == targetChannel) {
                    sum += sample(sampleRow, sampleCol);
                    count++;
                }
            }
            if (count == 0) {
                return nearest(row, col, targetChannel);
            }
            return sum / count;
        }

        private double nearest(int row, int col, int targetChannel) {
            for (int radius = 1; radius <= 3; radius++) {
                double sum = 0.0;
                int count = 0;
                for (int sampleRow = row - radius; sampleRow <= row + radius; sampleRow++) {
                    for (int sampleCol = col - radius; sampleCol <= col + radius; sampleCol++) {
                        if (inside(sampleRow, sampleCol) && bayerChannel(sampleRow, sampleCol) == targetChannel) {
                            sum += sample(sampleRow, sampleCol);
                            count++;
                        }
                    }
                }
                if (count > 0) {
                    return sum / count;
                }
            }
            return sample(row, col);
        }

        private double sample(int row, int col) {
            int value = raw[row * header.fullWidth + col];
            return clamp((value - blackLevel) / (double) (whiteLevel - blackLevel), 0.0, 1.0);
        }

        private int bayerChannel(int row, int col) {
            return CanonCr3RawImageReader.bayerChannel(header.cfaLayout, row, col);
        }

        private boolean inside(int row, int col) {
            return row >= 0 && row < header.fullHeight && col >= 0 && col < header.fullWidth;
        }

        private void cameraRgbToLab(double[] cameraRgb, double[] lab) {
            double cameraRed = cameraRgb[RED] * whiteBalance[RED];
            double cameraGreen = cameraRgb[GREEN] * whiteBalance[GREEN];
            double cameraBlue = cameraRgb[BLUE] * whiteBalance[BLUE];
            double red = Math.max(0.0, cameraToSrgb[RED][RED] * cameraRed
                    + cameraToSrgb[RED][GREEN] * cameraGreen
                    + cameraToSrgb[RED][BLUE] * cameraBlue);
            double green = Math.max(0.0, cameraToSrgb[GREEN][RED] * cameraRed
                    + cameraToSrgb[GREEN][GREEN] * cameraGreen
                    + cameraToSrgb[GREEN][BLUE] * cameraBlue);
            double blue = Math.max(0.0, cameraToSrgb[BLUE][RED] * cameraRed
                    + cameraToSrgb[BLUE][GREEN] * cameraGreen
                    + cameraToSrgb[BLUE][BLUE] * cameraBlue);

            double x = (SRGB_TO_XYZ[0][RED] * red + SRGB_TO_XYZ[0][GREEN] * green + SRGB_TO_XYZ[0][BLUE] * blue)
                    / D65_WHITE_X;
            double y = (SRGB_TO_XYZ[1][RED] * red + SRGB_TO_XYZ[1][GREEN] * green + SRGB_TO_XYZ[1][BLUE] * blue)
                    / D65_WHITE_Y;
            double z = (SRGB_TO_XYZ[2][RED] * red + SRGB_TO_XYZ[2][GREEN] * green + SRGB_TO_XYZ[2][BLUE] * blue)
                    / D65_WHITE_Z;

            double fx = labPivot(x);
            double fy = labPivot(y);
            double fz = labPivot(z);
            lab[0] = 116.0 * fy - 16.0;
            lab[1] = 500.0 * (fx - fy);
            lab[2] = 200.0 * (fy - fz);
        }

        private double labPivot(double value) {
            if (value > LAB_EPSILON) {
                return Math.cbrt(value);
            }
            return (LAB_KAPPA * value + 16.0) / 116.0;
        }

        private double labDistance(double[] left, double[] right) {
            return Math.abs(left[0] - right[0])
                    + Math.abs(left[1] - right[1])
                    + Math.abs(left[2] - right[2]);
        }

        private void copyRgb(double[] source, double[] target) {
            target[RED] = source[RED];
            target[GREEN] = source[GREEN];
            target[BLUE] = source[BLUE];
        }

        private void averageRgb(double[] left, double[] right, double[] target) {
            target[RED] = (left[RED] + right[RED]) * 0.5;
            target[GREEN] = (left[GREEN] + right[GREEN]) * 0.5;
            target[BLUE] = (left[BLUE] + right[BLUE]) * 0.5;
        }

        private int toByte(double linear) {
            double gamma = linear <= 0.0031308 ? 12.92 * linear : 1.055 * Math.pow(linear, 1.0 / 2.4) - 0.055;
            return clamp((int) Math.round(gamma * 255.0), 0, 255);
        }

        private static final class AhdWorkspace {

            private final double[] horizontalRgb = new double[3];
            private final double[] verticalRgb = new double[3];
            private final double[] neighborHorizontalRgb = new double[3];
            private final double[] neighborVerticalRgb = new double[3];
            private final double[] horizontalLab = new double[3];
            private final double[] verticalLab = new double[3];
            private final double[] neighborHorizontalLab = new double[3];
            private final double[] neighborVerticalLab = new double[3];
        }
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

    private static int defaultBlackLevel(CrxHeader header) {
        int whiteLevel = (1 << header.bitsPerSample) - 1;
        return Math.min(whiteLevel - 1, 1 << Math.max(0, header.bitsPerSample - 3));
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

    private static double[][] convertAdobeColorMatrix(int[] adobeColorMatrix) {
        if (adobeColorMatrix.length != 9) {
            throw new IllegalArgumentException("Canon color matrices must contain 9 values.");
        }

        double[][] camXyz = new double[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                camXyz[row][col] = adobeColorMatrix[row * 3 + col] / 10000.0;
            }
        }

        double[][] camRgb = multiply3x3(camXyz, SRGB_TO_XYZ);
        for (int row = 0; row < 3; row++) {
            double rowSum = camRgb[row][RED] + camRgb[row][GREEN] + camRgb[row][BLUE];
            if (Math.abs(rowSum) < 0.00001) {
                throw new IllegalArgumentException("Canon color matrix has a zero row sum.");
            }
            for (int col = 0; col < 3; col++) {
                camRgb[row][col] /= rowSum;
            }
        }

        return invert3x3(camRgb);
    }

    private static double[][] multiply3x3(double[][] left, double[][] right) {
        double[][] result = new double[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int index = 0; index < 3; index++) {
                    result[row][col] += left[row][index] * right[index][col];
                }
            }
        }
        return result;
    }

    private static double[][] invert3x3(double[][] matrix) {
        double a = matrix[0][0];
        double b = matrix[0][1];
        double c = matrix[0][2];
        double d = matrix[1][0];
        double e = matrix[1][1];
        double f = matrix[1][2];
        double g = matrix[2][0];
        double h = matrix[2][1];
        double i = matrix[2][2];
        double determinant = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
        if (Math.abs(determinant) < 0.00000001) {
            throw new IllegalArgumentException("Canon color matrix is not invertible.");
        }

        return new double[][]{
                {(e * i - f * h) / determinant, (c * h - b * i) / determinant, (b * f - c * e) / determinant},
                {(f * g - d * i) / determinant, (a * i - c * g) / determinant, (c * d - a * f) / determinant},
                {(d * h - e * g) / determinant, (b * g - a * h) / determinant, (a * e - b * d) / determinant}
        };
    }

    private static final class CameraMatrix {

        private final String[] modelPrefixes;
        private final double[][] cameraToSrgb;

        private CameraMatrix(String modelPrefix, int[] adobeColorMatrix) {
            this.modelPrefixes = new String[]{normalizeModel(modelPrefix)};
            this.cameraToSrgb = convertAdobeColorMatrix(adobeColorMatrix);
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

    private static boolean hasModelBoundary(String model, int prefixLength) {
        if (prefixLength >= model.length()) {
            return true;
        }
        char next = model.charAt(prefixLength);
        return !((next >= 'A' && next <= 'Z') || (next >= '0' && next <= '9'));
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
