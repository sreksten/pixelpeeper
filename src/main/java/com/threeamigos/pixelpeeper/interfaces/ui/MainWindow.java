package com.threeamigos.pixelpeeper.interfaces.ui;

import javax.swing.*;
import java.awt.*;

/**
 * An interface for the main UI of the application. When asked for a menu, it should return it or create it
 * on-the-fly in order to let plugins add their capabilities to the main menu bar.
 *
 * @author Stefano Reksten
 */
public interface MainWindow {

    /**
     * @param menuTitle title of the menu a plugin would like to add some voices to
     * @return the JMenu
     */
    JMenu getMenu(String menuTitle);

    /**
     * @return main UI component
     */
    Component getComponent();

}
