package com.threeamigos.pixelpeeper.interfaces.edgedetect.ui;

import java.awt.*;

/**
 * An interface that provides an UI to allow the end-user to set the edge-detection
 * algorithm parameters. This UI may vary according to the parameters each flavour
 * of the algorithm accepts.
 *
 * @author Stefano Reksten
 */
public interface EdgesDetectorPreferencesSelector {

    void selectParameters(Component component);

}
