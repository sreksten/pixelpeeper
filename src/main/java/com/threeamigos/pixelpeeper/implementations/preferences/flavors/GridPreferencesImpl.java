package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.GridSpacingChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.GridVisibilityChangedEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.GridPreferences;

public class GridPreferencesImpl extends BasicPropertyChangeAware implements GridPreferences {

	private boolean gridVisible;
	private int gridSpacing;

	@Override
	public void setGridVisible(boolean gridVisible) {
		boolean oldGridVisible = this.gridVisible;
		this.gridVisible = gridVisible;
		EventBus.get().publish(new GridVisibilityChangedEvent(gridVisible));
	}

	@Override
	public boolean isGridVisible() {
		return gridVisible;
	}

	@Override
	public void setGridSpacing(int gridSpacing) {
		int oldGridSpacing = this.gridSpacing;
		this.gridSpacing = gridSpacing;
		EventBus.get().publish(new GridSpacingChangedEvent(gridSpacing));
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
