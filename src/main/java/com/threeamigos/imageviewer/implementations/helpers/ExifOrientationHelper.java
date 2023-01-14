package com.threeamigos.imageviewer.implementations.helpers;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class ExifOrientationHelper {

	public static final int AS_IS = 1;
	public static final int FLIP_HORIZONTALLY = 2;
	public static final int CLOCKWISE_180 = 3;
	public static final int FLIP_VERTICALLY = 4;
	public static final int ANTICLOCKWISE_90_FLIP_VERTICALLY = 5;
	public static final int ANTICLOCKWISE_90 = 6;
	public static final int CLOCKWISE_90_FLIP_VERTICALLY = 7;
	public static final int CLOCKWISE_90 = 8;

	public static final BufferedImage correctOrientation(final BufferedImage original, final int orientation) {

		if (orientation < 1 || orientation > 8) {
			return original;
		}

		if (orientation == AS_IS) {
			return original;
		}

		final BufferedImage rotated;

		if (orientation <= 4) {
			rotated = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		} else {
			rotated = new BufferedImage(original.getHeight(), original.getWidth(), original.getType());
		}

		final WritableRaster rasterOriginal = original.copyData(null);
		final WritableRaster rasterRotated = rotated.copyData(null);

		if (orientation == FLIP_HORIZONTALLY) {

			flipHorizontally(rasterOriginal, rasterRotated);

		} else if (orientation == CLOCKWISE_180) {

			rotate180(rasterOriginal, rasterRotated);

		} else if (orientation == FLIP_VERTICALLY) {

			flipVertically(rasterOriginal, rasterRotated);

		} else if (orientation == ANTICLOCKWISE_90_FLIP_VERTICALLY) {

			rotateAnticlockwise90FlipVertically(rasterOriginal, rasterRotated);

		} else if (orientation == ANTICLOCKWISE_90) {

			rotateClockwise90(rasterOriginal, rasterRotated);

		} else if (orientation == CLOCKWISE_90_FLIP_VERTICALLY) {

			rotateClockwise90FlipVertically(rasterOriginal, rasterRotated);

		} else if (orientation == CLOCKWISE_90) {

			rotateAnticlockwise90(rasterOriginal, rasterRotated);

		}
		rotated.setData(rasterRotated);

		return rotated;
	}

	public static final BufferedImage undoOrientationCorrection(final BufferedImage rotated, final int orientation) {

		if (orientation < 1 || orientation > 8) {
			return rotated;
		}

		if (orientation == AS_IS) {
			return rotated;
		}

		final BufferedImage original;

		if (orientation <= 4) {
			original = new BufferedImage(rotated.getWidth(), rotated.getHeight(), rotated.getType());
		} else {
			original = new BufferedImage(rotated.getHeight(), rotated.getWidth(), rotated.getType());
		}

		final WritableRaster rasterRotated = rotated.copyData(null);
		final WritableRaster rasterOriginal = original.copyData(null);

		if (orientation == FLIP_HORIZONTALLY) {

			flipHorizontally(rasterRotated, rasterOriginal);

		} else if (orientation == CLOCKWISE_180) {

			rotate180(rasterRotated, rasterOriginal);

		} else if (orientation == FLIP_VERTICALLY) {

			flipVertically(rasterRotated, rasterOriginal);

		} else if (orientation == ANTICLOCKWISE_90_FLIP_VERTICALLY) {

			rotateAnticlockwise90FlipVertically(rasterRotated, rasterOriginal);

		} else if (orientation == ANTICLOCKWISE_90) {

			rotateAnticlockwise90(rasterRotated, rasterOriginal);

		} else if (orientation == CLOCKWISE_90_FLIP_VERTICALLY) {

			rotateClockwise90FlipVertically(rasterRotated, rasterOriginal);

		} else if (orientation == CLOCKWISE_90) {

			rotateClockwise90(rasterRotated, rasterOriginal);

		}
		original.setData(rasterOriginal);

		return original;
	}

	private static final void flipHorizontally(WritableRaster original, WritableRaster destination) {
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

	private static final void flipVertically(WritableRaster original, WritableRaster destination) {
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

	private static final void rotate180(WritableRaster original, WritableRaster destination) {
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

	private static final void rotateAnticlockwise90(WritableRaster original, WritableRaster destination) {
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

	private static final void rotateAnticlockwise90FlipVertically(WritableRaster original, WritableRaster destination) {
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

	private static final void rotateClockwise90(WritableRaster original, WritableRaster destination) {
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

	private static final void rotateClockwise90FlipVertically(WritableRaster original, WritableRaster destination) {
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
