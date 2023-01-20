package com.threeamigos.imageviewer.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.WindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;

public class GridDecorator implements ImageDecorator {

	private static final int SPACING = 25;

	private final WindowPreferences windowPreferences;
	private final GridPreferences gridPreferences;

	public GridDecorator(WindowPreferences windowPreferences, GridPreferences gridPreferences) {
		this.windowPreferences = windowPreferences;
		this.gridPreferences = gridPreferences;
	}

	@Override
	public void paint(Graphics2D graphics) {

		if (gridPreferences.isGridVisible()) {

			Color previousColor = graphics.getColor();

			graphics.setColor(Color.YELLOW);

			int width = windowPreferences.getMainWindowWidth();
			int height = windowPreferences.getMainWindowHeight();

			int spacing = gridPreferences.getGridSpacing();

			for (int x = spacing; x < width; x += spacing) {
				graphics.drawLine(x, 0, x, height - 1);
			}

			for (int y = spacing; y < height; y += spacing) {
				graphics.drawLine(0, y, width - 1, y);
			}

			graphics.setColor(previousColor);

		}
	}

}