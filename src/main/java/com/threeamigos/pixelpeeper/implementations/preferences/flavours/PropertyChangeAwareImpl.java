package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.pixelpeeper.interfaces.preferences.PropertyChangeAware;

public abstract class PropertyChangeAwareImpl implements PropertyChangeAware {

	// transient to make Gson serializer ignore this
	private final transient PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	protected void firePropertyChange(String propertyName) {
		propertyChangeSupport.firePropertyChange(propertyName, null, null);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
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
