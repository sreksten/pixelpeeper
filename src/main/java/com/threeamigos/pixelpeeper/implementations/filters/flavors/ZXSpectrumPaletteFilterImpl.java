package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.ZXSpectrumPaletteFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ZXSpectrumPaletteFilterPreferences;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZXSpectrumPaletteFilterImpl extends ColorClashPaletteFilterImpl implements ZXSpectrumPaletteFilter {

    private static final int PALETTE_BLACK_INDEX = 0;
    private static final int PALETTE_BLUE_INDEX = 1;
    private static final int PALETTE_RED_INDEX = 2;
    private static final int PALETTE_MAGENTA_INDEX = 3;
    private static final int PALETTE_GREEN_INDEX = 4;
    private static final int PALETTE_CYAN_INDEX = 5;
    private static final int PALETTE_YELLOW_INDEX = 6;
    private static final int PALETTE_WHITE_INDEX = 7;

    private static final HSL[] standardPaletteNormalLuminosity = new HSL[]{
            new HSL(new RGB(0x000000)), // Black
            new HSL(new RGB(0x0100CE)), // Blue
            new HSL(new RGB(0xCF0100)), // Red
            new HSL(new RGB(0xCF01CE)), // Magenta
            new HSL(new RGB(0x00CF15)), // Green
            new HSL(new RGB(0x01CFCF)), // Cyan
            new HSL(new RGB(0xCFCF15)), // Yellow
            new HSL(new RGB(0xCFCFCF)) // White
    };
    private static final HSL[] standardPaletteHighLuminosity = new HSL[]{
            new HSL(new RGB(0x000000)), // Bright Black
            new HSL(new RGB(0x0200FD)), // Bright Blue
            new HSL(new RGB(0xFF0201)), // Bright Red
            new HSL(new RGB(0xFF02FD)), // Bright Magenta
            new HSL(new RGB(0x00FF1C)), // Bright Green
            new HSL(new RGB(0x02FFFF)), // Bright Cyan
            new HSL(new RGB(0xFFFF1D)), // Bright Yellow
            new HSL(new RGB(0xFFFFFF)) // Bright White
    };

    private final ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences;

    private List<DitheredHSL> mixedPaletteNormalLuminosity;
    private List<DitheredHSL> mixedPaletteHighLuminosity;

    public ZXSpectrumPaletteFilterImpl(ZXSpectrumPaletteFilterPreferences zxSpectrumPaletteFilterPreferences,
                                       ExceptionHandler exceptionHandler) {
        super(zxSpectrumPaletteFilterPreferences, exceptionHandler);
        this.zxSpectrumPaletteFilterPreferences = zxSpectrumPaletteFilterPreferences;
    }

    @Override
    public void process() {
        isAborted = false;
        preparePalette();

        // Prepare new image
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // How many rows and columns of characters do we have?
        int horizontalBlocks = (width + 7) >> 3;
        int verticalBlocks = (height + 7) >> 3;

        // Split task between available processors
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

        // Start calculation and wait for completion
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
            // Display a gradient in the upper left corner, just to test
            for (int color = 0; color < 9; color++) {
                DitheredHSL hsl;
                if (color == 0) {
                    hsl = mixedPaletteNormalLuminosity.get(7);
                } else if (color == 8) {
                    hsl = mixedPaletteNormalLuminosity.get(0);
                } else {
                    hsl = mixedPaletteNormalLuminosity.get(7 + color);
                }
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        newImage.setRGB(x + color * 8, y, hsl.getHSLToMap().getRGB());
                        newImage.setRGB(x + color * 8, y + 8, hsl.getDitheredHSLAtPixel(x, y).getRGB());
                    }
                }
            }

            filteredImage = newImage;
        }
    }
    private void preparePalette() {
        mixedPaletteNormalLuminosity = new ArrayList<>();
        mix(standardPaletteNormalLuminosity, mixedPaletteNormalLuminosity);
        mixedPaletteHighLuminosity = new ArrayList<>();
        mix(standardPaletteHighLuminosity, mixedPaletteHighLuminosity);
    }

    private void mix(HSL[] originalPalette, List<DitheredHSL> destinationList) {
        for (HSL hsl : originalPalette) {
            destinationList.add(new DitheredHSL(hsl));
        }

        mix(originalPalette[PALETTE_WHITE_INDEX], originalPalette[PALETTE_BLACK_INDEX], destinationList);

        fromBlack(originalPalette[PALETTE_BLACK_INDEX], originalPalette[PALETTE_BLUE_INDEX], destinationList);
        mix(originalPalette[PALETTE_BLUE_INDEX], originalPalette[PALETTE_CYAN_INDEX], destinationList);
        toWhite(originalPalette[PALETTE_CYAN_INDEX], originalPalette[PALETTE_WHITE_INDEX], destinationList);

        fromBlack(originalPalette[PALETTE_BLACK_INDEX], originalPalette[PALETTE_GREEN_INDEX], destinationList);
        mix(originalPalette[PALETTE_GREEN_INDEX], originalPalette[PALETTE_CYAN_INDEX], destinationList);

        fromBlack(originalPalette[PALETTE_BLACK_INDEX], originalPalette[PALETTE_RED_INDEX], destinationList);
        toWhite(originalPalette[PALETTE_RED_INDEX], originalPalette[PALETTE_WHITE_INDEX], destinationList);

        mix(originalPalette[PALETTE_RED_INDEX], originalPalette[PALETTE_YELLOW_INDEX], destinationList);
        toWhite(originalPalette[PALETTE_YELLOW_INDEX], originalPalette[PALETTE_WHITE_INDEX], destinationList);

        mix(originalPalette[PALETTE_YELLOW_INDEX], originalPalette[PALETTE_GREEN_INDEX], destinationList);

        mix(originalPalette[PALETTE_RED_INDEX], originalPalette[PALETTE_GREEN_INDEX], destinationList);

        if (zxSpectrumPaletteFilterPreferences.isSkinTonesMappingEnabled()) {
            addSkinTones(originalPalette[PALETTE_BLACK_INDEX], originalPalette[PALETTE_RED_INDEX],
                    originalPalette[PALETTE_WHITE_INDEX], destinationList);
        }
    }

    private void processBlock(BufferedImage newImage, int startX, int startY, int endX, int endY) {
        int width = endX - startX;
        int height = endY - startY;
        HSL[][] originalHsl = new HSL[width][height];
        HSL[][] destinationHSL = new HSL[width][height];

        // Calculate the average lightness of the block to determine which palette to use
        int totalLightness = 0;
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int indexX = x - startX;
                int indexY = y - startY;
                int imageRGB = sourceImage.getRGB(x, y);
                HSL hsl = new HSL(imageRGB);
                originalHsl[indexX][indexY] = hsl;
                totalLightness += hsl.lightness;
            }
        }
        totalLightness /= (width * height);

        List<DitheredHSL> ditheredPalette =
                totalLightness > 70 ? mixedPaletteHighLuminosity : mixedPaletteNormalLuminosity;

        // Calculate the dithered colors for each pixel in the block
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int indexX = x - startX;
                int indexY = y - startY;
                HSL hsl = originalHsl[indexX][indexY];

                DitheredHSL closestColor = mapToClosestPaletteColor(hsl, ditheredPalette);
                destinationHSL[indexX][indexY] = closestColor.getDitheredHSLAtPixel(indexX, indexY);
            }
        }

        if (zxSpectrumPaletteFilterPreferences.isColorClashEnabled()) {
            // Count color usage
            Map<HSL, Integer> hlsCountMap = new HashMap<>();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    HSL ditheredHSL = destinationHSL[x][y];
                    hlsCountMap.put(ditheredHSL, hlsCountMap.getOrDefault(ditheredHSL, 0) + 1);
                }
            }

            // Sort the colors by their count
            List<HSL> hlsList = new ArrayList<>(hlsCountMap.keySet());
            hlsList.sort((o1, o2) -> hlsCountMap.get(o2) - hlsCountMap.get(o1));

            // Find the two colors that are most used
            HSL firstMostUsedColor = hlsList.get(0);
            HSL secondMostUsedColor = null;
            if (hlsList.size() > 1) {
                secondMostUsedColor = hlsList.get(1);
            }
            List<HSL> mostUsedColors = new ArrayList<>();
            mostUsedColors.add(firstMostUsedColor);
            if (secondMostUsedColor != null) {
                mostUsedColors.add(secondMostUsedColor);
            }

            // Change the remaining colors remapping them to the two most used
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    HSL hsl = destinationHSL[x][y];
                    if (hsl.equals(firstMostUsedColor) || hsl.equals(secondMostUsedColor)) {
                        continue;
                    }
                    destinationHSL[x][y] = adjustToMostUsedColor(hsl, mostUsedColors);
                }
            }
        }

        // Set the dithered colors for the block in the new image
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                HSL ditheredHSL = destinationHSL[x][y];
                newImage.setRGB(startX + x, startY + y, ditheredHSL.getRGB());
            }
        }
    }

    private DitheredHSL mapToClosestPaletteColor(HSL hsl, List<DitheredHSL> ditheredPalette) {
        int hue = hsl.getHue();
        int saturation = hsl.getSaturation();
        int lightness = hsl.getLightness();

        // Handle special cases first
        if (saturation < zxSpectrumPaletteFilterPreferences.getSaturationThreshold()) {
            return getBlackAndWhiteDitheredHSL(ditheredPalette, lightness);
        }
        if (lightness < zxSpectrumPaletteFilterPreferences.getLightnessMinThreshold()) return ditheredPalette.get(PALETTE_BLACK_INDEX);
        if (lightness > zxSpectrumPaletteFilterPreferences.getLightnessMaxThreshold()) return ditheredPalette.get(PALETTE_WHITE_INDEX);

        double minDistance = Double.MAX_VALUE;
        DitheredHSL closestColor = ditheredPalette.get(0);

        for (DitheredHSL paletteColor : ditheredPalette) {
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

    private DitheredHSL getBlackAndWhiteDitheredHSL(List<DitheredHSL> ditheredPalette, int lightness) {
        DitheredHSL closestColor = ditheredPalette.get(PALETTE_WHITE_INDEX);
        int minDistance = lightnessDistance(closestColor.getHSLToMap().getLightness(), lightness);
        for (DitheredHSL paletteColor : ditheredPalette) {
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
