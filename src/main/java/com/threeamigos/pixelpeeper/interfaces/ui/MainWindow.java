package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.Component;

import javax.swing.JMenu;

public interface MainWindow {

	JMenu getMenu(String menuTitle);

	Component getComponent();

}
