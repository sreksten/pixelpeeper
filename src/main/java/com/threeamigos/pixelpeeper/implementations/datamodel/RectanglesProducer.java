package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences.Disposition;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RectanglesProducer {

    private RectanglesProducer() {
    }

    public static List<Rectangle> createRectangles(final int panelWidth, final int panelHeight,
                                                   final Disposition disposition, final int totalSlices) {
        switch (disposition) {
            case HORIZONTAL:
                return sliceHorizontally(panelWidth, panelHeight, totalSlices);
            case VERTICAL:
                return sliceVertically(panelWidth, panelHeight, totalSlices);
            case GRID:
                return sliceGrid(panelWidth, panelHeight, totalSlices);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static List<Rectangle> sliceVertically(final int panelWidth, final int panelHeight,
                                                   final int totalSlices) {
        List<Rectangle> slices = new ArrayList<>();
        int sliceWidth = panelWidth / totalSlices;
        int currentScreenOffsetX = 0;
        for (int i = 0; i < totalSlices; i++) {
            slices.add(new Rectangle(currentScreenOffsetX, 0, sliceWidth, panelHeight));
            currentScreenOffsetX += sliceWidth;
        }
        return slices;
    }

    private static List<Rectangle> sliceHorizontally(final int panelWidth, final int panelHeight,
                                                     final int totalSlices) {
        List<Rectangle> slices = new ArrayList<>();
        int sliceHeight = panelHeight / totalSlices;
        int currentScreenOffsetY = 0;
        for (int i = 0; i < totalSlices; i++) {
            slices.add(new Rectangle(0, currentScreenOffsetY, panelWidth, sliceHeight));
            currentScreenOffsetY += sliceHeight;
        }
        return slices;
    }

    private static List<Rectangle> sliceGrid(final int panelWidth, final int panelHeight, final int totalSlices) {
        List<Rectangle> slices = new ArrayList<>();
        int rows = (int) Math.sqrt(totalSlices);
        int columns = (totalSlices + rows - 1) / rows;
        int sliceWidth = panelWidth / columns;
        int sliceHeight = panelHeight / rows;
        int currentScreenOffsetX = 0;
        int currentScreenOffsetY = 0;
        int currentSlice = 0;

        for (int row = 0; row < rows; row++) {
            currentScreenOffsetX = 0;
            for (int column = 0; column < columns; column++) {
                if (currentSlice == totalSlices) {
                    // We're done
                    return slices;
                }
                slices.add(new Rectangle(currentScreenOffsetX, currentScreenOffsetY, sliceWidth, sliceHeight));
                currentScreenOffsetX += sliceWidth;
                currentSlice++;
            }
            currentScreenOffsetY += sliceHeight;
        }
        return slices;
    }

}
