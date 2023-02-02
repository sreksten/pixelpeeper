package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;

public class GridPlugin extends AbstractMainWindowPlugin {

	private final GridPreferences gridPreferences;
	private Map<Integer, JMenuItem> gridSpacingBySize = new HashMap<>();
	private JMenuItem gridVisibleMenuItem;

	public GridPlugin(GridPreferences gridPreferences) {
		super();
		this.gridPreferences = gridPreferences;
	}

	@Override
	public void createMenu() {

		JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

		gridVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Show grid", SHOW_GRID_KEY,
				gridPreferences.isGridVisible(), event -> {
					gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
				});
		JMenu gridSpacingMenu = new JMenu("Grid spacing");
		imageHandlingMenu.add(gridSpacingMenu);
		for (int gridSpacing = GridPreferences.GRID_SPACING_MIN; gridSpacing <= GridPreferences.GRID_SPACING_MAX; gridSpacing += GridPreferences.GRID_SPACING_STEP) {
			final int currentSpacing = gridSpacing;
			JMenuItem gridSpacingItem = addCheckboxMenuItem(gridSpacingMenu, String.valueOf(gridSpacing), -1,
					gridSpacing == gridPreferences.getGridSpacing(), event -> {
						gridPreferences.setGridSpacing(currentSpacing);
					});
			gridSpacingBySize.put(gridSpacing, gridSpacingItem);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (CommunicationMessages.GRID_VISIBILITY_CHANGED.equals(evt.getPropertyName())) {
			gridVisibleMenuItem.setSelected(gridPreferences.isGridVisible());
		} else if (CommunicationMessages.GRID_SPACING_CHANGED.equals(evt.getPropertyName())) {
			updateGridSpacingMenu(gridPreferences.getGridSpacing());
		}
	}

	private void updateGridSpacingMenu(final int gridSpacing) {
		for (Map.Entry<Integer, JMenuItem> entry : gridSpacingBySize.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == gridSpacing);
		}
	}

	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyRegistry.SHOW_GRID_KEY) {
					gridPreferences.setGridVisible(!gridPreferences.isGridVisible());
					e.consume();
				} else if (e.getKeyCode() == KeyRegistry.ENLARGE_KEY) {
					if (gridPreferences.isGridVisible()) {
						int spacing = gridPreferences.getGridSpacing();
						if (spacing < GridPreferences.GRID_SPACING_MAX) {
							gridPreferences.setGridSpacing(spacing + GridPreferences.GRID_SPACING_STEP);
						}
						e.consume();
					}
				} else if (e.getKeyCode() == KeyRegistry.REDUCE_KEY) {
					if (gridPreferences.isGridVisible()) {
						int spacing = gridPreferences.getGridSpacing();
						if (spacing > GridPreferences.GRID_SPACING_MIN) {
							gridPreferences.setGridSpacing(spacing - GridPreferences.GRID_SPACING_STEP);
						}
						e.consume();
					}
				}
			}
		};
	}

}
