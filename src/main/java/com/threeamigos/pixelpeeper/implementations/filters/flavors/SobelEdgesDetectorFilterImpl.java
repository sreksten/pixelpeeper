package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.flavors.SobelEdgesDetectorFilter;

import java.awt.image.BufferedImage;

public class SobelEdgesDetectorFilterImpl implements SobelEdgesDetectorFilter {

    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private boolean isAborted;

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {

        isAborted = false;

        int x = sourceImage.getWidth();
        int y = sourceImage.getHeight();

        filteredImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);

        int[][] edgeColors = new int[x][y];
        int maxGradient = -1;

        for (int i = 1; i < x - 1 && !isAborted; i++) {
            for (int j = 1; j < y - 1; j++) {

                int val00 = getGrayScale(sourceImage.getRGB(i - 1, j - 1));
                int val01 = getGrayScale(sourceImage.getRGB(i - 1, j));
                int val02 = getGrayScale(sourceImage.getRGB(i - 1, j + 1));

                int val10 = getGrayScale(sourceImage.getRGB(i, j - 1));
                int val12 = getGrayScale(sourceImage.getRGB(i, j + 1));

                int val20 = getGrayScale(sourceImage.getRGB(i + 1, j - 1));
                int val21 = getGrayScale(sourceImage.getRGB(i + 1, j));
                int val22 = getGrayScale(sourceImage.getRGB(i + 1, j + 1));

                // gx =
                // ((-1 * val00) + (0 * val01) + (1 * val02)) +
                // ((-2 * val10) + (0) + (2 * val12)) +
                // ((-1 * val20) + (0 * val21) + (1 * val22))

                int gx = -val00 + val02 - val10 - val10 + val12 + val12 - val20 + val22;

                // gy =
                // ((-1 * val00) + (-2 * val01) + (-1 * val02)) +
                // ((0 * val10) + (0) + (0 * val12)) +
                // ((1 * val20) + (2 * val21) + (1 * val22))

                int gy = -val00 - val01 - val01 - val02 + val20 + val21 + val21 + val22;

                int square = (gx * gx) + (gy * gy);
                int g = (int) Math.sqrt(square);

                if (maxGradient < g) {
                    maxGradient = g;
                }

                edgeColors[i][j] = g;
            }
        }

        double scale = 255.0 / maxGradient;

        for (int i = 1; i < x - 1 && !isAborted; i++) {
            for (int j = 1; j < y - 1; j++) {
                int edgeColor = edgeColors[i][j];
                edgeColor = (int) (edgeColor * scale);
                edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

                filteredImage.setRGB(i, j, edgeColor);
            }
        }

        if (isAborted) {
            filteredImage = null;
        }
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    private int getGrayScale(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = (rgb) & 0xff;

        // from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
        return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }
}
