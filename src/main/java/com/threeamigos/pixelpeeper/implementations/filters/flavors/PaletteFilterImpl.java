package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.PaletteFilterPreferences;

import java.awt.image.BufferedImage;
import java.util.List;

abstract class PaletteFilterImpl {

    protected BufferedImage sourceImage;
    protected BufferedImage filteredImage;
    protected boolean isAborted;

    protected final PaletteFilterPreferences paletteFilterPreferences;
    protected final ExceptionHandler exceptionHandler;

    PaletteFilterImpl(PaletteFilterPreferences paletteFilterPreferences, ExceptionHandler exceptionHandler) {
        this.paletteFilterPreferences = paletteFilterPreferences;
        this.exceptionHandler = exceptionHandler;
    }

    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    protected void fromBlack(HSL black, HSL hsl, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            HSL newHhsl = new HSL(hsl.hue, hsl.saturation, hsl.lightness / 7 * (i + 1));
            destinationList.add(new DitheredHSL(newHhsl, hsl, black, i));
        }
    }

    protected void mix(HSL first, HSL second, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            destinationList.add(new DitheredHSL(first, second, i));
        }
    }

    protected void toWhite(HSL hsl, HSL white, List<DitheredHSL> destinationList) {
        for (int i = 0; i <= 6; i++) {
            HSL newHhsl = new HSL(hsl.hue, hsl.saturation, (white.lightness - hsl.lightness) / 7 * (i + 1) + hsl.lightness);
            destinationList.add(new DitheredHSL(newHhsl, hsl, white, i));
        }
    }

    protected void addSkinTones(HSL black, HSL red, HSL white, List<DitheredHSL> destinationPalette) {
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
                destinationPalette.add(new DitheredHSL(new HSL(color), black, red, intensity));
            } else {
                destinationPalette.add(new DitheredHSL(new HSL(color), red, white, intensity));
            }
        }
    }

    /**
     * Calculates the distance between two colors in HSL space
     * Uses a weighted Euclidean distance with special handling for hue
     */
    protected double calculateColorDistance(
            int h1, int s1, int l1,
            int h2, int s2, int l2
    ) {
        // Weight factors for each component
        final double HUE_WEIGHT = paletteFilterPreferences.getHueWeight();
        final double SATURATION_WEIGHT = paletteFilterPreferences.getSaturationWeight();
        final double LIGHTNESS_WEIGHT = paletteFilterPreferences.getLightnessWeight();

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

    protected int lightnessDistance(int lightness1, int lightness2) {
        return Math.abs(lightness1 - lightness2);
    }

    public void abort() {
        isAborted = true;
    }

    public BufferedImage getResultingImage() {
        return filteredImage;
    }

}
