package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.Windows311PaletteFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.Windows311PaletteFilterPreferences;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Windows311PaletteFilterImpl extends PaletteFilterImpl implements Windows311PaletteFilter {

    private static final int PALETTE_BLACK_INDEX = 0;
    private static final int PALETTE_DARK_GRAY_INDEX = 1;
    private static final int PALETTE_LIGHT_GRAY_INDEX = 2;
    private static final int PALETTE_WHITE_INDEX = 3;
    private static final int PALETTE_DARK_RED_INDEX = 4;
    private static final int PALETTE_RED_INDEX = 5;
    private static final int PALETTE_DARK_GREEN_INDEX = 6;
    private static final int PALETTE_GREEN_INDEX = 7;
    private static final int PALETTE_DARK_YELLOW_INDEX = 8;
    private static final int PALETTE_YELLOW_INDEX = 9;
    private static final int PALETTE_DARK_BLUE_INDEX = 10;
    private static final int PALETTE_BLUE_INDEX = 11;
    private static final int PALETTE_DARK_MAGENTA_INDEX = 12;
    private static final int PALETTE_MAGENTA_INDEX = 13;
    private static final int PALETTE_DARK_CYAN_INDEX = 14;
    private static final int PALETTE_CYAN_INDEX = 15;

    private static final HSL[] standardPalette = new HSL[]{
            new HSL(new RGB(0x000000)), // Black
            new HSL(new RGB(0x7E7E7E)), // Dark gray
            new HSL(new RGB(0xBEBEBE)), // Light gray
            new HSL(new RGB(0xFFFFFF)), // White
            new HSL(new RGB(0x7E0000)), // Dark red
            new HSL(new RGB(0xFE0000)), // Red
            new HSL(new RGB(0x047E00)), // Dark green
            new HSL(new RGB(0x06FF04)), // Green
            new HSL(new RGB(0x7E7E00)), // Dark yellow
            new HSL(new RGB(0xFFFF04)), // Yellow
            new HSL(new RGB(0x00007E)), // Dark blue
            new HSL(new RGB(0x0000FF)), // Blue
            new HSL(new RGB(0x7E007E)), // Dark magenta
            new HSL(new RGB(0xFE00FF)), // Magenta
            new HSL(new RGB(0x047E7E)), // Dark cyan
            new HSL(new RGB(0x06FFFF)), // Cyan
    };

    private final Windows311PaletteFilterPreferences windows311PaletteFilterPreferences;

    private List<DitheredHSL> mixedPalette;

    public Windows311PaletteFilterImpl(Windows311PaletteFilterPreferences windows311PaletteFilterPreferences,
                                ExceptionHandler exceptionHandler) {
        super(windows311PaletteFilterPreferences, exceptionHandler);
        this.windows311PaletteFilterPreferences = windows311PaletteFilterPreferences;
    }

    @Override
    public void process() {
        isAborted = false;
        preparePalette();

        // Prepare new image
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final HSL[][] destinationHSL = new HSL[width][height];

        // How many rows and columns of characters do we have?
        int horizontalBlocks = (width + 7) >> 3;
        int verticalBlocks = (height + 7) >> 3;

        int processors = Runtime.getRuntime().availableProcessors();
        int blocksPerProcessor = (horizontalBlocks + processors - 1) / processors;

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < processors; i++) {
            int startBlock = i * blocksPerProcessor;
            int endBlock = Math.min(startBlock + blocksPerProcessor, horizontalBlocks);
            if (startBlock >= horizontalBlocks) {
                break;
            }
            threads.add(new Thread(() -> {
                for (int x = startBlock; x < endBlock && !isAborted; x++) {
                    for (int y = 0; y < verticalBlocks; y++) {
                        int startX = x << 3;
                        int startY = y << 3;
                        int endX = Math.min(startX + 8, width);
                        int endY = Math.min(startY + 8, height);
                        processBlock(newImage, startX, startY, endX, endY);
                    }
                }
            }));
        }
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                exceptionHandler.handleException(e);
            }
        });

        if (isAborted) {
            filteredImage = null;
        } else {
            filteredImage = newImage;
        }
    }

    private void preparePalette() {
        mixedPalette = new ArrayList<>();

        for (HSL hsl : standardPalette) {
            mixedPalette.add(new DitheredHSL(hsl));
        }

        mix(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_GRAY_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_GRAY_INDEX], standardPalette[PALETTE_LIGHT_GRAY_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_LIGHT_GRAY_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        fromBlack(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_RED_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_RED_INDEX], standardPalette[PALETTE_RED_INDEX], mixedPalette);
        toWhite(standardPalette[PALETTE_RED_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        fromBlack(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_GREEN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_GREEN_INDEX], standardPalette[PALETTE_GREEN_INDEX], mixedPalette);
        toWhite(standardPalette[PALETTE_GREEN_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        fromBlack(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_YELLOW_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_YELLOW_INDEX], standardPalette[PALETTE_YELLOW_INDEX], mixedPalette);
        toWhite(standardPalette[PALETTE_YELLOW_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        fromBlack(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_BLUE_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_BLUE_INDEX], standardPalette[PALETTE_BLUE_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_BLUE_INDEX], standardPalette[PALETTE_DARK_CYAN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_CYAN_INDEX], standardPalette[PALETTE_CYAN_INDEX], mixedPalette);
        toWhite(standardPalette[PALETTE_CYAN_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_DARK_RED_INDEX], standardPalette[PALETTE_DARK_GREEN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_RED_INDEX], standardPalette[PALETTE_GREEN_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_DARK_RED_INDEX], standardPalette[PALETTE_DARK_YELLOW_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_RED_INDEX], standardPalette[PALETTE_YELLOW_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_DARK_MAGENTA_INDEX], standardPalette[PALETTE_MAGENTA_INDEX], mixedPalette);
        toWhite(standardPalette[PALETTE_MAGENTA_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        if (windows311PaletteFilterPreferences.isSkinTonesMappingEnabled()) {
            addSkinTones(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_RED_INDEX],
                    standardPalette[PALETTE_WHITE_INDEX], mixedPalette);
        }
    }

    private void processBlock(BufferedImage newImage, int startX, int startY, int endX, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int imageRGB = sourceImage.getRGB(x, y);
                HSL hsl = new HSL(imageRGB);
                DitheredHSL closestColor = mapToClosestPaletteColor(hsl);
                HSL destinationHSL = closestColor.getDitheredHSLAtPixel(x % 8, y % 8);
                newImage.setRGB(x, y, destinationHSL.getRGB());
            }
        }
    }

    private DitheredHSL mapToClosestPaletteColor(HSL hsl) {
        int hue = hsl.getHue();
        int saturation = hsl.getSaturation();
        int lightness = hsl.getLightness();

        // Handle special cases first
        if (saturation < windows311PaletteFilterPreferences.getSaturationThreshold()) {
            return getBlackAndWhiteDitheredHSL(lightness);
        }
        if (lightness < windows311PaletteFilterPreferences.getLightnessMinThreshold())
            return mixedPalette.get(PALETTE_BLACK_INDEX);
        if (lightness > windows311PaletteFilterPreferences.getLightnessMaxThreshold())
            return mixedPalette.get(PALETTE_WHITE_INDEX);

        double minDistance = Double.MAX_VALUE;
        DitheredHSL closestColor = mixedPalette.get(0);

        for (DitheredHSL paletteColor : mixedPalette) {
            double distance = calculateColorDistance(
                    hue, saturation, lightness,
                    paletteColor.getHSLToMap().hue, paletteColor.getHSLToMap().saturation, paletteColor.getHSLToMap().lightness
            );

            if (distance < minDistance) {
                minDistance = distance;
                closestColor = paletteColor;
            }
        }

        return closestColor;
    }

    private DitheredHSL getBlackAndWhiteDitheredHSL(int lightness) {
        DitheredHSL closestColor = mixedPalette.get(0);
        int minDistance = lightnessDistance(closestColor.getHSLToMap().getLightness(), lightness);
        for (DitheredHSL paletteColor : mixedPalette) {
            if (paletteColor.getHSLToMap().getSaturation() < 15) {
                int newDistance = lightnessDistance(paletteColor.getHSLToMap().getLightness(), lightness);
                if (newDistance < minDistance) {
                    minDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return closestColor;
    }
}
