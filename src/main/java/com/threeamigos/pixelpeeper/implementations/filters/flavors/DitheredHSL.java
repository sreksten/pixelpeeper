package com.threeamigos.pixelpeeper.implementations.filters.flavors;

public class DitheredHSL {

    private static final Dither dither = new Dither();

    private final HSL hslToMap;
    private final HSL firstDitherColor;
    private final HSL secondDitherColor;
    private final boolean dithered;
    private final int ditherIntensity;

    /**
     * Maps a system color to itself
     * @param systemColor a color the machine is able to produce
     */
    DitheredHSL(HSL systemColor) {
        this.firstDitherColor = systemColor;
        this.secondDitherColor = null;
        this.dithered = false;
        this.ditherIntensity = 0;
        this.hslToMap = systemColor;
    }

    /**
     * Creates a gradient to map a non-native color to a dithered combination of two system colors;
     * for example to map various hues of orange using red and yellow.
     * @param firstDitherColor first color to use when dithering
     * @param secondDitherColor second color to use for dithering
     * @param ditherIntensity from 1 to 7, indicates how much of the first color is used
     */
    DitheredHSL(HSL firstDitherColor, HSL secondDitherColor, int ditherIntensity) {
        this.firstDitherColor = firstDitherColor;
        this.secondDitherColor = secondDitherColor;
        this.dithered = true;
        this.ditherIntensity = ditherIntensity;

        int firstRGB = firstDitherColor.getRGB();
        int firstRed = (firstRGB & 0xFF0000) >> 16;
        int firstGreen = (firstRGB & 0xFF00) >> 8;
        int firstBlue = firstRGB & 0xFF;

        int secondRGB = secondDitherColor.getRGB();
        int secondRed = (secondRGB & 0xFF0000) >> 16;
        int secondGreen = (secondRGB & 0xFF00) >> 8;
        int secondBlue = secondRGB & 0xFF;

        int redDelta = (secondRed - firstRed) / 7 * (ditherIntensity + 1);
        int greenDelta = (secondGreen - firstGreen) / 7 * (ditherIntensity + 1);
        int blueDelta = (secondBlue - firstBlue) / 7 * (ditherIntensity + 1);

        hslToMap = new HSL(new RGB(0xFF, firstRed + redDelta, firstGreen + greenDelta, firstBlue + blueDelta));
    }

    /**
     * Maps a specific color to a combination of two system colors.
     * @param toBeMapped color to be mapped to a combination of other two colors
     * @param firstDitherColor first color to use when dithering
     * @param secondDitherColor second color to use for dithering
     * @param ditherIntensity from 1 to 7, indicates how much of the first color is used
     */
    DitheredHSL(HSL toBeMapped, HSL firstDitherColor, HSL secondDitherColor, int ditherIntensity) {
        this.hslToMap = toBeMapped;
        this.firstDitherColor = firstDitherColor;
        this.secondDitherColor = secondDitherColor;
        this.dithered = true;
        this.ditherIntensity = ditherIntensity;
    }

    /**
     * The original color we are dithering
     * @return a color
     */
    public HSL getHSLToMap() {
        return hslToMap;
    }

    /**
     * The color to be used at a specific location to produce the dithering
     * @param x row coordinate of an 8x8 block
     * @param y column coordinate of an 8x8 block
     * @return the color that should be used to produce the dithering
     */
    public HSL getDitheredHSLAtPixel(int x, int y) {
        if (dithered && dither.getDitheredPixel(ditherIntensity, x, y) == 1) {
            return secondDitherColor;
        }
        return firstDitherColor;
    }
}
