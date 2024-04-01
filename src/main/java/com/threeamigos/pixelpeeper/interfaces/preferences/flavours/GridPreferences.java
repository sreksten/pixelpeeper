package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface GridPreferences extends Preferences {

    boolean GRID_VISIBLE_DEFAULT = false;
    int GRID_SPACING_DEFAULT = 50;
    int GRID_SPACING_STEP = 25;
    int GRID_SPACING_MIN = 25;
    int GRID_SPACING_MAX = 200;

    default String getDescription() {
        return "Grid preferences";
    }

    void setGridVisible(boolean gridVisible);

    boolean isGridVisible();

    void setGridSpacing(int gridSpacing);

    int getGridSpacing();

}
