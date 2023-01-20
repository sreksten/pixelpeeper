package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.GridPreferences;

public class GridPreferencesStatusTracker implements StatusTracker<GridPreferences> {

	private boolean gridVisibleAtStart;
	private int gridSpacingAtStart;

	private final GridPreferences gridPreferences;

	public GridPreferencesStatusTracker(GridPreferences gridPreferences) {
		this.gridPreferences = gridPreferences;
	}

	@Override
	public void loadInitialValues() {
		gridVisibleAtStart = gridPreferences.isGridVisible();
		gridSpacingAtStart = gridPreferences.getGridSpacing();
	}

	@Override
	public boolean hasChanged() {
		return gridPreferences.isGridVisible() != gridVisibleAtStart
				|| gridPreferences.getGridSpacing() != gridSpacingAtStart;
	}

}
