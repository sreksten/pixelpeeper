package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.interfaces.messagehandler.ExceptionHandler;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.ColorClashPaletteFilterPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.PaletteFilterPreferences;

import java.awt.image.BufferedImage;
import java.util.List;

abstract class ColorClashPaletteFilterImpl extends PaletteFilterImpl {

    private final ColorClashPaletteFilterPreferences colorClashPaletteFilterPreferences;

    ColorClashPaletteFilterImpl(ColorClashPaletteFilterPreferences paletteFilterPreferences, ExceptionHandler exceptionHandler) {
        super(paletteFilterPreferences, exceptionHandler);
        this.colorClashPaletteFilterPreferences = paletteFilterPreferences;
    }

    protected HSL adjustToMostUsedColor(HSL hsl, List<HSL> ditheredPalette) {
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

}
