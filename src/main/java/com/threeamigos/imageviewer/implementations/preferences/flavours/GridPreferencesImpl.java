package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;

public class GridPreferencesImpl implements GridPreferences {

	private boolean gridVisible;
	private int gridSpacing;

	@Override
	public void setGridVisible(boolean gridVisible) {
		this.gridVisible = gridVisible;
	}

	@Override
	public boolean isGridVisible() {
		return gridVisible;
	}

	@Override
	public void setGridSpacing(int gridSpacing) {
		this.gridSpacing = gridSpacing;
	}

	@Override
	public int getGridSpacing() {
		return gridSpacing;
	}

	@Override
	public void loadDefaultValues() {
		gridVisible = GRID_VISIBLE_DEFAULT;
		gridSpacing = GRID_SPACING_DEFAULT;
	}

	@Override
	public void validate() {
		if (gridSpacing < GRID_SPACING_MIN || gridSpacing > GRID_SPACING_MAX) {
			throw new IllegalArgumentException("Invalid grid spacing");
		}
	}

}
