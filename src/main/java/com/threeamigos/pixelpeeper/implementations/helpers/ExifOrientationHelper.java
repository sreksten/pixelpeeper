package com.threeamigos.pixelpeeper.implementations.helpers;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static com.threeamigos.pixelpeeper.data.ExifValue.*;

public class ExifOrientationHelper {

    private ExifOrientationHelper() {
    }

    public static BufferedImage correctOrientation(final BufferedImage original, final int orientation) {

        if (rotationNotNeeded(orientation)) {
            return original;
        }

        final BufferedImage rotated = buildEmptyImage(original, orientation);

        final WritableRaster rasterOriginal = original.copyData(null);
        final WritableRaster rasterRotated = rotated.copyData(null);

        if (orientation == PICTURE_ORIENTATION_FLIP_HORIZONTALLY) {
            flipHorizontally(rasterOriginal, rasterRotated);
        } else if (orientation == PICTURE_ORIENTATION_CLOCKWISE_180) {
            rotate180(rasterOriginal, rasterRotated);
        } else if (orientation == PICTURE_ORIENTATION_FLIP_VERTICALLY) {
            flipVertically(rasterOriginal, rasterRotated);
        } else if (orientation == PICTURE_ORIENTATION_ANTICLOCKWISE_90_FLIP_VERTICALLY) {
            rotateAnticlockwise90FlipVertically(rasterOriginal, rasterRotated);
        } else if (orientation == PICTURE_ORIENTATION_ANTICLOCKWISE_90) {
            rotateClockwise90(rasterOriginal, rasterRotated);
        } else if (orientation == PICTURE_ORIENTATION_CLOCKWISE_90_FLIP_VERTICALLY) {
            rotateClockwise90FlipVertically(rasterOriginal, rasterRotated);
        } else {
            // orientation is CLOCKWISE_90
            rotateAnticlockwise90(rasterOriginal, rasterRotated);
        }
        rotated.setData(rasterRotated);

        return rotated;
    }

    public static BufferedImage undoOrientationCorrection(final BufferedImage rotated, final int orientation) {

        if (rotationNotNeeded(orientation)) {
            return rotated;
        }

        final BufferedImage original = buildEmptyImage(rotated, orientation);
        final WritableRaster rasterRotated = rotated.copyData(null);
        final WritableRaster rasterOriginal = original.copyData(null);

        if (orientation == PICTURE_ORIENTATION_FLIP_HORIZONTALLY) {
            flipHorizontally(rasterRotated, rasterOriginal);
        } else if (orientation == PICTURE_ORIENTATION_CLOCKWISE_180) {
            rotate180(rasterRotated, rasterOriginal);
        } else if (orientation == PICTURE_ORIENTATION_FLIP_VERTICALLY) {
            flipVertically(rasterRotated, rasterOriginal);
        } else if (orientation == PICTURE_ORIENTATION_ANTICLOCKWISE_90_FLIP_VERTICALLY) {
            rotateAnticlockwise90FlipVertically(rasterRotated, rasterOriginal);
        } else if (orientation == PICTURE_ORIENTATION_ANTICLOCKWISE_90) {
            rotateAnticlockwise90(rasterRotated, rasterOriginal);
        } else if (orientation == PICTURE_ORIENTATION_CLOCKWISE_90_FLIP_VERTICALLY) {
            rotateClockwise90FlipVertically(rasterRotated, rasterOriginal);
        } else {
            // orientation is CLOCKWISE_90
            rotateClockwise90(rasterRotated, rasterOriginal);
        }
        original.setData(rasterOriginal);

        return original;
    }

    private static boolean rotationNotNeeded(int orientation) {
        return orientation <= PICTURE_ORIENTATION_AS_IS || orientation > PICTURE_ORIENTATION_CLOCKWISE_90;
    }

    private static BufferedImage buildEmptyImage(BufferedImage original, int orientation) {
        if (orientation <= PICTURE_ORIENTATION_FLIP_VERTICALLY) {
            return new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        } else {
            return new BufferedImage(original.getHeight(), original.getWidth(), original.getType());
        }
    }

    private static void flipHorizontally(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int widthMinusOne = width - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(widthMinusOne - x, y, pixel);
            }
        }
    }

    private static void flipVertically(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int heightMinusOne = height - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(x, heightMinusOne - y, pixel);
            }
        }
    }

    private static void rotate180(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int widthMinusOne = width - 1;
        int heightMinusOne = height - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(widthMinusOne - x, heightMinusOne - y, pixel);
            }
        }
    }

    private static void rotateAnticlockwise90(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int widthMinusOne = width - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(y, widthMinusOne - x, pixel);
            }
        }
    }

    private static void rotateAnticlockwise90FlipVertically(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(y, x, pixel);
            }
        }
    }

    private static void rotateClockwise90(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int heightMinusOne = height - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(heightMinusOne - y, x, pixel);
            }
        }
    }

    private static void rotateClockwise90FlipVertically(WritableRaster original, WritableRaster destination) {
        int width = original.getWidth();
        int height = original.getHeight();
        int widthMinusOne = width - 1;
        int heightMinusOne = height - 1;

        final int[] pixel = new int[original.getSampleModel().getNumBands()];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                original.getPixel(x, y, pixel);
                destination.setPixel(heightMinusOne - y, widthMinusOne - x, pixel);
            }
        }
    }

}
