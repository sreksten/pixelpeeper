package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.flavors.SobelEdgesDetectorFilter;

import java.awt.image.BufferedImage;

public class SobelEdgesDetectorFilterImpl implements SobelEdgesDetectorFilter {

    private BufferedImage sourceImage;
    private BufferedImage filteredImage;
    private volatile boolean isAborted;

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {

        isAborted = false;

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();

        filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[][] edgeColors = new int[width][height];

        int processors = Runtime.getRuntime().availableProcessors();
        int usableCols = width - 2; // columns 1..width-2
        int colsPerProcessor = (usableCols + processors - 1) / processors;
        int[] localMaxima = new int[processors];

        // Pass 1: compute gradients in parallel, each thread tracks its local maximum
        Thread[] threads = new Thread[processors];
        for (int t = 0; t < processors; t++) {
            final int threadIndex = t;
            final int startCol = 1 + threadIndex * colsPerProcessor;
            final int endCol = Math.min(startCol + colsPerProcessor, width - 1);
            threads[t] = new Thread(() -> {
                int localMax = -1;
                for (int i = startCol; i < endCol && !isAborted; i++) {
                    for (int j = 1; j < height - 1; j++) {

                        int val00 = getGrayScale(sourceImage.getRGB(i - 1, j - 1));
                        int val01 = getGrayScale(sourceImage.getRGB(i - 1, j));
                        int val02 = getGrayScale(sourceImage.getRGB(i - 1, j + 1));

                        int val10 = getGrayScale(sourceImage.getRGB(i, j - 1));
                        int val12 = getGrayScale(sourceImage.getRGB(i, j + 1));

                        int val20 = getGrayScale(sourceImage.getRGB(i + 1, j - 1));
                        int val21 = getGrayScale(sourceImage.getRGB(i + 1, j));
                        int val22 = getGrayScale(sourceImage.getRGB(i + 1, j + 1));

                        int gx = -val00 + val02 - val10 - val10 + val12 + val12 - val20 + val22;
                        int gy = -val00 - val01 - val01 - val02 + val20 + val21 + val21 + val22;

                        int square = (gx * gx) + (gy * gy);
                        int g = (int) Math.sqrt(square);

                        if (localMax < g) {
                            localMax = g;
                        }
                        edgeColors[i][j] = g;
                    }
                }
                localMaxima[threadIndex] = localMax;
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            filteredImage = null;
            return;
        }

        // Find global maximum across all thread-local results
        int maxGradient = -1;
        for (int localMax : localMaxima) {
            if (localMax > maxGradient) {
                maxGradient = localMax;
            }
        }

        if (maxGradient <= 0) {
            return;
        }

        final double scale = 255.0 / maxGradient;

        // Pass 2: normalize and write to output image in parallel
        for (int t = 0; t < processors; t++) {
            final int startCol = 1 + t * colsPerProcessor;
            final int endCol = Math.min(startCol + colsPerProcessor, width - 1);
            threads[t] = new Thread(() -> {
                for (int i = startCol; i < endCol && !isAborted; i++) {
                    for (int j = 1; j < height - 1; j++) {
                        int edgeColor = (int) (edgeColors[i][j] * scale);
                        edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;
                        filteredImage.setRGB(i, j, edgeColor);
                    }
                }
            });
            threads[t].start();
        }

        joinAll(threads);

        if (isAborted) {
            filteredImage = null;
        }
    }

    private void joinAll(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public String getDescription() {
        return "The Sobel edge detector highlights areas of rapid brightness change using two 3×3 convolution kernels — " +
                "one measuring horizontal gradients (Gx) and one vertical (Gy). " +
                "The gradient magnitude G = √(Gx² + Gy²) is computed per pixel and normalised to a greyscale output.\n\n" +
                "High values (bright pixels) indicate strong edges; dark pixels indicate uniform regions. " +
                "This filter has no tuneable parameters — the transparency slider controls how strongly the edge map " +
                "is blended over the original image.";
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
