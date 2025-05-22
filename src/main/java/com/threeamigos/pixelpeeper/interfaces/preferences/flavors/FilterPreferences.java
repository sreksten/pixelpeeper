package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;

/**
 * Preferences for the filter capabilities. They track:
 * <ul>
 *     <li>The filter flavor</li>
 *     <li>The transparency level to apply to see the underlying image</li>
 * </ul>
 *
 * @author Stefano Reksten
 */
public interface FilterPreferences extends Preferences {

    boolean SHOW_RESULT_DEFAULT = false;
    int NO_TRANSPARENCY = 0;
    int TRANSPARENCY_DEFAULT = 30;
    int TOTAL_TRANSPARENCY = 100;
    FilterFlavor FILTER_FLAVOR_DEFAULT = FilterFlavor.CANNY_EDGES_DETECTOR;

    default String getDescription() {
        return "Filter preferences";
    }

    void setShowResults(boolean showEdges);

    boolean isShowResults();

    void setTransparency(int edgesTransparency);

    int getTransparency();

    void setFilterFlavor(FilterFlavor flavor);

    FilterFlavor getFilterFlavor();

}
