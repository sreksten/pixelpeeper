package com.threeamigos.pixelpeeper.interfaces.edgedetect.ui;

import java.awt.*;

/**
 * A factory providing {@link EdgesDetectorPreferencesSelector}s.
 *
 * @author Stefano Reksten
 */
public interface EdgesDetectorPreferencesSelectorFactory {

    EdgesDetectorPreferencesSelector createSelector(Component component);

}
