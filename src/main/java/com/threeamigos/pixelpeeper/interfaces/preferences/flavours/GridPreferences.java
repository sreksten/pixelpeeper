package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface GridPreferences extends Preferences {

	public static final boolean GRID_VISIBLE_DEFAULT = false;
	public static final int GRID_SPACING_DEFAULT = 50;
	public static final int GRID_SPACING_STEP = 25;
	public static final int GRID_SPACING_MIN = 25;
	public static final int GRID_SPACING_MAX = 200;

	default String getDescription() {
		return "Grid preferences";
	}

	public void setGridVisible(boolean gridVisible);

	public boolean isGridVisible();

	public void setGridSpacing(int gridSpacing);

	public int getGridSpacing();

}
