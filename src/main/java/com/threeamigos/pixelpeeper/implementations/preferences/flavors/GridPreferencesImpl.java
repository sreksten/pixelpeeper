package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.GridPreferences;

public class GridPreferencesImpl extends BasicPropertyChangeAware implements GridPreferences {

	private boolean gridVisible;
	private int gridSpacing;

	@Override
	public void setGridVisible(boolean gridVisible) {
		boolean oldGridVisible = this.gridVisible;
		this.gridVisible = gridVisible;
		firePropertyChange(CommunicationMessages.GRID_VISIBILITY_CHANGED, oldGridVisible, gridVisible);
	}

	@Override
	public boolean isGridVisible() {
		return gridVisible;
	}

	@Override
	public void setGridSpacing(int gridSpacing) {
		int oldGridSpacing = this.gridSpacing;
		this.gridSpacing = gridSpacing;
		firePropertyChange(CommunicationMessages.GRID_SPACING_CHANGED, oldGridSpacing, gridSpacing);
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
