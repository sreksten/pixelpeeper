package com.threeamigos.pixelpeeper.implementations.edgedetect.flavours;

import com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours.SobelEdgesDetector;

import java.awt.image.BufferedImage;

public class SobelEdgesDetectorImpl implements SobelEdgesDetector {

    private BufferedImage sourceImage;
    private BufferedImage edgesImage;
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

        int maxThreads = Runtime.getRuntime().availableProcessors();

        int sliceWidth = (x - 2) / maxThreads;
        Thread[] threads = new Thread[maxThreads];

        int[][] edgeColors = new int[x][y];
        int maxGradient = -1;

        int startx = 1;
        Calculator[] calculators = new Calculator[maxThreads];
        for (int i = 0; i < maxThreads; i++) {
            calculators[i] = new Calculator(startx, i < maxThreads - 1 ? (startx + sliceWidth) : (x - 2), 1, y - 1, edgeColors);
            threads[i] = new Thread(calculators[i]);
            threads[i].start();
            startx += sliceWidth;
        }
        if (joinThreadsFailed(threads)) return;
        for (Calculator calculator : calculators) {
            maxGradient = Math.max(maxGradient, calculator.getMaxGradient());
        }

        double scale = 255.0 / maxGradient;
        edgesImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);

        startx = 1;
        Painter[] painters = new Painter[maxThreads];
        for (int i = 0; i < maxThreads; i++) {
            painters[i] = new Painter(startx, i < maxThreads - 1 ? (startx + sliceWidth) : (x - 2), 1, y - 1, edgeColors, scale);
            threads[i] = new Thread(painters[i]);
            threads[i].start();
            startx += sliceWidth;
        }
        if (joinThreadsFailed(threads)) return;

        if (isAborted) { //NOSONAR
            edgesImage = null;
        }
    }

    private boolean joinThreadsFailed(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                thread.interrupt();
                isAborted = true;
                edgesImage = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public void abort() {
        isAborted = true;
    }

    @Override
    public BufferedImage getEdgesImage() {
        return edgesImage;
    }

    private class Calculator implements Runnable {

        private final int startX;
        private final int endX;
        private final int startY;
        private final int endY;
        private final int[][] edgeColors;

        private int maxGradient = -1;

        Calculator(int startX, int endX, int startY, int endY, int[][] edgeColors) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
            this.edgeColors = edgeColors;
        }

        public void run() {

            for (int i = startX; i < endX && !isAborted; i++) {
                for (int j = startY; j < endY; j++) {

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
        }

        private int getGrayScale(int rgb) {
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = (rgb) & 0xff;

            // from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
            return (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
        }

        public int getMaxGradient() {
            return maxGradient;
        }
    }

    private class Painter implements Runnable {
        private final int startX;
        private final int endX;
        private final int startY;
        private final int endY;
        private final int[][] edgeColors;
        private final double scale;

        Painter(int startX, int endX, int startY, int endY, int[][] edgeColors, double scale) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
            this.edgeColors = edgeColors;
            this.scale = scale;
        }

        public void run() {

            for (int i = startX; i < endX && !isAborted; i++) {
                for (int j = startY; j < endY; j++) {
                    int edgeColor = edgeColors[i][j];
                    edgeColor = (int) (edgeColor * scale);
                    edgeColor = 0xff000000 | (edgeColor << 16) | (edgeColor << 8) | edgeColor;

                    edgesImage.setRGB(i, j, edgeColor);
                }
            }
        }
    }
}
