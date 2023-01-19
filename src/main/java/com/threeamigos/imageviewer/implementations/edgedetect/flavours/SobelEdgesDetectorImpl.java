package com.threeamigos.imageviewer.implementations.edgedetect.flavours;

import java.awt.image.BufferedImage;

import com.threeamigos.imageviewer.interfaces.edgedetect.flavours.SobelEdgesDetector;

public class SobelEdgesDetectorImpl implements SobelEdgesDetector {

	private BufferedImage sourceImage;
	private BufferedImage edgesImage;

	@Override
	public void setSourceImage(BufferedImage sourceImage) {
		this.sourceImage = sourceImage;
	}

	@Override
	public void process() {

		int x = sourceImage.getWidth();
		int y = sourceImage.getHeight();

		edgesImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);

		int[][] edgeColors = new int[x][y];
		int maxGradient = -1;

		for (int i = 1; i < x - 1; i++) {
			for (int j = 1; j < y - 1; j++) {

				int val00 = getGrayScale(sourceImage.getRGB(i - 1, j - 1));
				int val01 = getGrayScale(sourceImage.getRGB(i - 1, j));
				int val02 = getGrayScale(sourceImage.getRGB(i - 1, j + 1));

				int val10 = getGrayScale(sourceImage.getRGB(i, j - 1));
				int val11 = getGrayScale(sourceImage.getRGB(i, j));
				int val12 = getGrayScale(sourceImage.getRGB(i, j + 1));

				int val20 = getGrayScale(sourceImage.getRGB(i + 1, j - 1));
				int val21 = getGrayScale(sourceImage.getRGB(i + 1, j));
				int val22 = getGrayScale(sourceImage.getRGB(i + 1, j + 1));

				int gx = ((-1 * val00) + (0 * val01) + (1 * val02)) + ((-2 * val10) + (0 * val11) + (2 * val12))
						+ ((-1 * val20) + (0 * val21) + (1 * val22));

				int gy = ((-1 * val00) + (-2 * val01) + (-1 * val02)) + ((0 * val10) + (0 * val11) + (0 * val12))
						+ ((1 * val20) + (2 * val21) + (1 * val22));

				double gval = Math.sqrt((gx * gx) + (gy * gy));
				int g = (int) gval;

				if (maxGradient < g) {
					maxGradient = g;
				}

				edgeColors[i][j] = g;
			}
		}

		double scale = 255.0 / maxGradient;

		for (int i = 1; i < x - 1; i++) {
			for (int j = 1; j < y - 1; j++) {
				int edgeColor = edgeColors[i][j];
				edgeColor = (int) (edgeColor * scale);
				edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

				edgesImage.setRGB(i, j, edgeColor);
			}
		}
	}

	@Override
	public BufferedImage getEdgesImage() {
		return edgesImage;
	}

	private int getGrayScale(int rgb) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb) & 0xff;

		// from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
		int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
		// int gray = (r + g + b) / 3;

		return gray;
	}
}
