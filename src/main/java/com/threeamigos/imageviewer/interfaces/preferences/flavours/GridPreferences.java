package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface GridPreferences extends Preferences {

	public static final boolean GRID_VISIBLE_DEFAULT = false;
	public static final int GRID_SPACING_DEFAULT = 50;
	public static final int GRID_SPACING_STEP = 25;
	public static final int GRID_SPACING_MIN = 25;
	public static final int GRID_SPACING_MAX = 200;

	public void setGridVisible(boolean gridVisible);

	public boolean isGridVisible();

	public void setGridSpacing(int gridSpacing);

	public int getGridSpacing();

}
