package com.threeamigos.pixelpeeper.interfaces.filters.ui;

import java.awt.*;

/**
 * A factory providing {@link FilterPreferencesSelector}s.
 *
 * @author Stefano Reksten
 */
public interface FilterPreferencesSelectorFactory {

    FilterPreferencesSelector createSelector(Component component);

}
