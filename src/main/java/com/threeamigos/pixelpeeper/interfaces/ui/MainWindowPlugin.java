package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public interface MainWindowPlugin {

	public void setMainWindow(MainWindow mainWindow);

	public void addPropertyChangeListener(PropertyChangeListener pcl);

	public void removePropertyChangeListener(PropertyChangeListener pcl);

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
