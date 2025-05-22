package com.threeamigos.pixelpeeper.interfaces.filters.ui;

import java.awt.*;

/**
 * An interface that provides a UI to allow the end-user to set the filter
 * algorithm parameters. This UI may vary according to the parameters each flavor
 * of the algorithm accepts.
 *
 * @author Stefano Reksten
 */
public interface FilterPreferencesSelector {

    void selectParameters(Component component);

}
