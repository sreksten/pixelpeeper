package com.threeamigos.pixelpeeper.implementations.filters.flavors;

public class HSL {

    int alpha;
    int hue;
    int saturation;
    int lightness;

    public HSL(int alpha, int hue, int saturation, int lightness) {
        this.alpha = alpha;
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
    }

    public HSL(int hue, int saturation, int lightness) {
        this.alpha = 255;
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;
    }

    public HSL(int rgb) {
        this(new RGB(rgb));
    }

    public HSL(RGB rgb) {
        double redD = rgb.red / 255.0d;
        double greenD = rgb.green / 255.0d;
        double blueD = rgb.blue / 255.0d;

        double max = Math.max(redD, Math.max(greenD, blueD));
        double min = Math.min(redD, Math.min(greenD, blueD));
        // lightness is the average of the largest and smallest color components
        double lightnessD = (max + min) / 2;
        double hueD;
        double saturationD;
        if (max == min) { // no saturation
            hueD = 0;
            saturationD = 0;
        } else {
            double chromaD = max - min;
            saturationD = chromaD / (1 - Math.abs(2 * lightnessD - 1));
            if (max == redD) {
                hueD = (greenD - blueD) / chromaD + (greenD < blueD ? 6 : 0);
            } else if (max == greenD) {
                hueD = (blueD - redD) / chromaD + 2;
            } else {
                hueD = (redD - greenD) / chromaD + 4;
            }
        }
        hueD = Math.round(hueD * 60); // in degrees; in the color wheel red starts at 0
        saturationD = Math.round(saturationD * 100); // in percentage
        lightnessD = Math.round(lightnessD * 100); // in percentage

        alpha = rgb.alpha;
        hue = (int) hueD;
        saturation = (int) saturationD;
        lightness = (int) lightnessD;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getLightness() {
        return lightness;
    }

    public void setLightness(int lightness) {
        this.lightness = lightness;
    }

    public int getRGB() {
        double lightnessD = (double) lightness / 100.0d;
        double saturationD = (double) saturation / 100.0d;
        double chromaD = (1 - Math.abs(2 * lightnessD - 1)) * saturationD;
        double hueD = hue / 60.0d;
        double x = chromaD * (1 - Math.abs(hueD % 2 - 1));
        double r = 0;
        double g = 0;
        double b = 0;
        if (hueD <= 1) {
            r = chromaD;
            g = x;
            b = 0;
        } else if (hueD <= 2) {
            r = x;
            g = chromaD;
            b = 0;
        } else if (hueD <= 3) {
            r = 0;
            g = chromaD;
            b = x;
        } else if (hueD <= 4) {
            r = 0;
            g = x;
            b = chromaD;
        } else if (hueD <= 5) {
            r = x;
            g = 0;
            b = chromaD;
        } else if (hueD <= 6) {
            r = chromaD;
            g = 0;
            b = x;
        }
        double m = lightnessD - chromaD / 2.0d;
        r = (r + m) * 255;
        g = (g + m) * 255;
        b = (b + m) * 255;
        int intR = (int) r;
        int intG = (int) g;
        int intB = (int) b;
        int transparency = (int) ((100 - this.alpha) / 100.0d * 255);
        return transparency << 24 | (intR << 16) | (intG << 8) | intB;
    }

    @Override
    public String toString() {
        return String.format("HSL(Hue: %d°, Saturation: %d%%, Lightness: %d%%, Transparency: %d%% ARGB: 0x%08X)", hue, saturation, lightness, alpha, getRGB());
    }
}
