package com.threeamigos.pixelpeeper.interfaces.ui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public interface MainWindowPlugin {

    void setMainWindow(MainWindow mainWindow);

    void addPropertyChangeListener(PropertyChangeListener pcl);

    void removePropertyChangeListener(PropertyChangeListener pcl);

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
