package com.threeamigos.imageviewer.implementations.datamodel;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.ImageHandlingPreferences.Disposition;

public class RectanglesProducer {

	public static final List<Rectangle> createRectangles(final int panelWidth, final int panelHeight,
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

	private static final List<Rectangle> sliceVertically(final int panelWidth, final int panelHeight,
			final int totalSlices) {
		List<Rectangle> slices = new ArrayList<>();
		int sliceWidth = panelWidth / totalSlices;
		int sliceHeight = panelHeight;
		int currentScreenOffsetX = 0;
		for (int i = 0; i < totalSlices; i++) {
			slices.add(new Rectangle(currentScreenOffsetX, 0, sliceWidth, sliceHeight));
			currentScreenOffsetX += sliceWidth;
		}
		return slices;
	}

	private static final List<Rectangle> sliceHorizontally(final int panelWidth, final int panelHeight,
			final int totalSlices) {
		List<Rectangle> slices = new ArrayList<>();
		int sliceWidth = panelWidth;
		int sliceHeight = panelHeight / totalSlices;
		int currentScreenOffsetY = 0;
		for (int i = 0; i < totalSlices; i++) {
			slices.add(new Rectangle(0, currentScreenOffsetY, sliceWidth, sliceHeight));
			currentScreenOffsetY += sliceHeight;
		}
		return slices;
	}

	private static final List<Rectangle> sliceGrid(final int panelWidth, final int panelHeight, final int totalSlices) {
		List<Rectangle> slices = new ArrayList<>();
		int rows = (int) Math.sqrt((double) totalSlices);
		int columns = (totalSlices + rows - 1) / rows;
		int sliceWidth = panelWidth / columns;
		int sliceHeight = panelHeight / rows;
		int currentScreenOffsetX = 0;
		int currentScreenOffsetY = 0;
		int currentSlice = 0;

		loop: for (int row = 0; row < rows; row++) {
			currentScreenOffsetX = 0;
			for (int column = 0; column < columns; column++) {
				if (currentSlice == totalSlices) {
					break loop;
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
