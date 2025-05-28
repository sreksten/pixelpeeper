package com.threeamigos.pixelpeeper.implementations.edgedetect.flavours;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours.C64EdgesDetector;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.C64EdgesDetectorPreferences;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class C64EdgesDetectorImpl implements C64EdgesDetector {

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

    private final C64EdgesDetectorPreferences c64EdgesDetectorPreferences;
    private final ExceptionHandler exceptionHandler;

    private BufferedImage sourceImage;
    private BufferedImage edgesImage;
    private boolean isAborted;

    private List<DitheredHSL> mixedPalette;

    public C64EdgesDetectorImpl(C64EdgesDetectorPreferences c64EdgesDetectorPreferences,
                                ExceptionHandler exceptionHandler) {
        this.c64EdgesDetectorPreferences = c64EdgesDetectorPreferences;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
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
                RGB rgb = new RGB(imageRGB);
                HSL hsl = new HSL(imageRGB);

                DitheredHSL closestColor = mapToClosestPaletteColor(rgb, hsl);
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
            edgesImage = null;
        } else {
            edgesImage = newImage;
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

        if (c64EdgesDetectorPreferences.isSkinTonesMappingEnabled()) {}
        addSkinTones(standardPalette[PALETTE_BLACK_INDEX], standardPalette[PALETTE_RED_INDEX],
                standardPalette[PALETTE_WHITE_INDEX]);
    }

    private void fromBlack(HSL black, HSL hsl, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            HSL newHhsl = new HSL(hsl.hue, hsl.saturation, hsl.lightness / 7 * (i + 1));
            destinationList.add(new DitheredHSL(newHhsl, hsl, black, i));
        }
    }

    private void toWhite(HSL hsl, HSL white, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            HSL newHhsl = new HSL(hsl.hue, hsl.saturation, (white.lightness - hsl.lightness) / 7 * (i + 1) + hsl.lightness);
            destinationList.add(new DitheredHSL(newHhsl, hsl, white, i));
        }
    }

    private void mix(HSL first, HSL second, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            destinationList.add(new DitheredHSL(first, second, i));
        }
    }

    private void addSkinTones(HSL black, HSL red, HSL white) {
        List<Integer> skinTones = List.of(
                0xEAD8C4, 0xEDD8C5, 0xEED7C7, 0xF0D6C9, 0xF2D6CB, 0xF5D4CD,
                0xE0C8AE, 0xE1C7AE, 0xE5C6B1, 0xE6C5B2, 0xE9C3B6, 0xEAC2BA,
                0xD2B897, 0xD5B699, 0xD9B59B, 0xDBC39F,
                0xC4A682, 0xCBA487, 0xCFA28B, 0xD1A08F, 0xD4A093,
                0xB4976F, 0xB89470, 0xBD9174, 0xC38F7A, 0xC68D82,
                0xA5855E, 0xA7835F, 0xAC8063, 0xBAF8066, 0xB27E69, 0xB67C70,
                0xA7835F, 0x95734E, 0x9A7153, 0x9D6F55, 0xA16E5B, 0xA36A5F,
                0x80643D, 0x836241, 0x876043, 0x8B5F46, 0x8D54B, 0x905C4F,
                0x6D5533, 0x715235, 0x745063, 0x754F38, 0x784D3D, 0x7B4B41,
                0x594427, 0x5D412B, 0x61402D, 0x594427,
                0x453420, 0x463420, 0x4E2F2A
        );
        for (int color : skinTones) {
            HSL skinToneToMap = new HSL(color);
            int intensity = skinToneToMap.lightness * 7 / 100;
            if (skinToneToMap.lightness < 50) {
                mixedPalette.add(new DitheredHSL(new HSL(color), black, red, intensity));
            } else {
                mixedPalette.add(new DitheredHSL(new HSL(color), red, white, intensity));
            }
        }
    }

    private void processBlock(BufferedImage newImage, int startX, int startY, int endX, int endY,
                              HSL[][] destinationHSL, HSL backgroundHSL) {
        Map<HSL, Integer> hlsCountMap = new HashMap<>();

        if (c64EdgesDetectorPreferences.isColorClashEnabled()) {
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

    private DitheredHSL mapToClosestPaletteColor(RGB rgb, HSL hsl) {
        int hue = hsl.getHue();
        int saturation = hsl.getSaturation();
        int lightness = hsl.getLightness();

        // Handle special cases first
        if (saturation < c64EdgesDetectorPreferences.getSaturationThreshold()) {
            return getBlackAndWhiteDitheredHSL(lightness);
        }
        if (lightness < c64EdgesDetectorPreferences.getLightnessMinThreshold())
            return mixedPalette.get(PALETTE_BLACK_INDEX);
        if (lightness > c64EdgesDetectorPreferences.getLightnessMaxThreshold())
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

    private int lightnessDistance(int lightness1, int lightness2) {
        return Math.abs(lightness1 - lightness2);
    }

    private HSL adjustToMostUsedColor(HSL hsl, List<HSL> ditheredPalette) {
        HSL closestColor = ditheredPalette.get(0);

        if (ditheredPalette.size() > 1) {
            int hue = hsl.getHue();
            int saturation = hsl.getSaturation();
            int lightness = hsl.getLightness();

            double minDistance = Double.MAX_VALUE;

            for (HSL paletteColor : ditheredPalette) {
                double distance = calculateColorDistance(
                        hue, saturation, lightness,
                        paletteColor.hue, paletteColor.saturation, paletteColor.lightness
                );

                if (distance < minDistance) {
                    minDistance = distance;
                    closestColor = paletteColor;
                }
            }
        }

        return closestColor;
    }

    /**
     * Calculates the distance between two colors in HSL space
     * Uses a weighted Euclidean distance with special handling for hue
     */
    private double calculateColorDistance(
            int h1, int s1, int l1,
            int h2, int s2, int l2
    ) {
        // Weight factors for each component
        final double HUE_WEIGHT = c64EdgesDetectorPreferences.getHueWeight();
        final double SATURATION_WEIGHT = c64EdgesDetectorPreferences.getSaturationWeight();
        final double LIGHTNESS_WEIGHT = c64EdgesDetectorPreferences.getLightnessWeight();

        // Calculate hue distance considering the circular nature of hue
        double hueDiff = Math.min(Math.abs(h1 - h2), 360 - Math.abs(h1 - h2));
        hueDiff = (hueDiff / 180.0) * Math.PI; // Normalize to [0, π]

        // Calculate weighted Euclidean distance
        double hueComponent = HUE_WEIGHT * hueDiff;
        double satComponent = SATURATION_WEIGHT * (s1 - s2) / 100.0;
        double lightComponent = LIGHTNESS_WEIGHT * (l1 - l2) / 100.0;

        return Math.sqrt(
                hueComponent * hueComponent +
                        satComponent * satComponent +
                        lightComponent * lightComponent
        );
    }

    public void abort() {
        isAborted = true;
    }

    @Override
    public BufferedImage getEdgesImage() {
        return edgesImage;
    }
}
