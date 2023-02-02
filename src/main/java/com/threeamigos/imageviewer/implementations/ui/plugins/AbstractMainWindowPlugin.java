package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.ui.KeyRegistry;
import com.threeamigos.imageviewer.interfaces.ui.MainWindow;
import com.threeamigos.imageviewer.interfaces.ui.MainWindowPlugin;

public abstract class AbstractMainWindowPlugin implements MainWindowPlugin, KeyRegistry, PropertyChangeListener {

	private final PropertyChangeSupport propertyChangeSupport;

	protected MainWindow mainWindow;

	protected AbstractMainWindowPlugin() {
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;

		createMenu();
	}

	public abstract void createMenu();

	protected void repaint() {
		propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, null);
	}

	protected void firePropertyChange(String message, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(message, oldValue, newValue);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
