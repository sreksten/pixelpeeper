package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindow;
import com.threeamigos.pixelpeeper.interfaces.ui.MainWindowPlugin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractMainWindowPlugin implements MainWindowPlugin {

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
