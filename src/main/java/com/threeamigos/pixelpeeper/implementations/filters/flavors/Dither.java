package com.threeamigos.pixelpeeper.implementations.filters.flavors;

public class Dither {

    public static final int DENSITIES = 7;

    private final int[][] ditherMatrix;

    public Dither() {

        ditherMatrix = new int[DENSITIES][];

        ditherMatrix[0] = new int[]
                {
                        0b10001000,
                        0b00000000,
                        0b00100010,
                        0b00000000,
                        0b10001000,
                        0b00000000,
                        0b00100010,
                        0b00000000
                };
        ditherMatrix[1] = new int[]
                {
                        0b10101010,
                        0b00000000,
                        0b10101010,
                        0b00000000,
                        0b10101010,
                        0b00000000,
                        0b10101010,
                        0b00000000
                };
        ditherMatrix[2] = new int[]
                {
                        0b10101010,
                        0b01000100,
                        0b10101010,
                        0b00010001,
                        0b10101010,
                        0b01000100,
                        0b10101010,
                        0b00010001
                };
        ditherMatrix[3] = new int[]
                {
                        0b10101010,
                        0b01010101,
                        0b10101010,
                        0b01010101,
                        0b10101010,
                        0b01010101,
                        0b10101010,
                        0b01010101
                };
        ditherMatrix[4] = new int[]
                {
                        0b11101110,
                        0b01010101,
                        0b10111011,
                        0b01010101,
                        0b11101110,
                        0b01010101,
                        0b10111011,
                        0b01010101
                };
        ditherMatrix[5] = new int[]
                {
                        0b11111111,
                        0b01010101,
                        0b11111111,
                        0b01010101,
                        0b11111111,
                        0b01010101,
                        0b11111111,
                        0b01010101
                };
        ditherMatrix[6] = new int[]
                {
                        0b11111111,
                        0b11011101,
                        0b11111111,
                        0b01110111,
                        0b11111111,
                        0b11011101,
                        0b11111111,
                        0b01110111
                };
    }

    public int getDitheredPixel(int density, int x, int y) {
        int [] matrix = ditherMatrix[density];
        return (matrix[y] >> (7 - x)) & 1;
    }

}
