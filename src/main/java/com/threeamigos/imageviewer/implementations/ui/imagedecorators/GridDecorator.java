package com.threeamigos.imageviewer.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.MainWindowPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;

public class GridDecorator implements ImageDecorator {

	private final MainWindowPreferences mainWindowPreferences;
	private final GridPreferences gridPreferences;

	private final PropertyChangeSupport propertyChangeSupport;

	public GridDecorator(MainWindowPreferences mainWindowPreferences, GridPreferences gridPreferences) {
		this.mainWindowPreferences = mainWindowPreferences;
		this.gridPreferences = gridPreferences;

		propertyChangeSupport = new PropertyChangeSupport(this);
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

	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_G) {
					gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
					propertyChangeSupport.firePropertyChange(CommunicationMessages.GRID_VISIBILITY_CHANGE, null, this);
				} else if (e.getKeyCode() == KeyEvent.VK_ADD) {
					int spacing = gridPreferences.getGridSpacing();
					if (spacing < GridPreferences.GRID_SPACING_MAX) {
						gridPreferences.setGridSpacing(spacing + GridPreferences.GRID_SPACING_STEP);
						propertyChangeSupport.firePropertyChange(CommunicationMessages.GRID_SIZE_CHANGED, null, this);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
					int spacing = gridPreferences.getGridSpacing();
					if (spacing > GridPreferences.GRID_SPACING_MIN) {
						gridPreferences.setGridSpacing(spacing - GridPreferences.GRID_SPACING_STEP);
						propertyChangeSupport.firePropertyChange(CommunicationMessages.GRID_SIZE_CHANGED, null, this);
					}
				}
			}
		};
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
