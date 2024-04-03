package com.threeamigos.pixelpeeper.interfaces.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/**
 * An interface that would like to plug in to the main window, adding its menus to the menu bar at startup
 * or a later time.<br/>
 * It needs a pointer to the MainWindow in order to ask it for certain menus. It also can register as a
 * listener on some properties or preferences, in order to update its menus when needed.
 *
 * @author Stefano Reksten
 */
public interface MainWindowPlugin {

    /**
     * @param mainWindow a reference to the MainWindow object, in order to ask it one or more menus to work with
     */
    void setMainWindow(MainWindow mainWindow);

    void addPropertyChangeListener(PropertyChangeListener pcl);

    void removePropertyChangeListener(PropertyChangeListener pcl);

    /**
     * A method that allows to add a checkbox menu on the menu bar
     *
     * @param menu           a JMenu returned by the MainWindow
     * @param title          title of the new voice
     * @param mnemonic       character shortcut
     * @param initialValue   initial value for the checkbox
     * @param actionListener an action that will be invoked when the menu is selected
     * @return the newly-added menu item
     */
    default JCheckBoxMenuItem addCheckboxMenuItem(JMenu menu, String title, KeyRegistry mnemonic, boolean initialValue,
                                                  ActionListener actionListener) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(title);
        if (actionListener != null) {
            menuItem.addActionListener(actionListener);
        }
        if (mnemonic != KeyRegistry.NO_KEY) {
            menuItem.setMnemonic(mnemonic.getKeyCode());
        }
        menuItem.setSelected(initialValue);
        menu.add(menuItem);
        return menuItem;
    }

    /**
     * A method that allows to add a menu item on the menu bar
     *
     * @param menu           a JMenu returned by the MainWindow
     * @param title          title of the new voice
     * @param mnemonic       character shortcut
     * @param actionListener an action that will be invoked when the menu is selected
     * @return the newly-added menu item
     */
    default JMenuItem addMenuItem(JMenu menu, String title, KeyRegistry mnemonic, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        if (actionListener != null) {
            menuItem.addActionListener(actionListener);
        }
        if (mnemonic != KeyRegistry.NO_KEY) {
            menuItem.setMnemonic(mnemonic.getKeyCode());
        }
        menu.add(menuItem);
        return menuItem;
    }
}
