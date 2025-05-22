package com.threeamigos.pixelpeeper.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;

import com.threeamigos.common.util.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.GridPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.ImageDecorator;

public class GridDecorator implements ImageDecorator {

	private final MainWindowPreferences mainWindowPreferences;
	private final GridPreferences gridPreferences;

	public GridDecorator(MainWindowPreferences mainWindowPreferences, GridPreferences gridPreferences) {
		this.mainWindowPreferences = mainWindowPreferences;
		this.gridPreferences = gridPreferences;
	}

	@Override
	public void paint(Graphics2D graphics) {

		if (gridPreferences.isGridVisible()) {

			Color previousColor = graphics.getColor();

			graphics.setColor(Color.YELLOW);

			int width = mainWindowPreferences.getWidth();
			int height = mainWindowPreferences.getHeight();

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
