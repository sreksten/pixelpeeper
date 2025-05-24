package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.C64PaletteFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.C64PaletteFilterPreferences;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class C64PaletteFilterImpl extends ColorClashPaletteFilterImpl implements C64PaletteFilter {

    private static final int PALETTE_BLACK_INDEX = 0;
    private static final int PALETTE_DARK_GRAY_INDEX = 1;
    private static final int PALETTE_MEDIUM_GRAY_INDEX = 2;
    private static final int PALETTE_LIGHT_GRAY_INDEX = 3;
    private static final int PALETTE_WHITE_INDEX = 4;
    private static final int PALETTE_RED_INDEX = 5;
    private static final int PALETTE_PINK_INDEX = 6;
    private static final int PALETTE_DARK_BROWN_INDEX = 7;
    private static final int PALETTE_LIGHT_BROWN_INDEX = 8;
    private static final int PALETTE_YELLOW_INDEX = 9;
    private static final int PALETTE_LIGHT_GREEN_INDEX = 10;
    private static final int PALETTE_GREEN_INDEX = 11;
    private static final int PALETTE_CYAN_INDEX = 12;
    private static final int PALETTE_LIGHT_BLUE_INDEX = 13;
    private static final int PALETTE_BLUE_INDEX = 14;
    private static final int PALETTE_PURPLE_INDEX = 15;

    private static final HSL[] standardPalette = new HSL[]{
            new HSL(new RGB(0x000000)), // Black
            new HSL(new RGB(0x626262)), // Dark gray
            new HSL(new RGB(0x898989)), // Medium gray
            new HSL(new RGB(0xADADAD)), // Light gray
            new HSL(new RGB(0xFFFFFF)), // White
            new HSL(new RGB(0x9F4E44)), // "Red"
            new HSL(new RGB(0xCB7E75)), // Sort of Pink
            new HSL(new RGB(0x6D5412)), // Dark brown
            new HSL(new RGB(0xA1683C)), // Light brown
            new HSL(new RGB(0xC9D487)), // Kind of Yellow
            new HSL(new RGB(0x9AE29B)), // Light green
            new HSL(new RGB(0x5CAB5E)), // Green
            new HSL(new RGB(0x6ABFC6)), // Cyan
            new HSL(new RGB(0x887ECB)), // Violet
            new HSL(new RGB(0x50459B)), // Blue
            new HSL(new RGB(0xA057A3)), // Purple
    };

    private final C64PaletteFilterPreferences c64PaletteFilterPreferences;

    private List<DitheredHSL> mixedPalette;

    public C64PaletteFilterImpl(C64PaletteFilterPreferences c64PaletteFilterPreferences,
                                ExceptionHandler exceptionHandler) {
        super(c64PaletteFilterPreferences, exceptionHandler);
        this.c64PaletteFilterPreferences = c64PaletteFilterPreferences;
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
        Map<HSL, Integer> hlsCountMap = new HashMap<>();

        // Calculate the dithered colors for each pixel in the block and count color usage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int imageRGB = sourceImage.getRGB(x, y);
                HSL hsl = new HSL(imageRGB);

                DitheredHSL closestColor = mapToClosestPaletteColor(hsl);
                HSL destinationPixelHSL = closestColor.getDitheredHSLAtPixel(x % 8, y % 8);
                destinationHSL[x][y] = destinationPixelHSL;
                hlsCountMap.put(destinationPixelHSL, hlsCountMap.getOrDefault(destinationPixelHSL, 0) + 1);
            }
        }

        // Sort the colors by their count
        List<HSL> hlsList = new ArrayList<>(hlsCountMap.keySet());
        hlsList.sort((o1, o2) -> hlsCountMap.get(o2) - hlsCountMap.get(o1));

        // Find background color
        final HSL mostUsedHSL = hlsList.get(0);

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
                        processBlock(newImage, startX, startY, endX, endY, destinationHSL, mostUsedHSL);
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

        mix(standardPalette[PALETTE_WHITE_INDEX], standardPalette[PALETTE_LIGHT_GRAY_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_LIGHT_GRAY_INDEX], standardPalette[PALETTE_MEDIUM_GRAY_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_MEDIUM_GRAY_INDEX], standardPalette[PALETTE_DARK_GRAY_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_GRAY_INDEX], standardPalette[PALETTE_BLACK_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_BLUE_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_BLUE_INDEX], standardPalette[PALETTE_LIGHT_BLUE_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_LIGHT_BLUE_INDEX], standardPalette[PALETTE_CYAN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_CYAN_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_GREEN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_GREEN_INDEX], standardPalette[PALETTE_CYAN_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_DARK_BROWN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_DARK_BROWN_INDEX], standardPalette[PALETTE_LIGHT_BROWN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_LIGHT_BROWN_INDEX], standardPalette[PALETTE_PINK_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_PINK_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_RED_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_RED_INDEX], standardPalette[PALETTE_YELLOW_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_YELLOW_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_YELLOW_INDEX], standardPalette[PALETTE_LIGHT_GREEN_INDEX], mixedPalette);
        mix(standardPalette[PALETTE_LIGHT_GREEN_INDEX], standardPalette[PALETTE_GREEN_INDEX], mixedPalette);

        mix(standardPalette[PALETTE_PURPLE_INDEX], standardPalette[PALETTE_WHITE_INDEX], mixedPalette);

        if (c64PaletteFilterPreferences.isSkinTonesMappingEnabled()) {
            addSkinTones(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_RED_INDEX],
                    standardPalette[PALETTE_WHITE_INDEX], mixedPalette);
        }
    }

    private void processBlock(BufferedImage newImage, int startX, int startY, int endX, int endY,
                              HSL[][] destinationHSL, HSL backgroundHSL) {
        Map<HSL, Integer> hlsCountMap = new HashMap<>();

        if (c64PaletteFilterPreferences.isColorClashEnabled()) {
            // Count color usage
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    HSL ditheredHSL = destinationHSL[x][y];
                    hlsCountMap.put(ditheredHSL, hlsCountMap.getOrDefault(ditheredHSL, 0) + 1);
                }
            }

            // Sort the colors by their count
            List<HSL> hlsList = new ArrayList<>(hlsCountMap.keySet());
            hlsList.sort((o1, o2) -> hlsCountMap.get(o2) - hlsCountMap.get(o1));

            // Find the three colors that are most used
            HSL firstMostUsedColor = hlsList.get(0);
            HSL secondMostUsedColor = null;
            HSL thirdMostUsedColor = null;
            if (hlsList.size() > 1) {
                secondMostUsedColor = hlsList.get(1);
            }
            if (hlsList.size() > 2) {
                thirdMostUsedColor = hlsList.get(2);
            }
            List<HSL> mostUsedColors = new ArrayList<>();
            mostUsedColors.add(backgroundHSL);
            mostUsedColors.add(firstMostUsedColor);
            if (secondMostUsedColor != null) {
                mostUsedColors.add(secondMostUsedColor);
            }
            if (thirdMostUsedColor != null) {
                mostUsedColors.add(thirdMostUsedColor);
            }

            // Change the remaining colors remapping them to the four most used
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    HSL hsl = destinationHSL[x][y];
                    if (hsl.equals(backgroundHSL) || hsl.equals(firstMostUsedColor) || hsl.equals(secondMostUsedColor) ||
                            hsl.equals(thirdMostUsedColor)) {
                        continue;
                    }
                    destinationHSL[x][y] = adjustToMostUsedColor(hsl, mostUsedColors);
                }
            }
        }

        // Set the dithered colors for the block in the new image
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                HSL ditheredHSL = destinationHSL[x][y];
                newImage.setRGB(x, y, ditheredHSL.getRGB());
            }
        }
    }

    private DitheredHSL mapToClosestPaletteColor(HSL hsl) {
        int hue = hsl.getHue();
        int saturation = hsl.getSaturation();
        int lightness = hsl.getLightness();

        // Handle special cases first
        if (saturation < c64PaletteFilterPreferences.getSaturationThreshold()) {
            return getBlackAndWhiteDitheredHSL(lightness);
        }
        if (lightness < c64PaletteFilterPreferences.getLightnessMinThreshold())
            return mixedPalette.get(PALETTE_BLACK_INDEX);
        if (lightness > c64PaletteFilterPreferences.getLightnessMaxThreshold())
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
