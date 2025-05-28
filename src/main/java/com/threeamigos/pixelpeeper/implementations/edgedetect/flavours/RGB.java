package com.threeamigos.pixelpeeper.implementations.edgedetect.flavours;

public class RGB {

    int alpha;
    int red;
    int green;
    int blue;

    public RGB(int alpha, int red, int green, int blue) {
        this.alpha = alpha;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB(int red, int green, int blue) {
        this.alpha = 255;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB(int rgb) {
        this.alpha = (rgb >> 24) & 0xFF;
        this.red = (rgb >> 16) & 0xFF;
        this.green = (rgb >> 8) & 0xFF;
        this.blue = rgb & 0xFF;
    }

    @Override
    public String toString() {
        return String.format("RGB(Alpha: %02X, Red: %02X, Green: %02X, Blue: %02X)", alpha, red, green, blue);
    }
}